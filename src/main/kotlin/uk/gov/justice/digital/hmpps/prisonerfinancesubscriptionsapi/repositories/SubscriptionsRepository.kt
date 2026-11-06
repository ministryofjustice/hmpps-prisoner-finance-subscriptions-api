package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.SubscriptionEntity
import java.time.LocalDate
import java.util.UUID

interface SubscriptionsRepository : JpaRepository<SubscriptionEntity, UUID> {
  fun findByPrisonNumber(prisonNumber: String): List<SubscriptionEntity>
  fun getSubscriptionEntityBySubscriptionId(id: UUID): SubscriptionEntity?

  @Query("select s from SubscriptionEntity s where s.prisonNumber = :prisonNumber and ( s.endDate is null or s.endDate >= CURRENT_DATE)")
  fun getActiveSubscriptionsForPrisonNumber(
    prisonNumber: String,
    pageable: Pageable,
  ): Page<SubscriptionEntity>

  fun getSubscriptionEntitiesByPrisonNumber(prisonNumber: String, pageable: Pageable? = null): Page<SubscriptionEntity>

  fun getSubscriptionEntitiesByPrisonNumberAndEndDateIsBefore(prisonNumber: String, endDate: LocalDate = LocalDate.now(), pageable: Pageable? = null): Page<SubscriptionEntity>
}
