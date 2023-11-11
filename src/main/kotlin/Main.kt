package app.hopsify

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableCaching
@EnableAutoConfiguration(exclude = [R2dbcAutoConfiguration::class])
@EnableJpaRepositories(basePackages = ["app.hopsify"])
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
