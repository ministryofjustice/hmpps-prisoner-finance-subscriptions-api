package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.integration.helpers

import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.IdempotencyKeyDataRepository
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.SubscriptionsRepository

@TestConfiguration
class IntegrationTestHelpers(
  private val idempotencyKeyDataRepository: IdempotencyKeyDataRepository,
  private val subscriptionsRepository: SubscriptionsRepository,
) {

  @Autowired
  lateinit var entityManager: EntityManager

  @Transactional
  fun clearDatabase() {
    entityManager.clear()
    idempotencyKeyDataRepository.deleteAllInBatch()
    subscriptionsRepository.deleteAllInBatch()
  }
}
