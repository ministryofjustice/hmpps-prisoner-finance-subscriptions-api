package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.SubscriptionEntity
import java.util.UUID

interface SubscriptionsRepository : JpaRepository<SubscriptionEntity, UUID>
