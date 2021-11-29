package uml

import org.eclipse.uml2.uml.Model
import org.eclipse.uml2.uml.Operation
import org.eclipse.uml2.uml.Property

/**
 * Интерфейс построителя UML-модели.
 */
interface IUMLBuilder {
    val model: Model

    fun setName(modelName: String)

    fun startPackage(packageName: String)
    fun endPackage()

    fun startClass(className: String)
    fun endClass()

    fun addAttribute(attributeName: String, typeName: String): Property?

    fun startMethod(funType: String, funName: String): Operation?
    fun addParameter(parName: String, typeName: String)
    fun endMethod()
}