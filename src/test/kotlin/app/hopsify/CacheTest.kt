package app.hopsify

import app.hopsify.controller.AnnouncementCreateRequest
import app.hopsify.model.Announcement
import app.hopsify.support.AbstractMultiInstanceTest
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.expectBodyList
import org.springframework.test.web.reactive.server.returnResult
import java.util.UUID
import kotlin.test.assertEquals

class CacheTest : AbstractMultiInstanceTest() {
    @Test
    fun `verify caches synchronization`() {
        val venueId = UUID.randomUUID().toString()

        // create a record
        val createdAnnouncement =
            firstInstanceClient.post().uri("/announcements")
                .bodyValue(AnnouncementCreateRequest(venueId, "Tap #9 is over"))
                .exchange()
                .expectStatus().isOk
                .returnResult<Announcement>()
                .responseBody
                .blockFirst()!!

        // ensure both instances return created record
        firstInstanceClient.get().uri("/venue/$venueId/announcements")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Announcement>()
            .hasSize(1)

        secondInstanceClient.get().uri("/venue/$venueId/announcements")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Announcement>()
            .hasSize(1)

        // delete record
        secondInstanceClient.delete().uri("/announcements/${createdAnnouncement.id}")
            .exchange()
            .expectStatus().isOk

        // ensure record deleted from both instances
        secondInstanceClient.get().uri("/venue/$venueId/announcements")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Announcement>()
            .hasSize(0)

        waitSync()

        firstInstanceClient.get().uri("/venue/$venueId/announcements")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Announcement>()
            .hasSize(0)
    }

    @Test
    fun `verify caching is enabled`() {
        val venueId = UUID.randomUUID().toString()

        // ensure instances return no records
        firstInstanceClient.get().uri("/venue/$venueId/announcements")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Announcement>()
            .hasSize(0)

        secondInstanceClient.get().uri("/venue/$venueId/announcements")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Announcement>()
            .hasSize(0)

        // create a record
        firstInstanceClient.post().uri("/announcements")
            .bodyValue(AnnouncementCreateRequest(venueId, "New cans available!"))
            .exchange()
            .expectStatus().isOk

        // ensure both instances return created record
        firstInstanceClient.get().uri("/venue/$venueId/announcements")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Announcement>()
            .hasSize(1)

        waitSync()

        secondInstanceClient.get().uri("/venue/$venueId/announcements")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Announcement>()
            .hasSize(1)

        // remove record directly from db
        val recordsDeleted = jdbcTemplate.update("DELETE FROM announcements WHERE venue_id = '$venueId'")
        assertEquals(1, recordsDeleted)

        // ensure record is still in cache
        firstInstanceClient.get().uri("/venue/$venueId/announcements")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Announcement>()
            .hasSize(1)

        secondInstanceClient.get().uri("/venue/$venueId/announcements")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<Announcement>()
            .hasSize(1)
    }
}
