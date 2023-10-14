package uml.builders

import org.eclipse.emf.common.util.EList
import org.eclipse.uml2.uml.Class
import org.eclipse.uml2.uml.Interface
import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.Operation
import org.eclipse.uml2.uml.Package
import org.eclipse.uml2.uml.Property
import uml.IUMLBuilder
import uml.util.UMLUtil
import util.messages.IMessageHandler
import java.util.*

class CPP14UMLBuilderPass1(override val model: Model, val mh: IMessageHandler) : IUMLBuilder {
    private var currentPackage: Package = model
    private var currentClass: Class? = null
    private var currentInterface: Interface? = null
    private var packageStack: Stack<String> = Stack()

    override fun setName(modelName: String) {
        model.name = modelName
    }

    override fun startPackage(packageName: String, byteSize: Int?) {
        if (packageStack.empty()) packageStack.push(packageName)
        else packageStack.push("${packageStack.peek()}.$packageName")
        currentPackage = UMLUtil.getPackage(currentPackage, packageStack.peek().split(".").get(packageStack.size - 1))
        currentPackage.createOwnedComment()?.setBody(byteSize.toString())
    }

    override fun endPackage() {
        packageStack.pop()
        currentPackage = if (!packageStack.empty())
            UMLUtil.getPackage(model, packageStack.peek())
        else model
    }

    override fun startClass(
        className: String, extendName: String?, modifiers: List<String>?, isAbstract: Boolean,
        interfaceList: List<String>?, isNested: Boolean?
    ) {
        if (isNested == null || isNested == false) {
        currentClass = UMLUtil.getClass(currentPackage, className)
        currentClass?.setIsAbstract(isAbstract)
        }
    }

    override fun endClass() {}

    override fun startInterface(interfaceName: String, parentName: String?, parentModifier: String?) {
        currentInterface = UMLUtil.getInterface(currentPackage, interfaceName)
    }

    override fun endInterface() {}
    override fun startEnumeration(enumerationName: String) {
        UMLUtil.getEnum(currentPackage, enumerationName)
    }

    override fun endEnumeration() {}

    override fun addAttribute(attributeName: String, typeName: String): Property? {
        UMLUtil.getType(model, typeName)
        return null
    }

    override fun startMethod(funType: String, funName: String, typeList: EList<String>, argList: EList<String>, isVirtual: Boolean): Operation? = null
    override fun addParameter(parName: String, typeName: String) {}
    override fun endMethod() {}
    override fun addClassSize(byteSize: Int?) {}
}