package org.tera201.code2uml.db.builders

import org.eclipse.emf.common.util.EList
import org.eclipse.uml2.uml.internal.impl.UMLFactoryImpl
import org.tera201.code2uml.uml.DBBuilder
import org.tera201.code2uml.uml.helpers.BuilderClass
import org.tera201.code2uml.uml.helpers.BuilderInterface
import org.tera201.code2uml.util.messages.DataBaseUtil
import org.tera201.code2uml.util.messages.IMessageHandler
import java.util.*

class CodeDBBuilderPass2(override val model: Int, val mh: IMessageHandler, override val dataBaseUtil: DataBaseUtil) : DBBuilder {
    private var currentPackage: Int? = null
    private var currentClass: Int? = null
    private var currentNestedClass: Int? = null
    private var currentInterface: Int? = null
    private var currentOwner: Type? = null
    private var packageStackId: Stack<String> = Stack()
    private var packageStackName: Stack<String> = Stack()
    private val umlFactoryImpl = UMLFactoryImpl()

    override fun setName(modelName: String) {}

    override fun startPackage(packageName: String, byteSize: Int?, filePath: String) {
        if (packageStackName.empty()) packageStackName.push(packageName)
        else packageStackName.push("${packageStackName.peek()}.$packageName")
        currentPackage = dataBaseUtil.getPackageIdByPackageName(packageStackName.peek(), model)
        if (packageStackId.empty()) packageStackId.push(currentPackage.toString())
        else packageStackId.push("${packageStackId.peek()}.$currentPackage")
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
            currentClass = dataBaseUtil.getClassIdByNameAndFilePath(builderClass.name, filePath)
            currentOwner = Type.CLASS
            builderClass.interfaceList?.forEach {
                currentClass?.let { it1 -> dataBaseUtil.insertClassRelationShip(it1, dataBaseUtil.getInterfaceIdByName(it.substringBefore("<")), null) }
            }
//            currentClass = UMLUtil.getClass(currentPackage, builderClass.name)
//            currentClass?.setIsAbstract(builderClass.modifiers.isAbstract)
//            currentClass?.visibility = UMLUtil.returnModifier(builderClass.modifiers.visibility)
            if (builderClass.parentName != null) {
                currentClass?.let { dataBaseUtil.insertClassRelationShip(it, null, dataBaseUtil.getClassIdByName(builderClass.parentName.substringBefore("<"))) }
            }
        } else {
            currentOwner = null
//            val nestedClass: Class = umlFactoryImpl.createClass()
//            nestedClass.createOwnedComment().body = "0"
//            nestedClass.createOwnedComment().body = "0"
//            nestedClass.name = builderClass.name
//            currentNestedClass = nestedClass
//            currentClass?.nestedClassifiers?.add(nestedClass)
//            currentOwner = currentPackage.getOwnedMember(builderClass.name)
        }
    }

    override fun endClass() {}

    override fun startInterface(interfaceBuilderInterface: BuilderInterface, filePath: String) {
        currentInterface = dataBaseUtil.getInterfaceIdByNameAndFilePath(interfaceBuilderInterface.name, filePath)
        currentOwner = Type.INTERFACE
        interfaceBuilderInterface.parentsNameList?.forEach {
            val interfaceId = dataBaseUtil.getInterfaceIdByName(it)
            currentInterface?.let { it1 ->
                if (interfaceId != null) {
                    dataBaseUtil.insertInterfaceRelationShip(it1, interfaceId)
                }
            }
        }
    }

    override fun endInterface() {}
    override fun startEnumeration(enumerationName: String, filePath: String) {}
    override fun endEnumeration() {}

    override fun addAttribute(attributeName: String, typeName: String) {}

    override fun startMethod(funType: String, funName: String, typeList: EList<String>, argList: EList<String>, isVirtual: Boolean) {
        if(currentOwner == Type.CLASS)
            currentPackage?.let { dataBaseUtil.insertMethod(funName, funType, model, it, currentClass, null) }
        else if (currentOwner == Type.INTERFACE)
            currentPackage?.let { dataBaseUtil.insertMethod(funName, funType, model, it, null, currentInterface) }
    }

    override fun  addClassSize(byteSize: Int) {
        if(currentOwner == Type.CLASS)
            currentClass?.let { dataBaseUtil.updateSizeForClass(it, byteSize) }
        else if (currentOwner == Type.INTERFACE)
            currentInterface?.let { dataBaseUtil.updateSizeForInterface(it, byteSize) }
    }
    override fun addParameter(parName: String, typeName: String) {}
    override fun endMethod() {}
}

enum class Type {
    CLASS,
    INTERFACE
}