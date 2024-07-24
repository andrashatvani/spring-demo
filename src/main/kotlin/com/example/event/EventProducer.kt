package com.example.event

import com.example.event.BaseEvent.Companion.EVENT_TYPE_HEADER
import com.example.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.kafka.support.KafkaHeaders.KEY
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import reactor.core.publisher.Sinks
import reactor.core.publisher.Sinks.EmitResult.FAIL_NON_SERIALIZED
import reactor.core.publisher.Sinks.EmitResult.FAIL_TERMINATED

interface EventProducer<T : BaseEvent<*>> {
    val sink: Sinks.Many<Message<T>>

    suspend fun produce(event: T) = withContext(Dispatchers.IO) {
        val messageKey =
            if (event.messageKey is String)
                event.messageKey
            else
                configuredObjectMapper.writeValueAsString(event.messageKey)
        val message = MessageBuilder
            .withPayload(event)
            .setHeader(KEY, messageKey)
            .setHeader(EVENT_TYPE_HEADER, event.eventType)
            .build()
        var result: Sinks.EmitResult
        do {
            result = try {
                sink.tryEmitNext(message)
            } catch (e: Exception) {
                logger().error("Exception while tryEmitNext()", e)
                FAIL_TERMINATED
            }
        } while (result == FAIL_NON_SERIALIZED)
        if (result.isFailure) {
            logger().error("Event producing failed with result $result. Event: $message")
        }
        result
    }
}
