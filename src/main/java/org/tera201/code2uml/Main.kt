package org.tera201.code2uml

import org.tera201.code2uml.util.messages.DataBaseUtil
import java.io.File

fun main() {
    val projectPath = "."
    val projectDir = File(projectPath).canonicalFile.toString()
    val dbUrl = "$projectDir/Samples/db/model.db"
    val dataBaseUtil = DataBaseUtil(dbUrl)
    val analyzer = AnalyzerBuilder("Main", "v1")
        .setLanguage(Language.JAVA_KOTLIN)
        .setPath("$projectDir")
        .setDataBaseUtil(dataBaseUtil)
        .threads(4)
    analyzer.buildDB()
}