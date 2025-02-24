package org.tera201.code2uml.util.messages

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException

private val log: Logger = LogManager.getLogger(DataBaseUtil::class.java)
/**
 * Creates necessary tables in the SQLite database if they do not already exist.
 *
 * @param url JDBC connection URL to the database.
 */
fun createTables(connection: Connection) {
    // Map containing table creation SQL statements
    val tableCreationQueries = mapOf(
        "Models" to """
            CREATE TABLE IF NOT EXISTS Models (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                filePath TEXT NOT NULL,
                projectId INTEGER NOT NULL,
                FOREIGN KEY (projectId) REFERENCES Projects(id),
                UNIQUE (name, filePath, projectId)
            );
        """.trimIndent(),

        "Packages" to """
            CREATE TABLE IF NOT EXISTS Packages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                package TEXT NOT NULL,
                size LONG NOT NULL,
                projectId INTEGER NOT NULL,
                FOREIGN KEY (projectId) REFERENCES Projects(id),
                UNIQUE (name, package, projectId)
            );
        """.trimIndent(),

        "ModelPackageRelations" to """
            CREATE TABLE IF NOT EXISTS ModelPackageRelations (
                modelId INTEGER,
                packageId INTEGER,
                FOREIGN KEY (modelId) REFERENCES Models(id) ON DELETE CASCADE,
                FOREIGN KEY (packageId) REFERENCES Packages(id),
                UNIQUE (modelId, packageId)
            );
        """.trimIndent(),

        "PackageChecksumRelations" to """
            CREATE TABLE IF NOT EXISTS PackageChecksumRelations (
                packageId INTEGER,
                checksum TEXT,
                FOREIGN KEY (packageId) REFERENCES Packages(id),
                FOREIGN KEY (checksum) REFERENCES Files(checksum),
                UNIQUE (packageId, checksum)
            );
        """.trimIndent(),

        "Class" to """
            CREATE TABLE IF NOT EXISTS Classes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                filePath TEXT NOT NULL,
                size LONG NOT NULL,
                packageId INTEGER,
                type INTEGER,
                modificator INTEGER,
                checksum TEXT,
                FOREIGN KEY (packageId) REFERENCES Packages(id),
                FOREIGN KEY (checksum) REFERENCES Files(checksum),
                UNIQUE (name, filePath, packageId, checksum) 
            );
        """.trimIndent(),

        "Interface" to """
            CREATE TABLE IF NOT EXISTS Interfaces (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                filePath TEXT NOT NULL,
                size LONG NOT NULL,
                packageId INTEGER,
                checksum TEXT,
                FOREIGN KEY (packageId) REFERENCES Packages(id),
                FOREIGN KEY (checksum) REFERENCES Files(checksum),
                UNIQUE (name, filePath, packageId, checksum) 
            );
        """.trimIndent(),

        "Enumeration" to """
            CREATE TABLE IF NOT EXISTS Enumerations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                filePath TEXT NOT NULL,
                size LONG NOT NULL,
                packageId INTEGER,
                checksum TEXT,
                FOREIGN KEY (packageId) REFERENCES Packages(id) ON DELETE CASCADE,
                FOREIGN KEY (checksum) REFERENCES Files(checksum) ON DELETE CASCADE,
                UNIQUE (name, filePath, packageId, checksum) 
            );
        """.trimIndent(),

        "Method" to """
            CREATE TABLE IF NOT EXISTS Methods (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                classId INTEGER,
                interfaceId INTEGER,
                FOREIGN KEY (classId) REFERENCES Classes(id),
                FOREIGN KEY (interfaceId) REFERENCES Interfaces(id),
                UNIQUE (name, classId),
                UNIQUE (name, interfaceId) 
            );
        """.trimIndent(),

        "PackageRelationship" to """
            CREATE TABLE IF NOT EXISTS PackageRelationship (
                packageParentId INTEGER,
                packageChildId INTEGER,
                modelId INTEGER,
                FOREIGN KEY (packageParentId) REFERENCES Packages(id),
                FOREIGN KEY (packageChildId) REFERENCES Packages(id),
                FOREIGN KEY (modelId) REFERENCES Models(id) ON DELETE CASCADE,
                UNIQUE (packageParentId, packageChildId, modelId) 
            );
        """.trimIndent(),

        "ClassRelationship" to """
            CREATE TABLE IF NOT EXISTS ClassRelationship (
                classId INTEGER,
                interfaceId INTEGER,
                parentClassId INTEGER,
                FOREIGN KEY (classId) REFERENCES Classes(id),
                FOREIGN KEY (interfaceId) REFERENCES Interfaces(id),
                FOREIGN KEY (parentClassId) REFERENCES Classes(id),
                UNIQUE (classId, interfaceId), 
                UNIQUE (classId,parentClassId) 
            );
        """.trimIndent(),

        "InterfaceRelationship" to """
            CREATE TABLE IF NOT EXISTS InterfaceRelationship (
                interfaceId INTEGER,
                parentInterfaceId INTEGER,
                FOREIGN KEY (interfaceId) REFERENCES Interfaces(id),
                FOREIGN KEY (parentInterfaceId) REFERENCES Interfaces(id),
                UNIQUE (interfaceId, parentInterfaceId) 
            );
        """.trimIndent(),

        "Projects" to """
            CREATE TABLE IF NOT EXISTS Projects (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                filePath TEXT NOT NULL,
                UNIQUE (name, filePath)
            );
        """.trimIndent(),

        "ImportedClasses" to """
            CREATE TABLE IF NOT EXISTS ImportedClasses (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                classId INTEGER,
                packageId INTEGER,
                FOREIGN KEY (classId) REFERENCES Classes(id),
                FOREIGN KEY (packageId) REFERENCES Packages(id),
                UNIQUE (name, classId) 
            );
        """.trimIndent(),

        "Files" to """
            CREATE TABLE IF NOT EXISTS Files (
                checksum TEXT PRIMARY KEY,
                fileName TEXT NOT NULL,
                projectId INTEGER NOT NULL,
                FOREIGN KEY (projectId) REFERENCES Projects(id),
                UNIQUE (checksum, projectId)
            );
        """.trimIndent(),

        "FileModelRelations" to """
            CREATE TABLE IF NOT EXISTS FileModelRelations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                checksum TEXT,
                modelId INTEGER,
                FOREIGN KEY (checksum) REFERENCES Files(checksum),
                FOREIGN KEY (modelId) REFERENCES Models(id) ON DELETE CASCADE,
                UNIQUE (checksum, modelId)
            );
        """.trimIndent(),

        "FilePaths" to """
            CREATE TABLE IF NOT EXISTS FilePaths (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                checksum TEXT,
                filePath TEXT NOT NULL,
                FOREIGN KEY (checksum) REFERENCES Files(checksum),
                UNIQUE (checksum, filePath)
            );
        """.trimIndent(),
    )

    // Establish database connection and execute table creation queries
    try {
        connection.createStatement().use { stmt ->
                for ((tableName, sqlQuery) in tableCreationQueries) {
                    stmt.execute(sqlQuery)
                    log.info("Table $tableName created successfully.")
                }
        }
    } catch (e: SQLException) {
        log.error("Error creating tables: ${e.message}")
    }
}

/**
 * Drops tables from the SQLite database if they exist.
 *
 * @param url JDBC connection URL to the database.
 */
fun dropTables(url: String) {
    // List of table names to drop
    val tableNames = listOf(
        "Models", "Packages", "Class", "Interface", "Enumeration", "Method", "PackageRelationship", "ClassRelationship",
        "InterfaceRelationship", "Projects", "Files", "FilePaths", "FileModelRelations", "ModelPackageRelations",
        "PackageChecksumRelations"
    )

    try {
        DriverManager.getConnection(url).use { conn ->
            conn.createStatement().use { stmt ->
                for (table in tableNames) {
                    stmt.execute("DROP TABLE IF EXISTS $table")
                    log.info("Dropped table: $table")
                }
            }
        }
    } catch (e: SQLException) {
        log.error("Error dropping tables: ${e.message}")
    }
}

/**
 * Sets an integer parameter in a prepared statement, allowing null values.
 *
 * @param index The index of the parameter.
 * @param value The integer value to set (or null).
 */
fun PreparedStatement.setIntOrNull(index: Int, value: Int?) {
    if (value != null) {
        this.setInt(index, value)
    } else {
        this.setNull(index, java.sql.Types.INTEGER)
    }
}
