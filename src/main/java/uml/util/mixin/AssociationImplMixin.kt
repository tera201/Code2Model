package uml.util.mixin

import com.fasterxml.jackson.annotation.JsonIncludeProperties

@JsonIncludeProperties(value = ["name"])
abstract class AssociationImplMixin {
}