package com.example.dummy.domain

import com.example.BaseApplication
import com.example.event.EventTypeMapping
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext

@SpringBootApplication(scanBasePackages = ["com.example"])
class DummyApplication(
    context: ApplicationContext,
    eventTypeMapping: EventTypeMapping,
) : BaseApplication(
    context = context,
    eventTypeMapping = eventTypeMapping,
) {
    @PostConstruct
    fun postConstruct() {
        super.initialize()
    }
}

fun main(args: Array<String>) {
    runApplication<DummyApplication>(*args)
}
