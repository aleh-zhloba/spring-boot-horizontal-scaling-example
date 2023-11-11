package app.hopsify.service

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Service
import app.hopsify.model.Announcement
import app.hopsify.model.AnnouncementSubscription
import app.hopsify.repository.SubscriptionRepository
import java.time.Instant
import java.util.UUID

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val mailSender: JavaMailSender
) {
    fun createSubscription(
        venueId: String,
        email: String
    ): AnnouncementSubscription =
        subscriptionRepository.save(
            AnnouncementSubscription(
                id = UUID.randomUUID().toString(),
                createdAt = Instant.now(),
                venueId = venueId,
                email = email,
                processedAt = Instant.now()
            )
        )

    fun deleteSubscription(subscriptionId: String) {
        subscriptionRepository.deleteById(subscriptionId)
    }

    fun updateSubscriptionProcessedTimestamp(subscriptionId: String) {
        subscriptionRepository.updateProcessedAt(subscriptionId)
    }

    fun findSubscriptionsForProcess(limit: Int): List<AnnouncementSubscription> {
        val subscriptionPageRequest =
            PageRequest.ofSize(limit)
                .withSort(Sort.Direction.ASC, AnnouncementSubscription::processedAt.name)

        return subscriptionRepository.findAll(subscriptionPageRequest)
    }

    fun sendSubscriptionEmail(
        subscription: AnnouncementSubscription,
        announcements: List<Announcement>
    ) {
        if (announcements.isEmpty()) return

        mailSender.send(
            SimpleMailMessage().apply {
                setTo(subscription.email)

                from = "no-reply@hopsify.app"
                subject = "Regular beer announcements update"
                text = "Hey, here are your recent beer announcements:\r\n" +
                    announcements.joinToString("\r\n") { it.message }
            }
        )
    }
}
