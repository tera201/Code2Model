package uml

import org.eclipse.emf.common.util.EList
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

    fun startClass(className: String, parentName: String? = null, parentModifier: String? = null, isAbstract: Boolean = false)
    fun endClass()

    fun startInterface(interfaceName: String, parentName: String? = null, parentModifier: String? = null)
    fun endInterface()

    fun addAttribute(attributeName: String, typeName: String): Property?

    fun startMethod(funType: String, funName: String, typeList: EList<String>, argList: EList<String>, isVirtual: Boolean): Operation?
    fun addParameter(parName: String, typeName: String)
    fun endMethod()
}