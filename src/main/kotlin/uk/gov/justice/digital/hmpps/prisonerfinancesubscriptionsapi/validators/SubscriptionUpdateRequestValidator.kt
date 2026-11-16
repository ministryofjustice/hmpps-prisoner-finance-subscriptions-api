package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.validators

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.UpdateSubscriptionRequest
import java.time.LocalDate

class SubscriptionUpdateRequestValidator : ConstraintValidator<ValidSubscriptionUpdateRequest, UpdateSubscriptionRequest> {

  override fun isValid(request: UpdateSubscriptionRequest?, p1: ConstraintValidatorContext?): Boolean {
    if (request == null) return false

    if (request.isEmpty()) return false

    val today = LocalDate.now()
    if (request.startDate != null && request.startDate.isBefore(today)) return false
    if (request.endDate != null && request.endDate.isBefore(today)) return false

    if (request.endDate != null && request.startDate != null && request.endDate.isBefore(request.startDate)) return false

    return true
  }
}
