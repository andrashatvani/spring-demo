package com.example

import com.example.event.EventTypeMapping
import com.example.event.configuredObjectMapper
import com.fasterxml.jackson.databind.JavaType
import jakarta.annotation.PostConstruct
import org.rocksdb.RocksDB
import org.springframework.context.ApplicationContext

abstract class BaseApplication(
    private val context: ApplicationContext,
    private val eventTypeMapping: EventTypeMapping,
) {
    companion object {
        var ID: String = "n/a" // will be overwritten
        var EVENT_TYPE_MAPPING: Map<String, JavaType> = mapOf()
    }

    init {
        RocksDB.loadLibrary()
    }

    @PostConstruct
    fun initialize() {
        ID = context.id!!
        EVENT_TYPE_MAPPING = eventTypeMapping.supportedEventTypeMap.mapValues { configuredObjectMapper.constructType(it.value) }
    }
}

