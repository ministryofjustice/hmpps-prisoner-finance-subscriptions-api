package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(
  name = "subscriptions",
  uniqueConstraints = [
    UniqueConstraint(
      name = "uc_legacy_payment_profile_id",
      columnNames = ["legacy_payment_profile_id"],
    ),
  ],
)
class SubscriptionEntity(

  @Id
  val id: UUID = UUID.randomUUID(),

  @Column(name = "legacy_payment_profile_id", nullable = false, unique = true)
  val legacyPaymentProfileId: Long,

  @Column(name = "amount", nullable = false, unique = false)
  val amount: Long,

  @Enumerated(EnumType.STRING)
  @Column(name = "day_of_payment", nullable = false, unique = false)
  val dayOfPayment: DayOfWeek,

  @Column(name = "start_date", nullable = false, unique = false)
  val startDate: LocalDate,

  @Column(name = "end_date", nullable = true, unique = false)
  val endDate: LocalDate? = null,

  @Column(name = "subscription_type", nullable = false, unique = false)
  val subscriptionType: String,

  @Column(name = "reference", nullable = true, unique = false)
  val reference: String? = null,

  @Column(name = "created_by", nullable = false, unique = false)
  val createdBy: String,

  @Column(name = "created_at", nullable = false, unique = false)
  val createdAt: Instant,

  @Column(name = "prison_id", nullable = false, unique = false)
  val prisonId: String,

  @Column(name = "prison_number", nullable = false, unique = false)
  val prisonNumber: String,

  @Column(name = "sub_account_ref", nullable = false, unique = false)
  val subAccountRef: String,

)
