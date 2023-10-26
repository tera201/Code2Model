package uml.builders

import org.eclipse.emf.common.util.BasicEList
import org.eclipse.emf.common.util.EList
import org.eclipse.uml2.uml.*
import org.eclipse.uml2.uml.internal.impl.UMLFactoryImpl
import uml.IUMLBuilder
import uml.helpers.BuilderClass
import uml.helpers.BuilderInterface
import uml.util.UMLUtil
import util.messages.IMessageHandler
import java.util.*

class CPP14UMLBuilderPass2(override val model: Model, val mh: IMessageHandler) : IUMLBuilder {
    private var currentPackage: Package = model
    private var currentClass: Class? = null
    private var currentNestedClass: Class? = null
    private var currentInterface: Interface? = null
    private var currentOwner: NamedElement? = null
    private var packageStack: Stack<String> = Stack()
    private val umlFactoryImpl = UMLFactoryImpl()

    override fun setName(modelName: String) {}

    override fun startPackage(packageName: String, byteSize: Int?) {
        if (packageStack.empty()) packageStack.push(packageName)
        else packageStack.push("${packageStack.peek()}.$packageName")
        currentPackage = UMLUtil.getPackage(model, packageStack.peek())
    }

    override fun endPackage() {
        packageStack.pop()
        currentPackage = if (!packageStack.empty())
            UMLUtil.getPackage(model, packageStack.peek())
        else model
    }

    override fun startClass(builderClass: BuilderClass) {
        if (!builderClass.isNested) {
            currentClass = UMLUtil.getClass(currentPackage, builderClass.name)
            currentClass?.setIsAbstract(builderClass.modifiers.isAbstract)
            currentClass?.visibility = UMLUtil.returnModifier(builderClass.modifiers.visibility)
            if (builderClass.parentName != null) {
                val parent: Class = umlFactoryImpl.createClass()
                parent.name = builderClass.parentName
                currentClass!!.createGeneralization(parent)
            }
            builderClass.interfaceList?.forEach {
                val interfaceRealization = umlFactoryImpl.createInterfaceRealization()
                val interfaceVar = umlFactoryImpl.createInterface()
                interfaceVar.name = it
                interfaceRealization.contract = interfaceVar
                currentClass?.interfaceRealizations?.add(interfaceRealization)
            }
            currentOwner = currentPackage.getOwnedMember(builderClass.name)
        } else {
            val nestedClass: Class = umlFactoryImpl.createClass()
            nestedClass.createOwnedComment().body = "0"
            nestedClass.createOwnedComment().body = "0"
            nestedClass.name = builderClass.name
            currentNestedClass = nestedClass
            currentClass?.nestedClassifiers?.add(nestedClass)
            currentOwner = currentPackage.getOwnedMember(builderClass.name)
        }
    }

    override fun endClass() {}

    override fun startInterface(interfaceBuilderInterface: BuilderInterface) {
        currentInterface = UMLUtil.getInterface(currentPackage, interfaceBuilderInterface.name)
        interfaceBuilderInterface.parentsNameList?.forEach {
            val parent: Interface = UMLUtil.getInterface(currentPackage, it)
            currentInterface!!.createGeneralization(parent)
        }
        currentOwner = currentPackage.getOwnedMember(interfaceBuilderInterface.name)
    }

    override fun endInterface() {}
    override fun startEnumeration(enumerationName: String) {}
    override fun endEnumeration() {}

    override fun addAttribute(attributeName: String, typeName: String): Property? {
        var property: Property? = null
        val type: Type? = UMLUtil.getType(model, typeName)

        if (type != null) {
            property = UMLFactory.eINSTANCE.createProperty()
            property.type = type
            property.name = typeName
            if (currentOwner?.name  == currentClass?.name) currentClass?.createOwnedAttribute(attributeName, type)
            else if (currentOwner?.name  == currentInterface?.name) currentInterface?.
                createOwnedAttribute(attributeName, type)
            else if (currentOwner == null && currentNestedClass != null) currentNestedClass?.
                createOwnedAttribute(attributeName, type)
        }
        return property
    }

    override fun startMethod(funType: String, funName: String, typeList: EList<String>, argList: EList<String>, isVirtual: Boolean): Operation? {
        val op: Operation?
        val type: Type? = UMLUtil.getType(model, funType)
        val types: BasicEList<Type> = BasicEList()
        for (i in typeList){
            types.add(UMLUtil.getType(model, i))
        }
        val current = if (currentOwner is Class) currentOwner as Class else if (currentOwner is Interface) currentOwner as Interface else currentNestedClass
        if (type != null) {
            op = current?.createOwnedOperation(funName, argList, types, type)
        }
        else {
            op = current?.createOwnedOperation(funName, argList, types)
        }
        if (isVirtual) op?.setIsAbstract(isVirtual);
        current?.ownedComments!![1].setBody((current.ownedComments!![1].body.toInt() + 1).toString())
//        }
        return op
    }

    override fun  addClassSize(byteSize: Int?) {
        currentOwner?.ownedComments?.get(0)?.setBody((currentOwner!!.ownedComments[0].body.toInt() + byteSize!!).toString())
    }
    override fun addParameter(parName: String, typeName: String) {}
    override fun endMethod() {}
}