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
    private var packageStack: Stack<String> = Stack()

    override fun setName(modelName: String) {}

    override fun startPackage(packageName: String) {
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

    override fun startClass(className: String, parentName: String?, parentModifier: String?) {
        currentClass = UMLUtil.getClass(currentPackage, className)
        if (parentName != null) {
            val parent: Class = UMLUtil.getClass(currentPackage, parentName)
            parent.setVisibility(UMLUtil.returnModifyer(parentModifier))
            currentClass!!.createGeneralization(parent)
        }
    }

    override fun endClass() {}

    override fun addAttribute(attributeName: String, typeName: String): Property? {
        var property: Property? = null
        val type: Type? = UMLUtil.getType(model, typeName)

        if (type != null) {
            property = UMLFactory.eINSTANCE.createProperty()
            property.type = type
            property.name = typeName
            currentClass?.createOwnedAttribute(attributeName, type)
        }
        return property
    }

    override fun startMethod(funType: String, funName: String, typeList: EList<String>, argList: EList<String>, isVirtual: Boolean): Operation? {
        var op: Operation?
        val type: Type? = UMLUtil.getType(model, funType)
        val types: BasicEList<Type> = BasicEList()
        for (i in typeList){
            types.add(UMLUtil.getType(model, i))
        }
        if (type != null) {
            op = currentClass?.createOwnedOperation(funName, argList, types, type)
        }
        else {
            op = currentClass?.createOwnedOperation(funName, argList, types)
        }
        if (isVirtual) op?.setIsAbstract(isVirtual);
        return op
    }
    override fun addParameter(parName: String, typeName: String) {}
    override fun endMethod() {}
}