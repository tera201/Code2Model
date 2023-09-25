package uml.util.mixin

import com.fasterxml.jackson.annotation.JsonIncludeProperties
@JsonIncludeProperties(value = ["contract"])
abstract class InterfaceRealizationImplMixin {
}