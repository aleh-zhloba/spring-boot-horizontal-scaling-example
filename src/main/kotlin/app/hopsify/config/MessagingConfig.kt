package app.hopsify.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.azhloba.postgresql.messaging.pubsub.PostgresPubSub
import io.github.azhloba.postgresql.messaging.spring.PostgresMessagingTemplate
import io.github.azhloba.postgresql.messaging.spring.converter.JacksonNotificationMessageConverter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.converter.MappingJackson2MessageConverter

@Configuration
class MessagingConfig {
    @Bean
    fun postgresMessagingTemplate(
        pubSub: PostgresPubSub,
        objectMapper: ObjectMapper
    ): PostgresMessagingTemplate {
        return PostgresMessagingTemplate(pubSub).apply {
            notificationMessageConverter = JacksonNotificationMessageConverter(objectMapper)
            messageConverter =
                MappingJackson2MessageConverter().apply {
                    this.objectMapper = objectMapper
                    this.serializedPayloadClass = String::class.java
                }
        }
    }
}
