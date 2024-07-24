package com.example.event

import com.example.BaseApplication
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY
import java.time.LocalDateTime
import java.util.*

abstract class BaseEvent<K>(
    open val action: Enum<*>?,
    @JsonProperty(access = READ_ONLY)
    val eventType: String,
    @JsonIgnore
    val messageKey: K,
    val metaInfo: MetaInfo = MetaInfo(),
) {
    companion object {
        const val EVENT_TYPE_HEADER = "event_type"
    }

    @JsonIgnore
    val hasBeenDeleted: Boolean? = false

    val updateEventType
        @JsonIgnore
        get() = eventType.substringBefore("-").uppercase()
}

data class MetaInfo(
    val eventId: UUID = UUID.randomUUID(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val correlationId: UUID = UUID.randomUUID(),
    val applicationId: String = BaseApplication.ID,
    val triggerEventId: UUID? = null,
    var userId: String? = null,
) {
    companion object {
        fun fromTriggerEvent(triggerEvent: BaseEvent<out Any>) =
            with(triggerEvent.metaInfo) {
                MetaInfo(
                    correlationId = correlationId,
                    triggerEventId = eventId,
                    userId = userId,
                )
            }
    }
}
