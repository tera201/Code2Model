package org.tera201.code2uml.java20.console

import org.tera201.code2uml.cpp.parser.CPP14ErrorListener
import org.tera201.code2uml.java20.parser.Java20TreeListener
import org.tera201.code2uml.java20.parser.generated.Java20Lexer
import org.tera201.code2uml.java20.parser.generated.Java20Parser
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
import org.tera201.code2uml.uml.decompiler.toKotlin
import org.tera201.code2uml.uml.util.UMLModelHandler
import org.tera201.code2uml.uml.util.clearPackageDir
import org.tera201.code2uml.util.FilesUtil
import org.tera201.code2uml.util.messages.FileMessage
import org.tera201.code2uml.util.messages.FileMessageHandler
import org.tera201.code2uml.util.messages.IMessageHandler
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.swing.JTextArea

@Deprecated("Use JavaParserRunnerDB instead")
class JavaParserRunner {

    // Logger for logging important information
    private val log: Logger = LogManager.getLogger(JavaParserRunner::class.java)

    // Path to model
    private var modelPath: String? = null

    /**
     * Collect Java files from given paths.
     *
     * @param paths Vararg of paths to search for Java files.
     * @return List of Java files found.
     */
    fun collectFiles(vararg paths: String): ArrayList<String> {
        modelPath = paths[0]
        log.info("Collecting files from ${paths.toList()}")
        val javaFiles = ArrayList<String>()
        paths.forEach { FilesUtil.walkRes(it, ::test, javaFiles::add) }
        return javaFiles
    }

    /**
     * Build UML model using the collected Java files.
     *
     * @param modelName Name of the model to be created.
     * @param javaFiles List of Java files to be included in the model.
     * @return Created UML Model.
     */
    fun buildModel(modelName: String, javaFiles: ArrayList<String>, logJTextArea: JTextArea? = null, numThreads: Int = 1): Model {
        val executorService: ExecutorService = Executors.newFixedThreadPool(numThreads)
        log.info("Building model: $modelName")

        // Initialize UML model and annotations
        val model = UMLFactory.eINSTANCE.createModel()
        model.name = modelName
        val annotation = EcoreFactory.eINSTANCE.createEAnnotation()
        annotation.source = "ResourcePath"
        annotation.getDetails().put("path", modelPath)
        model.eAnnotations.add(annotation)

        // Process files in two passes
        processFilesInPasses(model, javaFiles, logJTextArea, executorService)

        // Shutdown executor service after processing
        executorService.shutdown()

        return model
    }

    /**
     * Process Java files in two passes:
     * 1. Adding packages and data types to the model.
     * 2. Adding elements that use these packages and types.
     */
    private fun processFilesInPasses(model: Model, javaFiles: ArrayList<String>, logJTextArea: JTextArea?, executorService: ExecutorService) {
        // Pass 1: Add packages and data types
        logPass("1st: Adding packages and data types", logJTextArea)
        val mh1 = FileMessageHandler("messagesPass1.txt")
        val fileFutures1 = javaFiles.map { file ->
            CompletableFuture.runAsync({
                val umlBuilderPass1 = CodeUMLBuilderPass1(model, mh1)
                parseFile(file, mh1, umlBuilderPass1)
            }, executorService)
        }
        CompletableFuture.allOf(*fileFutures1.toTypedArray()).join()

        // Pass 2: Add elements that use the packages and data types
        logPass("2nd: Adding elements to the model", logJTextArea)
        val mh2 = FileMessageHandler("messagesPass2.txt")
        val fileFutures2 = javaFiles.map { file ->
            CompletableFuture.runAsync({
                val umlBuilderPass2 = CodeUMLBuilderPass2(model, mh2)
                parseFile(file, mh2, umlBuilderPass2)
            }, executorService)
        }
        CompletableFuture.allOf(*fileFutures2.toTypedArray()).join()
    }

    /**
     * Log the current pass stage with optional JTextArea logging.
     */
    private fun logPass(message: String, logJTextArea: JTextArea?) {
        log.debug(message)
        logJTextArea?.append("$message\n")
    }

