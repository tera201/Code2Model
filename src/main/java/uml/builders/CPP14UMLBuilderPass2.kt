package uml.builders

import org.eclipse.emf.common.util.BasicEList
import org.eclipse.emf.common.util.EList
import org.eclipse.uml2.uml.*
import uml.IUMLBuilder
import uml.util.UMLUtil
import util.messages.IMessageHandler
import java.util.*

class CPP14UMLBuilderPass2(override val model: Model, val mh: IMessageHandler) : IUMLBuilder {
    private var currentPackage: Package = model
    private var currentClass: Class? = null
    private var currentInterface: Interface? = null
    private var currentOwner: NamedElement? = null
    private var packageStack: Stack<String> = Stack()

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

    override fun startClass(className: String, parentName: String?, parentModifier: String?, isAbstract: Boolean) {
        currentClass = UMLUtil.getClass(currentPackage, className)
        currentClass?.setIsAbstract(isAbstract)
        if (parentName != null) {
            val parent: Class = UMLUtil.getClass(currentPackage, parentName)
            parent.setVisibility(UMLUtil.returnModifier(parentModifier))
            currentClass!!.createGeneralization(parent)
        }
        currentOwner = currentPackage.getOwnedMember(className)
    }

    override fun endClass() {}

    override fun startInterface(interfaceName: String, parentName: String?, parentModifier: String?) {
        currentInterface = UMLUtil.getInterface(currentPackage, interfaceName)
        if (parentName != null) {
            val parent: Interface = UMLUtil.getInterface(currentPackage, parentName)
            parent.setVisibility(UMLUtil.returnModifier(parentModifier))
            currentInterface!!.createGeneralization(parent)
        }
        currentOwner = currentPackage.getOwnedMember(interfaceName)
    }

    override fun endInterface() {}

    override fun addAttribute(attributeName: String, typeName: String): Property? {
        var property: Property? = null
        val type: Type? = UMLUtil.getType(model, typeName)

        if (type != null) {
            property = UMLFactory.eINSTANCE.createProperty()
            property.type = type
            property.name = typeName
            if (currentOwner?.name  == currentClass?.name) currentClass?.createOwnedAttribute(attributeName, type)
            else currentInterface?.createOwnedAttribute(attributeName, type)
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
        val current = if (currentOwner is Class) currentOwner as Class else currentOwner as Interface
        if (type != null) {
            op = current.createOwnedOperation(funName, argList, types, type)
        }
        else {
            op = current.createOwnedOperation(funName, argList, types)
        }
        if (isVirtual) op?.setIsAbstract(isVirtual);
        current.ownedComments!![1].setBody((current.ownedComments!![1].body.toInt() + 1).toString())
        return op
    }

    override fun  addClassSize(byteSize: Int?) {
        currentOwner?.ownedComments?.get(0)?.setBody((currentOwner!!.ownedComments[0].body.toInt() + byteSize!!).toString())
    }
    override fun addParameter(parName: String, typeName: String) {}
    override fun endMethod() {}
}