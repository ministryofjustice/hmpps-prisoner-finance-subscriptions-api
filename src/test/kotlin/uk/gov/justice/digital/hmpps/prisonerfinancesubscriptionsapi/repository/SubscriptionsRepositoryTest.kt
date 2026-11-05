package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.ContainersConfig
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.SubAccountRef
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.SubscriptionEntity
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.SubscriptionsRepository
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Import(ContainersConfig::class)
@DataJpaTest
class SubscriptionsRepositoryTest @Autowired constructor(
  val subscriptionsRepository: SubscriptionsRepository,
  private val entityManager: TestEntityManager,
) {
  val prisonNumber = "A1234AA"
  val pageRequest = PageRequest.of(
    0,
    25,
  )

  @BeforeEach
  fun setup() {
    subscriptionsRepository.deleteAll()
    entityManager.flush()
  }

  fun createSubscription(legacyPaymentProfileId: Long, startDate: LocalDate, endDate: LocalDate? = null, prisonNumber: String): SubscriptionEntity {
    val subscription = SubscriptionEntity(
      subscriptionId = UUID.randomUUID(),
      legacyPaymentProfileId = legacyPaymentProfileId,
      amount = 1,
      dayOfPayment = DayOfWeek.MONDAY,
      startDate = startDate,
      endDate = endDate,
      subscriptionType = "",
      createdBy = "",
      createdAt = Instant.now(),
      prisonId = "LEI",
      prisonNumber = prisonNumber,
      subAccountRef = SubAccountRef.CASH,
    )
    entityManager.persist(subscription)
    return subscription
  }

  @Nested
  inner class GetActiveSubscriptionsForPrisonNumber {

    @Test
    fun `should return an empty list when there are no subscriptions`() {
      val results = subscriptionsRepository.getActiveSubscriptionsForPrisonNumber(prisonNumber, pageRequest)

      assertThat(results.content.size).isEqualTo(0)
    }

    @Test
    fun `should include subscriptions where the endDate is null`() {
      repeat(5) { i ->
        createSubscription(
          legacyPaymentProfileId = i.toLong(),
          startDate = LocalDate.now().minusDays(1),
          endDate = null,
          prisonNumber = prisonNumber,
        )
      }

      val results = subscriptionsRepository.getActiveSubscriptionsForPrisonNumber(prisonNumber, pageRequest)

      assertThat(results.content.size).isEqualTo(5)
    }

    @Test
    fun `should include subscriptions where the endDate is today`() {
      createSubscription(
        legacyPaymentProfileId = 1,
        startDate = LocalDate.now().minusDays(1),
        endDate = LocalDate.now(),
        prisonNumber = prisonNumber,
      )
      val results = subscriptionsRepository.getActiveSubscriptionsForPrisonNumber(prisonNumber, pageRequest)

      assertThat(results.content.size).isEqualTo(1)
    }

    @Test
    fun `should exclude subscriptions where the endDate is in the past`() {
      createSubscription(
        legacyPaymentProfileId = 1,
        startDate = LocalDate.now().minusDays(7),
        endDate = LocalDate.now().minusDays(1),
        prisonNumber = prisonNumber,
      )
      val results = subscriptionsRepository.getActiveSubscriptionsForPrisonNumber(prisonNumber, pageRequest, LocalDate.now())

      assertThat(results.content.size).isEqualTo(0)
    }

    @Test
    fun `should include subscriptions where the endDate is in the future`() {
      createSubscription(
        legacyPaymentProfileId = 1,
        startDate = LocalDate.now().minusDays(7),
        endDate = LocalDate.now().plusDays(7),
        prisonNumber = prisonNumber,
      )
      val results = subscriptionsRepository.getActiveSubscriptionsForPrisonNumber(prisonNumber, pageRequest, LocalDate.now())

      assertThat(results.content.size).isEqualTo(1)
    }
  }
}