    /**
     * Parse a single Java file and build UML representation using the provided builder.
     *
     * @param fileName Name of the Java file to parse.
     * @param messageHandler Message handler to capture parsing messages.
     * @param umlBuilder UML builder used to construct the model.
     */
    private fun parseFile(fileName: String, messageHandler: IMessageHandler, umlBuilder: IUMLBuilder) {
        log.debug("Parsing file: $fileName")
        messageHandler.info(FileMessage("Parsing file:", fileName))

        try {
            val input = CharStreams.fromFileName(fileName)
            val lexer = Java20Lexer(input)
            val tokens = CommonTokenStream(lexer)
            val parser = Java20Parser(tokens)
            val errorListener = CPP14ErrorListener(messageHandler)
            parser.addErrorListener(errorListener)

            val tree = parser.compilationUnit()
            val walker = ParseTreeWalker()
            val listener = Java20TreeListener(parser, umlBuilder, fileName)
            walker.walk(listener, tree)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Parse the file and output the syntax tree to a text file.
     */
    fun parseFileToText(fileName: String, mh: IMessageHandler) {
        try {
            val input = CharStreams.fromFileName(fileName)
            val lexer = Java20Lexer(input)
            val tokens = CommonTokenStream(lexer)
            val parser = Java20Parser(tokens)
            val errorListener = CPP14ErrorListener(mh)
            parser.addErrorListener(errorListener)

            val tree = parser.compilationUnit()

            val ps = PrintStream("$fileName.txt")
            ps.println(tree.toStringTree(parser))
            ps.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Test function to determine if a file should be processed (i.e., is a Java file and not in test or jvm directories).
     */
    private fun test(fileName: String): Boolean =
        fileName.endsWith(".java") && !fileName.contains("/test/") && !fileName.contains("/jvm/")
}

fun main() {
    val startTime = System.currentTimeMillis()
    val projectPath = "."
    val projectDir = File(projectPath).canonicalFile
    val sourcePath = "$projectDir/JavaToUMLSamples/src/a-foundation-master"
    val targetPathForCode = "$projectDir/target/src"
    val targetPathForUMLModels = "$projectDir/target/models"

    // Create necessary directories
    createDirectories(targetPathForCode, targetPathForUMLModels)

    val runner = JavaParserRunner()

    // Collect Java files
    val javaFiles = runner.collectFiles(sourcePath)

    // Build UML model for the collected files
    val model = runner.buildModel("JavaSampleModel", javaFiles, null, 4)

    // Generate C++ code and save UML models
    generateCppAndSaveModel(model, targetPathForCode, targetPathForUMLModels)

    // Convert UML to Kotlin and save the result
    convertUMLToKotlin(model, targetPathForUMLModels)

    // Save the final UML model to a JSON file
    saveModelToFile(model, targetPathForUMLModels)

    val endTime = System.currentTimeMillis()
    val executionTime = (endTime - startTime) / 1000.0
    println("Program execution time: $executionTime seconds")
}

/**
 * Create necessary directories for code and UML models.
 */
fun createDirectories(vararg paths: String) {
    paths.forEach {
        try {
            File(it).mkdirs()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

/**
 * Generate C++ code from UML model and save the model.
 */
fun generateCppAndSaveModel(model: Model, targetPathForCode: String, targetPathForUMLModels: String) {
    clearPackageDir(targetPathForCode)
    model.saveModel(targetPathForUMLModels)
    model.nestedPackages.forEach { it.generateCpp(targetPathForCode) }
}

/**
 * Convert UML model to Kotlin and save to the target directory.
 */
fun convertUMLToKotlin(model: Model, targetPathForUMLModels: String) {
    val kotlinPath = File(".")
        .absolutePath
        .replace(".", "target/generated/kotlin/${model.name}")
    File(kotlinPath).mkdirs()
    model.toKotlin(kotlinPath)
}

/**
 * Save the UML model to a file in the specified directory.
 */
fun saveModelToFile(model: Model, targetPathForUMLModels: String) {
    val handler = UMLModelHandler()
    handler.saveModelToFile(model, "$targetPathForUMLModels/Dimonmodel.json")
}

/**
 * Extension function to save the UML model to a file.
 */
fun Model.saveModel(file: String?) {
    val uri = URI.createFileURI("$file")
    val reg: Resource.Factory.Registry = Resource.Factory.Registry.INSTANCE
    val m: MutableMap<String, Any> = reg.getExtensionToFactoryMap()
    m[Resource.Factory.Registry.DEFAULT_EXTENSION] = XMIResourceFactoryImpl()
    val resource: Resource = ResourceSetImpl().createResource(uri)
    resource.getContents().add(this)
    try {
        resource.save(null)
    } catch (_: IOException) {
    }
}