package com.example.event

import com.example.BaseApplication.Companion.EVENT_TYPE_MAPPING
import com.example.event.BaseEvent.Companion.EVENT_TYPE_HEADER
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonToken.END_OBJECT
import com.fasterxml.jackson.databind.JavaType
import org.apache.kafka.common.header.Headers

@Suppress("unused")
class JsonValueTypeResolver {
    companion object {
        private val factory: JsonFactory = configuredObjectMapper.factory
        private fun readEventType(topic: String, data: ByteArray, headers: Headers?): String {
            headers?.lastHeader(EVENT_TYPE_HEADER)?.value()?.let { return String(it) }

            factory.createParser(data).use { parser ->
                with(parser) {
                    nextToken()
                    while (nextToken() != END_OBJECT) {
                        if (currentName() == "eventType") {
                            nextToken()
                            return valueAsString
                        } else {
                            skipChildren()
                        }
                    }
                }
                throw RuntimeException(String.format("No 'eventType' field in JSON object in topic '%s'", topic))
            }
        }

        @JvmStatic
        fun resolve(topic: String, data: ByteArray, headers: Headers?): JavaType {
            val eventType = readEventType(topic, data, headers)
            return EVENT_TYPE_MAPPING[eventType] ?: run {
                throw RuntimeException(String.format("Unsupported event type '%s' in topic '%s'", eventType, topic))
            }
        }
    }
}
