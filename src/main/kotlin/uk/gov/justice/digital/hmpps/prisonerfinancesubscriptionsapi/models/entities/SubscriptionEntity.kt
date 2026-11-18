package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.Immutable
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.models.enums.SubscriptionPaymentSchedule
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
data class SubscriptionEntity(

  @Id
  @Immutable
  @Column(name = "subscription_id", nullable = false, unique = true)
  val subscriptionId: UUID = UUID.randomUUID(),

  @Immutable
  @Column(name = "legacy_payment_profile_id", nullable = true, unique = true)
  val legacyPaymentProfileId: Long?,

  @Immutable
  @Column(name = "amount", nullable = false, unique = false)
  val amount: Long,

  @Immutable
  @Enumerated(EnumType.STRING)
  @Column(name = "payment_schedule", nullable = false, unique = false)
  val paymentSchedule: SubscriptionPaymentSchedule,

  @Column(name = "start_date", nullable = false, unique = false)
  var startDate: LocalDate,

  @Column(name = "end_date", nullable = true, unique = false)
  var endDate: LocalDate? = null,

  @Immutable
  @Column(name = "subscription_type", nullable = false, unique = false)
  val subscriptionType: String,

  @Immutable
  @Column(name = "reference", nullable = true, unique = false)
  val reference: String? = null,

  @Immutable
  @Column(name = "created_by", nullable = false, unique = false)
  val createdBy: String,

  @Immutable
  @Column(name = "created_at", nullable = false, unique = false)
  val createdAt: Instant,

  @Immutable
  @Column(name = "prison_id", nullable = false, unique = false)
  val prisonId: String,

  @Immutable
  @Column(name = "prison_number", nullable = false, unique = false)
  val prisonNumber: String,

  @Immutable
  @Enumerated(EnumType.STRING)
  @Column(name = "sub_account_ref", nullable = false, unique = false)
  val subAccountRef: SubAccountRef,
) {
  val hasStarted: Boolean
    get() = startDate < LocalDate.now()
}

enum class SubAccountRef {
  CASH,
  SPENDS,
  SAVINGS,
}
