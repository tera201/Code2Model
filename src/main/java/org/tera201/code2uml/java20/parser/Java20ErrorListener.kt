package org.tera201.code2uml.java20.parser

import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.atn.ATNConfigSet
import org.antlr.v4.runtime.dfa.DFA
import java.util.*

class Java20ErrorListener() : ANTLRErrorListener {
    override fun reportAmbiguity(
        arg0: Parser, arg1: DFA, arg2: Int, arg3: Int, arg4: Boolean, arg5: BitSet,
        arg6: ATNConfigSet,
    ) {
        // TODO Auto-generated method stub
    }

    override fun reportAttemptingFullContext(
        arg0: Parser,
        arg1: DFA,
        arg2: Int,
        arg3: Int,
        arg4: BitSet,
        arg5: ATNConfigSet,
    ) {
        // TODO Auto-generated method stub
    }

    override fun reportContextSensitivity(
        arg0: Parser,
        arg1: DFA,
        arg2: Int,
        arg3: Int,
        arg4: Int,
        arg5: ATNConfigSet,
    ) {
        // TODO Auto-generated method stub
    }

    override fun syntaxError(
        recognizer: Recognizer<*, *>, offendingSymbol: Any, line: Int, column: Int, message: String,
        e: RecognitionException,
    ) {
//        val fileName = recognizer.inputStream.sourceName
//        System.err.format("file: %s line: %s col: %s %n %s%n", fileName, line, column, message)
    }
}