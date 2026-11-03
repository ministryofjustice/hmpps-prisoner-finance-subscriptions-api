package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.config

import org.springframework.http.HttpStatusCode

class CustomException(
  override val message: String,
  val status: HttpStatusCode,
  override val cause: Exception? = null,
  val developerMessage: String? = null,
) : Exception(message)
