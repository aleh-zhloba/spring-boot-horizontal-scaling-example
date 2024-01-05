package app.hopsify.repository

import app.hopsify.model.Announcement
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AnnouncementRepository : CrudRepository<Announcement, String> {
    fun findAllByVenueIdOrderByCreatedAtDesc(venueId: String): List<Announcement>
}
