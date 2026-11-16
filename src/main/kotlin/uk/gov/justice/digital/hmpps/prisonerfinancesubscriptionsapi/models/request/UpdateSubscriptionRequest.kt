package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

class UpdateSubscriptionRequest(
  @field:Schema(
    description = "The start date to set for the subscription. " +
      "You cannot update a start date if the subscription is already active i.e. the start date is in the past. " +
      "Must be before the end date if set.",
    example = "2026-11-04",
    required = false,
  )
  val startDate: LocalDate? = null,

  @field:Schema(
    description = "The end date of the subscription. " +
      "Must be after the start date if set",
    example = "2026-12-04",
    required = false,
  )
  val endDate: LocalDate? = null,
) {

  fun isEmpty(): Boolean = this.endDate == null && this.startDate == null
}
