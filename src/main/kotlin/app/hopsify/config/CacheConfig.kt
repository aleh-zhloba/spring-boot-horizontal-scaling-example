package app.hopsify.config

import app.hopsify.infrastructure.SimpleDistributedCacheManager
import com.github.benmanes.caffeine.cache.Caffeine
import io.github.azhloba.postgresql.messaging.spring.PostgresMessagingTemplate
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig {
    @Bean
    fun cacheManager(messagingTemplate: PostgresMessagingTemplate): CacheManager =
        SimpleDistributedCacheManager(
            messagingTemplate,
            CaffeineCacheManager().apply {
                setCaffeine(Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES))
            }
        )
}
