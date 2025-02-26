package org.tera201.code2uml.util.messages

import java.io.File
import java.sql.*


class DataBaseUtil(url:String) {
    val conn: Connection

    init {
        try {
            Class.forName("org.sqlite.JDBC") // Explicitly load SQLite driver
        } catch (e: ClassNotFoundException) {
            throw RuntimeException("SQLite JDBC driver not found!", e)
        }
        conn = createDatabaseConnection("jdbc:sqlite:$url")
    }

    private fun createDatabaseConnection(dbUrl: String): Connection {
        return try {
            DriverManager.getConnection(dbUrl).also { enableForeignKeys(it) }.also { createTables(it) }
        } catch (e: SQLException) {
            throw RuntimeException("Error connecting to the database: ${e.message}", e)
        }
    }

    fun recreateTables() {
        createTables(conn)
    }

    fun clearTables() {
        dropTables(conn)
    }

    private fun enableForeignKeys(connection: Connection) {
        connection.createStatement().use { it.execute("PRAGMA foreign_keys = ON;") }
    }

    /** Helper function to set parameters for PreparedStatements */
    private fun setParams(pstmt: PreparedStatement, params: Array<out Any?>) {
        params.forEachIndexed { index, param ->
            when (param) {
                is Int -> pstmt.setInt(index + 1, param)
                is Int? -> pstmt.setIntOrNull(index + 1, param)
                is String -> pstmt.setString(index + 1, param)
                is Long -> pstmt.setLong(index + 1, param)
                else -> pstmt.setObject(index + 1, param)
            }
        }
    }

    /** Generic function to execute a SELECT query */
    private fun <T> executeQuery(sql: String, vararg params: Any?, mapper: (ResultSet) -> T): T {
        return conn.prepareStatement(sql).use { pstmt ->
            setParams(pstmt, params)
            pstmt.executeQuery().use { rs -> mapper(rs) }
        }
    }

    /** Generic function to execute an UPDATE or INSERT query */
    private fun executeUpdate(sql: String, vararg params: Any?): Boolean {
        return conn.prepareStatement(sql).use { pstmt ->
            setParams(pstmt, params)
            pstmt.executeUpdate() > 0
        }
    }

    /** Helper function to fetch last inserted ID */
    private fun getLastInsertId(): Int {
        val sql = "SELECT last_insert_rowid()"
        return executeQuery(sql) { rs -> if (rs.next()) rs.getInt(1) else -1 }
    }

    /** Executes a query that returns a single Int (e.g., fetching an ID) */
    private fun getIdResult(rs: ResultSet): Int {
        return if (rs.next()) rs.getInt("id") else -1
    }

    /** Executes a query that checks if a record exists */
    private fun isExistExecute(pstmt: PreparedStatement): Boolean {
        return pstmt.executeQuery().use { rs -> rs.next() }
    }

    /** Extension function to handle nullable Int values */
    private fun PreparedStatement.setIntOrNull(index: Int, value: Int?) {
        if (value == null) setNull(index, Types.INTEGER) else setInt(index, value)
    }

    // ---- INSERT FUNCTIONS ----
    fun insertProject(name: String, filePath: String): Int {
        val sql = "INSERT OR IGNORE INTO Projects(name, filePath) VALUES(?, ?)"
        return if (executeUpdate(sql, name, filePath)) getLastInsertId() else -1
    }

    fun insertModel(name: String, filePath: String, projectId: Int): Int {
        val sql = "INSERT OR IGNORE INTO Models(name, filePath, projectId) VALUES(?, ?, ?)"
        return if (executeUpdate(sql, name, filePath, projectId)) getModelId(name, filePath, projectId) else -1
    }

    fun insertPackageAndGetId(
        name: String, packageName:String, size: Long, projectId: Int
    ): Int {
        val sqlInsert = "INSERT OR IGNORE INTO Packages(name, package, size, projectId) VALUES(?, ?, ?, ?)"
        return if (executeUpdate(sqlInsert, name, packageName, size, projectId)) getPackageId(name, packageName, size, projectId) else -1
    }

    fun insertModelPackageRelation(modelId: Int, packageId: Int) {
        val sqlInsert = "INSERT OR IGNORE INTO ModelPackageRelations(modelId, packageId) VALUES(?, ?)"
        executeUpdate(sqlInsert, modelId, packageId)
    }

    fun insertPackageChecksumRelation(packageId: Int, checksum: String) {
        val sqlInsert = "INSERT OR IGNORE INTO PackageChecksumRelations(packageId, checksum) VALUES(?, ?)"
        executeUpdate(sqlInsert, packageId, checksum)
    }

    fun insertPackageRelationShip(packageParentId: Int, packageChildId: Int, modelId: Int) {
        val sqlInsert = "INSERT OR IGNORE INTO PackageRelationship(packageParentId, packageChildId, modelId) VALUES(?, ?, ?)"
        executeUpdate(sqlInsert, packageParentId, packageChildId, modelId)
    }

