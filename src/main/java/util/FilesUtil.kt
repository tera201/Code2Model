package util

import cpp.console.CppParserRunner
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.IOException
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Paths

object FilesUtil {

    private val log: Logger = LogManager.getLogger(FilesUtil::class.java)
    fun walkRes(root: String, fileFilter: (String) -> Boolean, action: (String) -> Unit) {
        val start = Paths.get(root)
        try {
            val stream = Files.walk(start, Int.MAX_VALUE, FileVisitOption.FOLLOW_LINKS)
            stream
                .map { it.toString() }
                .filter(fileFilter)
                .forEach(action)
            stream.close()
        } catch (e: IOException) {
            log.error("Parsing fail: $e")
        }
    }
}
