package app.hopsify.controller

import app.hopsify.model.Announcement
import app.hopsify.service.AnnouncementService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/announcements")
class AnnouncementController(
    private val announcementService: AnnouncementService
) {
    @PostMapping
    fun create(
        @RequestBody request: AnnouncementCreateRequest
    ): Announcement = announcementService.createAnnouncement(venueId = request.venueId, message = request.message)

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: String
    ) = announcementService.deleteAnnouncement(id)
}

data class AnnouncementCreateRequest(
    val venueId: String,
    val message: String
)
