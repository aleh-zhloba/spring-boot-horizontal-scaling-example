package app.hopsify.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import app.hopsify.model.Announcement

@Repository
interface AnnouncementRepository : CrudRepository<Announcement, String> {
    fun findAllByVenueIdOrderByCreatedAtDesc(venueId: String): List<Announcement>
}
