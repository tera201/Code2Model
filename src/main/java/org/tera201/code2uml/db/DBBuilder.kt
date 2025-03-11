package org.tera201.code2uml.db

import org.tera201.code2uml.uml.helpers.BuilderClass
import org.tera201.code2uml.uml.helpers.BuilderInterface
import org.tera201.code2uml.util.messages.DataBaseUtil

interface DBBuilder {
    val dataBaseUtil: DataBaseUtil
    val projectId: Int
    val model: Int

    fun setName(modelName: String)

    fun startPackage(packageName: String, byteSize: Int?, filePath: String, checksum: String)
    fun endPackage()

    fun startClass(builderClass: BuilderClass, filePath: String, checksum: String)
    fun endClass()

    fun startInterface(interfaceBuilderInterface: BuilderInterface, filePath: String, checksum: String)
    fun endInterface()

    fun startEnumeration(enumerationName: String, filePath: String, checksum: String)
    fun endEnumeration()

    fun addAttribute(attributeName: String, typeName: String)

    fun startMethod(funType: String, funName: String, typeList: List<String>, argList: List<String>, isVirtual: Boolean)
    fun addParameter(parName: String, typeName: String)
    fun endMethod()
    fun addClassSize(byteSize: Int)
}