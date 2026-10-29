package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.IdempotencyEntity
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.SubAccountRef
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.SubscriptionEntity
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.CreateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.response.SubscriptionResponse
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.IdempotencyKeyDataRepository
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.SubscriptionsRepository
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class SubscriptionServiceTest {

  @Mock
  lateinit var idempotencyKeyDataRepository: IdempotencyKeyDataRepository

  @Mock
  lateinit var subscriptionsRepository: SubscriptionsRepository

  @InjectMocks
  private lateinit var subscriptionService: SubscriptionService

  @Nested
  inner class CreateSubscription {

    val createSubscriptionRequest = CreateSubscriptionRequest(
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
      subAccountRef = SubAccountRef.CASH,
    )

    val idempotency = IdempotencyEntity(UUID.randomUUID(), createSubscriptionRequest.toEntity())

    lateinit var result: SubscriptionResponse

    val subscriptionCaptor = argumentCaptor<SubscriptionEntity>()
    val idempotencyCaptor = argumentCaptor<IdempotencyEntity>()

    @BeforeEach
    fun setup() {
      whenever { subscriptionsRepository.save(any<SubscriptionEntity>()) }.thenAnswer { it.arguments[0] }
      result = subscriptionService.createSubscription(createSubscriptionRequest, idempotency.idempotencyKey)
    }

    @Test
    fun `should save the subscription record and idempotency key`() {
      verify(subscriptionsRepository, times(1)).save(subscriptionCaptor.capture())
      verify(idempotencyKeyDataRepository, times(1)).save(idempotencyCaptor.capture())

      assertThat(subscriptionCaptor.firstValue.legacyPaymentProfileId).isEqualTo(createSubscriptionRequest.legacyPaymentProfileId)
      assertThat(idempotencyCaptor.firstValue.subscription).isEqualTo(subscriptionCaptor.firstValue)
      assertThat(idempotencyCaptor.firstValue.idempotencyKey).isEqualTo(idempotency.idempotencyKey)
    }

    @Test
    fun `should return the created subscription as a response`() {
      assertThat(result.id).isNotNull()
      assertThat(result.legacyPaymentProfileId).isEqualTo(createSubscriptionRequest.legacyPaymentProfileId)
      assertThat(result.amount).isEqualTo(createSubscriptionRequest.amount)
      assertThat(result.dayOfPayment).isEqualTo(createSubscriptionRequest.dayOfPayment)
      assertThat(result.startDate).isEqualTo(createSubscriptionRequest.startDate)
      assertThat(result.endDate).isEqualTo(createSubscriptionRequest.endDate)
      assertThat(result.subscriptionType).isEqualTo(createSubscriptionRequest.subscriptionType)
      assertThat(result.reference).isEqualTo(createSubscriptionRequest.reference)
      assertThat(result.createdBy).isEqualTo(createSubscriptionRequest.createdBy)
      assertThat(result.createdAt).isEqualTo(createSubscriptionRequest.createdAt)
      assertThat(result.prisonId).isEqualTo(createSubscriptionRequest.prisonId)
      assertThat(result.prisonNumber).isEqualTo(createSubscriptionRequest.prisonNumber)
      assertThat(result.subAccountRef).isEqualTo(createSubscriptionRequest.subAccountRef)
    }
  }

  @Nested
  inner class GetSubscriptionSummary {
    val prisonerNumber = "A1234BC"

    @Test
    fun `should return the subscription summary as a response`() {
      val mockedSubscriptionEntity = SubscriptionEntity(
        subscriptionId = UUID.randomUUID(),
        legacyPaymentProfileId = 12345L,
        amount = 10,
        dayOfPayment = DayOfWeek.MONDAY,
        startDate = LocalDate.now(),
        endDate = LocalDate.now().plusDays(1),
        subscriptionType = "",
        reference = "",
        createdBy = "",
        createdAt = Instant.now(),
        prisonId = "",
        prisonNumber = prisonerNumber,
        subAccountRef = SubAccountRef.CASH,
      )

      whenever { subscriptionsRepository.findByPrisonNumber(prisonerNumber) }.thenReturn(
        listOf(mockedSubscriptionEntity, mockedSubscriptionEntity),
      )

      val result = subscriptionService.getSubscriptionSummary(prisonerNumber)

      assertThat(result.subscriptionCount).isEqualTo(2)
      assertThat(result.weeklyPaymentAmount).isEqualTo(20)
    }
  }
}
