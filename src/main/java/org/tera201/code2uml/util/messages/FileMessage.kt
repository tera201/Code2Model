package org.tera201.code2uml.util.messages

/**
 * Выдача диагностики с указанием файла содержащего ошибку.
 */
open class FileMessage(message: String, protected var fileName: String) : Message(message) {
    override fun toString() = "$kind: $fileName: $message"
}