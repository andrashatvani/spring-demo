package com.example.dummy.domain

import com.example.dummy.domain.DummyAggregat.Action.AGGREGATE
import com.example.dummy.domain.DummyEvent.Action.CREATE
import com.example.dummy.domain.field.Field
import com.example.event.BaseEvent

data class DummyAggregat(
    val dummyEvent: DummyEvent,
    override val action: Action = AGGREGATE,
): BaseEvent<String>(
    action = action,
    eventType = "dummy-aggregat-1",
    messageKey = dummyEvent.messageKey
) {
    enum class Action {
        AGGREGATE
    }
}

data class DummyEvent(
    val dummy: Dummy,
    override val action: Action = CREATE
): BaseEvent<String>(
    action = action,
    eventType = "dummy-event-1",
    messageKey = dummy.id
) {
    enum class Action {
        CREATE
    }
}

data class Dummy(
    val id: String,
    val field1: String,
    val field2: Int,
    val fields: List<Field> = emptyList()
)
