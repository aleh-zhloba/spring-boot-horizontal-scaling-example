package app.hopsify.config

import com.github.benmanes.caffeine.cache.Caffeine
import io.github.azhloba.postgresql.messaging.spring.PostgresMessagingTemplate
import org.springframework.cache.CacheManager
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import app.hopsify.infrastructure.SimpleDistributedCacheManager
import java.util.concurrent.TimeUnit

@Configuration
class CacheConfig {
    @Bean
    fun distributedCacheManager(template: PostgresMessagingTemplate): CacheManager =
        SimpleDistributedCacheManager(
            template,
            CaffeineCacheManager().apply {
                setCaffeine(Caffeine.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES))
            }
        )
}
