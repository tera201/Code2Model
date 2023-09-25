package uml.util.mixin

import com.fasterxml.jackson.annotation.JsonIncludeProperties

@JsonIncludeProperties(value = ["general"])
abstract class GeneralizationImplMixin {
}