package uml.util.mixin

import com.fasterxml.jackson.annotation.JsonIncludeProperties

@JsonIncludeProperties(value = ["name", "ownedParameters"])
abstract class OperationImplMixin {
}