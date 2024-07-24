package com.example.dummy.domain

import com.example.event.EventTypeHeaderProcessor
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.processor.api.ProcessorSupplier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.function.Function

@Configuration
class DummyAggregatStreamConfiguration {
    @Bean
    fun dummyAggregate() = Function { dummyEvents: KStream<String, DummyEvent> ->
        dummyEvents
            .mapValues { _, dummyEvent -> DummyAggregat(dummyEvent) }
            .process(ProcessorSupplier { EventTypeHeaderProcessor() })
    }
}