package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.IdempotencyEntity
import java.util.UUID

@Repository
interface IdempotencyKeyDataRepository : JpaRepository<IdempotencyEntity, UUID> {
  fun getIdempotencyEntityByIdempotencyKey(id: UUID): IdempotencyEntity?
}
