package uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.services

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.digital.hmpps.prisonerfinancesubscriptionsapi.repositories.SubscriptionsRepository

@ExtendWith(MockitoExtension::class)
class RecordServiceTest {

  @Mock
  lateinit var subscriptionsRepository: SubscriptionsRepository

  @InjectMocks
  private lateinit var recordService: RecordService


  @Nested
  inner class CreateSubscription {

    @Test
    fun `should save the subscription record`() {
      // TO-DO: Mock the save and verify
    }
  }
}
