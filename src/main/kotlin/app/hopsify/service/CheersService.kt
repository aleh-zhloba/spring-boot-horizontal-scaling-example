package app.hopsify.service

import io.github.azhloba.postgresql.messaging.spring.PostgresMessageListener
import io.github.azhloba.postgresql.messaging.spring.PostgresMessagingTemplate
import org.springframework.context.MessageSource
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class CheersService(
    private val postgresMessagingTemplate: PostgresMessagingTemplate,
    private val simpMessagingTemplate: SimpMessagingTemplate,
    private val messageSource: MessageSource
) {
    companion object {
        const val CHEERS_CHANNEL_NAME = "cheers"
    }

    fun broadcastCheersMessage(
        venueId: String,
        languageTag: String
    ) {
        postgresMessagingTemplate.convertAndSend(
            CHEERS_CHANNEL_NAME,
            CheersPayload(
                venueId = venueId,
                message = messageSource.getMessage("cheers", null, Locale.forLanguageTag(languageTag))
            )
        )
    }

    @PostgresMessageListener(value = [CHEERS_CHANNEL_NAME])
    fun notifyCheersListeners(
        @Payload payload: CheersPayload
    ) {
        simpMessagingTemplate.convertAndSend("/venue/${payload.venueId}/cheers", payload.message)
    }

    data class CheersPayload(val venueId: String, val message: String)
}