    fun insertClassAndGetId(name: String, filePath: String, size: Long, packageId: Int, type: Int, modificator: Int, checksum:String): Int {
        val sqlInsert = "INSERT OR IGNORE INTO Classes(name, filePath, size, packageId, type, modificator, checksum) VALUES(?, ?, ?, ?, ?, ?, ?)"
        return if (executeUpdate(sqlInsert, name, filePath, size, packageId, type, modificator, checksum)) getClassId(name, filePath, checksum) else -1
    }

    fun insertImportedClass(name: String, extendedByClassId: Int, packageId: Int) {
        val sqlInsert = "INSERT OR IGNORE INTO ImportedClasses(name, classId, packageId) VALUES(?, ?, ?)"
        executeUpdate(sqlInsert, name, extendedByClassId, packageId)
    }

    fun insertClassRelationShip(classId: Int, interfaceId: Int?, parentClassId:Int?) {
        if (interfaceId == null && parentClassId == null) return
        val sqlInsert = "INSERT OR IGNORE INTO ClassRelationship(classId, interfaceId, parentClassId) VALUES(?, ?, ?)"
        try {
            executeUpdate(sqlInsert, classId, interfaceId, parentClassId)
        } catch (e: SQLException) {
            println("+ ClassRelationship($classId, $interfaceId, $parentClassId)")
        }
    }

    fun insertInterfaceAndGetId(name: String, filePath: String, size: Long, packageId: Int, checksum:String): Int {
        val sqlInsert = "INSERT OR IGNORE INTO Interfaces(name, filePath, size, packageId, checksum) VALUES(?, ?, ?, ?, ?)"
        return if (executeUpdate(sqlInsert, name, filePath, size, packageId, checksum)) getInterfaceId(name, filePath, packageId, checksum) else -1
    }

    fun insertInterfaceRelationShip(interfaceId: Int, parentInterfaceId: Int) {
        val sqlInsert = "INSERT OR IGNORE INTO InterfaceRelationship(interfaceId, parentInterfaceId) VALUES(?, ?)"
        executeUpdate(sqlInsert, interfaceId, parentInterfaceId)
    }

    fun insertEnumerationAndGetId(name: String, filePath: String, size: Long, packageId: Int, checksum:String): Int {
        val sqlInsert = "INSERT OR IGNORE INTO Enumerations(name, filePath, size, packageId, checksum) VALUES(?, ?, ?, ?, ?)"
        return if (executeUpdate(sqlInsert, name, filePath, size, packageId, checksum)) getEnumerationId(name, filePath, checksum) else -1
    }

    fun insertMethod(name: String, type:String, classId: Int?, interfaceId: Int?) {
        if (classId == null && interfaceId == null) return
        val sqlInsert = """
        INSERT OR IGNORE INTO Methods(name, type, classId, interfaceId) VALUES(?, ?, ?, ?)
    """.trimIndent()
        executeUpdate(sqlInsert, name, type, classId, interfaceId)
    }

    fun insertFile(checksum:String, fileName: String, projectId: Int):Int {
        val sql = "INSERT OR IGNORE INTO Files(checksum, fileName, projectId) VALUES(?, ?, ?)"
        return if (executeUpdate(sql, checksum, fileName, projectId)) getLastInsertId() else -1
    }

    fun insertFilePath(checksum:String, filePath: String):Boolean {
        val sql = "INSERT OR IGNORE INTO FilePaths(checksum, filePath) VALUES(?, ?)"
        return executeUpdate(sql, checksum, filePath)
    }

    fun insertNewRelationsForModel(modelId: Int, checksum:String) {
        val sql = """
        INSERT OR IGNORE INTO PackageRelationship (packageParentId, packageChildId, modelId)
        SELECT pr.packageParentId, pr.packageChildId, ?
        FROM PackageRelationship pr
        JOIN PackageChecksumRelations pcr1 ON pr.packageParentId = pcr1.packageId
        JOIN PackageChecksumRelations pcr2 ON pr.packageChildId = pcr2.packageId
        WHERE pcr1.checksum = pcr2.checksum AND pcr1.checksum = ?;
    """
        executeUpdate(sql, modelId, checksum)
        val sqlModelPackageRelations = """
        INSERT OR IGNORE INTO ModelPackageRelations (modelId, packageId)
        SELECT ?, pcr.packageId
        FROM PackageChecksumRelations pcr
        WHERE pcr.checksum = ?;
    """
        executeUpdate(sqlModelPackageRelations, modelId, checksum)
        insertFileModelRelation(checksum, modelId)
    }

    fun insertFileModelRelation(checksum:String, modelId: Int) {
        val sql = "INSERT OR IGNORE INTO FileModelRelations(checksum, modelId) VALUES(?, ?)"
        executeUpdate(sql, checksum, modelId)
    }

    // ---- GET FUNCTIONS ----

    fun getProjectId(projectName: String, filePath: String): Int {
        val sql = "SELECT id FROM Projects WHERE name = ? AND filePath = ?"
        return executeQuery(sql, projectName, filePath) { getIdResult(it) }
    }

