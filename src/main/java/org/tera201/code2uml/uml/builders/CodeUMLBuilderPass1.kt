package org.tera201.code2uml.uml.builders

import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EAnnotation
import org.eclipse.emf.ecore.EcoreFactory
import org.eclipse.uml2.uml.*
import org.tera201.code2uml.uml.IUMLBuilder
import org.tera201.code2uml.uml.helpers.BuilderClass
import org.tera201.code2uml.uml.helpers.BuilderInterface
import org.tera201.code2uml.uml.util.UMLUtil
import org.tera201.code2uml.util.messages.IMessageHandler
import java.util.*


class CodeUMLBuilderPass1(override val model: Model, val mh: IMessageHandler) : IUMLBuilder {
    private var currentPackage: Package = model
    private var currentClass: Class? = null
    private var currentInterface: Interface? = null
    private var packageStack: Stack<String> = Stack()

    override fun setName(modelName: String) {
        model.name = modelName
    }

    override fun startPackage(packageName: String, byteSize: Int?, filePath: String) {
        if (packageStack.empty()) packageStack.push(packageName)
        else packageStack.push("${packageStack.peek()}.$packageName")
        currentPackage = UMLUtil.getPackage(currentPackage, packageStack.peek().split(".").get(packageStack.size - 1))
        currentPackage.eAnnotations.add(getPathAnnotation(filePath.substringBeforeLast("/")))
        currentPackage.createOwnedComment()?.setBody(byteSize.toString())
    }

    override fun endPackage() {
        packageStack.pop()
        currentPackage = if (!packageStack.empty())
            UMLUtil.getPackage(model, packageStack.peek())
        else model
    }

    override fun startClass(builderClass: BuilderClass, filePath: String) {
        if (!builderClass.isNested) {
            currentClass = UMLUtil.getClass(currentPackage, builderClass.name)
            builderClass.modifiers?.let { currentClass?.setIsAbstract(it.isAbstract) }
            currentClass?.eAnnotations?.add(getPathAnnotation(filePath))

        }
    }

    override fun endClass() {}

    override fun startInterface(interfaceBuilderInterface: BuilderInterface, filePath: String) {
        currentInterface = UMLUtil.getInterface(currentPackage, interfaceBuilderInterface.name)
        currentInterface?.eAnnotations?.add(getPathAnnotation(filePath))
    }

    override fun endInterface() {}
    override fun startEnumeration(enumerationName: String, filePath: String) {
        val enumeration = UMLUtil.getEnum(currentPackage, enumerationName)
        enumeration.eAnnotations?.add(getPathAnnotation(filePath))
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

    private fun getPathAnnotation(filePath: String): EAnnotation {
        val annotation = EcoreFactory.eINSTANCE.createEAnnotation()
        annotation.source = "ResourcePath"
        annotation.getDetails().put("path", filePath);
        return annotation
    }
}