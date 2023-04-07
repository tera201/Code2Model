package util

import java.io.IOException
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Paths

object FilesUtil {
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
            // TODO Auto-generated catch block
            e.printStackTrace()
        }
    }
}
