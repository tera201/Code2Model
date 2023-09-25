package uml.util

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import org.eclipse.emf.common.util.EList
import org.eclipse.uml2.uml.internal.impl.*
import uml.util.deserializers.*
import uml.util.serializers.*
import java.io.File
import java.io.IOException

class UMLModelHandler {
    internal val objectMapper = ObjectMapper().apply {
        val module = SimpleModule()
        module.addSerializer(ClassImpl::class.java, CustomClassSerializer())
            .addSerializer(InterfaceImpl::class.java, CustomInterfaceSerializer())
            .addSerializer(PackageImpl::class.java, CustomPackageSerializer())
            .addDeserializer(EList::class.java, EListDeserializer())
            .addDeserializer(ParameterImpl::class.java, ParameterDeserializer())
            .addDeserializer(PropertyImpl::class.java, PropertyDeserializer())
            .addDeserializer(GeneralizationImpl::class.java, GeneralizationDeserializer())
            .addDeserializer(InterfaceRealizationImpl::class.java, InterfaceRealizationDeserializer())
        registerModule(module)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        addMixIn(AssociationImpl::class.java, AssociationImplMixin::class.java)
        addMixIn(PropertyImpl::class.java, PropertyImplMixin::class.java)
        addMixIn(ParameterImpl::class.java, ParameterImplMixin::class.java)
        addMixIn(OperationImpl::class.java, OperationImplMixin::class.java)
        addMixIn(GeneralizationImpl::class.java, GeneralizationImplMixin::class.java)
        addMixIn(InterfaceRealizationImpl::class.java, InterfaceRealizationImplMixin::class.java)
        addMixIn(CommentImpl::class.java, CommentImplMixin::class.java)
    }

    fun addIndentation() {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT)
    }


    fun saveModelToFile(umlModel: Any, filename: String) {
        try {
            objectMapper.writeValue(File(filename), umlModel)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }
    }

    internal inline fun <reified T> loadModelFromFile(filename: String): T? {
        return try {
            objectMapper.readValue(File(filename), T::class.java)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
            return null
        }
    }
}