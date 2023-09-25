package uml.util.mixin

import com.fasterxml.jackson.annotation.JsonIncludeProperties
@JsonIncludeProperties(value = ["name", "type"])
abstract class PropertyImplMixin {
}