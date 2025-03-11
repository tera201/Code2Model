package org.tera201.code2uml

import org.tera201.code2uml.java20.console.JavaParserRunner
import org.tera201.code2uml.kotlin.console.KotlinParserRunner
import org.tera201.code2uml.util.messages.DataBaseUtil
import java.io.File
import javax.swing.JProgressBar
import javax.swing.JTextArea

class AnalyzerBuilder(
    private val projectName: String,
    private val modelName: String,
) {
    private var language:Language = Language.JAVA
    private var dataBaseUtil: DataBaseUtil? = null
    private var path: String = File(".").canonicalFile.toString()
    private var pathToDB: String = "$path/model.db"
    private var numThread: Int = 0
    private var textArea: JTextArea? = null
    private var progressBar: JProgressBar? = null

    fun threads(numThread: Int?) = apply { this.numThread = numThread ?: 0 }

    fun textArea(textArea: JTextArea?) = apply { this.textArea = textArea }

    fun progressBar(progressBar: JProgressBar) = apply { this.progressBar = progressBar }

    fun setLanguage(language: Language) = apply { this.language = language }

    fun setPath(path: String) = apply { this.path = path }

    fun setPathToDB(pathToDB: String) = apply { this.pathToDB = pathToDB }

    fun setDataBaseUtil(dataBaseUtil: DataBaseUtil) = apply { this.dataBaseUtil = dataBaseUtil }

    fun buildDB(): Int = when (language) {
        Language.JAVA -> {
            dataBaseUtil = if (dataBaseUtil == null) DataBaseUtil(pathToDB) else dataBaseUtil
            val javaParserRunner = JavaParserRunner()
            val javaFiles = javaParserRunner.collectFiles(path)
            javaParserRunner.buildModel(dataBaseUtil!!, projectName, modelName, javaFiles, textArea, progressBar, numThread)
        }
        Language.KOTLIN -> {
            dataBaseUtil = if (dataBaseUtil == null) DataBaseUtil(pathToDB) else dataBaseUtil
            val kotlinParserRunner = KotlinParserRunner()
            val kotlinFiles = kotlinParserRunner.collectFiles(path)
            kotlinParserRunner.buildModel(dataBaseUtil!!, projectName, modelName, kotlinFiles, textArea, progressBar, numThread)
        }
        Language.JAVA_KOTLIN -> {
            dataBaseUtil = if (dataBaseUtil == null) DataBaseUtil(pathToDB) else dataBaseUtil
            val javaParserRunner = JavaParserRunner()
            val kotlinParserRunner = KotlinParserRunner()
            val javaFiles = javaParserRunner.collectFiles(path)
            val kotlinFiles = kotlinParserRunner.collectFiles(path)
            kotlinParserRunner.buildModel(dataBaseUtil!!, projectName, modelName, kotlinFiles, textArea, progressBar, numThread)
            javaParserRunner.buildModel(dataBaseUtil!!, projectName, modelName, javaFiles, textArea, progressBar, numThread)
        }
    }
}
