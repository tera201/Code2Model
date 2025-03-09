package org.tera201.code2uml.cpp.console

import org.tera201.code2uml.cpp.parser.CPP14ErrorListener
import org.tera201.code2uml.cpp.parser.CPP14TreeListener
import org.tera201.code2uml.cpp.parser.generated.CPP14Lexer
import org.tera201.code2uml.cpp.parser.generated.CPP14Parser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.tera201.code2uml.util.FilesUtil
import java.io.File
import java.io.IOException

class CppParserRunner {

    private val log: Logger = LogManager.getLogger(CppParserRunner::class.java)
    private var modelPath: String? = null

    fun collectFiles(vararg paths: String): ArrayList<String> {
        modelPath = paths[0]
        log.info("Collecting files from ${paths.toList()}")
        val cppFiles = ArrayList<String>()

        paths.forEach { FilesUtil.walkRes(it, ::test, cppFiles::add) }

        return cppFiles
    }

    private fun parseFile(fileName: String) {
        log.info("Parsing file: $fileName")
        try {
            // The input file with text in the form of code is read as a stream of characters
            val input = CharStreams.fromFileName(fileName)

            // The CPP14Lexer class allows you to group characters and determine the type of lexemes (identifier, number, string, etc.)
            val lexer = CPP14Lexer(input)

            // The code is then broken down into tokens
            val tokens = CommonTokenStream(lexer)

            // The code is prepared for use in building the parse tree
            val parser = CPP14Parser(tokens)

            // The code is checked for syntax errors
            val errorListener = CPP14ErrorListener()
            parser.addErrorListener(errorListener)

            // Allows you to determine the nesting of the parent-child type (class - class member/method)
            val tree = parser.translationUnit()
            val walker = ParseTreeWalker()
            val listener = CPP14TreeListener(parser, fileName)
            walker.walk(listener, tree)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun main() {
    val projectPath = "."
    val projectDir = File(projectPath).canonicalFile

    val sourcePath = "$projectDir/CppToUMLSamples/src/snark-master"
    val targetPathForCode = "$projectDir/target/generated/org.tera201.code2uml.cpp"
    val targetPathForUMLModels = "$projectDir/target/models"

    try {
        File(targetPathForCode).mkdirs()
        File(targetPathForUMLModels).mkdirs()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    val runner = CppParserRunner()

    println(sourcePath)

    // Collect C++ files.
    val cppFiles = runner.collectFiles(sourcePath)

    // Build UML model for these files.

    // Generate C++ code.
}

private fun test(fileName: String) =
    fileName.endsWith(".h") || fileName.endsWith(".c") || fileName.endsWith(".hpp") || fileName.endsWith(".cpp")