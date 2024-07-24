package com.example.dummy.domain

import com.example.annotation.IntegrationTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@IntegrationTest
@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    properties = ["spring.main.allow-bean-definition-overriding=true"],
    classes = [DummyApplication::class]
)
annotation class DummyIntegrationTest
