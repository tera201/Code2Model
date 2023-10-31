package org.tera201.code2uml.uml.util.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import org.eclipse.emf.common.util.BasicEList
import org.eclipse.uml2.uml.internal.impl.*

class EListDeserializer : JsonDeserializer<BasicEList<Any>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BasicEList<Any> {
        val node: JsonNode = p.codec.readTree(p)
        val list = BasicEList<Any>()
        val fieldName = p.parsingContext.currentName
        val objectNames = mapOf("ownedOperations" to OperationImpl::class.java,
            "ownedComments" to CommentImpl::class.java,
            "ownedAttributes" to PropertyImpl::class.java,
            "ownedParameters" to ParameterImpl::class.java,
            "interfaceRealizations" to InterfaceRealizationImpl::class.java,
            "generalizations" to GeneralizationImpl::class.java)

        for (itemNode in node) {
            if (itemNode.has("@class")) {
                val className: String = itemNode["@class"].asText()
                val itemClass = Class.forName(className)
                val item = p.codec.treeToValue(itemNode, itemClass)
                list.add(item)
            }
            else if (fieldName in objectNames.keys) {
                val item = p.codec.treeToValue(itemNode, objectNames.get(fieldName))
                list.add(item)
            }
        }
        return list
    }
}