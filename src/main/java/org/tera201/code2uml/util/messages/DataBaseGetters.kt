package org.tera201.code2uml.util.messages

import org.tera201.code2uml.uml.db.*


fun DataBaseUtil.getModel(id: Int):ModelDB {

    val sqlPackage = "SELECT * FROM Models WHERE id = ?"
    var modelDB = ModelDB(id, "", "")

    conn.prepareStatement(sqlPackage).use { pstmt ->
        pstmt.setInt(1, id)
        pstmt.executeQuery().use { rs ->
            while (rs.next()) {
                modelDB = ModelDB(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                    filePath = rs.getString("filePath"),
                )
            }
        }
    }
    return modelDB
}

fun DataBaseUtil.getPackage(packageId: Int, modelId: Int):PackageDB {
    val sqlPackage = "SELECT * FROM Packages WHERE id = ?"
    var packageDB = PackageDB(packageId, "", "", 0L)

    conn.prepareStatement(sqlPackage).use { pstmt ->
        pstmt.setInt(1, packageId)
        pstmt.executeQuery().use { rs ->
            while (rs.next()) {
                packageDB = PackageDB(
                    id = rs.getInt("id"),
                    name = rs.getString("name"),
                    packageName = rs.getString("package"),
                    size = rs.getLong("size"),
                )
            }
        }
    }

    val sqlMethods = "SELECT packageParentId, packageChildId FROM PackageRelationship WHERE modelId = ? AND (packageParentId = ? OR packageChildId = ?)"
    conn.prepareStatement(sqlMethods).use { pstmt ->
        pstmt.setInt(1, modelId)
        pstmt.setInt(2, packageId)
        pstmt.setInt(3, packageId)
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

    val sqlChecksum = "SELECT checksum FROM PackageChecksumRelations WHERE packageId = ?"
    conn.prepareStatement(sqlChecksum).use { pstmt ->
        pstmt.setInt(1, packageId)
        pstmt.executeQuery().use { rs ->
            while (rs.next()) {
                packageDB.checksumList.add(rs.getString("checksum"))
            }
        }
    }
    return packageDB

}

fun DataBaseUtil.getClass(classId: Int, modelId: Int): ClassDB {

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
                    getPackage(rs.getInt("packageId"), modelId).packageName,
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
                if (rs.wasNull().not().and(interfaceId != 0)) { classDB.interfaceIdList.add(interfaceId) }
                val parent = rs.getInt("parentClassId")
               if (rs.wasNull().not().and(parent != 0))  { classDB.parentClassIdList.add(parent) }
            }
        }
    }
    return classDB
}

fun DataBaseUtil.getEnumerations(enumerationId: Int, modelId: Int):EnumerationDB {

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
                    getPackage(rs.getInt("packageId"), modelId).packageName
                )
            }
        }
    }
    return enumerationDB
}

fun DataBaseUtil.getInterface(interfaceId: Int, modelId: Int): InterfaceDB {
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
                    getPackage(rs.getInt("packageId"), modelId).packageName,
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
        SELECT DISTINCT p.*
        FROM Packages p
        LEFT JOIN PackageRelationship pr ON p.id = pr.packageChildId AND pr.modelId = ?
        LEFT JOIN ModelPackageRelations mpr On p.id = mpr.packageId
        WHERE pr.packageChildId IS NULL AND mpr.modelId = ?;
    """
    conn.prepareStatement(sql).use { pstmt ->
        pstmt.setInt(1, modelId)
        pstmt.setInt(2, modelId)
        pstmt.executeQuery().use { rs ->
            while (rs.next()) {
                packages.add(rs.getInt("id"))
            }
        }
    }

    return packages
}

fun DataBaseUtil.getClassIdsByPackageId(modelId: Int, packageId: Int): List<Int> {
    return getIdsByPackageId(modelId, packageId, "Classes")
}

fun DataBaseUtil.getInterfacesIdsByPackageId(modelId: Int, packageId: Int): List<Int> {
    return getIdsByPackageId(modelId, packageId, "Interfaces")
}

fun DataBaseUtil.getEnumerationsIdsByPackageId(modelId: Int, packageId: Int): List<Int> {
    return getIdsByPackageId(modelId, packageId, "Enumerations")
}

private fun DataBaseUtil.getIdsByPackageId(modelId: Int, packageId: Int, table: String): List<Int> {
    val objectIds = mutableListOf<Int>()
    val sql = "SELECT tb.* FROM $table tb LEFT JOIN FileModelRelations fmr on tb.checksum = fmr.checksum WHERE packageId = ? and modelId = ?"
    conn.prepareStatement(sql).use { pstmt ->
        pstmt.setInt(1, packageId)
        pstmt.setInt(2, modelId)
        pstmt.executeQuery().use { rs ->
            while (rs.next()) {
                objectIds.add(rs.getInt("id"))
            }
        }
    }
    return objectIds
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