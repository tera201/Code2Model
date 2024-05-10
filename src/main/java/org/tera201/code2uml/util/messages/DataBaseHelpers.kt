package org.tera201.code2uml.util.messages

import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.SQLException

fun createTables(url: String) {
    // SQL statement for creating tables
    val sqlCreateModel = """
        CREATE TABLE IF NOT EXISTS Models (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            filePath TEXT NOT NULL,
            projectId INTEGER NOT NULL,
            FOREIGN KEY (projectId) REFERENCES Projects(id),
            UNIQUE (name, filePath, projectId)
        );
    """.trimIndent()

    val sqlCreatePackage = """
        CREATE TABLE IF NOT EXISTS Packages (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            package TEXT NOT NULL,
            size LONG NOT NULL,
            projectId INTEGER NOT NULL,
            FOREIGN KEY (projectId) REFERENCES Projects(id),
            UNIQUE (name, package, projectId) 
        );
    """.trimIndent()

    val sqlCreateModelPackageRelations = """
        CREATE TABLE IF NOT EXISTS ModelPackageRelations (
            modelId INTEGER,
            packageId INTEGER,
            FOREIGN KEY (modelId) REFERENCES Models(id) ON DELETE CASCADE,
            FOREIGN KEY (packageId) REFERENCES Packages(id),
            UNIQUE (modelId, packageId) 
        );
    """.trimIndent()

    val sqlCreatePackageChecksumRelations = """
        CREATE TABLE IF NOT EXISTS PackageChecksumRelations (
            packageId INTEGER,
            checksum TEXT,
            FOREIGN KEY (packageId) REFERENCES Packages(id),
            FOREIGN KEY (checksum) REFERENCES Files(checksum),
            UNIQUE (packageId, checksum) 
        );
    """.trimIndent()

    val sqlCreateClass = """
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
    """.trimIndent()

    val sqlCreateInterface = """
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
    """.trimIndent()

    val sqlCreateEnumeration = """
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
    """.trimIndent()

    val sqlCreateMethod = """
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
    """.trimIndent()

    val sqlCreatePackageRelationship = """
        CREATE TABLE IF NOT EXISTS PackageRelationship (
            packageParentId INTEGER,
            packageChildId INTEGER,
            modelId INTEGER,
            FOREIGN KEY (packageParentId) REFERENCES Packages(id),
            FOREIGN KEY (packageChildId) REFERENCES Packages(id),
            FOREIGN KEY (modelId) REFERENCES Models(id) ON DELETE CASCADE,
            UNIQUE (packageParentId, packageChildId, modelId) 
        );
    """.trimIndent()

    val sqlCreateClassRelationship = """
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
    """.trimIndent()

    val sqlCreateInterfaceRelationship = """
        CREATE TABLE IF NOT EXISTS InterfaceRelationship (
            interfaceId INTEGER,
            parentInterfaceId INTEGER,
            FOREIGN KEY (interfaceId) REFERENCES Interfaces(id),
            FOREIGN KEY (parentInterfaceId) REFERENCES Interfaces(id),
            UNIQUE (interfaceId, parentInterfaceId) 
        );
    """.trimIndent()

    val sqlCreateProjects = """
        CREATE TABLE IF NOT EXISTS Projects (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            filePath TEXT NOT NULL,
            UNIQUE (name, filePath)
        );
    """.trimIndent()

    val sqlCreateFiles = """
        CREATE TABLE IF NOT EXISTS Files (
            checksum TEXT PRIMARY KEY,
            fileName TEXT NOT NULL,
            projectId INTEGER NOT NULL,
            FOREIGN KEY (projectId) REFERENCES Projects(id),
            UNIQUE (checksum, projectId)
        );
    """.trimIndent()

    val sqlCreateFilePaths = """
        CREATE TABLE IF NOT EXISTS FilePaths (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            checksum TEXT,
            filePath TEXT NOT NULL,
            FOREIGN KEY (checksum) REFERENCES Files(checksum),
            UNIQUE (checksum, filePath)
        );
    """.trimIndent()

    val sqlCreateFileModelRelations = """
        CREATE TABLE IF NOT EXISTS FileModelRelations (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            checksum TEXT,
            modelId INTEGER,
            FOREIGN KEY (checksum) REFERENCES Files(checksum),
            FOREIGN KEY (modelId) REFERENCES Models(id) ON DELETE CASCADE,
            UNIQUE (checksum, modelId)
        );
    """.trimIndent()

    try {
        DriverManager.getConnection(url).use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute(sqlCreateModel)
                stmt.execute(sqlCreatePackage)
                stmt.execute(sqlCreateClass)
                stmt.execute(sqlCreateInterface)
                stmt.execute(sqlCreateEnumeration)
                stmt.execute(sqlCreateMethod)
                stmt.execute(sqlCreatePackageRelationship)
                stmt.execute(sqlCreateClassRelationship)
                stmt.execute(sqlCreateInterfaceRelationship)
                stmt.execute(sqlCreateProjects)
                stmt.execute(sqlCreateFiles)
                stmt.execute(sqlCreateFilePaths)
                stmt.execute(sqlCreateFileModelRelations)
                stmt.execute(sqlCreateModelPackageRelations)
                stmt.execute(sqlCreatePackageChecksumRelations)
                println("Tables have been created.")
            }
        }
    } catch (e: SQLException) {
        println(e.message)
    }
}

fun dropTables(url: String) {
    val sqlDropModels = "DROP TABLE IF EXISTS Models"
    val sqlDropPackages = "DROP TABLE IF EXISTS Packages"
    val sqlDropClasses = "DROP TABLE IF EXISTS Classes"
    val sqlDropInterfaces = "DROP TABLE IF EXISTS Interfaces"
    val sqlDropEnumerations = "DROP TABLE IF EXISTS Enumerations"
    val sqlDropMethods = "DROP TABLE IF EXISTS Methods"
    val sqlDropPackageRelationship = "DROP TABLE IF EXISTS PackageRelationship"
    val sqlDropClassRelationship = "DROP TABLE IF EXISTS ClassRelationship"
    val sqlDropInterfaceRelationship = "DROP TABLE IF EXISTS InterfaceRelationship"

    try {
        DriverManager.getConnection(url).use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute(sqlDropModels)
                stmt.execute(sqlDropPackages)
                stmt.execute(sqlDropClasses)
                stmt.execute(sqlDropInterfaces)
                stmt.execute(sqlDropEnumerations)
                stmt.execute(sqlDropMethods)
                stmt.execute(sqlDropPackageRelationship)
                stmt.execute(sqlDropClassRelationship)
                stmt.execute(sqlDropInterfaceRelationship)
                println("Tables have been created.")
            }
        }
    } catch (e: SQLException) {
        println(e.message)
    }
}

fun PreparedStatement.setIntOrNull(index: Int, value: Int?) {
    if (value != null) {
        this.setInt(index, value)
    } else {
        this.setNull(index, java.sql.Types.INTEGER)
    }
}