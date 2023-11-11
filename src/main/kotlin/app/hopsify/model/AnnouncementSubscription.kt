package app.hopsify.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "subscriptions")
class AnnouncementSubscription(
    @Id val id: String,
    val createdAt: Instant,
    val processedAt: Instant,
    val email: String,
    val venueId: String,
)
