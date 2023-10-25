package uml.helpers

data class BuilderClass (
    val imports: BuilderImports,
    val name: String,
    val modifiers: BuilderModifiers,
    val parentName: String?,
    val interfaceList: List<String>?,
    val isNested: Boolean,
) {
    constructor(name: String, isAbstract: Boolean, isNested: Boolean) : this(BuilderImports(), name, BuilderModifiers(isAbstract), null, null, isNested)
    constructor(name: String, isAbstract: Boolean, parentName:String?, isNested: Boolean) : this(BuilderImports(), name, BuilderModifiers(isAbstract), parentName, null, isNested)
}

data class BuilderImports (
    val elementImports: List<String> = ArrayList(),
    val packageImports: List<String> = ArrayList(),
    val staticElementImports: List<String> = ArrayList(),
    val staticPackageImports: List<String> = ArrayList(),
)

data class BuilderModifiers(
    val isAbstract: Boolean,
    val isStatic: Boolean,
    val isFinal: Boolean,
    val visibility: String?
) {
    constructor(isAbstract: Boolean) : this(isAbstract, false, false, null)
}