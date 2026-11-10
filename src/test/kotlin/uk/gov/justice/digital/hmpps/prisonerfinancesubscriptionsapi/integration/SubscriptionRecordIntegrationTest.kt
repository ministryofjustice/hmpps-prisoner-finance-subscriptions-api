package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.config.ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RO
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.config.ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.SubAccountRef
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.CreateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.UpdateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.AccountSubscriptionSummaryResponse
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.PagedResponse
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.SubscriptionResponse
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlin.random.Random

class SubscriptionRecordIntegrationTest : IntegrationTestBase() {

  @BeforeEach
  fun setup() {
    integrationTestHelpers.clearDatabase()
  }

  val mapper = ObjectMapper()

  val prisonNumber = "A1234BC"

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
    fun `should return 201 record when using the same idempotency key`() {
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
      assertThat(responseOne.legacyPaymentProfileId).isEqualTo(responseTwo.legacyPaymentProfileId)
      assertThat(responseOne.amount).isEqualTo(responseTwo.amount)
      assertThat(responseOne.dayOfPayment).isEqualTo(responseTwo.dayOfPayment)
      assertThat(responseOne.startDate).isEqualTo(responseTwo.startDate)
      assertThat(responseOne.endDate).isEqualTo(responseTwo.endDate)
      assertThat(responseOne.subscriptionType).isEqualTo(responseTwo.subscriptionType)
      assertThat(responseOne.reference).isEqualTo(responseTwo.reference)
      assertThat(responseOne.createdBy).isEqualTo(responseTwo.createdBy)
      assertThat(responseOne.prisonId).isEqualTo(responseTwo.prisonId)
      assertThat(responseOne.prisonNumber).isEqualTo(responseTwo.prisonNumber)
      assertThat(responseOne.subAccountRef).isEqualTo(responseTwo.subAccountRef)
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
    fun `should return 201 when legacy payment profile id is not included`() {
      val createSubscriptionRequestWithoutLegacyPaymentProfileId = CreateSubscriptionRequest(
        legacyPaymentProfileId = null,
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

      val idempotencyKey = UUID.randomUUID()
      val response = webTestClient.post().uri("/subscriptions")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .headers(setIdempotencyKey(idempotencyKey))
        .bodyValue(createSubscriptionRequestWithoutLegacyPaymentProfileId)
        .exchange()
        .expectStatus()
        .isCreated
        .expectBody<SubscriptionResponse>()
        .returnResult()
        .responseBody!!

      assertThat(response.id).isNotNull
      assertThat(response.legacyPaymentProfileId).isNull()
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

  @Nested
  inner class GetSubscriptionSummaryTest {

    @Test
    fun `should return 200 and correct values when the prisoner has subscriptions`() {
      val prisonNumber = "A1234BC"

      val firstCreateSubscriptionRequest = CreateSubscriptionRequest(
        legacyPaymentProfileId = 123456788,
        amount = 100,
        dayOfPayment = DayOfWeek.MONDAY,
        startDate = LocalDate.now(),
        endDate = null,
        subscriptionType = "TELE",
        reference = "REF",
        createdBy = "USER",
        createdAt = Instant.now(),
        prisonId = "LEI",
        prisonNumber = prisonNumber,
        subAccountRef = SubAccountRef.CASH,
      )

      val secondCreateSubscriptionRequest = CreateSubscriptionRequest(
        legacyPaymentProfileId = 123456789,
        amount = 150,
        dayOfPayment = DayOfWeek.MONDAY,
        startDate = LocalDate.now(),
        endDate = null,
        subscriptionType = "TELE",
        reference = "REF",
        createdBy = "USER",
        createdAt = Instant.now(),
        prisonId = "LEI",
        prisonNumber = prisonNumber,
        subAccountRef = SubAccountRef.CASH,
      )

      val expectedResponse = AccountSubscriptionSummaryResponse(
        weeklyPaymentAmount = firstCreateSubscriptionRequest.amount + secondCreateSubscriptionRequest.amount,
        subscriptionCount = 2,
      )

      webTestClient.post().uri("/subscriptions")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .headers(setIdempotencyKey(UUID.randomUUID()))
        .bodyValue(firstCreateSubscriptionRequest)
        .exchange()

      webTestClient.post().uri("/subscriptions")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .headers(setIdempotencyKey(UUID.randomUUID()))
        .bodyValue(secondCreateSubscriptionRequest)
        .exchange()

      val actualResponse = webTestClient.get().uri("/subscriptions/account/$prisonNumber/summary")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RO)))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<AccountSubscriptionSummaryResponse>()
        .returnResult()
        .responseBody!!

      assertThat(actualResponse.weeklyPaymentAmount).isEqualTo(expectedResponse.weeklyPaymentAmount)
      assertThat(actualResponse.subscriptionCount).isEqualTo(expectedResponse.subscriptionCount)
    }

    @Test
    fun `should return 0 balance when the prisoner has no subscriptions`() {
      val actualResponse = webTestClient.get().uri("/subscriptions/account/$prisonNumber/summary")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RO)))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<AccountSubscriptionSummaryResponse>()
        .returnResult()
        .responseBody!!

      assertThat(actualResponse.weeklyPaymentAmount).isEqualTo(0)
      assertThat(actualResponse.subscriptionCount).isEqualTo(0)
    }

    @Test
    fun `should return 401 when unauthorised`() {
      webTestClient.get().uri("/subscriptions/account/$prisonNumber/summary")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return 403 forbidden when user does not have the correct role`() {
      webTestClient.get().uri("/subscriptions/account/$prisonNumber/summary")
        .headers(setAuthorisation(roles = listOf("WRONG_ROLE")))
        .headers(setIdempotencyKey(UUID.randomUUID()))
        .exchange()
        .expectStatus()
        .isForbidden
    }
  }

  @Nested
  inner class UpdateSubscription {

    lateinit var subscriptionResponse: SubscriptionResponse

    @BeforeEach
    fun setup() {
      subscriptionResponse = createSubscription(prisonNumber, 123456788)
    }

    @Test
    fun `should update subscription with end date and return 200`() {
      val updateSubscriptionRequest = UpdateSubscriptionRequest(
        endDate = LocalDate.now(),
      )
      val updatedResponse = webTestClient.post().uri("/subscriptions/${subscriptionResponse.id}/update")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .bodyValue(updateSubscriptionRequest)
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<SubscriptionResponse>()
        .returnResult()
        .responseBody!!

      assertThat(updatedResponse.endDate).isEqualTo(updateSubscriptionRequest.endDate)
    }

    @Test
    fun `should update subscription with start date and return 200`() {
      val updateSubscriptionRequest = UpdateSubscriptionRequest(
        startDate = LocalDate.now().plusDays(7),
      )
      val updatedResponse = webTestClient.post().uri("/subscriptions/${subscriptionResponse.id}/update")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .bodyValue(updateSubscriptionRequest)
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<SubscriptionResponse>()
        .returnResult()
        .responseBody!!

      assertThat(updatedResponse.startDate).isEqualTo(updateSubscriptionRequest.startDate)
    }

    @Test
    fun `should update subscription with start date and end date and return 200`() {
      val updateSubscriptionRequest = UpdateSubscriptionRequest(
        startDate = LocalDate.now().plusDays(7),
        endDate = LocalDate.now().plusDays(14),
      )
      val updatedResponse = webTestClient.post().uri("/subscriptions/${subscriptionResponse.id}/update")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .bodyValue(updateSubscriptionRequest)
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<SubscriptionResponse>()
        .returnResult()
        .responseBody!!

      assertThat(updatedResponse.startDate).isEqualTo(updateSubscriptionRequest.startDate)
      assertThat(updatedResponse.endDate).isEqualTo(updateSubscriptionRequest.endDate)
    }

    @Test
    fun `should return 400 when end date has already updated`() {
      val updateSubscriptionRequest = UpdateSubscriptionRequest(
        endDate = LocalDate.now(),
      )

      webTestClient.post().uri("/subscriptions/${subscriptionResponse.id}/update")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .bodyValue(updateSubscriptionRequest)
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<SubscriptionResponse>()
        .returnResult()
        .responseBody!!

      webTestClient.post().uri("/subscriptions/${subscriptionResponse.id}/update")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .bodyValue(updateSubscriptionRequest)
        .exchange()
        .expectStatus()
        .isBadRequest
    }

    @Test
    fun `should return 400 when no fields to update provided`() {
      val updateSubscriptionRequest = UpdateSubscriptionRequest()

      webTestClient.post().uri("/subscriptions/${subscriptionResponse.id}/update")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .bodyValue(updateSubscriptionRequest)
        .exchange()
        .expectStatus()
        .isBadRequest
    }

    @Test
    fun `should return 400 when with a malformed request body`() {
      webTestClient.post().uri("/subscriptions/${subscriptionResponse.id}/update")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .bodyValue(
          """
          { 
          "key": "value"
          }
          """.trimIndent(),
        )
        .exchange()
        .expectStatus()
        .isBadRequest
    }

    @Test
    fun `should return 401 when unauthorised`() {
      val updateSubscriptionRequest = UpdateSubscriptionRequest(
        endDate = LocalDate.now(),
      )
      webTestClient.post().uri("/subscriptions/${subscriptionResponse.id}/update")
        .bodyValue(updateSubscriptionRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @ParameterizedTest
    @CsvSource("WRONG_ROLE", "ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RO")
    fun `should return 403 forbidden when user does not have the correct role`(role: String) {
      val updateSubscriptionRequest = UpdateSubscriptionRequest(
        endDate = LocalDate.now(),
      )
      webTestClient.post().uri("/subscriptions/${subscriptionResponse.id}/update")
        .headers(setAuthorisation(roles = listOf(role)))
        .bodyValue(updateSubscriptionRequest)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return 404 when subscription does not exist`() {
      val updateSubscriptionRequest = UpdateSubscriptionRequest(
        endDate = LocalDate.now(),
      )
      val invalidSubscriptionId = UUID.randomUUID()

      webTestClient.post().uri("/subscriptions/$invalidSubscriptionId/update")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .bodyValue(updateSubscriptionRequest)
        .exchange()
        .expectStatus()
        .isNotFound
    }
  }

  @Nested
  inner class GetSubscriptions {

    @ParameterizedTest
    @CsvSource("ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RO", "ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW")
    fun `should return 200 and a list of all subscriptions paged when filter not supplied`(role: String) {
      repeat(3) { i ->
        createSubscription(
          prisonNumber = prisonNumber,
          paymentProfileId = i.toLong(),
          startDate = LocalDate.now(),
          endDate = null,
        )
      }

      repeat(3) { i ->
        createSubscription(
          prisonNumber = prisonNumber,
          paymentProfileId = 3 + i.toLong(),
          startDate = LocalDate.now().minusWeeks(2),
          endDate = LocalDate.now().minusDays(1),
        )
      }

      val actualResponse = webTestClient.get().uri("/subscriptions/account/$prisonNumber")
        .headers(setAuthorisation(roles = listOf(role)))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<PagedResponse<SubscriptionResponse>>()
        .returnResult()
        .responseBody!!

      assertThat(actualResponse.totalElements).isEqualTo(6)
    }

    @Test
    fun `should return 200 and a list of active subscriptions paged when filter is supplied`() {
      repeat(3) { i ->
        createSubscription(
          prisonNumber = prisonNumber,
          paymentProfileId = i.toLong(),
          startDate = LocalDate.now(),
          endDate = null,
        )
      }

      repeat(3) { i ->
        createSubscription(
          prisonNumber = prisonNumber,
          paymentProfileId = 3 + i.toLong(),
          startDate = LocalDate.now().minusWeeks(2),
          endDate = LocalDate.now().minusDays(1),
        )
      }

      createSubscription(
        prisonNumber = prisonNumber,
        paymentProfileId = 7,
        startDate = LocalDate.now().minusWeeks(1),
        endDate = LocalDate.now().plusDays(1),
      )

      val actualResponse = webTestClient.get().uri("/subscriptions/account/$prisonNumber?status=ACTIVE")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RO)))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<PagedResponse<SubscriptionResponse>>()
        .returnResult()
        .responseBody!!

      assertThat(actualResponse.totalElements).isEqualTo(4)
    }

    @Test
    fun `should return 200 and a list of inactive subscriptions paged when filter is supplied`() {
      repeat(3) { i ->
        createSubscription(
          prisonNumber = prisonNumber,
          paymentProfileId = i.toLong(),
          startDate = LocalDate.now(),
          endDate = null,
        )
      }

      repeat(3) { i ->
        createSubscription(
          prisonNumber = prisonNumber,
          paymentProfileId = 3 + i.toLong(),
          startDate = LocalDate.now().minusWeeks(2),
          endDate = LocalDate.now().minusDays(1),
        )
      }

      createSubscription(
        prisonNumber = prisonNumber,
        paymentProfileId = 7,
        startDate = LocalDate.now().minusWeeks(1),
        endDate = LocalDate.now().plusDays(1),
      )

      val actualResponse = webTestClient.get().uri("/subscriptions/account/$prisonNumber?status=INACTIVE")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RO)))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<PagedResponse<SubscriptionResponse>>()
        .returnResult()
        .responseBody!!

      assertThat(actualResponse.totalElements).isEqualTo(3)
    }

    @Test
    fun `should return 200 and empty list if there are no subscriptions`() {
      val actualResponse = webTestClient.get().uri("/subscriptions/account/$prisonNumber")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RO)))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<PagedResponse<SubscriptionResponse>>()
        .returnResult()
        .responseBody!!

      assertThat(actualResponse.totalElements).isEqualTo(0)
      assertThat(actualResponse.content.size).isEqualTo(0)
    }

    @Test
    fun `should order results by startDate`() {
      val startDate = LocalDate.now()

      val startDates = mutableListOf<LocalDate>()

      repeat(10) { i ->

        val randomStartDate = startDate.minusDays(Random.nextLong(0, 20))

        startDates.add(randomStartDate)

        createSubscription(
          prisonNumber = prisonNumber,
          paymentProfileId = i.toLong(),
          startDate = randomStartDate,
        )
      }

      val actualResponse = webTestClient.get().uri("/subscriptions/account/$prisonNumber")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RO)))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<PagedResponse<SubscriptionResponse>>()
        .returnResult()
        .responseBody!!

      val expectedStartDateOrder = startDates.sortedDescending()

      val responseStartDateOrder = actualResponse.content.map { it.startDate }

      assertThat(expectedStartDateOrder).isEqualTo(responseStartDateOrder)
    }

    @Test
    fun `should get second page of results of subscriptions`() {
      repeat(25) { i ->
        createSubscription(prisonNumber, i.toLong())
      }

      val responseBody = webTestClient.get().uri("/subscriptions/account/$prisonNumber?pageSize=24&pageNumber=2")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RO)))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody<PagedResponse<SubscriptionResponse>>()
        .returnResult()
        .responseBody!!

      assertThat(responseBody.content).hasSize(1)
      assertThat(responseBody.totalElements).isEqualTo(25)
      assertThat(responseBody.totalPages).isEqualTo(2)
      assertThat(responseBody.pageNumber).isEqualTo(2)
    }

    @Test
    fun `should return 401 when unauthorised`() {
      webTestClient.get().uri("/subscriptions/account/$prisonNumber")
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return 403 forbidden when user does not have the correct role`() {
      webTestClient.get().uri("/subscriptions/account/$prisonNumber")
        .headers(setAuthorisation(roles = listOf("WRONG_ROLE")))
        .exchange()
        .expectStatus()
        .isForbidden
    }
  }
}
