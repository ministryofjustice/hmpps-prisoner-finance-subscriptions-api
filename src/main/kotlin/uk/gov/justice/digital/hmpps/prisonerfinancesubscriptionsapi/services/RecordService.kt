package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.services

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.CreateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.SubscriptionResponse
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.SubscriptionsRepository
import java.util.UUID

@Service
class RecordService(private val subscriptionsRepository: SubscriptionsRepository) {
  @Transactional
  fun createSubscription(createSubscriptionRequest: CreateSubscriptionRequest, idempotencyKey: UUID): SubscriptionResponse {
    val subscriptionEntity = createSubscriptionRequest.toEntity()
    val createdSubscription = subscriptionsRepository.save(subscriptionEntity)
    return SubscriptionResponse.fromEntity(createdSubscription)
  }
}
