package org.tera201.code2uml.uml

import org.eclipse.emf.common.util.EList
import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.Operation
import org.eclipse.uml2.uml.Property
import org.tera201.code2uml.uml.helpers.BuilderClass
import org.tera201.code2uml.uml.helpers.BuilderInterface

/**
 * UML Model Builder Interface.
 */
interface IUMLBuilder {
    val model: Model

    fun setName(modelName: String)

    fun startPackage(packageName: String, byteSize: Int?, filePath: String)
    fun endPackage()

    fun startClass(builderClass: BuilderClass, filePath: String)
    fun endClass()

    fun startInterface(interfaceBuilderInterface: BuilderInterface, filePath: String)
    fun endInterface()

    fun startEnumeration(enumerationName: String, filePath: String)
    fun endEnumeration()

    fun addAttribute(attributeName: String, typeName: String): Property?

    fun startMethod(funType: String, funName: String, typeList: EList<String>, argList: EList<String>, isVirtual: Boolean): Operation?
    fun addParameter(parName: String, typeName: String)
    fun endMethod()
    fun addClassSize(byteSize: Int?)
}