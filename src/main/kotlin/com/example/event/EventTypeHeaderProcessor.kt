package com.example.event

import com.example.event.BaseEvent.Companion.EVENT_TYPE_HEADER
import org.apache.kafka.streams.processor.api.Processor
import org.apache.kafka.streams.processor.api.ProcessorContext
import org.apache.kafka.streams.processor.api.Record

class EventTypeHeaderProcessor<K, X, V : BaseEvent<in X>> : Processor<K, V, K, V> {
    private lateinit var context: ProcessorContext<K, V>

    override fun init(context: ProcessorContext<K, V>) {
        this.context = context
    }

    override fun process(record: Record<K, V>) {
        val forwardedRecord = record.withHeaders(record.headers())
        forwardedRecord.headers()
            .remove(EVENT_TYPE_HEADER)
            .add(EVENT_TYPE_HEADER, (record.value().eventType).toByteArray())
        context.forward(forwardedRecord)
    }
}

