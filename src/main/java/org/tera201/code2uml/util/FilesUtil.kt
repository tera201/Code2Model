package org.tera201.code2uml.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Paths

object FilesUtil {

    private val log: Logger = LoggerFactory.getLogger(FilesUtil::class.java)
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
