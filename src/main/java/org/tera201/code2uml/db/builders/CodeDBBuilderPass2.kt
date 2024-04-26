package org.tera201.code2uml.uml.builders

import org.eclipse.emf.common.util.BasicEList
import org.eclipse.emf.common.util.EList
import org.eclipse.uml2.uml.*
import org.eclipse.uml2.uml.internal.impl.UMLFactoryImpl
import org.tera201.code2uml.uml.DBBuilder
import org.tera201.code2uml.uml.IUMLBuilder
import org.tera201.code2uml.uml.helpers.BuilderClass
import org.tera201.code2uml.uml.helpers.BuilderInterface
import org.tera201.code2uml.uml.util.UMLUtil
import org.tera201.code2uml.util.messages.DataBaseUtil
import org.tera201.code2uml.util.messages.IMessageHandler
import java.util.*

class CodeDBBuilderPass2(override val model: Int, val mh: IMessageHandler, override val dataBaseUtil: DataBaseUtil) : DBBuilder {
    private var currentPackage: Int? = null
    private var currentClass: Int? = null
    private var currentNestedClass: Int? = null
    private var currentInterface: Int? = null
    private var currentOwner: Int? = null
    private var packageStackId: Stack<String> = Stack()
    private var packageStackName: Stack<String> = Stack()
    private val umlFactoryImpl = UMLFactoryImpl()

    override fun setName(modelName: String) {}

    override fun startPackage(packageName: String, byteSize: Int?, filePath: String) {
        if (packageStackName.empty()) packageStackName.push(packageName)
        else packageStackName.push("${packageStackName.peek()}.$packageName")
        currentPackage = dataBaseUtil.getPackageIdByPackage(packageStackName.peek())
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
//        currentInterface = UMLUtil.getInterface(currentPackage, interfaceBuilderInterface.name)
//        interfaceBuilderInterface.parentsNameList?.forEach {
//            val parent: Interface = UMLUtil.getInterface(currentPackage, it)
//            currentInterface!!.createGeneralization(parent)
//        }
//        currentOwner = currentPackage.getOwnedMember(interfaceBuilderInterface.name)
    }

    override fun endInterface() {}
    override fun startEnumeration(enumerationName: String, filePath: String) {}
    override fun endEnumeration() {}

    override fun addAttribute(attributeName: String, typeName: String): Property? {
        var property: Property? = null
//        val type: Type? = UMLUtil.getType(model, typeName)
//
//        if (type != null) {
//            property = UMLFactory.eINSTANCE.createProperty()
//            property.type = type
//            property.name = typeName
//            if (currentOwner?.name  == currentClass?.name) currentClass?.createOwnedAttribute(attributeName, type)
//            else if (currentOwner?.name  == currentInterface?.name) currentInterface?.
//                createOwnedAttribute(attributeName, type)
//            else if (currentOwner == null && currentNestedClass != null) currentNestedClass?.
//                createOwnedAttribute(attributeName, type)
//        }
        return property
    }

    override fun startMethod(funType: String, funName: String, typeList: EList<String>, argList: EList<String>, isVirtual: Boolean): Operation? {
        val op: Operation? = null
//        val type: Type? = UMLUtil.getType(model, funType)
//        val types: BasicEList<Type> = BasicEList()
//        for (i in typeList){
//            types.add(UMLUtil.getType(model, i))
//        }
//        val current = if (currentOwner is Class) currentOwner as Class else if (currentOwner is Interface) currentOwner as Interface else currentNestedClass
//        if (type != null) {
//            op = current?.createOwnedOperation(funName, argList, types, type)
//        }
//        else {
//            op = current?.createOwnedOperation(funName, argList, types)
//        }
//        if (isVirtual) op?.setIsAbstract(isVirtual);
//        current?.ownedComments!![1].setBody((current.ownedComments!![1].body.toInt() + 1).toString())
////        }
        return op
    }

    override fun  addClassSize(byteSize: Int?) {
//        currentOwner?.ownedComments?.get(0)?.setBody((currentOwner!!.ownedComments[0].body.toInt() + byteSize!!).toString())
    }
    override fun addParameter(parName: String, typeName: String) {}
    override fun endMethod() {}
}