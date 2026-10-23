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
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.SubscriptionEntity
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.CreateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.SubscriptionsRepository
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class RecordServiceTest {

  @Mock
  lateinit var subscriptionsRepository: SubscriptionsRepository

  @InjectMocks
  private lateinit var recordService: RecordService

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
      subAccountRef = "CASH",
    )

    @BeforeEach
    fun setup() {
      whenever { subscriptionsRepository.save(any<SubscriptionEntity>()) }.thenAnswer { it.arguments[0] }
      recordService.createSubscription(createSubscriptionRequest)
    }

    @Test
    fun `should save the subscription record`() {
      verify(subscriptionsRepository, times(1)).save(any())
    }

    @Test
    fun `should return the created subscription as a response`() {
      val createdSubscription = recordService.createSubscription(createSubscriptionRequest)
      assertThat(createdSubscription.id).isNotNull()
      assertThat(createdSubscription.legacyPaymentProfileId).isEqualTo(createSubscriptionRequest.legacyPaymentProfileId)
      assertThat(createdSubscription.amount).isEqualTo(createSubscriptionRequest.amount)
      assertThat(createdSubscription.dayOfPayment).isEqualTo(createSubscriptionRequest.dayOfPayment)
      assertThat(createdSubscription.startDate).isEqualTo(createSubscriptionRequest.startDate)
      assertThat(createdSubscription.endDate).isEqualTo(createSubscriptionRequest.endDate)
      assertThat(createdSubscription.subscriptionType).isEqualTo(createSubscriptionRequest.subscriptionType)
      assertThat(createdSubscription.reference).isEqualTo(createSubscriptionRequest.reference)
      assertThat(createdSubscription.createdBy).isEqualTo(createSubscriptionRequest.createdBy)
      assertThat(createdSubscription.createdAt).isEqualTo(createSubscriptionRequest.createdAt)
      assertThat(createdSubscription.prisonId).isEqualTo(createSubscriptionRequest.prisonId)
      assertThat(createdSubscription.prisonNumber).isEqualTo(createSubscriptionRequest.prisonNumber)
      assertThat(createdSubscription.subAccountRef).isEqualTo(createSubscriptionRequest.subAccountRef)
    }
  }
}