    fun getModelId(modelName: String, filePath: String, projectId: Int): Int {
        val sql = "SELECT id FROM Models WHERE name = ? AND filePath = ? AND projectId = ?"
        return executeQuery(sql, modelName, filePath, projectId) { getIdResult(it) }
    }

    fun getModelIdByNameAndFilePath(modelName: String, filePath: String): Int {
        val sql = "SELECT id FROM Models WHERE name = ? AND filePath = ?"
        return executeQuery(sql, modelName, filePath) { getIdResult(it) }
    }

    fun getModelNameById(id: Int): String? {
        val sql = "SELECT * FROM Models WHERE id = ?"
        return executeQuery(sql, id) { rs -> if (rs.next()) rs.getString("name") else null }
    }

    fun getClassIdByName(name: String): Int {
        val sql = "SELECT id FROM Classes WHERE name = ?"
        return executeQuery(sql, name) { getIdResult(it) }
    }

    fun getClassId(name: String, filePath: String, checksum: String): Int {
        val sql = "SELECT id FROM Classes WHERE name = ? AND filePath = ? AND checksum = ?"
        return executeQuery(sql, name, filePath, checksum) { getIdResult(it) }
    }

    fun getInterfaceId(name: String, filePath: String, packageId:Int, checksum: String): Int {
        val sql = "SELECT id FROM Interfaces WHERE name = ? AND filePath = ? AND packageId = ? AND checksum = ?"
        return executeQuery(sql, name, filePath, packageId, checksum) { getIdResult(it) }
    }

    fun getInterfaceIdByName(name: String): Int {
        val sql = "SELECT id FROM Interfaces WHERE name = ?"
        return executeQuery(sql, name) { getIdResult(it) }
    }

    fun getEnumerationId(name: String, filePath: String, checksum: String): Int {
        val sql = "SELECT id FROM Enumerations WHERE name = ? AND filePath = ? AND checksum = ?"
        return executeQuery(sql, name, filePath, checksum) { getIdResult(it) }
    }

    fun getPackageId(name: String, packageName:String, size: Long, projectId: Int): Int {
        val sql = "SELECT id FROM Packages WHERE name = ? AND package = ? AND size = ? AND projectId = ?"
        return executeQuery(sql, name, packageName, size, projectId) { getIdResult(it) }
    }

    fun getPackageIdByPackageName(packageName: String, projectId: Int): Int {
        val sql = "SELECT id FROM Packages WHERE package = ? AND projectId = ?"
        return executeQuery(sql, packageName, projectId) { getIdResult(it) }
    }

    fun getFilePathId(checksum:String, filePath: String):Int {
        val sql = "SELECT * FROM Files WHERE checksum = ? AND filePath = ?"
        return executeQuery(sql, checksum, filePath) { getIdResult(it) }
    }

    // ---- DELETE FUNCTION ----

    fun deleteModel(id: Int):Boolean {
        val sql = "DELETE FROM Models WHERE id = ?"
        return executeUpdate(sql, id)
    }

    // ---- UPDATE FUNCTIONS ----

    fun updateClass(classId: Int, type: Int, modificator: Int) {
        val sql = "UPDATE Classes SET type = ?, modificator = ? WHERE id = ?"
        executeUpdate(sql, type, modificator, classId)
    }

    fun updateSizeForInterface(id: Int, size: Int) {
        updateSize(id, size, "Interfaces")
    }

    fun updateSizeForClass(id: Int, size: Int) {
        updateSize(id, size, "Classes")
    }

    fun updateSize(id: Int, size: Int, table: String) {
        val sql = "UPDATE $table SET size = ? WHERE id = ?"
        executeUpdate(sql, size, id)
    }

    fun addSizeForInterface(id: Int, size: Int) {
        addSize(id, size, "Interfaces")
    }

    fun addSizeForClass(id: Int, size: Int) {
        addSize(id, size, "Classes")
    }

    fun addSize(id: Int, size: Int, table: String) {
        val sql = "UPDATE $table SET size = size + ? WHERE id = ?"
        executeUpdate(sql, size, id)
    }

    fun isFileExist(checksum: String): Boolean {
        val sql = "SELECT * FROM Files WHERE checksum = ?"
        return executeQuery(sql, checksum) { rs -> rs.next() }
    }

    fun isFileModelRelationExist(checksum: String, modelId: Int): Boolean {
        val sql = "SELECT * FROM FileModelRelations WHERE checksum = ? AND modelId = ?"
        return executeQuery(sql, checksum, modelId) { rs -> rs.next() }
    }

    companion object {
        @Volatile
        private var instance: DataBaseUtil? = null

        fun getInstance(databaseUrl: String): DataBaseUtil {
            return instance ?: synchronized(this) {
                instance ?: DataBaseUtil(databaseUrl).also { instance = it }
            }
        }
    }

}

fun main() {
    val projectPath = "."
    val projectDir = File(projectPath).canonicalFile
    val dbUrl = "$projectDir/JavaToUMLSamples/db/model.db"
    val dataBaseUtil = DataBaseUtil.getInstance(dbUrl)
    dataBaseUtil.clearTables()
}