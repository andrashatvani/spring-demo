package com.example.dummy.event

import com.example.dummy.domain.DummyFixtures.DUMMY_EVENT_1
import com.example.dummy.domain.DummyIntegrationTest
import com.example.event.AbstractProducerIntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource

@DummyIntegrationTest
@TestPropertySource(properties = [
    "spring.cloud.function.definition=dummyEventPublisher",
])
class DummyEventProducerIntegrationTest : AbstractProducerIntegrationTest() {
    companion object {
        const val DUMMY_TOPIC = "dummy-topic"
    }

    @Test
    fun `published event without security context will be received`() = runTest {
        publishedEventWillBeReceived(DUMMY_EVENT_1, DUMMY_TOPIC)
    }
}