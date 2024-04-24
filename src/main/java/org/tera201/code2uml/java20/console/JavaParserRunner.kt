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
import org.eclipse.uml2.uml.internal.impl.ModelImpl
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


class JavaParserRunner() {

    private val log: Logger = LogManager.getLogger(JavaParserRunner::class.java)
    private var modelPath:String? = null

    class JavaParserRunner() {}

    fun collectFiles(vararg paths: String): ArrayList<String> {
        modelPath = paths[0]
        log.info("Collecting files from ${paths.toList()}")
        val cppFiles = ArrayList<String>()

        paths.forEach { FilesUtil.walkRes(it, ::test, cppFiles::add) }

        return cppFiles
    }


    fun buildModel(modelName: String, javaFiles: ArrayList<String>): Model  {
        return buildModel(modelName, javaFiles, null)
    }


    fun buildModel(modelName: String, javaFiles: ArrayList<String>, numThreads:Int): Model  {
        return buildModel(modelName, javaFiles, null, numThreads)
    }

    /**
     * Создать именованную модель для заданных файлов.
     */
    fun buildModel(modelName: String, javaFiles: ArrayList<String>, logJTextArea: JTextArea?, numThreads:Int): Model {
        val projectPath = "."
        val executorService: ExecutorService = Executors.newFixedThreadPool(numThreads)
        log.info("Building model")
        val model = UMLFactory.eINSTANCE.createModel()
        model.name = modelName

        val annotation = EcoreFactory.eINSTANCE.createEAnnotation()
        annotation.source = "ResourcePath"
        annotation.getDetails().put("path", modelPath);
        model.eAnnotations.add(annotation)

        //
        // 1-й проход. Добавление в UML-модель пакетов и типов данных.
        //
        if (logJTextArea != null) logJTextArea.append("1st: adding packages and data types to model\n")
        log.debug("1st: adding packages and data types to model")
        val mh1 = FileMessageHandler("$projectPath/messagesPass1.txt")
        val fileFutures = javaFiles.map { file ->
            CompletableFuture.runAsync({
                val umlBuilderPass1 = CodeUMLBuilderPass1(model, mh1)
                parseFile(file, mh1, umlBuilderPass1)
            }, executorService)
        }
        val allOf = CompletableFuture.allOf(*fileFutures.toTypedArray())
        allOf.join()

        //
        // 2-й проход. Добавление в UML-модель элементов использующих пакеты и типы
        // данных.
        //
        if (logJTextArea != null) logJTextArea.append("2st: adding elements to model\n")
        log.debug("2st: adding elements to model")
        val mh2 = FileMessageHandler("$projectPath/messagesPass2.txt")
        val fileFutures2 = javaFiles.map { file ->
            CompletableFuture.runAsync({
                val umlBuilderPass2 = CodeUMLBuilderPass2(model, mh2)
                parseFile(file, mh1, umlBuilderPass2)
            }, executorService)
        }
        val allOf2 = CompletableFuture.allOf(*fileFutures2.toTypedArray())
        allOf2.join()
        executorService.shutdown()
        return model
    }
    fun buildModel(modelName: String, javaFiles: ArrayList<String>, logJTextArea: JTextArea?): Model {
        val projectPath = "."
        log.info("Building model")
        val model = UMLFactory.eINSTANCE.createModel()
        model.name = modelName

        val annotation = EcoreFactory.eINSTANCE.createEAnnotation()
        annotation.source = "ResourcePath"
        annotation.getDetails().put("path", modelPath);
        model.eAnnotations.add(annotation)

        if (logJTextArea != null) logJTextArea.append("1st: adding packages and data types to model\n")
        log.debug("1st: adding packages and data types to model")
        val mh1 = FileMessageHandler("$projectPath/messagesPass1.txt")
        val umlBuilderPass1 = CodeUMLBuilderPass1(model, mh1)
        javaFiles.forEach { parseFile(it, mh1, umlBuilderPass1) }

        if (logJTextArea != null) logJTextArea.append("2st: adding elements to model\n")
        log.debug("2st: adding elements to model")
        val mh2 = FileMessageHandler("$projectPath/messagesPass2.txt")
        val umlBuilderPass2 = CodeUMLBuilderPass2(model, mh2)
        javaFiles.forEach { parseFile(it, mh2, umlBuilderPass2) }
        return model
    }

