package uml.helpers

data class BuilderClass (
    val imports: BuilderImports,
    val name: String,
    val isAbstract: Boolean?,
    val isStatic: Boolean?,
    val isFinal: Boolean?,
    val modifier: String?,
    val extend: String?,
    val interfaceList: List<String>?,
    val isNested: Boolean,
)

data class BuilderImports (
    val elementImports: List<String> = ArrayList(),
    val packageImports: List<String> = ArrayList(),
    val staticElementImports: List<String> = ArrayList(),
    val staticPackageImports: List<String> = ArrayList(),
)