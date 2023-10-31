package org.tera201.code2uml.util.messages

/**
 * Выдача диагностики с указанием координат (строки и колонки) в файле.
 */
class ParseMessage(message: String, fileName: String, val line: Int = -1, val column: Int = -1) :
    FileMessage(message, fileName) {
    override fun toString() = "$kind: $fileName($line, $column): $message"
}

