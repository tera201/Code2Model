package org.tera201.code2uml.util.messages

import org.tera201.code2uml.uml.db.ClassDB
import org.tera201.code2uml.uml.db.EnumerationDB
import org.tera201.code2uml.uml.db.InterfaceDB
import org.tera201.code2uml.uml.db.PackageDB



fun DataBaseUtil.getPackage(packageId: Int):PackageDB {
    val sqlPackage = "SELECT * FROM Packages WHERE id = ?"
    var packageDB = PackageDB(packageId, "", "", "", 0L)

    conn.prepareStatement(sqlPackage).use { pstmt ->
        pstmt.setInt(1, packageId)
        pstmt.executeQuery().use { rs ->
            while (rs.next()) {
                packageDB = PackageDB(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                    packageName = rs.getString("package"),
                    filePath = rs.getString("filePath"),
                    size = rs.getLong("size"),
                )
            }
        }
    }

    val sqlMethods = "SELECT packageParentId, packageChildId FROM PackageRelationship WHERE packageParentId = ? OR packageChildId = ?"
    conn.prepareStatement(sqlMethods).use { pstmt ->
        pstmt.setInt(1, packageId)
        pstmt.setInt(2, packageId)
        pstmt.executeQuery().use { rs ->
            while (rs.next()) {
                val parent = rs.getInt("packageParentId")
                val child = rs.getInt("packageChildId")
                if (parent == packageId) {
                    packageDB.childrenId.add(child)
                } else {
                    packageDB.parentId.add(parent)
                }
            }
        }
    }
    return packageDB

}

fun DataBaseUtil.getClass(classId: Int): ClassDB {

    val sqlPackage = "SELECT * FROM Classes WHERE id = ?"
    var classDB = ClassDB(classId, "", "", 0L, "", 0, 0)
    conn.prepareStatement(sqlPackage).use { pstmt ->
        pstmt.setInt(1, classId)
        pstmt.executeQuery().use { rs ->
            while (rs.next()) {
                classDB = ClassDB(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                    filePath = rs.getString("filePath"),
                    size = rs.getLong("size"),
                    getPackage(rs.getInt("packageId")).packageName,
                    type = rs.getInt("type"),
                    modificator = rs.getInt("modificator"),
                    methodCount = getMethodsCountForClass(classId)
                )
            }
        }
    }
    val sqlMethods = "SELECT * FROM ClassRelationship WHERE classId = ?"
    conn.prepareStatement(sqlMethods).use { pstmt ->
        pstmt.setInt(1, classId)
        pstmt.executeQuery().use { rs ->
            while (rs.next()) {
                val interfaceId = rs.getInt("interfaceId")
                rs.wasNull().not().apply { classDB.interfaceIdList.add(interfaceId) }
                val parent = rs.getInt("parentClassId")
                rs.wasNull().not().apply { classDB.parentClassIdList.add(parent) }
            }
        }
    }
    return classDB
}

fun DataBaseUtil.getEnumerations(enumerationId: Int):EnumerationDB {

    val sqlPackage = "SELECT * FROM Enumerations WHERE id = ?"
    var enumerationDB = EnumerationDB(enumerationId, "", "", 0L, "")

    conn.prepareStatement(sqlPackage).use { pstmt ->
        pstmt.setInt(1, enumerationId)
        pstmt.executeQuery().use { rs ->
            while (rs.next()) {
                enumerationDB = EnumerationDB(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                    filePath = rs.getString("filePath"),
                    size = rs.getLong("size"),
                    getPackage(rs.getInt("packageId")).packageName
                )
            }
        }
    }
    return enumerationDB
}

fun DataBaseUtil.getInterface(interfaceId: Int): InterfaceDB {
    val sqlPackage = "SELECT * FROM Interfaces WHERE id = ?"
    var interfaceDB = InterfaceDB(interfaceId, "", "", 0L, "")
    conn.prepareStatement(sqlPackage).use { pstmt ->
        pstmt.setInt(1, interfaceId)
        pstmt.executeQuery().use { rs ->
            while (rs.next()) {
                interfaceDB = InterfaceDB(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                    filePath = rs.getString("filePath"),
                    size = rs.getLong("size"),
                    getPackage(rs.getInt("packageId")).packageName,
                    methodCount = getMethodsCountForInterface(interfaceId)
                )
            }
        }
    }
    val sqlMethods = "SELECT * FROM InterfaceRelationship WHERE interfaceId = ?"
    conn.prepareStatement(sqlMethods).use { pstmt ->
        pstmt.setInt(1, interfaceId)
        pstmt.executeQuery().use { rs ->
            while (rs.next()) {
                val parent = rs.getInt("parentInterfaceId")
                interfaceDB.extendsId.add(parent)
                }
            }
    }
    return interfaceDB
}

fun DataBaseUtil.getRootPackageIds(modelId: Int): List<Int> {
    val packages = mutableListOf<Int>()
    val sql = """
        SELECT p.*
        FROM Packages p
        LEFT JOIN PackageRelationship pr ON p.id = pr.packageChildId
        WHERE p.modelId = ? AND pr.packageChildId IS NULL;
    """
    conn.prepareStatement(sql).use { pstmt ->
        pstmt.setInt(1, modelId)
        pstmt.executeQuery().use { rs ->
            while (rs.next()) {
                packages.add(rs.getInt("id"))
            }
        }
    }

    return packages
}

fun DataBaseUtil.getMethodsCountForClass(classId: Int): Int {
    return getMethodsCount(classId, "classId")
}
fun DataBaseUtil.getMethodsCountForInterface(interfaceId: Int): Int {
    return getMethodsCount(interfaceId, "interfaceId")
}
fun DataBaseUtil.getMethodsCount(id: Int, param:String): Int {
    val sql = "SELECT COUNT(*) FROM Methods WHERE $param = ?"
    conn.prepareStatement(sql).use { pstmt ->
        pstmt.setInt(1, id)
        pstmt.executeQuery().use { rs ->
            if (rs.next()) {
                return rs.getInt(1)  // Получение результата подсчета
            }
        }
    }
    return 0
}