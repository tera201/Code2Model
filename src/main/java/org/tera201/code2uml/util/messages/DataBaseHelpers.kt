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
            UNIQUE (name, filePath)
        );
    """.trimIndent()

    val sqlCreatePackage = """
        CREATE TABLE IF NOT EXISTS Packages (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            package TEXT NOT NULL,
            filePath TEXT NOT NULL,
            size LONG NOT NULL,
            modelId INTEGER,
            FOREIGN KEY (modelId) REFERENCES Models(id),
            UNIQUE (name, package, filePath, modelId) 
            
        );
    """.trimIndent()

    val sqlCreateClass = """
        CREATE TABLE IF NOT EXISTS Classes (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            filePath TEXT NOT NULL,
            size LONG NOT NULL,
            modelId INTEGER,
            packageId INTEGER,
            type INTEGER,
            modificator INTEGER,
            FOREIGN KEY (modelId) REFERENCES Models(id),
            FOREIGN KEY (packageId) REFERENCES Packages(id),
            UNIQUE (name, filePath, modelId, packageId) 
        );
    """.trimIndent()

    val sqlCreateInterface = """
        CREATE TABLE IF NOT EXISTS Interfaces (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            filePath TEXT NOT NULL,
            size LONG NOT NULL,
            modelId INTEGER,
            packageId INTEGER,
            FOREIGN KEY (modelId) REFERENCES Models(id),
            FOREIGN KEY (packageId) REFERENCES Packages(id),
            UNIQUE (name, filePath, modelId, packageId) 
        );
    """.trimIndent()

    val sqlCreateEnumeration = """
        CREATE TABLE IF NOT EXISTS Enumerations (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            filePath TEXT NOT NULL,
            size LONG NOT NULL,
            modelId INTEGER,
            packageId INTEGER,
            FOREIGN KEY (modelId) REFERENCES Models(id),
            FOREIGN KEY (packageId) REFERENCES Packages(id),
            UNIQUE (name, filePath, modelId, packageId) 
        );
    """.trimIndent()

    val sqlCreatePackageRelationship = """
        CREATE TABLE IF NOT EXISTS PackageRelationship (
            packageParentId INTEGER,
            packageChildId INTEGER,
            FOREIGN KEY (packageParentId) REFERENCES Packages(id),
            FOREIGN KEY (packageChildId) REFERENCES Packages(id),
            UNIQUE (packageParentId, packageChildId) 
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

    try {
        DriverManager.getConnection(url).use { conn ->
            conn.createStatement().use { stmt ->
                stmt.execute(sqlCreateModel)
                stmt.execute(sqlCreatePackage)
                stmt.execute(sqlCreateClass)
                stmt.execute(sqlCreateInterface)
                stmt.execute(sqlCreateEnumeration)
                stmt.execute(sqlCreatePackageRelationship)
                stmt.execute(sqlCreateClassRelationship)
                stmt.execute(sqlCreateInterfaceRelationship)
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