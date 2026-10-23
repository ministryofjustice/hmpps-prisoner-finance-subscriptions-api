package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.integration.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.config.ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.SubAccountRef
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.CreateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.SubscriptionResponse
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class SubscriptionRecordIntegrationTest : IntegrationTestBase() {

  @BeforeEach
  fun setup() {
    integrationTestHelpers.clearDatabase()
  }

  val mapper = ObjectMapper()

  @Nested
  inner class CreateSubscription {

    val createSubscriptionRequest = CreateSubscriptionRequest(
      legacyPaymentProfileId = 123456789,
      amount = 50,
      dayOfPayment = DayOfWeek.MONDAY,
      startDate = LocalDate.now(),
      endDate = null,
      subscriptionType = "TELE",
      reference = "REF",
      createdBy = "USER",
      createdAt = Instant.now(),
      prisonId = "LEI",
      prisonNumber = "AD12345",
      subAccountRef = SubAccountRef.CASH,
    )

    @Test
    fun `should return 201 and created record`() {
      val idempotencyKey = UUID.randomUUID()
      val response = webTestClient.post().uri("/subscriptions")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .headers(setIdempotencyKey(idempotencyKey))
        .bodyValue(createSubscriptionRequest)
        .exchange()
        .expectStatus()
        .isCreated
        .expectBody<SubscriptionResponse>()
        .returnResult()
        .responseBody!!

      assertThat(response.id).isNotNull
      assertThat(response.legacyPaymentProfileId).isEqualTo(123456789)
      assertThat(response.amount).isEqualTo(50)
      assertThat(response.dayOfPayment).isEqualTo(DayOfWeek.MONDAY)
      assertThat(response.startDate).isEqualTo(LocalDate.now())
      assertThat(response.endDate).isNull()
      assertThat(response.subscriptionType).isEqualTo("TELE")
      assertThat(response.reference).isEqualTo("REF")
      assertThat(response.createdAt).isEqualTo(createSubscriptionRequest.createdAt)
      assertThat(response.createdBy).isEqualTo(createSubscriptionRequest.createdBy)
      assertThat(response.prisonId).isEqualTo(createSubscriptionRequest.prisonId)
      assertThat(response.prisonNumber).isEqualTo(createSubscriptionRequest.prisonNumber)
      assertThat(response.subAccountRef).isEqualTo(createSubscriptionRequest.subAccountRef)
    }

    @Test
    fun `should return 201 and the existing record when using the same idempotency key`() {
      val idempotencyKey = UUID.randomUUID()

      val responseOne = webTestClient.post().uri("/subscriptions")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .headers(setIdempotencyKey(idempotencyKey))
        .bodyValue(createSubscriptionRequest)
        .exchange()
        .expectStatus()
        .isCreated
        .expectBody<SubscriptionResponse>()
        .returnResult()
        .responseBody!!

      val responseTwo = webTestClient.post().uri("/subscriptions")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .headers(setIdempotencyKey(idempotencyKey))
        .bodyValue(createSubscriptionRequest)
        .exchange()
        .expectStatus()
        .isCreated
        .expectBody<SubscriptionResponse>()
        .returnResult()
        .responseBody!!

      assertThat(responseOne.id).isEqualTo(responseTwo.id)
    }

    @Test
    fun `should return 400 if given a malformed request body`() {
      // Incorrect data types on keys
      webTestClient.post().uri("/subscriptions")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .header("Content-Type", "application/json")
        .bodyValue(
          "{\n" +
            "  \"legacyPaymentProfileId\": \"123456789\",\n" +
            "  \"amount\": \"50\",\n" +
            "  \"dayOfPayment\": 1,\n" +
            "  \"startDate\": 20261203,\n" +
            "  \"endDate\": false,\n" +
            "  \"subscriptionType\": 1001,\n" +
            "  \"reference\": 999.99,\n" +
            "  \"createdBy\": {\n" +
            "    \"username\": \"TEST_USER\"\n" +
            "  },\n" +
            "  \"createdAt\": 1688212800,\n" +
            "  \"prisonId\": true,\n" +
            "  \"prisonNumber\": 12345,\n" +
            "  \"subAccountRef\": [\"CASH\"]\n" +
            "}",
        )
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBody<ErrorResponse>()
        .returnResult()
        .responseBody!!

      // Missing required fields
      webTestClient.post().uri("/subscriptions")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .header("Content-Type", "application/json")
        .bodyValue("{ \"key\": \"value\" }")
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBody<ErrorResponse>()
        .returnResult()
        .responseBody!!

      // Missing headers
      webTestClient.post().uri("/subscriptions")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .bodyValue(mapper.writeValueAsString(mapOf("key" to "value")))
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBody<ErrorResponse>()
        .returnResult()
        .responseBody!!
    }

    @Test
    fun `should return 400 if given a malformed idempotency key`() {
      webTestClient.post().uri("/subscriptions")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .headers { it.set("Idempotency-Key", "NOT_A_UUID") }
        .header("Content-Type", "application/json")
        .bodyValue(createSubscriptionRequest)
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBody<ErrorResponse>()
        .returnResult()
        .responseBody!!
    }

    @Test
    fun `should return 400 if missing idempotency key`() {
      webTestClient.post().uri("/subscriptions")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .header("Content-Type", "application/json")
        .bodyValue(createSubscriptionRequest)
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBody<ErrorResponse>()
        .returnResult()
        .responseBody!!
    }

    @ParameterizedTest
    @CsvSource("WRONG_ROLE,ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RO")
    fun `should return 403 forbidden when user does not have the correct role`(role: String) {
      webTestClient.post().uri("/subscriptions")
        .headers(setAuthorisation(roles = listOf(role)))
        .headers(setIdempotencyKey(UUID.randomUUID()))
        .bodyValue(createSubscriptionRequest)
        .exchange()
        .expectStatus()
        .isForbidden
    }
  }
}
