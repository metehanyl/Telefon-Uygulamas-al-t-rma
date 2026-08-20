package com.metehanyil.sifrekasasi.util

/**
 * Minimal RFC4180-style CSV parser: handles quoted fields, commas and
 * newlines inside quotes, and doubled quotes ("") as an escaped quote.
 * Good enough for Chrome's password export file.
 */
object CsvParser {

    fun parse(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0
        val n = text.length

        fun endField() {
            row.add(field.toString())
            field.clear()
        }

        fun endRow() {
            endField()
            rows.add(row)
            row = mutableListOf()
        }

        while (i < n) {
            val ch = text[i]
            if (inQuotes) {
                when {
                    ch == '"' && i + 1 < n && text[i + 1] == '"' -> {
                        field.append('"')
                        i += 2
                    }
                    ch == '"' -> {
                        inQuotes = false
                        i++
                    }
                    else -> {
                        field.append(ch)
                        i++
                    }
                }
            } else {
                when (ch) {
                    '"' -> {
                        inQuotes = true
                        i++
                    }
                    ',' -> {
                        endField()
                        i++
                    }
                    '\r' -> i++
                    '\n' -> {
                        endRow()
                        i++
                    }
                    else -> {
                        field.append(ch)
                        i++
                    }
                }
            }
        }
        if (field.isNotEmpty() || row.isNotEmpty()) {
            endRow()
        }

        return rows.filterNot { it.isEmpty() || (it.size == 1 && it[0].isBlank()) }
    }
}
