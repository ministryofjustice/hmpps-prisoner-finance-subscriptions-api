package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.integration.helpers

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.config.ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.SubAccountRef
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.enums.SubscriptionPaymentSchedule
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.CreateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.UpdateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.PagedResponse
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.SubscriptionResponse
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.IdempotencyKeyDataRepository
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.SubscriptionsRepository
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@TestConfiguration
class IntegrationTestHelpers(
  private val idempotencyKeyDataRepository: IdempotencyKeyDataRepository,
  private val subscriptionsRepository: SubscriptionsRepository,
) {

  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @Transactional
  fun clearDatabase() {
    entityManager.clear()
    idempotencyKeyDataRepository.deleteAllInBatch()
    subscriptionsRepository.deleteAllInBatch()
  }

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  fun getSubscriptions(prisonNumber: String): PagedResponse<SubscriptionResponse> = webTestClient.get().uri("/subscriptions/account/$prisonNumber")
    .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
    .exchange()
    .expectStatus().isOk
    .expectBody<PagedResponse<SubscriptionResponse>>()
    .returnResult().responseBody!!

  fun createSubscription(
    prisonNumber: String,
    paymentProfileId: Long,
    startDate: LocalDate = LocalDate.now(),
    endDate: LocalDate? = null,
  ): SubscriptionResponse {
    val firstCreateSubscriptionRequest = CreateSubscriptionRequest(
      legacyPaymentProfileId = paymentProfileId,
      amount = 100,
      paymentSchedule = SubscriptionPaymentSchedule.MONDAY,
      startDate = startDate,
      endDate = endDate,
      subscriptionType = "TELE",
      reference = "REF",
      createdBy = "USER",
      createdAt = Instant.now(),
      prisonId = "LEI",
      prisonNumber = prisonNumber,
      subAccountRef = SubAccountRef.CASH,
    )

    val subscriptionResponse = webTestClient.post().uri("/subscriptions")
      .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
      .headers(setIdempotencyKey(UUID.randomUUID()))
      .bodyValue(firstCreateSubscriptionRequest)
      .exchange()
      .expectStatus().isCreated
      .expectBody<SubscriptionResponse>()
      .returnResult()
      .responseBody!!

    return subscriptionResponse
  }

  fun updateSubscription(subscriptionId: UUID, endDate: LocalDate, updatedBy: String = "UPDATED_USER"): SubscriptionResponse {
    val updateSubscriptionRequest: UpdateSubscriptionRequest = UpdateSubscriptionRequest(
      endDate = endDate,
      updatedBy = updatedBy,
    )

    val updatedResponse = webTestClient.post().uri("/subscriptions/$subscriptionId/update")
      .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
      .bodyValue(updateSubscriptionRequest)
      .exchange()
      .expectStatus()
      .isOk
      .expectBody<SubscriptionResponse>()
      .returnResult()
      .responseBody!!

    return updatedResponse
  }

  companion object {
    fun setIdempotencyKey(
      key: UUID,
    ): (HttpHeaders) -> Unit = { it.set("Idempotency-Key", key.toString()) }
  }
}
