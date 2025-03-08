package org.tera201.code2uml.db.builders

import org.eclipse.emf.common.util.EList
import org.tera201.code2uml.uml.DBBuilder
import org.tera201.code2uml.uml.helpers.BuilderClass
import org.tera201.code2uml.uml.helpers.BuilderInterface
import org.tera201.code2uml.util.messages.DataBaseUtil
import java.util.*


class CodeDBBuilderPass1(override val projectId: Int, override val model: Int, override val dataBaseUtil: DataBaseUtil) : DBBuilder {
    private var currentPackage: Int? = null
    private var currentClass: Int? = null
    private var currentInterface: Int? = null
    private var packageStackId: Stack<Int> = Stack()
    private var packageStackName: Stack<String> = Stack()

    override fun setName(modelName: String) {}

    override fun startPackage(packageName: String, byteSize: Int?, filePath: String, checksum: String) {
        if (packageStackName.empty()) packageStackName.push(packageName)
        else packageStackName.push("${packageStackName.peek()}.$packageName")
        val oldPackage = currentPackage

        currentPackage = dataBaseUtil.getPackageIdByPackageName(packageStackName.peek(), projectId)

        if (currentPackage == -1) {
            currentPackage = dataBaseUtil.insertPackageAndGetId(
                packageName,
                packageStackName.peek(),
                byteSize!!.toLong(),
                projectId
            )
        }

        dataBaseUtil.insertModelPackageRelation(model, currentPackage!!)
        dataBaseUtil.insertPackageChecksumRelation(currentPackage!!, checksum)

        packageStackId.push(currentPackage)
        if(oldPackage != null) dataBaseUtil.insertPackageRelationShip(oldPackage, currentPackage!!, model)
    }

    override fun endPackage() {
        packageStackId.pop()
        packageStackName.pop()
        currentPackage = if (!packageStackId.empty()) packageStackId.peek() else null
    }

    override fun startClass(builderClass: BuilderClass, filePath: String, checksum: String) {
        if (!builderClass.isNested) {
            currentClass = dataBaseUtil.getClassId(builderClass.name, filePath, checksum)
            if (currentClass == -1) {
                currentPackage?.let {currentClass = dataBaseUtil.insertClassAndGetId(builderClass.name, filePath, 0, it, 0, 0, checksum, null) }
            }
        } else {
            currentPackage?.let {dataBaseUtil.insertClassAndGetId(builderClass.name, filePath, 0, it, 0, 0, checksum, currentClass) }
        }
    }

    override fun endClass() {}

    override fun startInterface(interfaceBuilderInterface: BuilderInterface, filePath: String, checksum: String) {
        currentPackage?.let {
            currentInterface = dataBaseUtil.getInterfaceId(interfaceBuilderInterface.name, filePath, it, checksum)
            if (currentInterface == -1) {
                currentInterface =
                    dataBaseUtil.insertInterfaceAndGetId(interfaceBuilderInterface.name, filePath, 0, it, checksum)
            }
        }
    }

    override fun endInterface() {}
    override fun startEnumeration(enumerationName: String, filePath: String, checksum: String) {
        val enumerationId = dataBaseUtil.getEnumerationId(enumerationName, filePath, checksum)
        if (enumerationId == -1) {
            currentPackage?.let { dataBaseUtil.insertEnumerationAndGetId(enumerationName, filePath, 0, it, checksum) }
        }
    }

    override fun endEnumeration() {}

    override fun addAttribute(attributeName: String, typeName: String) {}

    override fun startMethod(funType: String, funName: String, typeList: EList<String>, argList: EList<String>, isVirtual: Boolean) {}
    override fun addParameter(parName: String, typeName: String) {}
    override fun endMethod() {}
    override fun addClassSize(byteSize: Int) {}
}