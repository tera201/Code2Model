package uml.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.PropertyWriter
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
import org.eclipse.emf.common.util.BasicEList
import org.eclipse.emf.common.util.EList
import org.eclipse.uml2.uml.internal.impl.*
import uml.util.mixin.*
import java.io.File

class UMLModelHandler {
    internal val objectMapper = ObjectMapper()

    fun addIndentation() {
        objectMapper.apply {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }


    fun saveModelToFile(umlModel: Any, filename: String) {
//        val filterProvider = SimpleFilterProvider()
//            .addFilter("customPropertyFilter", CustomPropertyFilter())
//            .addFilter("nonEmptyFilter", EListPropertyFilter())

        val module = SimpleModule()
        module.addSerializer(ClassImpl::class.java, CustomClassSerializer())
            .addSerializer(InterfaceImpl::class.java, CustomInterfaceSerializer())
            .addSerializer(PackageImpl::class.java, CustomPackageSerializer())
            .addDeserializer(EList::class.java, EListDeserializer())
            .addDeserializer(ParameterImpl::class.java, ParameterDeserializer())
            .addDeserializer(PropertyImpl::class.java, PropertyDeserializer())
            .addDeserializer(GeneralizationImpl::class.java, GeneralizationDeserializer())
            .addDeserializer(InterfaceRealizationImpl::class.java, InterfaceRealizationDeserializer())
        objectMapper.registerModule(module)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

//
//        objectMapper.setFilterProvider(filterProvider)
//        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
//        objectMapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
//        objectMapper.addMixIn(Object::class.java, AnyClassMixin::class.java)
//        objectMapper.addMixIn(Object::class.java, AnyEmptyClassMixin::class.java)
        objectMapper.addMixIn(AssociationImpl::class.java, AssociationImplMixin::class.java)
        objectMapper.addMixIn(PropertyImpl::class.java, PropertyImplMixin::class.java)
        objectMapper.addMixIn(ParameterImpl::class.java, ParameterImplMixin::class.java)
        objectMapper.addMixIn(OperationImpl::class.java, OperationImplMixin::class.java)
        objectMapper.addMixIn(GeneralizationImpl::class.java, GeneralizationImplMixin::class.java)
        objectMapper.addMixIn(InterfaceRealizationImpl::class.java, InterfaceRealizationImplMixin::class.java)
        objectMapper.addMixIn(CommentImpl::class.java, CommentImplMixin::class.java)
        try {
            objectMapper.writeValue(File(filename), umlModel)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    internal inline fun <reified T> loadModelFromFile(filename: String): T? {
        return try {
            objectMapper.readValue(File(filename), T::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

//    class CustomPropertyFilter : SimpleBeanPropertyFilter() {
//        companion object {
//            val PROPERTIES_TO_SKIP = setOf("owner", "nearestPackage", "model", "class_", "redefinitionContexts",
//                "namespace", "featuringClassifiers", "featuringClassifiersGen")
//        }
//
//        override fun serializeAsField(
//            pojo: Any?,
//            jgen: JsonGenerator?,
//            prov: SerializerProvider?,
//            writer: PropertyWriter?
//        ) {
//            if (PROPERTIES_TO_SKIP.contains(writer?.name)) {
//                return
//            }
//            super.serializeAsField(pojo, jgen, prov, writer)
//        }
//    }
//    class EListPropertyFilter : SimpleBeanPropertyFilter() {
//        override fun serializeAsField(
//            pojo: Any?,
//            jgen: JsonGenerator,
//            prov: SerializerProvider,
//            writer: PropertyWriter
//        ) {
//            val includedBooleanFields = setOf("someBoolField")
//            if (pojo == null) {
//                if (!writer.isRequired) {
//                    writer.serializeAsOmittedField(pojo, jgen, prov)
//                }
//                return
//            }
//
//            val value = writer.getMember().getValue(pojo)
//            if (value is EList<*> && value.isEmpty()) {
//                writer.serializeAsOmittedField(pojo, jgen, prov)
//                return
//            }
//
//            if (value is Boolean) {
//                writer.serializeAsOmittedField(pojo, jgen, prov)
//                return
//            }
//            writer.serializeAsField(pojo, jgen, prov)
//        }
//    }

    class CustomClassSerializer : JsonSerializer<ClassImpl>() {
        override fun serialize(value: ClassImpl, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeStartObject()
            gen.writeStringField("@class", value.javaClass.name)
            gen.writeFieldName("name")
            serializers.defaultSerializeValue(value.name, gen)
            if (value.generalizations.isNotEmpty()) {
                gen.writeFieldName("generalizations")
                serializers.defaultSerializeValue(value.generalizations, gen)
            }
            if (value.interfaceRealizations.isNotEmpty()) {
                gen.writeFieldName("interfaceRealizations")
                serializers.defaultSerializeValue(value.interfaceRealizations, gen)
            }
            if (value.ownedAttributes.isNotEmpty()) {
                gen.writeFieldName("ownedAttributes")
                serializers.defaultSerializeValue(value.ownedAttributes, gen)
            }
            if (value.ownedOperations.isNotEmpty()) {
                gen.writeFieldName("ownedOperations")
                serializers.defaultSerializeValue(value.ownedOperations, gen)
            }
            if (value.ownedComments.isNotEmpty()) {
                gen.writeFieldName("ownedComments")
                serializers.defaultSerializeValue(value.ownedComments, gen)
            }

            gen.writeEndObject()
        }
    }

    class CustomInterfaceSerializer : JsonSerializer<InterfaceImpl>() {
        override fun serialize(value: InterfaceImpl, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeStartObject()
            gen.writeStringField("@class", value.javaClass.name)
            gen.writeFieldName("name")
            serializers.defaultSerializeValue(value.name, gen)
            if (value.ownedAttributes.isNotEmpty()) {
                gen.writeFieldName("ownedAttributes")
                serializers.defaultSerializeValue(value.ownedAttributes, gen)
            }
            if (value.ownedOperations.isNotEmpty()) {
                gen.writeFieldName("ownedOperations")
                serializers.defaultSerializeValue(value.ownedOperations, gen)
            }
            if (value.ownedComments.isNotEmpty()) {
                gen.writeFieldName("ownedComments")
                serializers.defaultSerializeValue(value.ownedComments, gen)
            }

            gen.writeEndObject()
        }
    }

    class CustomPackageSerializer : JsonSerializer<PackageImpl>() {
        override fun serialize(value: PackageImpl, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeStartObject()
            gen.writeStringField("@class", value.javaClass.name)
            gen.writeFieldName("name")
            serializers.defaultSerializeValue(value.name, gen)
            if (value.packagedElements.isNotEmpty()) {
                gen.writeFieldName("packagedElements")
                serializers.defaultSerializeValue(value.packagedElements, gen)
            }
            if (value.ownedComments.isNotEmpty()) {
                gen.writeFieldName("ownedComments")
                serializers.defaultSerializeValue(value.ownedComments, gen)
            }

            gen.writeEndObject()
        }
    }
    class ParameterDeserializer : JsonDeserializer<ParameterImpl>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ParameterImpl {
            val node: JsonNode = p.codec.readTree(p)

            val name = node.get("name")?.asText()
            val typeNode = node.get("type")

            val type: AssociationImpl? = if (typeNode != null && !typeNode.isNull) {
                p.codec.treeToValue(typeNode, AssociationImpl::class.java)
            } else {
                null
            }
            val parameterImpl = UMLFactoryImpl.eINSTANCE.createParameter()
            parameterImpl.name = name
            parameterImpl.type = type
            return parameterImpl as ParameterImpl
        }
    }
    class PropertyDeserializer : JsonDeserializer<PropertyImpl>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PropertyImpl {
            val node: JsonNode = p.codec.readTree(p)

            val name = node.get("name")?.asText()
            val typeNode = node.get("type")

            val type: AssociationImpl? = if (typeNode != null && !typeNode.isNull) {
                p.codec.treeToValue(typeNode, AssociationImpl::class.java)
            } else {
                null
            }
            val propertyImpl = UMLFactoryImpl.eINSTANCE.createProperty()
            propertyImpl.name = name
            propertyImpl.type = type
            return propertyImpl as PropertyImpl
        }
    }
    class GeneralizationDeserializer : JsonDeserializer<GeneralizationImpl>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): GeneralizationImpl {
            val node: JsonNode = p.codec.readTree(p)

            val generalNode = node.get("general")

            val general: ClassImpl? = if (generalNode != null && !generalNode.isNull) {
                p.codec.treeToValue(generalNode, ClassImpl::class.java)
            } else {
                null
            }
            val generalizationImpl = UMLFactoryImpl.eINSTANCE.createGeneralization()
            generalizationImpl.general = general
            return generalizationImpl as GeneralizationImpl
        }
    }
    class InterfaceRealizationDeserializer : JsonDeserializer<InterfaceRealizationImpl>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): InterfaceRealizationImpl {
            val node: JsonNode = p.codec.readTree(p)

            val contractNode = node.get("contract")

            val contract: InterfaceImpl? = if (contractNode != null && !contractNode.isNull) {
                p.codec.treeToValue(contractNode, InterfaceImpl::class.java)
            } else {
                null
            }
            val generalizationImpl = UMLFactoryImpl.eINSTANCE.createInterfaceRealization()
            generalizationImpl.contract = contract
            return generalizationImpl as InterfaceRealizationImpl
        }
    }

