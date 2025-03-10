package org.tera201.code2uml.cpp.console

import org.tera201.code2uml.cpp.parser.CPP14TreeListener
import org.tera201.code2uml.cpp.parser.generated.CPP14Lexer
import org.tera201.code2uml.cpp.parser.generated.CPP14Parser
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.tree.ParseTree
import org.tera201.code2uml.ParserRunner
import java.io.File
import java.io.IOException

class CppParserRunner : ParserRunner(::CPP14Lexer, ::CPP14Parser, ::CPP14TreeListener) {

    override fun getParseTree(parser: Parser): ParseTree = (parser as CPP14Parser).translationUnit()

    override fun test(fileName: String) =
        fileName.endsWith(".h") || fileName.endsWith(".c") || fileName.endsWith(".hpp") || fileName.endsWith(".cpp")
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