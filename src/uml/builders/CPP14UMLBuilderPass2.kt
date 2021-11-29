package uml.builders

import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.Operation
import org.eclipse.uml2.uml.Property
import util.messages.IMessageHandler
import uml.IUMLBuilder

class CPP14UMLBuilderPass2(override val model: Model, val mh: IMessageHandler) : IUMLBuilder {
    override fun setName(modelName: String) {}

    override fun startPackage(packageName: String) {}
    override fun endPackage() {}

    override fun startClass(className: String) {}
    override fun endClass() {}

    override fun addAttribute(attributeName: String, typeName: String): Property? = null

    override fun startMethod(funType: String, funName: String): Operation? = null
    override fun addParameter(parName: String, typeName: String) {}
    override fun endMethod() {}
}