package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.integration.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.config.ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.CreateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.SubscriptionResponse
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

class SubscriptionRecordIntegrationTest : IntegrationTestBase() {

  @Nested
  inner class CreateSubscription {

    @Test
    fun `should return 201 and created record`() {
      val createSubscriptionRequest: CreateSubscriptionRequest = CreateSubscriptionRequest(
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
        subAccountRef = "CASH",
      )

      val response = webTestClient.post().uri("/subscriptions")
        .headers(setAuthorisation(roles = listOf(ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW)))
        .bodyValue(createSubscriptionRequest)
        .exchange()
        .expectStatus()
        .isOk
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
  }
}
