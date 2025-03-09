package org.tera201.code2uml.uml.helpers

/**
 * Represents a class in the UML model with its properties.
 */
data class BuilderClass(
    val imports: BuilderImports? = null,  // Import statements related to the class
    val name: String,                     // Name of the class
    val modifiers: BuilderClassModifiers = BuilderClassModifiers(), // Class modifiers (e.g., abstract, static)
    val parentName: String? = null,       // Parent class name (if any)
    val interfaceList: List<String>? = null, // List of interfaces implemented by the class
    val isNested: Boolean                 // Whether the class is nested within another class
) {
    // Simplified constructors with default values to reduce redundancy
    constructor(name: String, isNested: Boolean) : this(null, name, BuilderClassModifiers(), null, null, isNested)
    constructor(name: String, isAbstract: Boolean, isNested: Boolean) :
            this(BuilderImports(), name, BuilderClassModifiers(isAbstract), null, null, isNested)

    constructor(name: String, isAbstract: Boolean, parentName: String?, isNested: Boolean) :
            this(BuilderImports(), name, BuilderClassModifiers(isAbstract), parentName, null, isNested)
}

/**
 * Represents an interface in the UML model.
 */
data class BuilderInterface(
    val imports: BuilderImports? = null,  // Import statements related to the interface
    val name: String,                     // Name of the interface
    val modifiers: BuilderInterfaceModifiers = BuilderInterfaceModifiers(), // Interface modifiers
    val parentsNameList: List<String>? = null, // Parent interfaces (if any)
    val isNested: Boolean = false         // Whether the interface is nested
) {
    constructor(name: String, isNested: Boolean) : this(null, name, BuilderInterfaceModifiers(), null, isNested)
    constructor(name: String) : this(BuilderImports(), name, BuilderInterfaceModifiers(isAbstract = false, isPublic = true))
    constructor(name: String, parentsNameList: List<String>?) :
            this(BuilderImports(), name, BuilderInterfaceModifiers(isAbstract = false, isPublic = true), parentsNameList)
}

/**
 * Represents a method (function) in the UML model.
 */
data class BuilderMethod(
    val returnType: String,   // Return type of the function
    val name: String,         // Function name
    val parameterTypes: List<String>, // List of parameter types
    val parameterNames: List<String>, // List of parameter names
    val isAbstract: Boolean   // Whether the function is abstract
)

/**
 * Represents an attribute (field) in the UML model.
 */
data class BuilderAttribute(
    val type: String,  // Data type of the attribute
    val name: String   // Name of the attribute
)

/**
 * Represents a collection of imports for a UML class or interface.
 */
data class BuilderImports(
    val elementImports: List<String> = emptyList(), // Standard imports
    val packageImports: List<String> = emptyList(), // Package imports
    val staticElementImports: List<String> = emptyList(), // Static member imports
    val staticPackageImports: List<String> = emptyList() // Static package imports
)

/**
 * Represents modifiers for an interface (e.g., visibility).
 */
data class BuilderInterfaceModifiers(
    val isAbstract: Boolean = false, // Whether the interface is abstract (default false)
    val isPublic: Boolean = false    // Whether the interface is public (default false)
)

/**
 * Represents modifiers for a class (e.g., visibility, static, final).
 */
data class BuilderClassModifiers(
    val isAbstract: Boolean = false, // Whether the class is abstract (default false)
    val isStatic: Boolean = false,   // Whether the class is static (default false)
    val isFinal: Boolean = false,    // Whether the class is final (default false)
    val visibility: String? = null   // Visibility (public, private, protected)
) {
    constructor(isAbstract: Boolean) : this(isAbstract, isStatic = false, isFinal = false, visibility = null)
}
