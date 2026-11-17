package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.validators

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [SubscriptionUpdateRequestValidator::class])
annotation class ValidSubscriptionUpdateRequest(
  val message: String = "invalid request",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = [],
)
