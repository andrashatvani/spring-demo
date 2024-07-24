package com.example.dummy.domain.field

import java.time.LocalDate

data class LocalDateRange(val from: LocalDate?, val to: LocalDate?) {

    companion object {
        private fun toLocalDate(value: String?) =
            if (value?.isNotEmpty() == true)
                LocalDate.parse(value)
            else
                null

        fun parse(from: String?, to: String?) = LocalDateRange(toLocalDate(from), toLocalDate(to))
    }
}
