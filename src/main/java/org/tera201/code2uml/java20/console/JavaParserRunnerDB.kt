package org.tera201.code2uml.java20.console

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.tera201.code2uml.db.builders.CodeDBBuilderPass1
import org.tera201.code2uml.db.builders.CodeDBBuilderPass2
import org.tera201.code2uml.java20.parser.Java20DBTreeListener
import org.tera201.code2uml.java20.parser.Java20ErrorListener
import org.tera201.code2uml.java20.parser.generated.Java20Lexer
import org.tera201.code2uml.java20.parser.generated.Java20Parser
import org.tera201.code2uml.uml.DBBuilder
import org.tera201.code2uml.uml.util.clearPackageDir
import org.tera201.code2uml.util.FilesUtil
import org.tera201.code2uml.util.messages.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.swing.JProgressBar
import javax.swing.JTextArea

/**
 * Class responsible for running the Java parser, collecting files, and building UML models.
 */
class JavaParserRunnerDB {

    private val log: Logger = LoggerFactory.getLogger(JavaParserRunnerDB::class.java)
    private var modelPath: String? = null
    private var checksumMap = mutableMapOf<String, String>()

    /**
     * Collects Java files from given paths and stores them in a list.
     */
    fun collectFiles(vararg paths: String): ArrayList<String> {
        modelPath = paths[0]
        log.info("Collecting files from ${paths.toList()}")
        val javaFiles = ArrayList<String>()
        checksumMap.clear()

        // Walk through directories and collect Java files
        paths.forEach { FilesUtil.walkRes(it, ::test, javaFiles::add) }

        // Compute checksum for each file
        javaFiles.forEach { checksumMap[it] = calculateChecksum(it) }

        return javaFiles
    }

    /**
     * Creates or retrieves a project from the database by name and file path.
     */
    private fun createOrGetProject(dataBaseUtil: DataBaseUtil, name: String, filePath: String): Int {
        val projectId = dataBaseUtil.getProjectId(name, filePath)
        return if (projectId == -1) dataBaseUtil.insertProject(name, filePath) else projectId
    }

    /**
     * Builds a UML model for the given Java files.
     */
    fun buildModel(dataBaseUtil: DataBaseUtil, projectName: String, modelName: String, javaFiles: ArrayList<String>, logJTextArea: JTextArea? = null, progressBar: JProgressBar? = null, numThreads: Int? = null): Int {
        val projectPath = "."
        val executorService: ExecutorService? = numThreads?.let { Executors.newFixedThreadPool(it) }

        log.info("Building model")

        // Retrieve or create project and model in the database
        val projectId = createOrGetProject(dataBaseUtil, projectName, projectPath)
        var modelId = dataBaseUtil.getModelId(modelName, projectPath, projectId)

        // Insert new model if necessary
        if (modelId == -1) modelId = dataBaseUtil.insertModel(modelName, projectPath, projectId)

        javaFiles.map { checksumMap.getOrDefault(it, calculateChecksum(it)) }
            .filter { checksum -> dataBaseUtil.isFileExist(checksum) }
            .forEach { checksum -> dataBaseUtil.insertNewRelationsForModel(modelId, checksum)
        }

        // Files that need analysis
        val javaFilesUnanalyzed = javaFiles.filter { file ->
            val checksum = checksumMap.getOrDefault(file, calculateChecksum(file))
            !dataBaseUtil.isFileModelRelationExist(checksum, modelId)
        }

        progressBar?.minimum = 0
        progressBar?.maximum = javaFilesUnanalyzed.size * 2;

        // Step 1: Add packages and data types to model
        logJTextArea?.append("1st: adding packages and data types to model\n")
        log.debug("1st: adding packages and data types to model")
        parseFilesWithAsync(javaFilesUnanalyzed, projectId, modelId, dataBaseUtil, executorService, ::parseFilesBuilder1, progressBar)

        // Step 2: Add elements to model
        logJTextArea?.append("2nd: adding elements to model\n")
        log.debug("2nd: adding elements to model")
        parseFilesWithAsync(javaFilesUnanalyzed, projectId, modelId, dataBaseUtil, executorService, ::parseFilesBuilder2, progressBar)

        // Shutdown executor service if used
        executorService?.shutdown()

        return modelId
    }

