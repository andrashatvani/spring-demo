package com.example.event

interface EventTypeMapping {
    val supportedEventTypeMap: Map<String, Class<out BaseEvent<*>>>
}


