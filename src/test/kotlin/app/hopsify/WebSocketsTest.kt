package app.hopsify

import app.hopsify.support.AbstractMultiInstanceTest
import org.junit.jupiter.api.Test
import reactor.test.StepVerifier
import java.time.Duration
import java.util.UUID

class WebSocketsTest : AbstractMultiInstanceTest() {
    @Test
    fun `verify inter-instances web socket communication`() {
        val venueId: String = UUID.randomUUID().toString()
        val destination = "/venue/$venueId/cheers"

        // take first instance web socket session and listen for "cheers"
        val firstInstanceVerifier =
            StepVerifier.create(
                firstInstanceStompSession.subscribe(destination).next()
            )
                .expectNext("Skál!")
                .expectComplete()
                .verifyLater()

        // take second instance web socket session and send a "cheers" in Icelandic language
        secondInstanceStompSession.send(destination, "is")

        firstInstanceVerifier.verify(Duration.ofMillis(500))
    }
}
