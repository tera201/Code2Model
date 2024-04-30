package org.tera201.code2uml

import org.eclipse.uml2.uml.Model
import org.tera201.code2uml.cpp.console.CppParserRunner
import org.tera201.code2uml.java20.console.JavaParserRunner
import org.tera201.code2uml.java20.console.JavaParserRunnerDB
import org.tera201.code2uml.util.messages.DataBaseUtil
import javax.swing.JTextArea

class AnalyzerBuilder(private var language: Language, private var modelName: String, private var path: String, private var pathToDB:String) {
    private var numThread:Int? = null
    private var textArea:JTextArea? = null

    fun threads(numThread: Int?):AnalyzerBuilder {
        this.numThread = numThread
        return this
    }

    fun textArea(textArea: JTextArea?):AnalyzerBuilder {
        this.textArea = JTextArea()
        return this
    }

    fun setPathToDB(pathToDB:String):AnalyzerBuilder {
        this.pathToDB = pathToDB
        return this
    }

    fun buildDB():Int {
        when (language){
            Language.Java -> {
                val javaParserRunner = JavaParserRunnerDB()
                val javaFiles = javaParserRunner.collectFiles(path)
                val dataBaseUtil = DataBaseUtil(pathToDB)
                val model:Int
                if (numThread != null) model = javaParserRunner.buildModel(dataBaseUtil, modelName, javaFiles, textArea, numThread!!)
                else model = javaParserRunner.buildModel(dataBaseUtil, modelName, javaFiles, textArea)
                return model
            }

            Language.Cpp -> TODO()
        }
    }

    fun build():Model {
        when (language){
            Language.Java -> {
                val javaParserRunner = JavaParserRunner()
                val javaFiles = javaParserRunner.collectFiles(path)
                val model:Model
                if (numThread != null) model = javaParserRunner.buildModel(modelName, javaFiles, textArea, numThread!!)
                else model = javaParserRunner.buildModel(modelName, javaFiles, textArea)
                return model
            }
            Language.Cpp -> {
                val cppParserRunner = CppParserRunner()
                val cppFiles = cppParserRunner.collectFiles(path)
                return cppParserRunner.buildModel(modelName, cppFiles)
            }
        }
    }
}