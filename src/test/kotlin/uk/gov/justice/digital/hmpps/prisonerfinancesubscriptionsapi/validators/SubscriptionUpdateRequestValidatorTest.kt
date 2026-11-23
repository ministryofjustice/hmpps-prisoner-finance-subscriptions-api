package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.validators

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.UpdateSubscriptionRequest
import java.time.LocalDate

class SubscriptionUpdateRequestValidatorTest {

  val updatedBy = "UPDATED_USER"

  private val validator = SubscriptionUpdateRequestValidator()

  @Test
  fun `should return true when valid request provided`() {
    val updateSubscriptionRequest = UpdateSubscriptionRequest(
      startDate = LocalDate.now(),
      endDate = LocalDate.now().plusDays(30),
      updatedBy = updatedBy,
    )
    val ok = validator.isValid(updateSubscriptionRequest, null)
    assertThat(ok).isTrue()
  }

  @Test
  fun `should return false if the start date is in the past`() {
    val updateSubscriptionRequest = UpdateSubscriptionRequest(
      startDate = LocalDate.now().minusDays(30),
      updatedBy = updatedBy,
    )
    val ok = validator.isValid(updateSubscriptionRequest, null)
    assertThat(ok).isFalse()
  }

  @Test
  fun `should return false if the end date is in the past`() {
    val updateSubscriptionRequest = UpdateSubscriptionRequest(
      endDate = LocalDate.now().minusDays(30),
      updatedBy = updatedBy,
    )
    val ok = validator.isValid(updateSubscriptionRequest, null)
    assertThat(ok).isFalse()
  }

  @Test
  fun `should return false if the start date is after the end date`() {
    val updateSubscriptionRequest = UpdateSubscriptionRequest(
      startDate = LocalDate.now().plusDays(30),
      endDate = LocalDate.now().plusDays(10),
      updatedBy = updatedBy,
    )

    val ok = validator.isValid(updateSubscriptionRequest, null)
    assertThat(ok).isFalse()
  }

  @Test
  fun `should return false if the request is empty`() {
    val updateSubscriptionRequest = UpdateSubscriptionRequest(updatedBy = updatedBy)
    val ok = validator.isValid(updateSubscriptionRequest, null)
    assertThat(ok).isFalse()
  }
}
