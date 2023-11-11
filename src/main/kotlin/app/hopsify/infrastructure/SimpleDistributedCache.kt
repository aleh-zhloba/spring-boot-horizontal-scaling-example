package app.hopsify.infrastructure

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import io.github.azhloba.postgresql.messaging.spring.PostgresMessageListener
import io.github.azhloba.postgresql.messaging.spring.PostgresMessagingTemplate
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import app.hopsify.infrastructure.DistributedCacheEventBus.Companion.CACHE_CHANNEL
import java.lang.RuntimeException

class SimpleDistributedCacheManager(
    messagingTemplate: PostgresMessagingTemplate,
    private val underlining: CacheManager
) : CacheManager by underlining {
    private val logger = KotlinLogging.logger {}

    private val cacheEventBus = DistributedCacheEventBus(messagingTemplate, CACHE_CHANNEL)

    override fun getCache(name: String): Cache? =
        underlining.getCache(name)?.let { cache -> SimpleDistributedCache(cache, cacheEventBus) }

    @PostgresMessageListener(value = [CACHE_CHANNEL], skipLocal = true)
    fun handleNotification(notification: CacheNotification) {
        try {
            logger.info { "Received cache notification: $notification" }

            underlining.getCache(notification.cacheName)?.let { cache ->
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
    private val underlining: Cache,
    private val cacheEventBus: DistributedCacheEventBus
) : Cache by underlining {
    override fun evict(key: Any) =
        underlining.evict(key).also {
            cacheEventBus.sendEvict(name, key)
        }

    override fun clear() =
        underlining.clear().also {
            cacheEventBus.sendClear(name)
        }
}

class DistributedCacheEventBus(
    private val messagingTemplate: PostgresMessagingTemplate,
    private val cacheChannel: String
) {
    companion object {
        const val CACHE_CHANNEL = "cache"
    }

    fun sendEvict(
        cacheName: String,
        key: Any
    ) {
        val notification = CacheNotification.Evict(
            cacheName = cacheName,
            key = key
        )
        try {
            messagingTemplate.convertAndSend(cacheChannel, notification)
        } catch (e: Exception) {
            throw CacheSynchronizationException(notification, e)
        }
    }

    fun sendClear(cacheName: String) {
        val notification = CacheNotification.Clear(cacheName = cacheName)

        try {
            messagingTemplate.convertAndSend(cacheChannel, notification)
        } catch (e: Exception) {
            throw CacheSynchronizationException(notification, e)
        }
    }
}

class CacheSynchronizationException(notification: CacheNotification, cause: Throwable) :
    RuntimeException("Sending cache notification $notification exception", cause)

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
