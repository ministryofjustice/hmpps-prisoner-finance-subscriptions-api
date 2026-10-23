package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.services

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.IdempotencyEntity
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.IdempotencyKeyDataRepository
import java.util.UUID

@Service
class IdempotencyService(
  private val idempotencyKeyDataRepository: IdempotencyKeyDataRepository,
) {
  fun readIdempotencyKey(idempotencyKey: UUID): IdempotencyEntity? = idempotencyKeyDataRepository.getIdempotencyEntityByIdempotencyKey(idempotencyKey)
}