    /**
     * Parse files asynchronously with a given step builder.
     */
    private fun parseFilesWithAsync(
        javaFiles: List<String>,
        projectId: Int,
        modelId: Int,
        dataBaseUtil: DataBaseUtil,
        executorService: ExecutorService?,
        parseStep: (List<String>, Int, Int, DataBaseUtil, JProgressBar?) -> Unit,
        progressBar: JProgressBar?
    ) {
        if (executorService != null) {
            val groupedFiles = javaFiles.groupBy { File(it).parent }
            val fileFutures = groupedFiles.values.map { files ->
                CompletableFuture.runAsync({ parseStep(files, projectId, modelId, dataBaseUtil, progressBar) }, executorService)
            }
            CompletableFuture.allOf(*fileFutures.toTypedArray()).join()
        } else {
            parseStep(javaFiles, projectId, modelId, dataBaseUtil, progressBar)
        }
    }

    /**
     * Parse files for the first step (adding packages and data types).
     */
    private fun parseFilesBuilder1(javaFiles: List<String>,
                                   projectId: Int,
                                   modelId: Int,
                                   dataBaseUtil: DataBaseUtil,
                                   progressBar: JProgressBar? ) {
        val dbBuilderPass1 = CodeDBBuilderPass1(projectId, modelId, dataBaseUtil)
        javaFiles.forEach {
            parseFile(it, dbBuilderPass1)
            progressBar?.let { progressBar.value = progressBar.value.plus(1) }
        }
    }

    /**
     * Parse files for the second step (adding elements).
     */
    private fun parseFilesBuilder2(javaFiles: List<String>,
                                   projectId: Int,
                                   modelId: Int,
                                   dataBaseUtil: DataBaseUtil,
                                   progressBar: JProgressBar? ) {
        val dbBuilderPass2 = CodeDBBuilderPass2(projectId, modelId, dataBaseUtil)
        javaFiles.forEach {
            parseFile(it, dbBuilderPass2)
            progressBar?.let { progressBar.value = progressBar.value.plus(1) }
        }
    }

    /**
     * Parse a single Java file and update the database.
     */
    private fun parseFile(filePath: String, dbBuilder: DBBuilder) {
        log.debug("Parsing file: $filePath")
        val fileName = filePath.substringAfterLast("/")
        val checksum = checksumMap.getOrDefault(filePath, calculateChecksum(filePath))

        // Insert file if it's not already in the database
        dbBuilder.dataBaseUtil.insertFile(checksum, fileName, dbBuilder.projectId)
        if (dbBuilder.dataBaseUtil.isFileModelRelationExist(checksum, dbBuilder.model)) return

        // Insert file-path relation if needed
        if (dbBuilder is CodeDBBuilderPass2) {
            dbBuilder.dataBaseUtil.insertFilePath(checksum, filePath)
            dbBuilder.dataBaseUtil.insertFileModelRelation(checksum, dbBuilder.model)
        }

        try {
            val input = CharStreams.fromFileName(filePath)
            val lexer = Java20Lexer(input)
            val tokens = CommonTokenStream(lexer)
            val parser = Java20Parser(tokens)

            parser.removeErrorListeners()
            lexer.removeErrorListeners()

//            val errorListener = Java20ErrorListener()
//            parser.addErrorListener(errorListener)
//            lexer.addErrorListener(errorListener)

            // Build the parse tree
            val tree = parser.compilationUnit()
            val walker = ParseTreeWalker()

            // Walk the parse tree and build the model
            val listener = Java20DBTreeListener(parser, dbBuilder, filePath, checksum)
            walker.walk(listener, tree)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Calculates the checksum for a file using the specified algorithm (default: SHA-256).
     */
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

    /**
     * Converts a byte array to its hexadecimal string representation.
     */
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
    val dbUrl = "$projectDir/JavaToUMLSamples/db/model.db"
    val dataBaseUtil = DataBaseUtil(dbUrl)

    try {
        File(targetPathForCode).mkdirs()
        File(targetPathForUMLModels).mkdirs()
    } catch (e: IOException) {
        e.printStackTrace()
    }

    val runner = JavaParserRunnerDB()

    // Collect java files
    val javaFiles = runner.collectFiles(sourcePath)

    // Build UML model for these files
    val model = runner.buildModel(dataBaseUtil, "JavaSampleModel", "master", javaFiles, null, null,4)

    clearPackageDir(targetPathForCode)
    println("Model saved")

    val endTime = System.currentTimeMillis()
    val executionTime = (endTime - startTime) / 1000.0
    println("Execution time: $executionTime seconds")
}