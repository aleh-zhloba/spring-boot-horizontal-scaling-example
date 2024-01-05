package app.hopsify.controller

import app.hopsify.model.Announcement
import app.hopsify.service.AnnouncementService
import app.hopsify.service.CheersService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/venue")
class VenueController(
    val announcementService: AnnouncementService,
    val cheersService: CheersService
) {
    @GetMapping("/{venueId}/announcements")
    fun getVenueAnnouncements(
        @PathVariable venueId: String
    ): List<Announcement> = announcementService.findVenueAnnouncements(venueId)

    @MessageMapping("/venue/{venueId}/cheers")
    fun sendCheers(
        @DestinationVariable venueId: String,
        @Payload languageTag: String
    ) {
        cheersService.broadcastCheersMessage(venueId, languageTag)
    }
}
