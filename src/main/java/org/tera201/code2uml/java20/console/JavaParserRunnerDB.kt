package org.tera201.code2uml.java20.console

import org.tera201.code2uml.cpp.parser.CPP14ErrorListener
import org.tera201.code2uml.java20.parser.Java20DBTreeListener
import org.tera201.code2uml.java20.parser.generated.Java20Lexer
import org.tera201.code2uml.java20.parser.generated.Java20Parser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.tera201.code2uml.uml.DBBuilder
import org.tera201.code2uml.db.builders.CodeDBBuilderPass1
import org.tera201.code2uml.db.builders.CodeDBBuilderPass2
import org.tera201.code2uml.uml.util.clearPackageDir
import org.tera201.code2uml.util.FilesUtil
import org.tera201.code2uml.util.messages.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.PrintStream
import java.security.MessageDigest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.swing.JTextArea


class JavaParserRunnerDB() {

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


    fun buildModel(dataBaseUtil: DataBaseUtil, modelName: String, javaFiles: ArrayList<String>):Int  {
        return buildModel(dataBaseUtil, modelName, javaFiles, null, null)
    }


    fun buildModel(dataBaseUtil: DataBaseUtil, modelName: String, javaFiles: ArrayList<String>, numThreads:Int?):Int  {
        return buildModel(dataBaseUtil, modelName, javaFiles, null, numThreads)
    }


    fun buildModel(dataBaseUtil: DataBaseUtil, modelName: String, javaFiles: ArrayList<String>, logJTextArea: JTextArea?):Int  {
        return buildModel(dataBaseUtil, modelName, javaFiles, logJTextArea, null)
    }

    /**
     * Создать именованную модель для заданных файлов.
     */
    fun buildModel(dataBaseUtil: DataBaseUtil, modelName: String, javaFiles: ArrayList<String>, logJTextArea: JTextArea?, numThreads:Int?):Int {
        val projectPath = "."
        val executorService: ExecutorService? = numThreads?.let { Executors.newFixedThreadPool(it) }
        log.info("Building model")
        var modelId = dataBaseUtil.getModelIdByNameAndFilePath(modelName, modelPath!!)
        if (modelId == null) {modelId = dataBaseUtil.insertModel(modelName, modelPath!!)}
        else return modelId

        //
        // 1st step
        //
        if (logJTextArea != null) logJTextArea.append("1st: adding packages and data types to model\n")
        log.debug("1st: adding packages and data types to model")
        val mh1 = FileMessageHandler("$projectPath/messagesPass1.txt")
        val dbBuilderPass1 = CodeDBBuilderPass1(modelId, mh1, dataBaseUtil)
        parseFilesWithAsync(javaFiles, mh1, dbBuilderPass1, executorService)

        //
        // 2nd step
        //
        if (logJTextArea != null) logJTextArea.append("2st: adding elements to model\n")
        log.debug("2st: adding elements to model")
        val mh2 = FileMessageHandler("$projectPath/messagesPass2.txt")
        val dbBuilderPass2 = CodeDBBuilderPass2(modelId, mh2, dataBaseUtil)
        parseFilesWithAsync(javaFiles, mh1, dbBuilderPass2, executorService)
        executorService?.shutdown()
        return modelId
    }

    fun parseFilesWithAsync(javaFiles: ArrayList<String>, mh: IMessageHandler, umlBuilder: DBBuilder, executorService: ExecutorService?) {
        if (executorService != null) {
            val fileFutures =  javaFiles.map { file -> CompletableFuture.runAsync({parseFile(file, mh, umlBuilder)}, executorService)}
            val allOf = CompletableFuture.allOf(*fileFutures.toTypedArray())
            allOf.join()
        }
        else  javaFiles.map { file -> parseFile(file, mh, umlBuilder)}
    }

    private fun parseFile(fileName: String, messageHandler: IMessageHandler, umlBuilder: DBBuilder) {
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
            val listener = Java20DBTreeListener(parser, umlBuilder, fileName)
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
    var dbUrl = "$projectDir/JavaToUMLSamples/db/model.db"
    val dataBaseUtil = DataBaseUtil(dbUrl)

    try {
        File(targetPathForCode).mkdirs()
        File(targetPathForUMLModels).mkdirs()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    var runner = JavaParserRunnerDB();

    System.out.println(sourcePath)

    // Collect java files.
    val javaFiles = runner.collectFiles(sourcePath)

    // Build UML-model for these files.
    val model =  runner.buildModel(dataBaseUtil, "JavaSampleModel", javaFiles, 4)
    println(dataBaseUtil.getClass(1))

    //
    // Generate C++ code.
    //
    clearPackageDir(targetPathForCode)
    println("Model saved")
    val endTime = System.currentTimeMillis()
    val executionTime = (endTime - startTime) / 1000.0
    println("Время выполнения программы: $executionTime секунд")

}

fun calculateChecksum(filePath: String, algorithm: String = "SHA-256"): String {
    val buffer = ByteArray(8192)
    val md = MessageDigest.getInstance(algorithm)

    FileInputStream(File(filePath)).use { fis ->
        var numRead: Int
        while (fis.read(buffer).also { numRead = it } != -1) {
            md.update(buffer, 0, numRead)
        }
    }

    return bytesToHex(md.digest())
}

private fun bytesToHex(bytes: ByteArray): String {
    val hexArray = "0123456789ABCDEF".toCharArray()
    val hexChars = CharArray(bytes.size * 2)
    for (i in bytes.indices) {
        val v = bytes[i].toInt() and 0xFF
        hexChars[i * 2] = hexArray[v ushr 4]
        hexChars[i * 2 + 1] = hexArray[v and 0x0F]
    }
    return String(hexChars)
}

//TODO: kick large files
private fun test(fileName: String) =
    fileName.endsWith(".java") and
            !fileName.contains("/test/") and
            !fileName.contains("/jvm/")
