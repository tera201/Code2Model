package uml.util.mixin

import com.fasterxml.jackson.annotation.JsonFilter

@JsonFilter("nonEmptyFilter")
abstract class AnyEmptyClassMixin {
}