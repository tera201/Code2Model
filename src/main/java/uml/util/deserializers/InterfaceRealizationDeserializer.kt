package uml.util.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import org.eclipse.uml2.uml.internal.impl.InterfaceImpl
import org.eclipse.uml2.uml.internal.impl.InterfaceRealizationImpl
import org.eclipse.uml2.uml.internal.impl.UMLFactoryImpl

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