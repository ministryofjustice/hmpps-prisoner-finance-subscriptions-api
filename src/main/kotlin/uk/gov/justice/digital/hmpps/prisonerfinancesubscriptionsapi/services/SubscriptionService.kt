package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.services

import jakarta.transaction.Transactional
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.config.CustomException
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.IdempotencyEntity
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.enums.SubscriptionsStatus
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.CreateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.UpdateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.AccountSubscriptionSummaryResponse
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.PagedResponse
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.SubscriptionResponse
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.IdempotencyKeyDataRepository
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.SubscriptionsRepository
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.utils.toPageResponse
import java.time.LocalDate
import java.util.UUID

@Service
class SubscriptionService(private val subscriptionsRepository: SubscriptionsRepository, private val idempotencyRepository: IdempotencyKeyDataRepository) {
  @Transactional(rollbackOn = [Exception::class, Error::class])
  fun createSubscription(createSubscriptionRequest: CreateSubscriptionRequest, idempotencyKey: UUID): SubscriptionResponse {
    createSubscriptionRequest.endDate?.let { endDate ->
      if (endDate < createSubscriptionRequest.startDate) {
        throw CustomException("Subscription end date before start date", HttpStatus.BAD_REQUEST)
      }
    }

    val createdSubscription = subscriptionsRepository.saveAndFlush(createSubscriptionRequest.toEntity())
    idempotencyRepository.saveAndFlush(IdempotencyEntity(idempotencyKey, createdSubscription))
    return SubscriptionResponse.fromEntity(createdSubscription)
  }

  fun getSubscriptionSummary(prisonerNumber: String): AccountSubscriptionSummaryResponse {
    val subscriptionRecords = subscriptionsRepository.findByPrisonNumber(prisonerNumber)

    return AccountSubscriptionSummaryResponse(
      weeklyPaymentAmount = subscriptionRecords.sumOf { it.amount },
      subscriptionCount = subscriptionRecords.size,
    )
  }

  fun updateSubscription(subscriptionId: UUID, updateSubscriptionRequest: UpdateSubscriptionRequest): SubscriptionResponse {
    val subscriptionEntity = subscriptionsRepository.getSubscriptionEntityBySubscriptionId(subscriptionId)
      ?: throw CustomException("Could not find subscription", HttpStatus.NOT_FOUND)

    val updatedStartDate = updateSubscriptionRequest.startDate ?: subscriptionEntity.startDate
    val updatedEndDate = updateSubscriptionRequest.endDate ?: subscriptionEntity.endDate
    val updatedComment = updateSubscriptionRequest.comment ?: subscriptionEntity.comment
    val entityCopy = subscriptionEntity.copy(startDate = updatedStartDate, endDate = updatedEndDate, comment = updatedComment, updatedBy = updateSubscriptionRequest.updatedBy)

    val endDateIsBeingUpdated = entityCopy.endDate != null
    if (endDateIsBeingUpdated && entityCopy.endDate!!.isBefore(entityCopy.startDate)) {
      throw CustomException("Subscription end date before start date", HttpStatus.BAD_REQUEST)
    }

    val startDateIsBeingUpdated = subscriptionEntity.startDate != updatedStartDate
    if (subscriptionEntity.hasStarted && startDateIsBeingUpdated) {
      throw CustomException("Subscription has already started - unable to update", HttpStatus.BAD_REQUEST)
    }

    val updatedEntity = subscriptionsRepository.save(entityCopy)

    return SubscriptionResponse.fromEntity(updatedEntity)
  }

  fun getSubscriptions(prisonNumber: String, pageNumber: Int = 1, pageSize: Int = 25, status: SubscriptionsStatus? = null): PagedResponse<SubscriptionResponse> {
    val zeroIndexedPage: Int = pageNumber - 1
    val pagedRequest = PageRequest.of(
      zeroIndexedPage,
      pageSize,
      Sort.by(
        Sort.Order.desc("startDate"),
        Sort.Order.desc("subscriptionId"),
      ),
    )

    val subscriptions = when (status) {
      SubscriptionsStatus.ACTIVE -> subscriptionsRepository.getActiveSubscriptionsForPrisonNumber(prisonNumber, pagedRequest)

      SubscriptionsStatus.INACTIVE -> subscriptionsRepository.getSubscriptionEntitiesByPrisonNumberAndEndDateIsBefore(
        prisonNumber = prisonNumber,
        endDate = LocalDate.now(),
        pageable = pagedRequest,
      )

      null -> subscriptionsRepository.getSubscriptionEntitiesByPrisonNumber(prisonNumber, pagedRequest)
    }

    return subscriptions.toPageResponse { content ->
      content.map { SubscriptionResponse.fromEntity(it) }
    }
  }

  fun merge(survivedPrisonNumber: String, removedPrisonNumber: String) {
    val subscriptions = subscriptionsRepository.findByPrisonNumber(removedPrisonNumber)

    subscriptions.forEach {
      it.prisonNumber = survivedPrisonNumber
      it.updatedBy = "DPS-Merge-Event"
    }

    subscriptionsRepository.saveAll(subscriptions)
  }
}
