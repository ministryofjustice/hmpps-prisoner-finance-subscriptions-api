package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.integration

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.config.ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.integration.helpers.IntegrationTestHelpers
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.integration.wiremock.GeneralLedgerApiExtension
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.integration.wiremock.GeneralLedgerApiExtension.Companion.generalLedgerApi
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.SubAccountRef
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.CreateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.SubscriptionResponse
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@ExtendWith(HmppsAuthApiExtension::class, GeneralLedgerApiExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@Import(ContainersConfig::class, IntegrationTestHelpers::class)
@AutoConfigureWebTestClient
abstract class IntegrationTestBase {

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @Autowired
  protected lateinit var integrationTestHelpers: IntegrationTestHelpers

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  protected fun stubPingWithResponse(status: Int, statusGeneralLedger: Int) {
    hmppsAuth.stubHealthPing(status)
    generalLedgerApi.stubHealthPing(statusGeneralLedger)
  }

  fun createSubscription(prisonNumber: String, paymentProfileId: Long, startDate: LocalDate = LocalDate.now()): SubscriptionResponse {
    val firstCreateSubscriptionRequest = CreateSubscriptionRequest(
      legacyPaymentProfileId = paymentProfileId,
      amount = 100,
      dayOfPayment = DayOfWeek.MONDAY,
      startDate = startDate,
      endDate = null,
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

  companion object {
    fun setIdempotencyKey(
      key: UUID,
    ): (HttpHeaders) -> Unit = { it.set("Idempotency-Key", key.toString()) }
  }
}
