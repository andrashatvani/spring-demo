package com.example.dummy.event

import com.example.dummy.domain.DummyEvent
import com.example.event.BaseEvent
import com.example.event.EventProducer
import org.springframework.context.annotation.Bean
import org.springframework.messaging.Message
import org.springframework.stereotype.Component
import reactor.core.publisher.Sinks
import java.util.function.Supplier

@Component
class DummyGenericEventProducer : EventProducer<BaseEvent<*>> {
    override val sink: Sinks.Many<Message<BaseEvent<*>>> = Sinks.many().replay().latest()

    @Bean
    fun dummyEventPublisher() = Supplier { sink.asFlux().filter { it.payload is DummyEvent } }
}