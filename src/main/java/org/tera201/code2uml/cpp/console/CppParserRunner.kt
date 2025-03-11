package org.tera201.code2uml.cpp.console

import org.tera201.code2uml.cpp.parser.CPP14TreeListener
import org.tera201.code2uml.cpp.parser.generated.CPP14Lexer
import org.tera201.code2uml.cpp.parser.generated.CPP14Parser
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.tree.ParseTree
import org.tera201.code2uml.ParserRunner

class CppParserRunner : ParserRunner(::CPP14Lexer, ::CPP14Parser, ::CPP14TreeListener) {

    override fun getParseTree(parser: Parser): ParseTree = (parser as CPP14Parser).translationUnit()

    override fun filter(fileName: String) =
        fileName.endsWith(".h") || fileName.endsWith(".c") || fileName.endsWith(".hpp") || fileName.endsWith(".cpp")
}