package com.example.annotation

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode.ALL
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.lang.annotation.Inherited
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CLASS

@Retention(RUNTIME)
@Target(CLASS)
@Inherited
@ActiveProfiles("it")
@Tag("integration")
@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    properties = ["spring.main.allow-bean-definition-overriding=true"]
)
@Import(TestChannelBinderConfiguration::class)
@TestPropertySource(properties = ["spring.cloud.function.definition=none"])
@TestConstructor(autowireMode = ALL)
annotation class IntegrationTest
