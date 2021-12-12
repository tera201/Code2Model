package uml.builders

import org.eclipse.uml2.uml.*
import util.messages.IMessageHandler
import uml.IUMLBuilder
import uml.util.UMLUtil
import java.util.*

class CPP14UMLBuilderPass2(override val model: Model, val mh: IMessageHandler) : IUMLBuilder {
    private var currentPackage: Package = model
    private var currentClass: Class? = null
    private var packageStack: Stack<String> = Stack()

    override fun setName(modelName: String) {}

    override fun startPackage(packageName: String) {
        if (packageStack.empty()) packageStack.push(packageName)
        else packageStack.push("${packageStack.peek()}.$packageName")
        currentPackage = UMLUtil.getPackage(model, packageStack.peek()) ?: return}

    override fun endPackage() {
        packageStack.pop()
        currentPackage = if (!packageStack.empty())
            UMLUtil.getPackage(model, packageStack.peek()) ?: return
        else model}

    override fun startClass(className: String) {
        currentClass = currentPackage.createOwnedClass(className, false)}
    override fun endClass() {}

    override fun addAttribute(attributeName: String, typeName: String): Property? {
        var property: Property?=null
        val type: Type? = UMLUtil.getType(model, typeName)
        if (type != null) {
            property = UMLFactory.eINSTANCE.createProperty()
            property.type = type;
            property.name = typeName;
            currentClass?.createOwnedAttribute(attributeName, type)
        }
        return property
    }

    override fun startMethod(funType: String, funName: String): Operation? = null
    override fun addParameter(parName: String, typeName: String) {}
    override fun endMethod() {}
}