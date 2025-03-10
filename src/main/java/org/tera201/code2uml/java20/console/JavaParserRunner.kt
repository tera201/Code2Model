package org.tera201.code2uml.java20.console

import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.tree.ParseTree
import org.tera201.code2uml.ParserRunner
import org.tera201.code2uml.java20.parser.Java20TreeListener
import org.tera201.code2uml.java20.parser.generated.Java20Lexer
import org.tera201.code2uml.java20.parser.generated.Java20Parser

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