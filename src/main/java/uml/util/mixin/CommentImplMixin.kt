package uml.util.mixin

import com.fasterxml.jackson.annotation.JsonIncludeProperties

@JsonIncludeProperties(value = ["body", "packagedElements"])
abstract class CommentImplMixin {
}