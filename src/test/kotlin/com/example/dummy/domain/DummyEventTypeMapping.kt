package com.example.dummy.domain

import com.example.dummy.DummyAggregatType
import com.example.dummy.DummyEventType
import com.example.event.EventTypeMapping
import org.springframework.stereotype.Component

@Component
class DummyEventTypeMapping : EventTypeMapping {
    override val supportedEventTypeMap = mapOf(
        DummyEventType.NAME to DummyEvent::class.java,
        DummyAggregatType.NAME to DummyAggregat::class.java
    )
}