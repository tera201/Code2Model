package util.messages

/**
 * Вывод сообщений об ошибках на консоль.
 */
class ConsoleMessageHandler : BaseMessageHandler() {
    init {
        out = System.out
    }
}