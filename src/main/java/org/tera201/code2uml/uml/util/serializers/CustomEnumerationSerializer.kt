package org.tera201.code2uml.uml.util.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.eclipse.uml2.uml.internal.impl.EnumerationImpl

class CustomEnumerationSerializer : JsonSerializer<EnumerationImpl>() {
    override fun serialize(value: EnumerationImpl, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("@class", value.javaClass.name)
        gen.writeFieldName("name")
        serializers.defaultSerializeValue(value.name, gen)
        gen.writeEndObject()
    }
}