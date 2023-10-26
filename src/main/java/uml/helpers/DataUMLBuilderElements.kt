package uml.helpers

data class BuilderClass (
    val imports: BuilderImports,
    val name: String,
    val modifiers: BuilderClassModifiers,
    val parentName: String?,
    val interfaceList: List<String>?,
    val isNested: Boolean
) {
    constructor(name: String, isAbstract: Boolean, isNested: Boolean) : this(BuilderImports(), name, BuilderClassModifiers(isAbstract), null, null, isNested)
    constructor(name: String, isAbstract: Boolean, parentName:String?, isNested: Boolean) : this(BuilderImports(), name, BuilderClassModifiers(isAbstract), parentName, null, isNested)
}

data class BuilderInterface(
    val imports: BuilderImports,
    val name: String,
    val modifiers: BuilderInterfaceModifiers,
    val parentsNameList: List<String>?,
    val isNested: Boolean
)

data class BuilderImports (
    val elementImports: List<String> = ArrayList(),
    val packageImports: List<String> = ArrayList(),
    val staticElementImports: List<String> = ArrayList(),
    val staticPackageImports: List<String> = ArrayList(),
)

data class BuilderInterfaceModifiers(
    val isAbstract: Boolean,
    val isPublic: String?
)

data class BuilderClassModifiers(
    val isAbstract: Boolean,
    val isStatic: Boolean,
    val isFinal: Boolean,
    val visibility: String?
) {
    constructor(isAbstract: Boolean) : this(isAbstract, false, false, null)
}