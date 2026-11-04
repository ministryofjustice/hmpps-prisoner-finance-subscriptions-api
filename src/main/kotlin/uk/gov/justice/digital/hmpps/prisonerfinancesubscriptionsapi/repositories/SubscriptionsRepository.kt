package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.SubscriptionEntity
import java.util.UUID

interface SubscriptionsRepository : JpaRepository<SubscriptionEntity, UUID> {
  fun findByPrisonNumber(prisonerNumber: String): List<SubscriptionEntity>
  fun getSubscriptionEntityBySubscriptionId(id: UUID): SubscriptionEntity?
  fun getSubscriptionEntitiesByPrisonNumber(prisonNumber: String, pageable: Pageable): Page<SubscriptionEntity>
}
