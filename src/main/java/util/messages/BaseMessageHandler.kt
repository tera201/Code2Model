package util.messages

/**
 * Базовый класс для обработчиков сообщений выводящих текст
 * в форматированный поток вывода.
 */
open class BaseMessageHandler : IMessageHandler {
    protected var out = System.out

    override fun startGroup(e: Message) {
        out.println(e)
    }

    override fun info(e: Message) {
        e.level = Message.info
        out.println(e)
    }

    override fun warning(e: Message) {
        e.level = Message.warning
        out.println(e)
    }

    override fun error(e: Message) {
        e.level = Message.error
        out.println(e)
    }

    override fun fatalError(e: Message) {
        e.level = Message.fatalError
        out.println(e)
    }

    override fun endGroup(e: Message) {
        out.println(e)
    }

    override fun clear() {}
}