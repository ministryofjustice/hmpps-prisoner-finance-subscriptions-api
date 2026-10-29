package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.config.ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.CreateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.AccountSubscriptionSummaryResponse
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.SubscriptionResponse
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.services.IdempotencyService
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.services.SubscriptionService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.UUID

@Tag(name = "Subscription Records")
@RestController
class SubscriptionRecordsController(
  private val subscriptionService: SubscriptionService,
  private val idempotencyService: IdempotencyService,
) {

  @Operation(
    summary = "Create a new subscription record",
    description = "Creates a new subscription for a prisoners sub-account.\n Requires an Idempotency Key to ensure that subscriptions are not duplicated.",
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "Subscription created",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = SubscriptionResponse::class))],
      ),
      ApiResponse(
        responseCode = "400",
        description = "Bad Request",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized - requires a valid OAuth2 token",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden - requires an appropriate role",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "500",
        description = "Internal Server Error - An unexpected error occurred.",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @SecurityRequirement(name = "bearer-jwt", scopes = [ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW])
  @PreAuthorize("hasAnyAuthority('$ROLE_PRISONER_FINANCE__SUBSCRIPTIONS__RW')")
  @PostMapping("/subscriptions")
  fun createSubscriptionRecord(
    @Parameter(
      name = "Idempotency-Key",
      `in` = ParameterIn.HEADER,
      required = true,
      description = "An Idempotency Key to ensure that subscription creation requests are not repeated",
    )
    @RequestHeader(
      "Idempotency-Key",
      required = true,
    )
    idempotencyKey: UUID,
    @Valid @RequestBody createSubscriptionRequest: CreateSubscriptionRequest,
  ): ResponseEntity<SubscriptionResponse> {
    idempotencyService.findIdempotencyKeyOrNull(idempotencyKey)?.let {
      return ResponseEntity.status(201).body(SubscriptionResponse.fromEntity(it.subscription))
    }

    val subscriptionResponse = subscriptionService.createSubscription(createSubscriptionRequest, idempotencyKey)

    return ResponseEntity.status(201).body(subscriptionResponse)
  }

  @GetMapping(path = ["/subscriptions/account/{prisonerNumber}/summary"])
  fun getSubscriptionRecords(
    @PathVariable prisonerNumber: String,
  ): ResponseEntity<AccountSubscriptionSummaryResponse> {
    val subscriptionRecords = subscriptionService.getSubscriptionSummary(prisonerNumber)
    return ResponseEntity.ok(subscriptionRecords)
  }
}
