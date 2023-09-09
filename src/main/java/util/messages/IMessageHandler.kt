package util.messages

/**
 * The class `MessageHandler` is used for output the messages:
 *
 * + information
 * + warning
 * + errors
 * + fatal errors
 */
interface IMessageHandler {
    fun clear()
    fun startGroup(e: Message)
    fun info(e: Message)
    fun warning(e: Message)
    fun error(e: Message)
    fun fatalError(e: Message)
    fun endGroup(e: Message)
}