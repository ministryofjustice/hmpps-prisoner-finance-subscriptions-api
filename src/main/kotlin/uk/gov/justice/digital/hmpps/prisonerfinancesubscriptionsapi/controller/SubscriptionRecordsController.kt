package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.controller

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Subscription Records")
@RestController
class SubscriptionRecordsController {

  @PostMapping("/subscriptions")
  fun createSubscriptionRecord() {
  }
}
