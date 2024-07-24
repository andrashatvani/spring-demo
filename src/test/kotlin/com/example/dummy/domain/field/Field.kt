package com.example.dummy.domain.field

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(using = FieldDeserializer::class)
data class Field(
    val name: String,
    val value: Any?,
    val type: Type,
) {
    enum class Type {
        STRING,
        INTEGER,
        NUMSTRING,
        DATE,
        DATE_RANGE,
        BOOLEAN,
        GEPART,
        CURRENCY
    }
}
