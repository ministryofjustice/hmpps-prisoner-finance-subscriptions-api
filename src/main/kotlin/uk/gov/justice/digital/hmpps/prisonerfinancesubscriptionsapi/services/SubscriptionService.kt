package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.services

import jakarta.transaction.Transactional
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.config.CustomException
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.IdempotencyEntity
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.CreateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.UpdateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.AccountSubscriptionSummaryResponse
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.SubscriptionResponse
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.IdempotencyKeyDataRepository
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.SubscriptionsRepository
import java.util.UUID

@Service
class SubscriptionService(private val subscriptionsRepository: SubscriptionsRepository, private val idempotencyRepository: IdempotencyKeyDataRepository) {
  @Transactional(rollbackOn = [Exception::class, Error::class])
  fun createSubscription(createSubscriptionRequest: CreateSubscriptionRequest, idempotencyKey: UUID): SubscriptionResponse {
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

    if (subscriptionEntity.endDate != null) {
      throw CustomException("Subscription end date is already set", HttpStatus.BAD_REQUEST)
    }

    subscriptionEntity.endDate = updateSubscriptionRequest.endDate
    subscriptionsRepository.save(subscriptionEntity)

    return SubscriptionResponse.fromEntity(subscriptionEntity)
  }
}
