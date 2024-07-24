package com.example.dummy.domain

import com.example.dummy.domain.field.Field
import com.example.dummy.domain.field.Field.Type.CURRENCY
import com.example.dummy.domain.field.Field.Type.DATE
import java.time.LocalDate

object DummyFixtures {
    val DUMMY_1 = Dummy(
        id = "1",
        field1 = "dummy1",
        field2 = 1,
        fields = listOf(
            Field("date", LocalDate.now(), DATE),
            Field("number", "10.23".toBigDecimal().setScale(2), CURRENCY)
        ),
    )
    val DUMMY_EVENT_1 = DummyEvent(
        dummy = DUMMY_1
    )

    val DUMMY_AGGREGAT_1 = DummyAggregat(
        dummyEvent = DUMMY_EVENT_1
    )

    val DUMMY_2 = Dummy(
        id = "2",
        field1 = "dummy2",
        field2 = 2,
    )
    val DUMMY_EVENT_2 = DummyEvent(
        dummy = DUMMY_2
    )
    val DUMMY_AGGREGAT_2 = DummyAggregat(
        dummyEvent = DUMMY_EVENT_2
    )

    val DUMMY_AGGREGATE = listOf(DUMMY_AGGREGAT_1, DUMMY_AGGREGAT_2)
}