    private fun parseFile(fileName: String, messageHandler: IMessageHandler, umlBuilder: IUMLBuilder) {
        log.debug("Parsing file: $fileName")
        messageHandler.info(FileMessage("Parsing file:", fileName))

        try {
            // Входящий файл с тексом в виде кода считывается как поток символов
            val input = CharStreams.fromFileName(fileName)

            // Далее класса Java20Lexer позволяет сгруппировать символы
            // и определить тип лексем (идентификатор, число, строка и т.п.).
            val lexer = Java20Lexer(input)

            // Далее код разбивается на токены
            val tokens = CommonTokenStream(lexer)

            // Код подготавливается для использования далее в построении дерева разбора
            val parser = Java20Parser(tokens)

            // Код проверяется на наличие синтаксических ошибок
            val errorListener = CPP14ErrorListener(messageHandler)
            parser.addErrorListener(errorListener)

            // Позволять определить вложенность вида родитель -
            // потомок (класс - член класса/метод)
            val tree = parser.compilationUnit()
            val walker = ParseTreeWalker()
            val listener = Java20TreeListener(parser, umlBuilder, fileName)
            walker.walk(listener, tree)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun parseFile(fileName: String, mh: IMessageHandler) {
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

}

fun main() {
    val startTime = System.currentTimeMillis()
    var projectPath = "."
    val projectDir = File(projectPath).canonicalFile
    var sourcePath = "$projectDir/JavaToUMLSamples/src/a-foundation-master"
    var targetPathForCode = "$projectDir/target/src"
    var targetPathForUMLModels = "$projectDir/target/models"

    try {
        File(targetPathForCode).mkdirs()
        File(targetPathForUMLModels).mkdirs()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    var runner = JavaParserRunner();

    System.out.println(sourcePath)

    // Collect java files.
    val javaFiles = runner.collectFiles(sourcePath)

    // Build UML-model for these files.
    val model =  runner.buildModel("JavaSampleModel", javaFiles, 4)
    println("Model end")

    //
    // Generate C++ code.
    //
    clearPackageDir(targetPathForCode)
    model.saveModel(targetPathForUMLModels)
    model.nestedPackages.forEach { it.generateCpp(targetPathForCode) }

    //
    // UML --> Kotlin
    //
    val kotlinPath = File(".")
        .absolutePath
        .replace(".", "target/generated/kotlin/${model.name}")
    File(kotlinPath).mkdirs()

    model.toKotlin(kotlinPath)
    val handler = UMLModelHandler()
    handler.saveModelToFile(model, "$targetPathForUMLModels/Dimonmodel.json")
    println("Model saved")
    val endTime = System.currentTimeMillis()
    val executionTime = (endTime - startTime) / 1000.0
    println("Время выполнения программы: $executionTime секунд")
//    val loadedModel: ModelImpl? = handler.loadModelFromFile("$targetPathForUMLModels/model.json")
//    println(loadedModel)
//    if (loadedModel != null)
//    handler.saveModelToFile(loadedModel, "$targetPathForUMLModels/loadedModel.json")

}

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

fun loadModelFromFile(file: String?) : ModelImpl {
    val uri = URI.createFileURI("$file")
    val resource = ResourceSetImpl().getResource(uri, true)
    return resource.contents.get(0) as ModelImpl
}

object UML2HTMLReporter {
    @JvmStatic
    fun generateReport(model: Model, htmlPath: String) {
            UML2HTMLReporter.generateReport(model, htmlPath)
    }
}

//TODO: kick large files
private fun test(fileName: String) =
    fileName.endsWith(".java") and
            !fileName.contains("/test/") and
            !fileName.contains("/jvm/")
