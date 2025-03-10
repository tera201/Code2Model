package org.tera201.code2uml.java20.console

import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.tree.ParseTree
import org.tera201.code2uml.ParserRunner
import org.tera201.code2uml.java20.parser.Java20TreeListener
import org.tera201.code2uml.java20.parser.generated.Java20Lexer
import org.tera201.code2uml.java20.parser.generated.Java20Parser
import org.tera201.code2uml.uml.util.clearPackageDir
import org.tera201.code2uml.util.messages.*
import java.io.File
import java.io.IOException

/**
 * Class responsible for running the Java parser, collecting files, and building UML models.
 */
class JavaParserRunner : ParserRunner(::Java20Lexer, ::Java20Parser, ::Java20TreeListener) {

    /**
     * Test function to determine if a file should be processed (i.e., is a Java file and not in test or jvm directories).
     */
    override fun test(fileName: String): Boolean =
        fileName.endsWith(".java") && !fileName.contains("/test/") && !fileName.contains("/jvm/")

    override fun getParseTree(parser: Parser): ParseTree = (parser as Java20Parser).compilationUnit()
}

fun main() {
    val startTime = System.currentTimeMillis()
    val projectPath = "."
    val projectDir = File(projectPath).canonicalFile
    val sourcePath = "$projectDir/JavaToUMLSamples/src/a-foundation-master"
    val targetPathForCode = "$projectDir/target/src"
    val targetPathForUMLModels = "$projectDir/target/models"
    val dbUrl = "$projectDir/JavaToUMLSamples/db/model.db"
    val dataBaseUtil = DataBaseUtil(dbUrl)

    try {
        File(targetPathForCode).mkdirs()
        File(targetPathForUMLModels).mkdirs()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    val runner = JavaParserRunner()

    // Collect java files
    val javaFiles = runner.collectFiles(sourcePath)

    // Build UML model for these files
    val model = runner.buildModel(dataBaseUtil, "JavaSampleModel", "master", javaFiles, null, null,4)

    clearPackageDir(targetPathForCode)
    println("Model saved")

    val endTime = System.currentTimeMillis()
    val executionTime = (endTime - startTime) / 1000.0
    println("Execution time: $executionTime seconds")
}