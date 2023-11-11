package app.hopsify.scheduled

import io.github.oshai.kotlinlogging.KotlinLogging
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import app.hopsify.service.AnnouncementService
import app.hopsify.service.SubscriptionService

@Component
class SubscriptionNewsletterScheduledTask(
    private val announcementService: AnnouncementService,
    private val subscriptionService: SubscriptionService
) {
    private val logger = KotlinLogging.logger { }

    companion object {
        const val SUBSCRIPTIONS_CHUNK_SIZE = 50
    }

    @Scheduled(fixedDelayString = "\${tasks.subscription.newsletter.delay}")
    @SchedulerLock(name = "subscription.newsletter")
    fun sendEmailNotifications() {
        subscriptionService.findSubscriptionsForProcess(SUBSCRIPTIONS_CHUNK_SIZE).forEach { subscription ->
            try {
                val subscriptionAnnouncements =
                    announcementService.findVenueAnnouncements(subscription.venueId)
                        .takeWhile { it.createdAt > subscription.processedAt }

                if (subscriptionAnnouncements.isNotEmpty()) {
                    subscriptionService.sendSubscriptionEmail(
                        subscription = subscription,
                        announcements = subscriptionAnnouncements
                    )

                    subscriptionService.updateSubscriptionProcessedTimestamp(subscription.id)

                    logger.info {
                        "Successfully sent email with ${subscriptionAnnouncements.size} announcements " +
                            "for subscription [email=${subscription.email}, venueId=${subscription.venueId}]"
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Unable to process subscription email=${subscription.email}, venueId=${subscription.venueId}]" }
            }
        }
    }
}
