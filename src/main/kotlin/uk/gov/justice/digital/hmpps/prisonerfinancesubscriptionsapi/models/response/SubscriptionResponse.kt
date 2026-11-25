package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.SubAccountRef
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.SubscriptionEntity
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.enums.SubscriptionPaymentSchedule
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class SubscriptionResponse(
  @field:Schema(description = "ID of the subscription", example = "123e4567-e89b-12d3-a456-426655440000")
  val id: UUID,
  @field:Schema(description = "The legacy payment profile ID", example = "123456789")
  val legacyPaymentProfileId: Long? = null,
  @field:Schema(description = "The amount paid weekly in pence", example = "50")
  val amount: Long,
  @field:Schema(description = "The day or days the payment is made weekly", example = "MONDAY_TO_FRIDAY / TUESDAY")
  val paymentSchedule: SubscriptionPaymentSchedule,
  @field:Schema(description = "The start date of the subscription", example = "2026-12-03")
  val startDate: LocalDate,
  @field:Schema(description = "The end date of the subscription", example = "2026-11-04")
  val endDate: LocalDate? = null,
  @field:Schema(description = "The transaction type on the payment profile", example = "TELE")
  val subscriptionType: String,
  @field:Schema(description = "The reference on the payment profile", example = "REF")
  val reference: String? = null,
  @field:Schema(description = "The user that created the subscription", example = "TEST_USER")
  val createdBy: String,
  @field:Schema(description = "The time that the subscription was created", example = "2023-07-01T12:00:00Z")
  val createdAt: Instant,
  @field:Schema(description = "The caseload ID of the prison", example = "LEI")
  val prisonId: String,
  @field:Schema(description = "The prison number of the offender that has the subscription", example = "AE12345")
  val prisonNumber: String,
  @field:Schema(description = "The sub-account that the subscription payment is debited from", example = "CASH")
  val subAccountRef: SubAccountRef,
  @field:Schema(description = "A comment to provide more context on the subscription", example = "This is a magazine subscription", required = false)
  val comment: String? = null,
  @field:Schema(description = "The user that last updated the record", example = "UPDATED_USER")
  val updatedBy: String? = null,
) {
  companion object {
    fun fromEntity(subscriptionEntity: SubscriptionEntity): SubscriptionResponse = SubscriptionResponse(
      id = subscriptionEntity.subscriptionId,
      legacyPaymentProfileId = subscriptionEntity.legacyPaymentProfileId,
      amount = subscriptionEntity.amount,
      paymentSchedule = subscriptionEntity.paymentSchedule,
      startDate = subscriptionEntity.startDate,
      endDate = subscriptionEntity.endDate,
      subscriptionType = subscriptionEntity.subscriptionType,
      reference = subscriptionEntity.reference,
      createdBy = subscriptionEntity.createdBy,
      createdAt = subscriptionEntity.createdAt,
      prisonId = subscriptionEntity.prisonId,
      prisonNumber = subscriptionEntity.prisonNumber,
      subAccountRef = subscriptionEntity.subAccountRef,
      comment = subscriptionEntity.comment,
      updatedBy = subscriptionEntity.updatedBy,
    )
  }
}
