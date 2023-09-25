package uml.util.mixin

import com.fasterxml.jackson.annotation.JsonFilter

@JsonFilter("customPropertyFilter")
abstract class AnyClassMixin {
}