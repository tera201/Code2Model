package org.tera201.code2uml.kotlin.console

import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.tree.ParseTree
import org.tera201.code2uml.ParserRunner
import org.tera201.code2uml.kotlin.parser.KotlinTreeListener
import org.tera201.code2uml.kotlin.parser.generated.KotlinLexer
import org.tera201.code2uml.kotlin.parser.generated.KotlinParser
import org.tera201.code2uml.util.messages.DataBaseUtil
import java.io.File

class KotlinParserRunner() : ParserRunner(::KotlinLexer, ::KotlinParser, ::KotlinTreeListener) {

    /**
     * Test function to determine if a file should be processed (i.e., is a Java file and not in test or jvm directories).
     */
    override fun test(fileName: String): Boolean =
        fileName.endsWith(".kt") && !fileName.contains("/test/") && !fileName.contains("/jvm/")

    override fun getParseTree(parser: Parser): ParseTree = (parser as KotlinParser).kotlinFile()
}

fun main() {
    val startTime = System.currentTimeMillis()
    val projectPath = "."
    val projectDir = File(projectPath).canonicalFile
    val runner = KotlinParserRunner()
    val dbUrl = "$projectDir/JavaToUMLSamples/db/model.db"
    val dataBaseUtil = DataBaseUtil(dbUrl)
    val kotlinFiles = runner.collectFiles(projectDir.toString())
    println(kotlinFiles)
    runner.buildModel(dataBaseUtil, "JavaSampleModel", "master", kotlinFiles, null, null,4)
}