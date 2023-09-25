package uml.util

import com.fasterxml.jackson.annotation.JsonIncludeProperties

@JsonIncludeProperties(value = ["name"])
abstract class AssociationImplMixin {
}

@JsonIncludeProperties(value = ["body", "packagedElements"])
abstract class CommentImplMixin {
}

@JsonIncludeProperties(value = ["general"])
abstract class GeneralizationImplMixin {
}
@JsonIncludeProperties(value = ["contract"])
abstract class InterfaceRealizationImplMixin {
}

@JsonIncludeProperties(value = ["name", "ownedParameters"])
abstract class OperationImplMixin {
}

@JsonIncludeProperties(value = ["name", "type"])
abstract class ParameterImplMixin {
}

@JsonIncludeProperties(value = ["name", "type"])
abstract class PropertyImplMixin {
}