    class EListDeserializer : JsonDeserializer<BasicEList<Any>>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BasicEList<Any> {
            val node: JsonNode = p.codec.readTree(p)
            val list = BasicEList<Any>()
            val fieldName = p.parsingContext.currentName

            for (itemNode in node) {
                if (itemNode.has("@class")) {
                    val className: String = itemNode["@class"].asText()
                    val itemClass = Class.forName(className)
                    val item = p.codec.treeToValue(itemNode, itemClass)
                    list.add(item)
                }
                else if (fieldName.equals("ownedOperations")) {
                    val item = p.codec.treeToValue(itemNode, OperationImpl::class.java)
                    list.add(item)
                }
                else if (fieldName.equals("ownedComments")) {
                    val item = p.codec.treeToValue(itemNode, CommentImpl::class.java)
                    list.add(item)
                }
                else if (fieldName.equals("ownedAttributes")) {
                    val item = p.codec.treeToValue(itemNode, PropertyImpl::class.java)
                    list.add(item)
                }
                else if (fieldName.equals("ownedOperations")) {
                    val item = p.codec.treeToValue(itemNode, OperationImpl::class.java)
                    list.add(item)
                }
                else if (fieldName.equals("ownedParameters")) {
                    val item = p.codec.treeToValue(itemNode, ParameterImpl::class.java)
                    list.add(item)
                }
                else if (fieldName.equals("interfaceRealizations")) {
                    val item = p.codec.treeToValue(itemNode, InterfaceRealizationImpl::class.java)
                    list.add(item)
                }
                else if (fieldName.equals("generalizations")) {
                    val item = p.codec.treeToValue(itemNode, GeneralizationImpl::class.java)
                    list.add(item)
                }
            }

            return list
        }
    }


}