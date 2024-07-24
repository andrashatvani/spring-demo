package com.example.event

import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.binder.test.OutputDestination

abstract class AbstractProducerIntegrationTest {
    @Autowired
    lateinit var eventProducer: EventProducer<BaseEvent<*>>

    @Autowired
    lateinit var outputDestination: OutputDestination

    protected suspend inline fun <reified E : BaseEvent<*>> publishedEventWillBeReceived(
        event: E,
        destinationTopicName: String,
    ) {
        eventProducer.produce(event)

        val message = outputDestination.receive(5000, destinationTopicName)
        val receivedObject = configuredObjectMapper.readValue<E>(message.payload)

        assertThat(receivedObject).isEqualTo(event)
    }
}