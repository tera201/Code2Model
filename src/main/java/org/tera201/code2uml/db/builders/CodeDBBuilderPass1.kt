package org.tera201.code2uml.uml.builders

import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EAnnotation
import org.eclipse.emf.ecore.EcoreFactory
import org.eclipse.uml2.uml.*
import org.tera201.code2uml.uml.DBBuilder
import org.tera201.code2uml.uml.IUMLBuilder
import org.tera201.code2uml.uml.helpers.BuilderClass
import org.tera201.code2uml.uml.helpers.BuilderInterface
import org.tera201.code2uml.uml.util.UMLUtil
import org.tera201.code2uml.util.messages.DataBaseUtil
import org.tera201.code2uml.util.messages.IMessageHandler
import java.util.*


class CodeDBBuilderPass1(override val model: Int, val mh: IMessageHandler, override val dataBaseUtil: DataBaseUtil) : DBBuilder {
    private var currentPackage: Int? = null
    private var currentClass: Int? = null
    private var currentInterface: Int? = null
    private var packageStackId: Stack<String> = Stack()
    private var packageStackName: Stack<String> = Stack()

    override fun setName(modelName: String) {
//        model.name = modelName
    }

    override fun startPackage(packageName: String, byteSize: Int?, filePath: String) {
        if (packageStackName.empty()) packageStackName.push(packageName)
        else packageStackName.push("${packageStackName.peek()}.$packageName")
        val findId = dataBaseUtil.getPackageIdByPackage(packageStackName.peek())

        if (findId != null) {
            currentPackage = findId
            if (packageStackId.empty()) packageStackId.push(findId.toString())
            else packageStackId.push("${packageStackId.peek()}.$findId")
        } else {
            val packageId = dataBaseUtil.insertPackageAndGetId(
                packageName,
                packageStackName.peek(),
                filePath.substringBeforeLast("/"),
                byteSize!!.toLong(),
                model
            )
            if(currentPackage != null) dataBaseUtil.insertPackageRelationShip(currentPackage!!, packageId)
            if (packageStackId.empty()) packageStackId.push(packageId.toString())
            else packageStackId.push("${packageStackId.peek()}.$packageId")
            currentPackage = packageId
        }
    }

    override fun endPackage() {
        packageStackId.pop()
        packageStackName.pop()
        currentPackage = if (!packageStackId.empty())
            packageStackId.peek().split(".").get(packageStackId.size - 1).toInt()
        else null
    }

    override fun startClass(builderClass: BuilderClass, filePath: String) {
        if (!builderClass.isNested) {
            val classId = dataBaseUtil.getClassIdByNameAndFilePath(builderClass.name, filePath)
            if (classId == null) {
                currentPackage?.let {currentClass = dataBaseUtil.insertClassAndGetId(builderClass.name, filePath, 0, model, it, 0, 0) }
            }
        } else {
//            currentPackage?.let {dataBaseUtil.insertClassAndGetId(builderClass.name, filePath, 0, model, it, 0, 0) }
        }
    }

    override fun endClass() {}

    override fun startInterface(interfaceBuilderInterface: BuilderInterface, filePath: String) {
        val interfaceId = dataBaseUtil.getInterfaceIdByNameAndFilePath(interfaceBuilderInterface.name, filePath)
        if (interfaceId == null) {
            currentPackage?.let {currentInterface = dataBaseUtil.insertInterfaceAndGetId(interfaceBuilderInterface.name, filePath, 0, model, it) }
        }
    }

    override fun endInterface() {}
    override fun startEnumeration(enumerationName: String, filePath: String) {
        val enumerationId = dataBaseUtil.getEnumerationIdByNameAndFilePath(enumerationName, filePath)
        if (enumerationId == null) {
            currentPackage?.let { dataBaseUtil.insertEnumerationAndGetId(enumerationName, filePath, 0, model, it) }
        }
    }

    override fun endEnumeration() {}

    override fun addAttribute(attributeName: String, typeName: String): Property? {
//        UMLUtil.getType(model, typeName)
        return null
    }

    override fun startMethod(funType: String, funName: String, typeList: EList<String>, argList: EList<String>, isVirtual: Boolean): Operation? = null
    override fun addParameter(parName: String, typeName: String) {}
    override fun endMethod() {}
    override fun addClassSize(byteSize: Int?) {}

    private fun getPathAnnotation(filePath: String): EAnnotation {
        val annotation = EcoreFactory.eINSTANCE.createEAnnotation()
        annotation.source = "ResourcePath"
        annotation.getDetails().put("path", filePath);
        return annotation
    }
}