package app.hopsify.infrastructure

import app.hopsify.infrastructure.SimpleDistributedCache.Companion.CACHE_CHANNEL
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.azhloba.postgresql.messaging.spring.PostgresMessageListener
import io.github.azhloba.postgresql.messaging.spring.PostgresMessagingTemplate
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager

class SimpleDistributedCacheManager(
    private val messagingTemplate: PostgresMessagingTemplate,
    private val underlying: CacheManager
) : CacheManager by underlying {
    private val logger = KotlinLogging.logger {}

    override fun getCache(name: String): Cache? =
        underlying.getCache(name)?.let { cache ->
            SimpleDistributedCache(messagingTemplate, cache)
        }

    @PostgresMessageListener(value = [CACHE_CHANNEL], skipLocal = true)
    fun handleNotification(notification: CacheNotification) {
        try {
            logger.debug { "Received cache notification: $notification" }

            underlying.getCache(notification.cacheName)?.let { cache ->
                when (notification) {
                    is CacheNotification.Clear -> cache.clear()
                    is CacheNotification.Evict -> cache.evict(notification.key)
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error during processing distributed cache notification: $notification" }
        }
    }
}

class SimpleDistributedCache(
    private val messagingTemplate: PostgresMessagingTemplate,
    private val underlying: Cache
) : Cache by underlying {
    companion object {
        const val CACHE_CHANNEL = "cache"
    }

    override fun evict(key: Any) =
        underlying.evict(key).also {
            // broadcast cache evict message after underlying local cache eviction
            messagingTemplate.convertAndSend(
                CACHE_CHANNEL,
                CacheNotification.Evict(
                    cacheName = name,
                    key = key
                )
            )
        }

    override fun clear() =
        underlying.clear().also {
            // broadcast cache clear message after underlying local cache cleared
            messagingTemplate.convertAndSend(
                CACHE_CHANNEL,
                CacheNotification.Clear(cacheName = name)
            )
        }
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
sealed interface CacheNotification {
    val cacheName: String

    @JsonTypeName("evict")
    data class Evict(override val cacheName: String, val key: Any) : CacheNotification

    @JsonTypeName("clear")
    data class Clear(override val cacheName: String) : CacheNotification
}
