package com.example.dummy.domain

import com.example.dummy.DummyAggregatType
import com.example.dummy.DummyEventType
import com.example.dummy.domain.DummyFixtures.DUMMY_EVENT_1
import com.example.event.AbstractStreamIntegrationTest
import com.example.event.BaseEvent.Companion.EVENT_TYPE_HEADER
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.test.TestRecord
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.function.Function

@DummyIntegrationTest
class DummyAggregatStreamConfigurationIntegrationTest(
    private val dummyAggregate: Function<KStream<String, DummyEvent>, KStream<String, DummyAggregat>>,
) : AbstractStreamIntegrationTest() {
    private val dummyEventInputTopic by lazy {
        testDriver.createInputTopic<String, DummyEvent>("dummyAggregate-in-0")
    }
    private val dummyAggregatOutputTopic by lazy {
        testDriver.createOutputTopic<String, DummyAggregat>("dummyAggregate-out-0")
    }

    @BeforeEach
    fun beforeEach() {
        setupTestDriver {
            dummyAggregate
                .apply(it.stream(DummyEventType.INPUT))
                .to(DummyAggregatType.OUTPUT)
        }
    }

    @Test
    fun `event will be aggregated`() {
        send(DUMMY_EVENT_1)
        read().assert(DUMMY_EVENT_1)
    }

    private fun send(event: DummyEvent) = dummyEventInputTopic.send(event)
    private fun read() = dummyAggregatOutputTopic.readRecord()
    fun TestRecord<String, DummyAggregat>.assert(event: DummyEvent) {
        assertThat(String(headers.lastHeader(EVENT_TYPE_HEADER).value())).isEqualTo(DummyAggregatType.NAME)
        assertThat(value.dummyEvent).isEqualTo(event)
    }
}