package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.controller

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.config.ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.CreateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.SubscriptionResponse
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.services.IdempotencyService
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.services.RecordService
import java.util.UUID

@Tag(name = "Subscription Records")
@RestController
class SubscriptionRecordsController(
  private val recordService: RecordService,
  private val idempotencyService: IdempotencyService,
) {

  @SecurityRequirement(name = "bearer-jwt", scopes = [ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW])
  @PreAuthorize("hasAnyAuthority('$ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW')")
  @PostMapping("/subscriptions")
  fun createSubscriptionRecord(
    @Parameter(
      name = "Idempotency-Key",
      `in` = ParameterIn.HEADER,
      required = true,
      description = "An Idempotency Key to ensure that transactions are not repeated",
    )
    @RequestHeader(
      "Idempotency-Key",
      required = true,
    )
    idempotencyKey: UUID,
    @Valid @RequestBody createSubscriptionRequest: CreateSubscriptionRequest,
  ): ResponseEntity<SubscriptionResponse> {
    val idempotency = idempotencyService.readIdempotencyKey(idempotencyKey)
    val subscriptionResponse = recordService.createSubscription(createSubscriptionRequest)
    return ResponseEntity.status(201).body(subscriptionResponse)
  }
}
