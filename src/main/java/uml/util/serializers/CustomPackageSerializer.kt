package uml.util.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.eclipse.uml2.uml.internal.impl.PackageImpl

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