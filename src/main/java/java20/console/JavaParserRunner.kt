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
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.UMLFactory
import uml.IUMLBuilder
import uml.builders.CPP14UMLBuilderPass1
import uml.builders.CPP14UMLBuilderPass2
import uml.cpp.generateCpp
import uml.util.clearPackageDir
import util.FilesUtil
import util.messages.FileMessage
import util.messages.FileMessageHandler
import util.messages.IMessageHandler
import java.io.File
import java.io.IOException
import java.io.PrintStream


class JavaParserRunner() {

    private val log: Logger = LogManager.getLogger(JavaParserRunner::class.java)

    class JavaParserRunner() {}

    fun collectFiles(vararg paths: String): ArrayList<String> {
        log.info("Collecting files from ${paths.toList()}")
        val cppFiles = ArrayList<String>()

        paths.forEach { FilesUtil.walkRes(it, ::test, cppFiles::add) }

        return cppFiles
    }

    /**
     * Создать именованную модель для заданных файлов.
     */
    fun buildModel(modelName: String, cppFiles: ArrayList<String>): Model {
        val projectPath = "."
        log.info("Building model")
        val model = UMLFactory.eINSTANCE.createModel()
        model.name = modelName

        //
        // 1-й проход. Добавление в UML-модель пакетов и типов данных.
        //
        log.info("1st: adding packages and data types to model")
        val mh1 = FileMessageHandler("$projectPath/messagesPass1.txt")
        val umlBuilderPass1 = CPP14UMLBuilderPass1(model, mh1)
        cppFiles.forEach { parseFile(it, mh1, umlBuilderPass1) }

        //
        // 2-й проход. Добавление в UML-модель элементов использующих пакеты и типы
        // данных.
        //
        log.info("2st: adding elements to model")
        val mh2 = FileMessageHandler("$projectPath/messagesPass2.txt")
        val umlBuilderPass2 = CPP14UMLBuilderPass2(model, mh2)
        cppFiles.forEach { parseFile(it, mh2, umlBuilderPass2) }

        return model
    }

    private fun parseFile(fileName: String, messageHandler: IMessageHandler, umlBuilder: IUMLBuilder) {
        log.info("Parsing file: $fileName")
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


    var sourcePath = "$projectDir/JavaToUMLSamples/src/samples"
//    var sourcePath = "$projectDir/CppToUMLSamples/src/snark-master"
//    var sourcePath = "$projectDir/CppToUMLSamples/src/cpprestsdk-master"
    var targetPathForCode = "$projectDir/targetPath/src"
    var targetPathForUMLModels = "$projectDir/targetPath/models"

    try {
        File(targetPathForCode).mkdirs()
        File(targetPathForUMLModels).mkdirs()

        val dumpDir = "$projectDir/dump-dir"
        File(dumpDir).mkdirs()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    var runner = JavaParserRunner();

    System.out.println(sourcePath)

    // Собираем файлы на языке C++.
    val cppFiles = runner.collectFiles(sourcePath)

    // Строим UML-модель для этих файлов.
    val model =  runner.buildModel("CppSampleModel", cppFiles)

    //
    // Генерация кода на языке C++.
    //
    clearPackageDir(targetPathForCode)
    model.saveModel(targetPathForUMLModels)
    model.nestedPackages.forEach { it.generateCpp(targetPathForCode) }
}

fun Model.saveModel(path: String?) {
    val uri = URI.createFileURI("$path/${name}.uml")
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

object UML2HTMLReporter {
    @JvmStatic
    fun generateReport(model: Model, htmlPath: String) {
            UML2HTMLReporter.generateReport(model, htmlPath)
    }
}

private fun test(fileName: String) =
    fileName.endsWith(".java")
