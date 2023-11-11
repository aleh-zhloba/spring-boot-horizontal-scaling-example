package app.hopsify.model

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "announcements")
class Announcement(
    @Id val id: String,
    val createdAt: Instant,
    val venueId: String,
    val message: String
)
