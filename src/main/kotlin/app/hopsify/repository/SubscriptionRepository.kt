package app.hopsify.repository

import app.hopsify.model.AnnouncementSubscription
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Repository
interface SubscriptionRepository : CrudRepository<AnnouncementSubscription, String> {
    fun findAll(pageable: Pageable): List<AnnouncementSubscription>

    @Modifying
    @Transactional
    @Query(
        "update AnnouncementSubscription s set s.processedAt = :processedAt " +
            "where s.id = :id"
    )
    fun updateProcessedAt(
        id: String,
        processedAt: Instant = Instant.now()
    )
}
