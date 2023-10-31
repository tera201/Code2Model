package org.tera201.code2uml.uml.util.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.eclipse.uml2.uml.internal.impl.ClassImpl

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