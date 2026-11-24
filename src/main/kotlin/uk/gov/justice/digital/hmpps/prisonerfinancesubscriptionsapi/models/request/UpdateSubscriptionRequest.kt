package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request

import com.fasterxml.jackson.annotation.JsonIgnore
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.validators.ValidSubscriptionUpdateRequest
import java.time.LocalDate

@ValidSubscriptionUpdateRequest
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

  @field:Schema(
    description = "The comment to set for the subscription",
    example = "This is a comment",
    required = false,
  )
  val comment: String? = null,

  @field:Schema(
    description = "The user name that updated the record.",
    example = "A739XYY",
    required = true,
  )
  val updatedBy: String,
) {

  @JsonIgnore
  @Schema(hidden = true)
  fun isEmpty(): Boolean = this.endDate == null && this.startDate == null && this.comment == null
}
