package util.messages

/**
 * Сообщение для выдачи диагностики.
 */
open class Message(var message: String) {
    var level = info

    override fun toString() = "$kind: $message"

    protected val kind: String
        get() {
            when (level) {
                info -> return "info"
                warning -> return "warning"
                error -> return "error"
                fatalError -> return "fatalError"
                else -> ""
            }
            return ""
        }

    companion object {
        const val info = 0
        const val warning = 1
        const val error = 2
        const val fatalError = 3
    }
}
