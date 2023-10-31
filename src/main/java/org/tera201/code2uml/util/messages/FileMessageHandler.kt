package org.tera201.code2uml.util.messages

import java.io.FileNotFoundException
import java.io.PrintStream

/**
 * Вывод сообщений об ошибках в файл.
 */
class FileMessageHandler(fileName: String) : BaseMessageHandler() {
    protected fun finalize() {
        out.close()
    }

    init {
        try {
            out = PrintStream(fileName)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
    }
}