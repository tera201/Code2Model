package uml.builders

import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.Operation
import org.eclipse.uml2.uml.Package
import org.eclipse.uml2.uml.Property
import uml.IUMLBuilder
import uml.util.UMLUtil
import util.messages.IMessageHandler

class CPP14UMLBuilderPass1(override val model: Model, val mh: IMessageHandler) : IUMLBuilder {
    private var currentPackage: Package = model

    override fun setName(modelName: String) { model.name = modelName}

    override fun startPackage(packageName: String) {
        currentPackage = UMLUtil.getPackage(currentPackage, packageName) ?: return
    }

    override fun endPackage() {}

    override fun startClass(className: String) {}
    override fun endClass() {}

    override fun addAttribute(attributeName: String, typeName: String): Property? = null

    override fun startMethod(funType: String, funName: String): Operation? = null
    override fun addParameter(parName: String, typeName: String) {}
    override fun endMethod() {}
}