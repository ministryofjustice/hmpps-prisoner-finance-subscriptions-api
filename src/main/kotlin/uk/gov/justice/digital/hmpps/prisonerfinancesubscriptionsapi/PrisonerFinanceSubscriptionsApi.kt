package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PrisonerFinanceSubscriptionsApi

fun main(args: Array<String>) {
  runApplication<PrisonerFinanceSubscriptionsApi>(*args)
}
