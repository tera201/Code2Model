package org.tera201.code2uml.db.builders

import org.tera201.code2uml.db.DBBuilder
import org.tera201.code2uml.uml.helpers.BuilderClass
import org.tera201.code2uml.uml.helpers.BuilderInterface
import org.tera201.code2uml.util.messages.DataBaseUtil
import java.util.*

class CodeDBBuilderPass2(override val projectId: Int, override val model: Int, override val dataBaseUtil: DataBaseUtil) :
    DBBuilder {
    private var currentPackage: Int? = null
    private var currentClass: Int? = null
    private var currentNestedClass: Int? = null
    private var currentInterface: Int? = null
    private var currentOwner: Type? = null
    private var packageStackId: Stack<Int> = Stack()
    private var packageStackName: Stack<String> = Stack()

    override fun setName(modelName: String) {}

    override fun startPackage(packageName: String, byteSize: Int?, filePath: String, checksum: String) {
        if (packageStackName.empty()) packageStackName.push(packageName)
        else packageStackName.push("${packageStackName.peek()}.$packageName")
        currentPackage = dataBaseUtil.getPackageIdByPackageName(packageStackName.peek(), projectId)
        packageStackId.push(currentPackage)
    }

    override fun endPackage() {
        packageStackId.pop()
        packageStackName.pop()
        currentPackage = if (!packageStackId.empty()) packageStackId.peek() else null
    }

    override fun startClass(builderClass: BuilderClass, filePath: String, checksum: String) {
        currentClass = dataBaseUtil.getClassId(builderClass.name, filePath, checksum).takeIf { it != -1 }
        if (currentClass == null) return
        currentOwner = Type.CLASS
        builderClass.interfaceList?.forEach {
            val interfaceId = dataBaseUtil.getInterfaceIdByName(it.substringBefore("<"))
            if (interfaceId == -1) dataBaseUtil.insertImportedClass(it.substringBefore("<"), currentClass!!, currentPackage!!)
            else dataBaseUtil.insertClassRelationShip(currentClass!!, interfaceId, null)
        }
        if (builderClass.parentName != null) {
            val parentClassId = dataBaseUtil.getClassIdByName(builderClass.parentName.substringBefore("<"))
            if (parentClassId == -1) dataBaseUtil.insertImportedClass(builderClass.parentName.substringBefore("<"), currentClass!!, currentPackage!!)
            else dataBaseUtil.insertClassRelationShip(currentClass!!, null, parentClassId)
        }
    }

    override fun endClass() {}

    override fun startInterface(interfaceBuilderInterface: BuilderInterface, filePath: String, checksum: String) {
        currentInterface = currentPackage?.let {dataBaseUtil.getInterfaceId(interfaceBuilderInterface.name, filePath, it, checksum)}.takeIf { it != -1 }
        currentOwner = Type.INTERFACE
        interfaceBuilderInterface.parentsNameList?.forEach {
            val interfaceId = dataBaseUtil.getInterfaceIdByName(it)
            currentInterface?.let { it1 ->
                if (interfaceId != -1) {
                    dataBaseUtil.insertInterfaceRelationShip(it1, interfaceId)
                }
            }
        }
    }

    override fun endInterface() {}
    override fun startEnumeration(enumerationName: String, filePath: String, checksum: String) {}
    override fun endEnumeration() {}

    override fun addAttribute(attributeName: String, typeName: String) {}

    override fun startMethod(funType: String, funName: String, typeList: List<String>, argList: List<String>, isVirtual: Boolean) {
        if(currentOwner == Type.CLASS)
            currentPackage.takeIf { it != -1 }?.let { dataBaseUtil.insertMethod(funName, funType, currentClass, null) }
        else if (currentOwner == Type.INTERFACE)
            currentPackage.takeIf { it != -1 }?.let { dataBaseUtil.insertMethod(funName, funType, null, currentInterface) }
    }

    override fun  addClassSize(byteSize: Int) {
        if(currentOwner == Type.CLASS)
            currentClass.takeIf { it != -1 }?.let { dataBaseUtil.updateSizeForClass(it, byteSize) }
        else if (currentOwner == Type.INTERFACE)
            currentInterface.takeIf { it != -1 }?.let { dataBaseUtil.updateSizeForInterface(it, byteSize) }
    }
    override fun addParameter(parName: String, typeName: String) {}
    override fun endMethod() {}

    enum class Type {
        CLASS,
        INTERFACE
    }
}