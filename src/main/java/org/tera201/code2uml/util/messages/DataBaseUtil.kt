package org.tera201.code2uml.util.messages

import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException


class DataBaseUtil(url:String) {
    var conn: Connection
    init {
        try {
            DriverManager.getConnection("jdbc:sqlite:" + url).use { conn ->
                if (conn != null) {
//                    println("A new database has been created.")
                }
            }
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
//                            println("A new model was inserted with ID: $insertedId")
                            return insertedId
                        }
                    }
                }
            } else {
//                println("The model already exists and was not inserted: $name")
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
                    val id = rs.getInt("id")
                    return id
                } else {
//                    println("No record found with modelName: $modelName")
                    return null
                }
            }
        }
    }

    fun getPackageIdByPackage(packageName: String): Int? {
        val sql = "SELECT id FROM Packages WHERE package = ?"
        conn.prepareStatement(sql).use { pstmt ->
            pstmt.setString(1, packageName)
            pstmt.executeQuery().use { rs ->
                if (rs.next()) {
                    val id = rs.getInt("id")
//                    println("Found ID: $id for name: $name and filePath: $filePath")
                    return id
                } else {
//                    println("No record found with package: $packageName")
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
//                            println("A new package was inserted with ID: $insertedId")
                            return insertedId
                        }
                    }
                }
            } else {
//                println("No new package was inserted, likely already exists.")
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
            val affectedRows = pstmt.executeUpdate()
            if (affectedRows > 0) {
//                println("A new package relationship was inserted with ID -> ID: $packageParentId -> $packageChildId")
                } else {
//                println("No new package was inserted, likely already exists.")
            }
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
//                            println("A new class was inserted with ID: $insertedId")
                            return insertedId
                        }
                    }
                }
            } else {
//                println("No new class was inserted, likely already exists.")
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
//                    println("No record found with name: $name")
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
//                    println("Found ID: $id for name: $name and filePath: $filePath")
                    return id
                } else {
//                    println("No record found with name: $name and filePath: $filePath")
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
            val affectedRows = pstmt.executeUpdate()
            if (affectedRows > 0) {
//                println("Class updated successfully.")
            } else {
//                println("No class was updated. Check if the ID is correct.")
            }
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
            val affectedRows = pstmt.executeUpdate()
            if (affectedRows > 0) {
//                println("A new class relationship for ID: $classId")
            } else {
//                println("No new class relationship was inserted, likely already exists.")
            }
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
//                            println("A new interface was inserted with ID: $insertedId")
                            return insertedId
                        }
                    }
                }
            } else {
//                println("No new interface was inserted, likely already exists.")
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
//                    println("Found ID: $id for name: $name and filePath: $filePath")
                    return id
                } else {
//                    println("No record found with name: $name and filePath: $filePath")
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
//                    println("No record found with name: $name")
                    return null
                }
            }
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
//                            println("A new enumeration was inserted with ID: $insertedId")
                            return insertedId
                        }
                    }
                }
            } else {
//                println("No new enumeration was inserted, likely already exists.")
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
//                    println("No record found with name: $name and filePath: $filePath")
                    return null
                }
            }
        }
    }

}
fun main() {
    var projectPath = "."
    val projectDir = File(projectPath).canonicalFile
    var dbUrl = "$projectDir/JavaToUMLSamples/db/model.db"
    dropTables("jdbc:sqlite:" + dbUrl)
}