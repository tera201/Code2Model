package org.tera201.code2uml.util.messages

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException


class DataBaseUtil(url:String) {
    var conn: Connection
    init {
        try {
//            DriverManager.getConnection("jdbc:sqlite:" + url).use { conn ->
////                if (conn != null) {
////                }
//            }
            createTables("jdbc:sqlite:" + url)
        } catch (e: SQLException) {
            println(e.message)
        }
        conn = DriverManager.getConnection("jdbc:sqlite:" + url)
    }

    fun insertModel(name: String, filePath: String):Int {
        val sql = "INSERT OR IGNORE INTO Models(name, filePath) VALUES(?, ?)"
        val sqlLastId = "SELECT last_insert_rowid()"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, filePath)
            val affectedRows = pstmt.executeUpdate()
            if (affectedRows > 0) {
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sqlLastId).use { rs ->
                        if (rs.next()) {
                            val insertedId = rs.getInt(1)
                            return insertedId
                        }
                    }
                }
            }
        }
        return -1
    }

    fun getModelIdByNameAndFilePath(modelName: String, filePath: String): Int? {
        val sql = "SELECT id FROM Models WHERE name = ? AND filePath = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, modelName)
            pstmt.setString(2, filePath)
            pstmt.executeQuery().use { rs ->
                if (rs.next()) {
                    return rs.getInt("id")
                } else {
                    return null
                }
            }
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

    fun getPackageIdByPackageName(packageName: String, modelId: Int): Int? {
        val sql = "SELECT id FROM Packages WHERE package = ? AND modelId = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, packageName)
            pstmt.setInt(2, modelId)
            pstmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val id = rs.getInt("id")
                    return id
                } else {
                    return null
                }
            }
        }
    }

    fun insertPackageAndGetId(
        name: String, packageName:String, filePath: String, size: Long, modelId: Int
    ): Int {
        val sqlInsert = """
        INSERT OR IGNORE INTO Packages(name, package, filePath, size, modelId) VALUES(?, ?, ?, ?, ?)
    """.trimIndent()
        val sqlLastId = "SELECT last_insert_rowid()"

        conn.prepareStatement(sqlInsert).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, packageName)
            pstmt.setString(3, filePath)
            pstmt.setLong(4, size)
            pstmt.setInt(5, modelId)
            val affectedRows = pstmt.executeUpdate()
            if (affectedRows > 0) {
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sqlLastId).use { rs ->
                        if (rs.next()) {
                            val insertedId = rs.getInt(1)
                            return insertedId
                        }
                    }
                }
            }
        }
        return -1 // return an invalid ID if no new record was inserted
    }

    fun insertPackageRelationShip(packageParentId: Int, packageChildId: Int) {
        val sqlInsert = """
        INSERT OR IGNORE INTO PackageRelationship(packageParentId, packageChildId) VALUES(?, ?)
    """.trimIndent()
        conn.prepareStatement(sqlInsert).use { pstmt ->
            pstmt.setInt(1, packageParentId)
            pstmt.setInt(2, packageChildId)
            pstmt.executeUpdate()
        }
    }

    fun insertClassAndGetId(
        name: String, filePath: String, size: Long, modelId: Int, packageId: Int, type: Int, modificator: Int
    ): Int {
        val sqlInsert = """
        INSERT OR IGNORE INTO Classes(name, filePath, size, modelId, packageId, type, modificator) VALUES(?, ?, ?, ?, ?, ?, ?)
    """.trimIndent()
        val sqlLastId = "SELECT last_insert_rowid()"

        conn.prepareStatement(sqlInsert).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, filePath)
            pstmt.setLong(3, size)
            pstmt.setInt(4, modelId)
            pstmt.setInt(5, packageId)
            pstmt.setInt(6, type)
            pstmt.setInt(7, modificator)
            val affectedRows = pstmt.executeUpdate()
            if (affectedRows > 0) {
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sqlLastId).use { rs ->
                        if (rs.next()) {
                            val insertedId = rs.getInt(1)
                            return insertedId
                        }
                    }
                }
            }
        }
        return -1 // return an invalid ID if no new record was inserted
    }

    fun getClassIdByName(name: String): Int? {
        val sql = "SELECT id FROM Classes WHERE name = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val id = rs.getInt("id")
                    return id
                } else {
                    return null
                }
            }
        }
    }

    fun getClassIdByNameAndFilePath(name: String, filePath: String): Int? {
        val sql = "SELECT id FROM Classes WHERE name = ? AND filePath = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, filePath)
            pstmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val id = rs.getInt("id")
                    return id
                } else {
                    return null
                }
            }
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
        name: String, filePath: String, size: Long, modelId: Int, packageId: Int
    ): Int {
        val sqlInsert = """
        INSERT OR IGNORE INTO Interfaces(name, filePath, size, modelId, packageId) VALUES(?, ?, ?, ?, ?)
    """.trimIndent()
        val sqlLastId = "SELECT last_insert_rowid()"

        conn.prepareStatement(sqlInsert).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, filePath)
            pstmt.setLong(3, size)
            pstmt.setInt(4, modelId)
            pstmt.setInt(5, packageId)
            val affectedRows = pstmt.executeUpdate()
            if (affectedRows > 0) {
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sqlLastId).use { rs ->
                        if (rs.next()) {
                            val insertedId = rs.getInt(1)
                            return insertedId
                        }
                    }
                }
            }
        }
        return -1 // return an invalid ID if no new record was inserted
    }

    fun getInterfaceIdByNameAndFilePath(name: String, filePath: String): Int? {
        val sql = "SELECT id FROM Interfaces WHERE name = ? AND filePath = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, filePath)
            pstmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val id = rs.getInt("id")
                    return id
                } else {
                    return null
                }
            }
        }
    }

    fun getInterfaceIdByName(name: String): Int? {
        val sql = "SELECT id FROM Interfaces WHERE name = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val id = rs.getInt("id")
                    return id
                } else {
                    return null
                }
            }
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

    fun insertEnumerationAndGetId(name: String, filePath: String, size: Long, modelId: Int, packageId: Int): Int {
        val sqlInsert = """
        INSERT OR IGNORE INTO Enumerations(name, filePath, size, modelId, packageId) VALUES(?, ?, ?, ?, ?)
    """.trimIndent()
        val sqlLastId = "SELECT last_insert_rowid()"

        conn.prepareStatement(sqlInsert).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, filePath)
            pstmt.setLong(3, size)
            pstmt.setInt(4, modelId)
            pstmt.setInt(5, packageId)
            val affectedRows = pstmt.executeUpdate()
            if (affectedRows > 0) {
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sqlLastId).use { rs ->
                        if (rs.next()) {
                            val insertedId = rs.getInt(1)
                            return insertedId
                        }
                    }
                }
            }
        }
        return -1 // return an invalid ID if no new record was inserted
    }

    fun getEnumerationIdByNameAndFilePath(name: String, filePath: String): Int? {
        val sql = "SELECT id FROM Enumerations WHERE name = ? AND filePath = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, filePath)
            pstmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val id = rs.getInt("id")
                    return id
                } else {
                    return null
                }
            }
        }
    }

    fun insertMethod(name: String, type:String, modelId: Int, packageId: Int, classId: Int?, interfaceId: Int?) {
        if (classId == null && interfaceId == null) return
        val sqlInsert = """
        INSERT OR IGNORE INTO Methods(name, type, modelId, packageId, classId, interfaceId) VALUES(?, ?, ?, ?, ?, ?)
    """.trimIndent()

        conn.prepareStatement(sqlInsert).use { pstmt ->
            pstmt.setString(1, name)
            pstmt.setString(2, type)
            pstmt.setInt(3, modelId)
            pstmt.setInt(4, packageId)
            pstmt.setIntOrNull(5, classId)
            pstmt.setIntOrNull(6, interfaceId)
            pstmt.executeUpdate()
        }
    }

}

fun main() {
    var projectPath = "."
    val projectDir = File(projectPath).canonicalFile
    var dbUrl = "$projectDir/JavaToUMLSamples/db/model.db"
    dropTables("jdbc:sqlite:" + dbUrl)
}