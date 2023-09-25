package uml.util.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import org.eclipse.uml2.uml.internal.impl.AssociationImpl
import org.eclipse.uml2.uml.internal.impl.PropertyImpl
import org.eclipse.uml2.uml.internal.impl.UMLFactoryImpl

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