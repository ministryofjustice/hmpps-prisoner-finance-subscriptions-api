package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

class UpdateSubscriptionRequest(
  @field:Schema(description = "The end date to set for the subscription", example = "2026-13-04", required = false)
  val endDate: LocalDate? = null,
)
