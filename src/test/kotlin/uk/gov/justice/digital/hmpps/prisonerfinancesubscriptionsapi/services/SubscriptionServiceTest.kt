package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.config.CustomException
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.IdempotencyEntity
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.SubAccountRef
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities.SubscriptionEntity
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.enums.SubscriptionsStatus
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.CreateSubscriptionRequest
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.request.UpdateSubscriptionRequest
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
      whenever { subscriptionsRepository.saveAndFlush(any<SubscriptionEntity>()) }.thenAnswer { it.arguments[0] }
      result = subscriptionService.createSubscription(createSubscriptionRequest, idempotency.idempotencyKey)
    }

    @Test
    fun `should save the subscription record and idempotency key`() {
      verify(subscriptionsRepository, times(1)).saveAndFlush(subscriptionCaptor.capture())
      verify(idempotencyKeyDataRepository, times(1)).saveAndFlush(idempotencyCaptor.capture())

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

    @Test
    fun `should throw exception if subscription end date is before start date`() {
      val startDate = LocalDate.now()

      val invalidCreateSubscriptionRequest = CreateSubscriptionRequest(
        legacyPaymentProfileId = 123456,
        amount = 50L,
        dayOfPayment = DayOfWeek.MONDAY,
        startDate = startDate.plusYears(180),
        endDate = startDate.plusDays(10),
        subscriptionType = "TELE",
        reference = "REF",
        createdBy = "TEST_USER",
        createdAt = Instant.now(),
        prisonId = "LEI",
        prisonNumber = "AD12345",
        subAccountRef = SubAccountRef.CASH,
      )

      val errorResponse = assertThrows<CustomException> {
        subscriptionService.createSubscription(invalidCreateSubscriptionRequest, idempotency.idempotencyKey)
      }

      assertThat(errorResponse.status).isEqualTo(HttpStatus.BAD_REQUEST)
      assertThat(errorResponse.message).isEqualTo("Subscription end date before start date")
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

  @Nested
  inner class UpdateSubscription {

    val subscriptionId: UUID = UUID.randomUUID()
    val updatedEndDate: LocalDate = LocalDate.now()

    @Test
    fun `should return subscription entity once end date updated successfully`() {
      val updateSubscriptionRequest = UpdateSubscriptionRequest(
        endDate = updatedEndDate,
      )

      val startDate = LocalDate.now()
      val subscriptionEntity = SubscriptionEntity(
        subscriptionId = subscriptionId,
        legacyPaymentProfileId = 1234,
        amount = 50,
        dayOfPayment = DayOfWeek.MONDAY,
        startDate = startDate,
        endDate = null,
        subscriptionType = "",
        reference = "",
        createdBy = "",
        createdAt = Instant.now(),
        prisonId = "LEI",
        prisonNumber = "A1234BC",
        subAccountRef = SubAccountRef.CASH,
      )

      whenever { subscriptionsRepository.getSubscriptionEntityBySubscriptionId(subscriptionId) }.thenReturn(subscriptionEntity)

      whenever { subscriptionsRepository.save(any<SubscriptionEntity>()) }
        .thenAnswer { it.arguments[0] }

      val response = subscriptionService.updateSubscription(subscriptionId, updateSubscriptionRequest)

      assertThat(response.endDate).isEqualTo(updatedEndDate)
      assertThat(response.startDate).isEqualTo(startDate)
    }

    @Test
    fun `should return subscription entity once start date updated successfully`() {
      val updateSubscriptionRequest = UpdateSubscriptionRequest(
        startDate = LocalDate.now(),
      )

      val subscriptionEntity = SubscriptionEntity(
        subscriptionId = subscriptionId,
        legacyPaymentProfileId = 1234,
        amount = 50,
        dayOfPayment = DayOfWeek.MONDAY,
        startDate = LocalDate.now().plusYears(180),
        endDate = null,
        subscriptionType = "",
        reference = "",
        createdBy = "",
        createdAt = Instant.now(),
        prisonId = "LEI",
        prisonNumber = "A1234BC",
        subAccountRef = SubAccountRef.CASH,
      )

      whenever { subscriptionsRepository.getSubscriptionEntityBySubscriptionId(subscriptionId) }.thenReturn(subscriptionEntity)

      whenever { subscriptionsRepository.save(any<SubscriptionEntity>()) }
        .thenAnswer { it.arguments[0] }

      val response = subscriptionService.updateSubscription(subscriptionId, updateSubscriptionRequest)

      assertThat(response.startDate).isEqualTo(updateSubscriptionRequest.startDate)
      assertThat(response.endDate).isNull()
    }

    @Test
    fun `should return subscription entity once start date and end date updated successfully`() {
      val updateSubscriptionRequest = UpdateSubscriptionRequest(
        startDate = LocalDate.now(),
        endDate = LocalDate.now().plusDays(10),
      )

      val subscriptionEntity = SubscriptionEntity(
        subscriptionId = subscriptionId,
        legacyPaymentProfileId = 1234,
        amount = 50,
        dayOfPayment = DayOfWeek.MONDAY,
        startDate = LocalDate.now().plusYears(180),
        endDate = null,
        subscriptionType = "",
        reference = "",
        createdBy = "",
        createdAt = Instant.now(),
        prisonId = "LEI",
        prisonNumber = "A1234BC",
        subAccountRef = SubAccountRef.CASH,
      )

      whenever { subscriptionsRepository.getSubscriptionEntityBySubscriptionId(subscriptionId) }.thenReturn(subscriptionEntity)

      whenever { subscriptionsRepository.save(any<SubscriptionEntity>()) }
        .thenAnswer { it.arguments[0] }

      val response = subscriptionService.updateSubscription(subscriptionId, updateSubscriptionRequest)

      assertThat(response.startDate).isEqualTo(updateSubscriptionRequest.startDate)
      assertThat(response.endDate).isEqualTo(updateSubscriptionRequest.endDate)
    }

    @Test
    fun `should throw exception if subscription does not exist`() {
      val updateSubscriptionRequest = UpdateSubscriptionRequest(
        endDate = updatedEndDate,
      )

      whenever { subscriptionsRepository.getSubscriptionEntityBySubscriptionId(subscriptionId) }
        .thenReturn(null)

      val errorResponse = assertThrows<CustomException> {
        subscriptionService.updateSubscription(subscriptionId, updateSubscriptionRequest)
      }

      assertThat(errorResponse.status).isEqualTo(HttpStatus.NOT_FOUND)
      assertThat(errorResponse.message).isEqualTo("Could not find subscription")
    }

    @Test
    fun `should throw exception if request is trying to set an end date before the start date`() {
      val startDate = LocalDate.now()
      val endDateInThePast = LocalDate.now().minusDays(7)
      val subscriptionEntity = SubscriptionEntity(
        subscriptionId = subscriptionId,
        legacyPaymentProfileId = 1234,
        amount = 50,
        dayOfPayment = DayOfWeek.MONDAY,
        startDate = startDate,
        endDate = null,
        subscriptionType = "",
        reference = "",
        createdBy = "",
        createdAt = Instant.now(),
        prisonId = "LEI",
        prisonNumber = "A1234BC",
        subAccountRef = SubAccountRef.CASH,
      )

      whenever { subscriptionsRepository.getSubscriptionEntityBySubscriptionId(subscriptionId) }.thenReturn(subscriptionEntity)

      val updateSubscriptionRequest = UpdateSubscriptionRequest(
        endDate = endDateInThePast,
      )

      val errorResponse = assertThrows<CustomException> {
        subscriptionService.updateSubscription(subscriptionId, updateSubscriptionRequest)
      }

      assertThat(errorResponse.status).isEqualTo(HttpStatus.BAD_REQUEST)
      assertThat(errorResponse.message).isEqualTo("Subscription end date before start date")
    }

    @Test
    fun `should throw exception if request is trying to set an start date after the end date`() {
      val initialStartDate = LocalDate.now()
      val startDateInTheFuture = LocalDate.now().plusDays(10)
      val endDate = LocalDate.now().plusDays(1)
      val subscriptionEntity = SubscriptionEntity(
        subscriptionId = subscriptionId,
        legacyPaymentProfileId = 1234,
        amount = 50,
        dayOfPayment = DayOfWeek.MONDAY,
        startDate = initialStartDate,
        endDate = endDate,
        subscriptionType = "",
        reference = "",
        createdBy = "",
        createdAt = Instant.now(),
        prisonId = "LEI",
        prisonNumber = "A1234BC",
        subAccountRef = SubAccountRef.CASH,
      )

      whenever { subscriptionsRepository.getSubscriptionEntityBySubscriptionId(subscriptionId) }.thenReturn(subscriptionEntity)

      val updateSubscriptionRequest = UpdateSubscriptionRequest(
        startDate = startDateInTheFuture,
      )

      val errorResponse = assertThrows<CustomException> {
        subscriptionService.updateSubscription(subscriptionId, updateSubscriptionRequest)
      }

      assertThat(errorResponse.status).isEqualTo(HttpStatus.BAD_REQUEST)
      assertThat(errorResponse.message).isEqualTo("Subscription end date before start date")
    }

    @Test
    fun `should throw exception if trying to update the start date on already active subscription`() {
      val initialStartDate = LocalDate.now().minusDays(1)
      val endDate = LocalDate.now().plusWeeks(1)
      val subscriptionEntity = SubscriptionEntity(
        subscriptionId = subscriptionId,
        legacyPaymentProfileId = 1234,
        amount = 50,
        dayOfPayment = DayOfWeek.MONDAY,
        startDate = initialStartDate,
        endDate = endDate,
        subscriptionType = "",
        reference = "",
        createdBy = "",
        createdAt = Instant.now(),
        prisonId = "LEI",
        prisonNumber = "A1234BC",
        subAccountRef = SubAccountRef.CASH,
      )

      whenever { subscriptionsRepository.getSubscriptionEntityBySubscriptionId(subscriptionId) }.thenReturn(subscriptionEntity)

      val updateSubscriptionRequest = UpdateSubscriptionRequest(
        startDate = initialStartDate.plusDays(1),
      )

      val errorResponse = assertThrows<CustomException> {
        subscriptionService.updateSubscription(subscriptionId, updateSubscriptionRequest)
      }

      assertThat(errorResponse.status).isEqualTo(HttpStatus.BAD_REQUEST)
      assertThat(errorResponse.message).isEqualTo("Subscription has already started - unable to update")
    }
  }

  @Nested
  inner class GetSubscriptions {

    @Test
    fun `should call for repository for active subscriptions with a pageable object`() {
      val prisonNumber = "A1234BC"

      val pageCaptor = argumentCaptor<Pageable>()

      val subscriptionEntity = SubscriptionEntity(
        subscriptionId = UUID.randomUUID(),
        legacyPaymentProfileId = 12345,
        amount = 10,
        dayOfPayment = DayOfWeek.MONDAY,
        startDate = LocalDate.now(),
        endDate = null,
        subscriptionType = "",
        reference = "",
        createdBy = "",
        createdAt = Instant.now(),
        prisonId = "LEI",
        prisonNumber = prisonNumber,
        subAccountRef = SubAccountRef.CASH,
      )

      whenever(
        subscriptionsRepository.getActiveSubscriptionsForPrisonNumber(
          prisonNumber = eq(prisonNumber),
          pageable = pageCaptor.capture(),
        ),
      ).thenReturn(
        PageImpl(listOf(subscriptionEntity)),
      )

      val subscriptionResponse = subscriptionService.getSubscriptions(prisonNumber, status = SubscriptionsStatus.ACTIVE)

      assertThat(subscriptionResponse.content).hasSize(1)
      assertThat(subscriptionResponse.content[0]).isEqualTo(SubscriptionResponse.fromEntity(subscriptionEntity))

      val capturedPage = pageCaptor.firstValue

      assertThat(capturedPage.pageNumber).isEqualTo(0)
      assertThat(capturedPage.pageSize).isEqualTo(25)
    }

    @Test
    fun `should call for repository for all subscriptions with a pageable object`() {
      val prisonNumber = "A1234BC"

      val pageCaptor = argumentCaptor<Pageable>()

      val subscriptionEntity = SubscriptionEntity(
        subscriptionId = UUID.randomUUID(),
        legacyPaymentProfileId = 12345,
        amount = 10,
        dayOfPayment = DayOfWeek.MONDAY,
        startDate = LocalDate.now(),
        endDate = null,
        subscriptionType = "",
        reference = "",
        createdBy = "",
        createdAt = Instant.now(),
        prisonId = "LEI",
        prisonNumber = prisonNumber,
        subAccountRef = SubAccountRef.CASH,
      )

      whenever(
        subscriptionsRepository.getSubscriptionEntitiesByPrisonNumber(
          prisonNumber = eq(prisonNumber),
          pageable = pageCaptor.capture(),
        ),
      ).thenReturn(
        PageImpl(listOf(subscriptionEntity)),
      )

      val subscriptionResponse = subscriptionService.getSubscriptions(prisonNumber, status = null)

      assertThat(subscriptionResponse.content).hasSize(1)
      assertThat(subscriptionResponse.content[0]).isEqualTo(SubscriptionResponse.fromEntity(subscriptionEntity))

      val capturedPage = pageCaptor.firstValue

      assertThat(capturedPage.pageNumber).isEqualTo(0)
      assertThat(capturedPage.pageSize).isEqualTo(25)
    }

    @Test
    fun `should call for repository for inactive subscriptions with a pageable object`() {
      val prisonNumber = "A1234BC"

      val pageCaptor = argumentCaptor<Pageable>()

      val subscriptionEntity = SubscriptionEntity(
        subscriptionId = UUID.randomUUID(),
        legacyPaymentProfileId = 12345,
        amount = 10,
        dayOfPayment = DayOfWeek.MONDAY,
        startDate = LocalDate.now(),
        endDate = null,
        subscriptionType = "",
        reference = "",
        createdBy = "",
        createdAt = Instant.now(),
        prisonId = "LEI",
        prisonNumber = prisonNumber,
        subAccountRef = SubAccountRef.CASH,
      )

      whenever(
        subscriptionsRepository.getSubscriptionEntitiesByPrisonNumberAndEndDateIsBefore(
          prisonNumber = eq(prisonNumber),
          endDate = eq(LocalDate.now()),
          pageable = pageCaptor.capture(),
        ),
      ).thenReturn(
        PageImpl(listOf(subscriptionEntity)),
      )

      val subscriptionResponse = subscriptionService.getSubscriptions(prisonNumber, status = SubscriptionsStatus.INACTIVE)

      assertThat(subscriptionResponse.content).hasSize(1)
      assertThat(subscriptionResponse.content[0]).isEqualTo(SubscriptionResponse.fromEntity(subscriptionEntity))

      val capturedPage = pageCaptor.firstValue

      assertThat(capturedPage.pageNumber).isEqualTo(0)
      assertThat(capturedPage.pageSize).isEqualTo(25)
    }
  }
}
