package org.tera201.code2uml.uml.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import org.eclipse.emf.common.util.EList
import org.eclipse.uml2.uml.internal.impl.*
import org.tera201.code2uml.uml.util.deserializers.*
import org.tera201.code2uml.uml.util.serializers.*
import java.io.File
import java.io.IOException

class UMLModelHandler {
    internal val objectMapper = ObjectMapper().apply {
        val module = SimpleModule()
        applySerializers(module)
        applyDeserializers(module)
        registerModule(module)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        applyMixin(this)
    }

    private fun applySerializers(module: SimpleModule) {
        module.addSerializer(ClassImpl::class.java, CustomClassSerializer())
            .addSerializer(InterfaceImpl::class.java, CustomInterfaceSerializer())
            .addSerializer(PackageImpl::class.java, CustomPackageSerializer())
            .addSerializer(EnumerationImpl::class.java, CustomEnumerationSerializer())
    }

    private fun applyDeserializers(module: SimpleModule) {
        module.addDeserializer(EList::class.java, EListDeserializer())
            .addDeserializer(ParameterImpl::class.java, ParameterDeserializer())
            .addDeserializer(PropertyImpl::class.java, PropertyDeserializer())
            .addDeserializer(GeneralizationImpl::class.java, GeneralizationDeserializer())
            .addDeserializer(InterfaceRealizationImpl::class.java, InterfaceRealizationDeserializer())
    }

    private fun applyMixin(objectMapper: ObjectMapper) {
        objectMapper.apply {
            addMixIn(AssociationImpl::class.java, AssociationImplMixin::class.java)
            addMixIn(PropertyImpl::class.java, PropertyImplMixin::class.java)
            addMixIn(ParameterImpl::class.java, ParameterImplMixin::class.java)
            addMixIn(OperationImpl::class.java, OperationImplMixin::class.java)
            addMixIn(GeneralizationImpl::class.java, GeneralizationImplMixin::class.java)
            addMixIn(InterfaceRealizationImpl::class.java, InterfaceRealizationImplMixin::class.java)
            addMixIn(CommentImpl::class.java, CommentImplMixin::class.java)
        }
    }

    fun addIndentation() {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
    }


    fun saveModelToFile(umlModel: Any, filename: String) {
        try {
            objectMapper.writeValue(File(filename), umlModel)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun loadModelFromFile(filename: String): ModelImpl? {
        return try {
            objectMapper.readValue(File(filename), ModelImpl::class.java)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun loadListModelFromFile(filename: String): ArrayList<ModelImpl>? {
        return try {
            val typeReference = object : TypeReference<ArrayList<ModelImpl>>() {}
            objectMapper.readValue(File(filename), typeReference)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}