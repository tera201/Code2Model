package org.tera201.code2uml.util.messages

/**
 * Вывод сообщений об ошибках на консоль.
 */
class ConsoleMessageHandler : BaseMessageHandler() {
    init {
        out = System.out
    }
}