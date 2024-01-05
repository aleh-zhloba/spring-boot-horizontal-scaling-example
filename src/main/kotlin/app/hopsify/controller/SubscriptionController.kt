package app.hopsify.controller

import app.hopsify.model.AnnouncementSubscription
import app.hopsify.service.SubscriptionService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("subscriptions")
class SubscriptionController(private val subscriptionService: SubscriptionService) {
    @PostMapping
    fun create(
        @RequestBody request: SubscriptionCreateRequest
    ): AnnouncementSubscription = subscriptionService.createSubscription(venueId = request.venueId, email = request.email)

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: String
    ) = subscriptionService.deleteSubscription(id)
}

data class SubscriptionCreateRequest(
    val venueId: String,
    val email: String
)
