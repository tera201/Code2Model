package cpp.console

import cpp.parser.CPP14ErrorListener
import cpp.parser.CPP14TreeListener
import cpp.parser.generated.CPP14Lexer
import cpp.parser.generated.CPP14Parser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.Package
import org.eclipse.uml2.uml.UMLFactory
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.common.util.URI
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

private lateinit var sourcePath: String
private lateinit var targetPathForCode: String
private lateinit var targetPathForUMLModels: String
private lateinit var projectPath: String

fun main() {
    try {
        projectPath = "."
        val projectDir = File(projectPath).canonicalFile

        sourcePath = "$projectDir/CppToUMLSamples/src"
        targetPathForCode = "$projectDir/targetPath/src"
        targetPathForUMLModels = "$projectDir/targetPath/models"

        File(targetPathForCode).mkdirs()
        File(targetPathForUMLModels).mkdirs()

        val dumpDir = "$projectDir/dump-dir"
        File(dumpDir).mkdirs()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    // Собираем файлы на языке C++.
    val cppFiles = collectFiles(sourcePath)

    // Строим UML-модель для этих файлов.
    val model = buildModel("CppSampleModel", cppFiles)

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

fun collectFiles(vararg paths: String): ArrayList<String> {
    val cppFiles = ArrayList<String>()

    paths.forEach { FilesUtil.walkRes(it, ::test, cppFiles::add) }

    return cppFiles
}

/**
 * Создать именованную модель для заданных файлов.
 */
fun buildModel(modelName: String, cppFiles: ArrayList<String>): Model {
    val model = UMLFactory.eINSTANCE.createModel()
    model.name = modelName

    //
    // 1-й проход. Добавление в UML-модель пакетов и типов данных.
    //
    val mh1 = FileMessageHandler("$projectPath/messagesPass1.txt")
    val umlBuilderPass1 = CPP14UMLBuilderPass1(model, mh1)
    cppFiles.forEach { parseFile(it, mh1, umlBuilderPass1) }

    //
    // 2-й проход. Добавление в UML-модель элементов использующих пакеты и типы
    // данных.
    //
    val mh2 = FileMessageHandler("$projectPath/messagesPass2.txt")
    val umlBuilderPass2 = CPP14UMLBuilderPass2(model, mh2)
    cppFiles.forEach { parseFile(it, mh2, umlBuilderPass2) }

    return model
}

private fun parseFile(fileName: String, messageHandler: IMessageHandler, umlBuilder: IUMLBuilder) {
    System.out.format("%nParsing file: %s%n", fileName)
    messageHandler.info(FileMessage("Parsing file:", fileName))

    try {
        // Входящий файл с тексом в виде кода считывается как поток символов
        val input = CharStreams.fromFileName(fileName)

        // Далее класса CPP14Lexer позволяет сгруппировать символы
        // и определить тип лексем (идентификатор, число, строка и т.п.).
        val lexer = CPP14Lexer(input)

        // Далее код разбивается на токены
        val tokens = CommonTokenStream(lexer)

        // Код подготавливается для использования далее в построении дерева разбора
        val parser = CPP14Parser(tokens)

        // Код проверяется на наличие синтаксических ошибок
        val errorListener = CPP14ErrorListener(messageHandler)
        parser.addErrorListener(errorListener)

        // Позволять определить вложенность вида родитель -
        // потомок (класс - член класса/метод)
        val tree = parser.translationUnit()
        val walker = ParseTreeWalker()
        val listener = CPP14TreeListener(parser, umlBuilder)
        walker.walk(listener, tree)
    } catch (e: IOException) {
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

private fun test(fileName: String) =
    fileName.endsWith(".h") or
            fileName.endsWith(".c") or
            fileName.endsWith(".hpp") or
            fileName.endsWith(".cpp")
