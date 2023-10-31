package org.tera201.code2uml.uml.util.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.eclipse.uml2.uml.internal.impl.InterfaceImpl

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