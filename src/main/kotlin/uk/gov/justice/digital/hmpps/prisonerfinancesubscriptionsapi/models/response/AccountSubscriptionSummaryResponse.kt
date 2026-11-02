package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response

import io.swagger.v3.oas.annotations.media.Schema

class AccountSubscriptionSummaryResponse(
  @field:Schema(description = "Combined maximum weekly payment amount of all subscriptions on the account in pence", example = "1000")
  val weeklyPaymentAmount: Long,
  @field:Schema(description = "The number of active subscriptions", example = "3")
  val subscriptionCount: Int,
)
