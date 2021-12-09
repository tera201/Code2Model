package uml.builders

import org.eclipse.uml2.uml.*
import uml.IUMLBuilder
import uml.util.UMLUtil
import util.messages.IMessageHandler

class CPP14UMLBuilderPass1(override val model: Model, val mh: IMessageHandler) : IUMLBuilder {
    private var currentPackage: Package = model
    private var currentClass: Class? = null

    override fun setName(modelName: String) { model.name = modelName}

    override fun startPackage(packageName: String) {
        currentPackage = UMLUtil.getPackage(currentPackage, packageName) ?: return
    }

    override fun endPackage() {}

    override fun startClass(className: String) {
        currentClass = currentPackage.createOwnedClass(className, false)
    }

    override fun endClass() {}



    override fun addAttribute(attributeName: String, typeName: String): Property? {
        var property: Property?=null
        val type: Type? = null// UMLUtil.getType(model, typeName) !!! need get type???
        if (type == null) {
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