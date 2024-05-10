package org.tera201.code2uml.util.messages

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException


class DataBaseUtil(url:String) {
    var conn: Connection
    init {
        try {
            createTables("jdbc:sqlite:" + url)
        } catch (e: SQLException) {
            println(e.message)
        }
        conn = DriverManager.getConnection("jdbc:sqlite:" + url)
        conn.createStatement().use { stmt ->
            stmt.execute("PRAGMA foreign_keys = ON;")
        }
    }

    private fun getLastInsertId():Int {
        val sqlLastId = "SELECT last_insert_rowid()"
        conn.createStatement().use { stmt ->
            stmt.executeQuery(sqlLastId).use { rs ->
                if (rs.next()) {
                    return rs.getInt(1)
                }
            }
        }
        return -1
    }

    private fun getIdExecute(pstmt: PreparedStatement):Int?{
        pstmt.executeQuery().use { rs ->
            if (rs.next()) return rs.getInt("id")
            else return null
        }
    }

    private fun isExistExecute(pstmt: PreparedStatement): Boolean{
        pstmt.executeQuery().use { rs -> if (rs.next())  return true }
        return false
    }

    fun insertProject(name: String, filePath: String):Int {
        val sql = "INSERT OR IGNORE INTO Projects(name, filePath) VALUES(?, ?)"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, filePath)
            if (pstmt.executeUpdate() > 0) return getLastInsertId()
        }
        return -1
    }

    fun getProjectId(projectName: String, filePath: String): Int? {
        val sql = "SELECT id FROM Projects WHERE name = ? AND filePath = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, projectName)
            pstmt.setString(2, filePath)
            return getIdExecute(pstmt)
        }
    }

    fun insertModel(name: String, filePath: String, projectId: Int):Int {
        val sql = "INSERT OR IGNORE INTO Models(name, filePath, projectId) VALUES(?, ?, ?)"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, filePath)
            pstmt.setInt(3, projectId)
            if (pstmt.executeUpdate() > 0) return getLastInsertId()
        }
        return -1
    }

    fun deleteModel(id: Int):Boolean {
        val sql = "DELETE FROM Models WHERE id = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setInt(1, id)
            if (pstmt.executeUpdate() > 0) return true
        }
        return false
    }

    fun getModelIdByNameAndFilePath(modelName: String, filePath: String): Int? {
        val sql = "SELECT id FROM Models WHERE name = ? AND filePath = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, modelName)
            pstmt.setString(2, filePath)
            return getIdExecute(pstmt)
        }
    }

    fun getModelNameById(id: Int): String? {
        val sql = "SELECT * FROM Models WHERE id = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setInt(1, id)
            pstmt.executeQuery().use { rs ->
                if (rs.next()) {
                    return rs.getString("name")
                } else {
                    return null
                }
            }
        }
    }

    fun getPackageIdByPackageName(packageName: String, projectId: Int): Int? {
        val sql = "SELECT id FROM Packages WHERE package = ? AND projectId = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, packageName)
            pstmt.setInt(2, projectId)
            return getIdExecute(pstmt)
        }
    }

    fun insertPackageAndGetId(
        name: String, packageName:String, size: Long, projectId: Int
    ): Int {
        val sqlInsert = """
        INSERT OR IGNORE INTO Packages(name, package, size, projectId) VALUES(?, ?, ?, ?)
    """.trimIndent()

        conn.prepareStatement(sqlInsert).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, packageName)
            pstmt.setLong(3, size)
            pstmt.setInt(4, projectId)
            if (pstmt.executeUpdate() > 0) return getLastInsertId()
        }
        return -1 // return an invalid ID if no new record was inserted
    }

    fun insertModelPackageRelation(modelId: Int, packageId: Int) {
        val sqlInsert = """
        INSERT OR IGNORE INTO ModelPackageRelations(modelId, packageId) VALUES(?, ?)
    """.trimIndent()
        conn.prepareStatement(sqlInsert).use { pstmt ->
            pstmt.setInt(1, modelId)
            pstmt.setInt(2, packageId)
            pstmt.executeUpdate()
        }
    }

    fun insertPackageChecksumRelation(packageId: Int, checksum: String) {
        val sqlInsert = """
        INSERT OR IGNORE INTO PackageChecksumRelations(packageId, checksum) VALUES(?, ?)
    """.trimIndent()
        conn.prepareStatement(sqlInsert).use { pstmt ->
            pstmt.setInt(1, packageId)
            pstmt.setString(2, checksum)
            pstmt.executeUpdate()
        }
    }

    fun insertPackageRelationShip(packageParentId: Int, packageChildId: Int, modelId: Int) {
        val sqlInsert = """
        INSERT OR IGNORE INTO PackageRelationship(packageParentId, packageChildId, modelId) VALUES(?, ?, ?)
    """.trimIndent()
        conn.prepareStatement(sqlInsert).use { pstmt ->
            pstmt.setInt(1, packageParentId)
            pstmt.setInt(2, packageChildId)
            pstmt.setInt(3, modelId)
            pstmt.executeUpdate()
        }
    }

    fun insertClassAndGetId(
        name: String, filePath: String, size: Long, packageId: Int, type: Int, modificator: Int, checksum:String
    ): Int {
        val sqlInsert = """
        INSERT OR IGNORE INTO Classes(name, filePath, size, packageId, type, modificator, checksum) VALUES(?, ?, ?, ?, ?, ?, ?)
    """.trimIndent()

        conn.prepareStatement(sqlInsert).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, filePath)
            pstmt.setLong(3, size)
            pstmt.setInt(4, packageId)
            pstmt.setInt(5, type)
            pstmt.setInt(6, modificator)
            pstmt.setString(7, checksum)
            if (pstmt.executeUpdate() > 0) return getLastInsertId()
        }
        return -1 // return an invalid ID if no new record was inserted
    }

    fun getClassIdByName(name: String): Int? {
        val sql = "SELECT id FROM Classes WHERE name = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, name)
            return getIdExecute(pstmt)
        }
    }

    fun getClassId(name: String, filePath: String, checksum: String): Int? {
        val sql = "SELECT id FROM Classes WHERE name = ? AND filePath = ? AND checksum = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, filePath)
            pstmt.setString(3, checksum)
            return getIdExecute(pstmt)
        }
    }

    fun updateClass(classId: Int, type: Int, modificator: Int) {
        val sqlUpdate = """
        UPDATE Classes SET type = ?, modificator = ? WHERE id = ?
    """.trimIndent()

        conn.prepareStatement(sqlUpdate).use { pstmt ->
            pstmt.setInt(1, type)
            pstmt.setInt(2, modificator)
            pstmt.setInt(3, classId)
            pstmt.executeUpdate()
        }
    }

    fun updateSizeForInterface(id: Int, size: Int) {
        updateSize(id, size, "Interfaces")
    }

    fun updateSizeForClass(id: Int, size: Int) {
        updateSize(id, size, "Classes")
    }

    fun updateSize(id: Int, size: Int, table: String) {
        val sqlUpdate = """
        UPDATE $table SET size = ? WHERE id = ?
    """.trimIndent()

        conn.prepareStatement(sqlUpdate).use { pstmt ->
            pstmt.setInt(1, size)
            pstmt.setInt(2, id)
            pstmt.executeUpdate()
        }
    }

    fun addSizeForInterface(id: Int, size: Int) {
        addSize(id, size, "Interfaces")
    }

    fun addSizeForClass(id: Int, size: Int) {
        addSize(id, size, "Classes")
    }

    fun addSize(id: Int, size: Int, table: String) {
        val sqlUpdate = """
        UPDATE $table SET size = size + ? WHERE id = ?
    """.trimIndent()

        conn.prepareStatement(sqlUpdate).use { pstmt ->
            pstmt.setInt(1, size)
            pstmt.setInt(2, id)
            pstmt.executeUpdate()
        }
    }

    fun insertClassRelationShip(classId: Int, interfaceId: Int?, parentClassId:Int?) {
        if (interfaceId == null && parentClassId == null) return
        val sqlInsert = """
        INSERT OR IGNORE INTO ClassRelationship(classId, interfaceId, parentClassId) VALUES(?, ?, ?)
    """.trimIndent()
        conn.prepareStatement(sqlInsert).use { pstmt ->
            pstmt.setInt(1, classId)
            pstmt.setIntOrNull(2, interfaceId)
            pstmt.setIntOrNull(3, parentClassId)
            pstmt.executeUpdate()
        }
    }

    fun insertInterfaceAndGetId(
        name: String, filePath: String, size: Long, packageId: Int, checksum:String
    ): Int {
        val sqlInsert = """
        INSERT OR IGNORE INTO Interfaces(name, filePath, size, packageId, checksum) VALUES(?, ?, ?, ?, ?)
    """.trimIndent()

        conn.prepareStatement(sqlInsert).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, filePath)
            pstmt.setLong(3, size)
            pstmt.setInt(4, packageId)
            pstmt.setString(5, checksum)
            if (pstmt.executeUpdate() > 0) return getLastInsertId()
        }
        return -1 // return an invalid ID if no new record was inserted
    }

    fun getInterfaceId(name: String, filePath: String, checksum: String): Int? {
        val sql = "SELECT id FROM Interfaces WHERE name = ? AND filePath = ? AND checksum = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, filePath)
            pstmt.setString(3, checksum)
            return getIdExecute(pstmt)
        }
    }

    fun getInterfaceIdByName(name: String): Int? {
        val sql = "SELECT id FROM Interfaces WHERE name = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, name)
            return getIdExecute(pstmt)
        }
    }

    fun insertInterfaceRelationShip(interfaceId: Int, parentInterfaceId: Int) {
        val sqlInsert = """
        INSERT OR IGNORE INTO InterfaceRelationship(interfaceId, parentInterfaceId) VALUES(?, ?)
    """.trimIndent()
        conn.prepareStatement(sqlInsert).use { pstmt ->
            pstmt.setInt(1, interfaceId)
            pstmt.setInt(2, parentInterfaceId)
            pstmt.executeUpdate()
        }
    }

    fun insertEnumerationAndGetId(name: String, filePath: String, size: Long, packageId: Int, checksum:String): Int {
        val sqlInsert = """
        INSERT OR IGNORE INTO Enumerations(name, filePath, size, packageId, checksum) VALUES(?, ?, ?, ?, ?)
    """.trimIndent()

        conn.prepareStatement(sqlInsert).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, filePath)
            pstmt.setLong(3, size)
            pstmt.setInt(4, packageId)
            pstmt.setString(5, checksum)
            if (pstmt.executeUpdate() > 0) return getLastInsertId()
        }
        return -1 // return an invalid ID if no new record was inserted
    }

    fun getEnumerationId(name: String, filePath: String, checksum: String): Int? {
        val sql = "SELECT id FROM Enumerations WHERE name = ? AND filePath = ? AND checksum = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, filePath)
            pstmt.setString(3, checksum)
            return getIdExecute(pstmt)
        }
    }

    fun insertMethod(name: String, type:String, classId: Int?, interfaceId: Int?) {
        if (classId == null && interfaceId == null) return
        val sqlInsert = """
        INSERT OR IGNORE INTO Methods(name, type, classId, interfaceId) VALUES(?, ?, ?, ?)
    """.trimIndent()

        conn.prepareStatement(sqlInsert).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, type)
            pstmt.setIntOrNull(3, classId)
            pstmt.setIntOrNull(4, interfaceId)
            pstmt.executeUpdate()
        }
    }

    fun insertFile(checksum:String, fileName: String, projectId: Int):Int {
        val sql = "INSERT OR IGNORE INTO Files(checksum, fileName, projectId) VALUES(?, ?, ?)"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, checksum)
            pstmt.setString(2, fileName)
            pstmt.setInt(3, projectId)
            if (pstmt.executeUpdate() > 0) return getLastInsertId()
        }
        return -1
    }

    fun isFileExist(checksum: String): Boolean {
        val sql = "SELECT * FROM Files WHERE checksum = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, checksum)
            return isExistExecute(pstmt)
        }
    }

    fun insertFilePath(checksum:String, filePath: String):Int {
        val sql = "INSERT OR IGNORE INTO FilePaths(checksum, filePath) VALUES(?, ?)"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, checksum)
            pstmt.setString(2, filePath)
            if (pstmt.executeUpdate() > 0) return getLastInsertId()
        }
        return -1
    }

    fun getFilePathId(checksum:String, filePath: String):Int? {
        val sql = "SELECT * FROM Files WHERE checksum = ? AND filePath = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, checksum)
            pstmt.setString(2, filePath)
            return getIdExecute(pstmt)
        }
    }

    fun insertFileModelRelation(checksum:String, modelId: Int):Int {
        val sql = "INSERT OR IGNORE INTO FileModelRelations(checksum, modelId) VALUES(?, ?)"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, checksum)
            pstmt.setInt(2, modelId)
            if (pstmt.executeUpdate() > 0) return getLastInsertId()
        }
        return -1
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
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setInt(1, modelId)
            pstmt.setString(2, checksum)
            pstmt.executeUpdate()
        }
        val sqlModelPackageRelations = """
        INSERT OR IGNORE INTO ModelPackageRelations (modelId, packageId)
        SELECT ?, pcr.packageId
        FROM PackageChecksumRelations pcr
        WHERE pcr.checksum = ?;
    """
        conn.prepareStatement(sqlModelPackageRelations).use { pstmt ->
            pstmt.setInt(1, modelId)
            pstmt.setString(2, checksum)
            pstmt.executeUpdate()
        }
        insertFileModelRelation(checksum, modelId)
    }

    fun isFileModelRelationExist(checksum: String, modelId: Int): Boolean {
        val sql = "SELECT * FROM FileModelRelations WHERE checksum = ? AND modelId = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, checksum)
            pstmt.setInt(2, modelId)
            return isExistExecute(pstmt)
        }
    }

}

fun main() {
    var projectPath = "."
    val projectDir = File(projectPath).canonicalFile
    var dbUrl = "$projectDir/JavaToUMLSamples/db/model.db"
    dropTables("jdbc:sqlite:" + dbUrl)
}