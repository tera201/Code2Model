package java20.console

import cpp.parser.CPP14ErrorListener
import java20.parser.Java20TreeListener
import java20.parser.generated.Java20Lexer
import java20.parser.generated.Java20Parser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl.ResourceLocator
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.UMLFactory
import org.eclipse.uml2.uml.UMLPackage
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl
import uml.IUMLBuilder
import uml.builders.CPP14UMLBuilderPass1
import uml.builders.CPP14UMLBuilderPass2
import uml.decompiler.generateCpp
import uml.decompiler.toKotlin
import uml.util.clearPackageDir
import util.FilesUtil
import util.messages.FileMessage
import util.messages.FileMessageHandler
import util.messages.IMessageHandler
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.util.*
import javax.swing.JTextArea


class JavaParserRunner() {

    private val log: Logger = LogManager.getLogger(JavaParserRunner::class.java)

    class JavaParserRunner() {}

    fun collectFiles(vararg paths: String): ArrayList<String> {
        log.info("Collecting files from ${paths.toList()}")
        val cppFiles = ArrayList<String>()

        paths.forEach { FilesUtil.walkRes(it, ::test, cppFiles::add) }

        return cppFiles
    }


    fun buildModel(modelName: String, javaFiles: ArrayList<String>): Model  {
        return buildModel(modelName, javaFiles, null)
    }

    /**
     * Создать именованную модель для заданных файлов.
     */
    fun buildModel(modelName: String, javaFiles: ArrayList<String>, logJTextArea: JTextArea?): Model {
        val projectPath = "."
        log.info("Building model")
        val model = UMLFactory.eINSTANCE.createModel()
        model.name = modelName

        //
        // 1-й проход. Добавление в UML-модель пакетов и типов данных.
        //
        if (logJTextArea != null) logJTextArea.append("1st: adding packages and data types to model\n")
        log.info("1st: adding packages and data types to model")
        val mh1 = FileMessageHandler("$projectPath/messagesPass1.txt")
        val umlBuilderPass1 = CPP14UMLBuilderPass1(model, mh1)
        if (logJTextArea != null)
        javaFiles.forEach { parseFile(it, mh1, umlBuilderPass1, logJTextArea) }
        else javaFiles.forEach { parseFile(it, mh1, umlBuilderPass1) }

        //
        // 2-й проход. Добавление в UML-модель элементов использующих пакеты и типы
        // данных.
        //
        if (logJTextArea != null) logJTextArea.append("2st: adding elements to model\n")
        log.info("2st: adding elements to model")
        val mh2 = FileMessageHandler("$projectPath/messagesPass2.txt")
        val umlBuilderPass2 = CPP14UMLBuilderPass2(model, mh2)
        if (logJTextArea != null)
        javaFiles.forEach { parseFile(it, mh2, umlBuilderPass2, logJTextArea) }
        else javaFiles.forEach { parseFile(it, mh2, umlBuilderPass2) }
        return model
    }

    private fun parseFile(fileName: String, messageHandler: IMessageHandler, umlBuilder: IUMLBuilder, logJTextArea: JTextArea) {
//        logJTextArea.append("Parsing file: $fileName\n")
        parseFile(fileName, messageHandler, umlBuilder)
    }

    private fun parseFile(fileName: String, messageHandler: IMessageHandler, umlBuilder: IUMLBuilder) {
//        log.info("Parsing file: $fileName")
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
            val listener = Java20TreeListener(parser, umlBuilder)
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
    var projectPath = "."
    val projectDir = File(projectPath).canonicalFile


//    var sourcePath = "$projectDir/JavaToUMLSamples/src/samples"
//    var sourcePath = "$projectDir/JavaToUMLSamples/src/JavaFXUMLGraph"
    var sourcePath = "$projectDir/JavaToUMLSamples/src/a-foundation-master"
//    var sourcePath = "$projectDir/JavaToUMLSamples/src/JavaFXUMLGraph/src/main/java/umlgraph/graphview/utils/"
    var targetPathForCode = "$projectDir/target/src"
    var targetPathForUMLModels = "$projectDir/target/models/"
    val targetModelFile = "$targetPathForUMLModels/model.uml"

    try {
        File(targetPathForCode).mkdirs()
        File(targetPathForUMLModels).mkdirs()

//        val dumpDir = "$projectDir/dump-dir"
//        File(dumpDir).mkdirs()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    var runner = JavaParserRunner();

    System.out.println(sourcePath)

    // Collect java files.
    val javaFiles = runner.collectFiles(sourcePath)

    // Build UML-model for these files.
    val model =  runner.buildModel("JavaSampleModel", javaFiles)

    //
    // Generate C++ code.
    //
    clearPackageDir(targetPathForCode)
    model.saveModel(targetModelFile)
    model.nestedPackages.forEach { it.generateCpp(targetPathForCode) }

    //
    // UML --> Kotlin
    //
    val kotlinPath = File(".")
        .absolutePath
        .replace(".", "generated/kotlin/${model.name}")
    File(kotlinPath).mkdirs()
    model.toKotlin(kotlinPath)

}

fun Model.saveModel2(file: String?) {
    Resource.Factory.Registry.INSTANCE.extensionToFactoryMap["uml"] =
        UMLResourceFactoryImpl()
    UMLPackage.eINSTANCE.eClass()
    val resourceSet: ResourceSet = ResourceSetImpl()
    val resource = resourceSet.createResource(URI.createFileURI(file))
    resource.getContents().add(this);
    try {
        resource.save(Collections.EMPTY_MAP)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    resource.unload();
}

fun Model.saveModel(file: String?) {
    val uri = URI.createFileURI("$file")
    val reg: Resource.Factory.Registry = Resource.Factory.Registry.INSTANCE
    val m: MutableMap<String, Any> = reg.getExtensionToFactoryMap()
    m[Resource.Factory.Registry.DEFAULT_EXTENSION] = XMIResourceFactoryImpl()
    val resourceIml = ResourceSetImpl()
    val resource = resourceIml.createResource(uri)
    resource.getContents().add(this)
    println(resourceIml.getResourceFactoryRegistry())
    try {
        resource.save(null)
    } catch (_: IOException) {
    }
}

fun loadModel(file: String?): Model? {
    val uri = URI.createFileURI(file)
    val resourceSet = ResourceSetImpl()
    UMLPackage.eINSTANCE.eClass()

    // Регистрация XMIResourceFactory для всех файлов *.uml
    resourceSet.resourceFactoryRegistry.extensionToFactoryMap["uml"] = XMIResourceFactoryImpl()

    try {
        // Загрузка ресурса
        val resource = resourceSet.getResource(uri, true)

        // Преобразование первого элемента ресурса в Model и возвращение его
        return resource.contents[0] as? org.eclipse.uml2.uml.Model
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}

//TODO: kick large files
private fun test(fileName: String) =
    fileName.endsWith(".java") and
            !fileName.contains("/test/") and
            !fileName.contains("/jvm/") and
            !fileName.contains("benchmark")
