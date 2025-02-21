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
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EcoreFactory
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.UMLFactory
import org.tera201.code2uml.uml.IUMLBuilder
import org.tera201.code2uml.uml.builders.CodeUMLBuilderPass1
import org.tera201.code2uml.uml.builders.CodeUMLBuilderPass2
import org.tera201.code2uml.uml.decompiler.generateCpp
import org.tera201.code2uml.uml.util.clearPackageDir
import org.tera201.code2uml.util.FilesUtil
import org.tera201.code2uml.util.messages.FileMessage
import org.tera201.code2uml.util.messages.FileMessageHandler
import org.tera201.code2uml.util.messages.IMessageHandler
import java.io.File
import java.io.IOException
import java.io.PrintStream

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

    /**
     * Create a named model for the given files.
     */
    fun buildModel(modelName: String, cppFiles: ArrayList<String>): Model {
        val projectPath = "."
        log.info("Building model")
        val model = UMLFactory.eINSTANCE.createModel()
        model.name = modelName

        val annotation = EcoreFactory.eINSTANCE.createEAnnotation()
        annotation.source = "ResourcePath"
        annotation.details.put("path", modelPath);
        model.eAnnotations.add(annotation)

        // 1st pass: Adding packages and data types to the UML model.
        log.info("1st: adding packages and data types to model")
        val mh1 = FileMessageHandler("$projectPath/messagesPass1.txt")
        val umlBuilderPass1 = CodeUMLBuilderPass1(model, mh1)
        cppFiles.forEach { parseFile(it, mh1, umlBuilderPass1) }

        // 2nd pass: Adding elements to the UML model that use packages and data types.
        log.info("2nd: adding elements to model")
        val mh2 = FileMessageHandler("$projectPath/messagesPass2.txt")
        val umlBuilderPass2 = CodeUMLBuilderPass2(model, mh2)
        cppFiles.forEach { parseFile(it, mh2, umlBuilderPass2) }

        return model
    }

    private fun parseFile(fileName: String, messageHandler: IMessageHandler, umlBuilder: IUMLBuilder) {
        log.info("Parsing file: $fileName")
        messageHandler.info(FileMessage("Parsing file:", fileName))

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
            val errorListener = CPP14ErrorListener(messageHandler)
            parser.addErrorListener(errorListener)

            // Allows you to determine the nesting of the parent-child type (class - class member/method)
            val tree = parser.translationUnit()
            val walker = ParseTreeWalker()
            val listener = CPP14TreeListener(parser, umlBuilder, fileName)
            walker.walk(listener, tree)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun parseFile(fileName: String, mh: IMessageHandler) {
        try {
            val input = CharStreams.fromFileName(fileName)
            val lexer = CPP14Lexer(input)
            val tokens = CommonTokenStream(lexer)
            val parser = CPP14Parser(tokens)

            val errorListener = CPP14ErrorListener(mh)
            parser.addErrorListener(errorListener)

            val tree = parser.translationUnit()

            val ps = PrintStream("$fileName.txt")
            ps.println(tree.toStringTree(parser))
            ps.close()
        } catch (e: IOException) {
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
    val model = runner.buildModel("CppSampleModel", cppFiles)

    // Generate C++ code.
    clearPackageDir(targetPathForCode)
    model.saveModel(targetPathForUMLModels)
    model.nestedPackages.forEach { it.generateCpp(targetPathForCode) }
}

fun Model.saveModel(path: String?) {
    val uri = URI.createFileURI("$path/${name}.org.tera201.code2uml.uml")
    val reg: Resource.Factory.Registry = Resource.Factory.Registry.INSTANCE
    val m: MutableMap<String, Any> = reg.extensionToFactoryMap
    m[Resource.Factory.Registry.DEFAULT_EXTENSION] = XMIResourceFactoryImpl()
    val resource: Resource = ResourceSetImpl().createResource(uri)
    resource.contents.add(this)
    try {
        resource.save(null)
    } catch (_: IOException) {
    }
}

private fun test(fileName: String) =
    fileName.endsWith(".h") || fileName.endsWith(".c") || fileName.endsWith(".hpp") || fileName.endsWith(".cpp")