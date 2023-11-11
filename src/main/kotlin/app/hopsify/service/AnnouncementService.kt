package app.hopsify.service

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import app.hopsify.model.Announcement
import app.hopsify.repository.AnnouncementRepository
import java.time.Instant
import java.util.UUID

@Service
class AnnouncementService(
    private val repository: AnnouncementRepository,
    private val cacheManager: CacheManager
) {
    companion object {
        const val ANNOUNCEMENT_CACHE_NAME = "announcements"
    }

    @Cacheable(ANNOUNCEMENT_CACHE_NAME)
    fun findVenueAnnouncements(venueId: String): List<Announcement> {
        return repository.findAllByVenueIdOrderByCreatedAtDesc(venueId)
    }

    @CacheEvict(cacheNames = [ANNOUNCEMENT_CACHE_NAME], key = "#venueId")
    fun createAnnouncement(
        venueId: String,
        message: String
    ): Announcement =
        repository.save(
            Announcement(
                id = UUID.randomUUID().toString(),
                createdAt = Instant.now(),
                venueId = venueId,
                message = message
            )
        )

    fun deleteAnnouncement(announcementId: String) =
        repository.findByIdOrNull(announcementId)?.let { announcement ->
            repository.deleteById(announcement.id)
            cacheManager.getCache(ANNOUNCEMENT_CACHE_NAME)?.evict(announcement.venueId)
        }
}
