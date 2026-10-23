package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.services

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.IdempotencyEntity
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.CreateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.SubscriptionResponse
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.IdempotencyKeyDataRepository
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.SubscriptionsRepository
import java.util.UUID

@Service
class RecordService(private val subscriptionsRepository: SubscriptionsRepository, private val idempotencyRepository: IdempotencyKeyDataRepository) {
  @Transactional(rollbackOn = [Exception::class, Error::class])
  fun createSubscription(createSubscriptionRequest: CreateSubscriptionRequest, idempotencyKey: UUID): SubscriptionResponse {
    val createdSubscription = subscriptionsRepository.save(createSubscriptionRequest.toEntity())
    idempotencyRepository.save(IdempotencyEntity(idempotencyKey, createdSubscription))
    return SubscriptionResponse.fromEntity(createdSubscription)
  }
}
