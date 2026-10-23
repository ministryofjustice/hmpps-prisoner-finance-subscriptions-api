package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.services.idempotency

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.IdempotencyEntity
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.SubscriptionEntity
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.IdempotencyKeyDataRepository
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.services.IdempotencyService
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class IdempotencyServiceTest {
  @Mock
  lateinit var idempotencyKeyDataRepository: IdempotencyKeyDataRepository

  @InjectMocks
  lateinit var idempotencyService: IdempotencyService

  @Nested
  inner class FindIdempotencyKeyOrNull {

    @Test
    fun `Should return null if no idempotency key exists`() {
      val idempotencyKey = UUID.randomUUID()

      whenever(idempotencyKeyDataRepository.getIdempotencyEntityByIdempotencyKey(idempotencyKey)).thenReturn(null)

      val idempotencyEntityOrNull = idempotencyService.findIdempotencyKeyOrNull(idempotencyKey)

      assert(idempotencyEntityOrNull == null)
    }

    @Test
    fun `Should return idempotency entity if it exists`() {
      val idempotencyKey = UUID.randomUUID()
      val subscription = SubscriptionEntity(
        id = UUID.randomUUID(),
        legacyPaymentProfileId = 123456,
        amount = 50L,
        dayOfPayment = DayOfWeek.MONDAY,
        startDate = LocalDate.now(),
        endDate = LocalDate.now().plusDays(10),
        subscriptionType = "TELE",
        reference = "REF",
        createdBy = "TEST_USER",
        createdAt = Instant.now(),
        prisonId = "LEI",
        prisonNumber = "AD12345",
        subAccountRef = "CASH",
      )

      val idempotencyEntity = IdempotencyEntity(idempotencyKey, subscription)

      whenever(idempotencyKeyDataRepository.getIdempotencyEntityByIdempotencyKey(idempotencyKey)).thenReturn(idempotencyEntity)

      val idempotencyEntityOrNull = idempotencyService.findIdempotencyKeyOrNull(idempotencyKey)

      Assertions.assertThat(idempotencyEntityOrNull!!.idempotencyKey).isEqualTo(idempotencyKey)
      Assertions.assertThat(idempotencyEntityOrNull.subscription).isEqualTo(subscription)
    }
  }
}
