package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import java.time.LocalDate

class MergeIntegrationTests : SqsIntegrationTestBase() {
  val mergedPrisonNumber = "A1231XX"
  val survivorPrisonNumber = "A1231YY"

  @BeforeEach
  fun setup() {
    hmppsAuth.stubGrantToken()
  }

  @Test
  fun `should merge subscriptions`() {
    val createdSubscription = integrationTestHelpers.createSubscription(
      prisonNumber = mergedPrisonNumber,
      paymentProfileId = 12345,
      startDate = LocalDate.now(),
      endDate = null,
    )

    val prisonerSubscriptionsToBeMerged = integrationTestHelpers.getSubscriptions(mergedPrisonNumber)
    assertThat(prisonerSubscriptionsToBeMerged.content).hasSize(1)
    assertThat(prisonerSubscriptionsToBeMerged.content[0].prisonNumber).isEqualTo(createdSubscription.prisonNumber)
    assertThat(prisonerSubscriptionsToBeMerged.content[0].id).isEqualTo(createdSubscription.id)

    val survivingPrisonerSubscriptionsPreMerge = integrationTestHelpers.getSubscriptions(survivorPrisonNumber)
    assertThat(survivingPrisonerSubscriptionsPreMerge.content).hasSize(0)

    publishMergeEvent(mergedPrisonNumber, survivorPrisonNumber)

    waitUntilEmpty(hmppsQueueService = hmppsQueueService)

    val mergedPrisonerSubscriptions = integrationTestHelpers.getSubscriptions(mergedPrisonNumber)
    assertThat(mergedPrisonerSubscriptions.content).hasSize(0)

    val survivingPrisonerSubscriptions = integrationTestHelpers.getSubscriptions(survivorPrisonNumber)
    assertThat(survivingPrisonerSubscriptions.content).hasSize(1)
    assertThat(survivingPrisonerSubscriptions.content[0].prisonNumber).isEqualTo(survivorPrisonNumber)
    assertThat(survivingPrisonerSubscriptions.content[0].id).isEqualTo(createdSubscription.id)
  }
}
