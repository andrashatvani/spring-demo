package com.example.dummy.domain.field

import com.example.dummy.domain.field.Field.Type
import com.example.dummy.domain.field.Field.Type.*
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeParseException

class FieldDeserializer : StdDeserializer<Field>(Field::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Field {
        val node: JsonNode = p.codec.readTree(p)
        val type = Type.valueOf(node.get("type").textValue())
        val kennung = node.get("name").textValue()
        val value = node.get("value")?.let {
            if (it is NullNode) {
                null
            } else {
                when (type) {
                    DATE -> LocalDate.parse(it.textValue())
                    DATE_RANGE -> if (it is ObjectNode) {
                        LocalDateRange.parse(it.get("from")?.textValue(), it.get("to")?.textValue())
                    } else {
                        throw DateTimeParseException("expected json object with optional 'from'/'to' properties", it.textValue(), 0)
                    }

                    STRING -> it.textValue()
                    BOOLEAN -> {
                        if (it.asText() != "true" && it.asText() != "false") throw IllegalArgumentException("Not a boolean value ${it.textValue()}")
                        else it.booleanValue()
                    }

                    INTEGER -> it.asText().toLong()
                    NUMSTRING -> it.asText().toNumString()
                    GEPART -> it.asText().toInt()
                    CURRENCY -> it.asText().toBigDecimal().setScale(2, RoundingMode.HALF_UP)
                }
            }
        }
        return Field(kennung, value, type)
    }
}

val NUMSTRING_REGEX = "\\d+".toRegex()
fun String.toNumString(): String {
    if (this.matches(NUMSTRING_REGEX)) {
        return this
    }
    throw RuntimeException("only numbers expected, but was '$this'")
}