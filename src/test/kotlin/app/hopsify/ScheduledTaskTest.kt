package app.hopsify

import app.hopsify.controller.AnnouncementCreateRequest
import app.hopsify.controller.SubscriptionCreateRequest
import app.hopsify.support.AbstractMultiInstanceTest
import java.time.Duration
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledTaskTest : AbstractMultiInstanceTest() {
    @Test
    fun `verify scheduled tasks do not conflict`() {
        val venueId: String = UUID.randomUUID().toString()
        val emailAddresses = (0..<100).map { "beer_geek_$it@belch.tech" }

        // create subscriptions
        emailAddresses.forEach { email ->
            roundRobinClient.post().uri("/subscriptions")
                .bodyValue(SubscriptionCreateRequest(venueId, email))
                .exchange()
                .expectStatus().isOk
        }

        // create new announcement to trigger newsletter
        roundRobinClient.post().uri("/announcements")
            .bodyValue(AnnouncementCreateRequest(venueId, "Tap takeover is coming"))
            .exchange()
            .expectStatus().isOk

        // ensure no duplicates received
        greenMail.waitForIncomingEmail(Duration.ofSeconds(10).toMillis(), emailAddresses.size)
        val receivedEmails =
            greenMail.receivedMessages
                .associateBy { it.allRecipients[0] }
        assertEquals(emailAddresses.size, receivedEmails.size)
    }
}
