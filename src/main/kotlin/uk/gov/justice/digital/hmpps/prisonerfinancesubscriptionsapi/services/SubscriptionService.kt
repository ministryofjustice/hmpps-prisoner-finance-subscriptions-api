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
    if (createSubscriptionRequest.endDate != null && createSubscriptionRequest.endDate < createSubscriptionRequest.startDate) {
      throw CustomException("Subscription end date before start date", HttpStatus.BAD_REQUEST)
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

    if (updateSubscriptionRequest.endDate != null) {
      subscriptionEntity.endDate = updateSubscriptionRequest.endDate
    }

    val expectedStartDate = updateSubscriptionRequest.startDate ?: subscriptionEntity.startDate
    val expectedEndDate = updateSubscriptionRequest.endDate ?: subscriptionEntity.endDate

    if (expectedEndDate != null && expectedEndDate < expectedStartDate) {
      throw CustomException("Subscription end date before start date", HttpStatus.BAD_REQUEST)
    }

    if (updateSubscriptionRequest.startDate != null) {
      val startDateIsBeingUpdated = subscriptionEntity.startDate != updateSubscriptionRequest.startDate
      val startDateIsInThePast = subscriptionEntity.startDate < LocalDate.now()

      if (startDateIsInThePast && startDateIsBeingUpdated) {
        throw CustomException("Subscription has already started - unable to update", HttpStatus.BAD_REQUEST)
      } else if (startDateIsBeingUpdated) {
        subscriptionEntity.startDate = updateSubscriptionRequest.startDate
      }
    }

    subscriptionsRepository.save(subscriptionEntity)

    return SubscriptionResponse.fromEntity(subscriptionEntity)
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
}
