package org.tera201.code2uml.uml.util.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import org.eclipse.uml2.uml.internal.impl.ClassImpl
import org.eclipse.uml2.uml.internal.impl.GeneralizationImpl
import org.eclipse.uml2.uml.internal.impl.UMLFactoryImpl

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