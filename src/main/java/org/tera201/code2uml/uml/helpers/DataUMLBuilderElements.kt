package org.tera201.code2uml.uml.helpers

import org.eclipse.emf.common.util.EList

data class BuilderClass (
    val imports: BuilderImports?,
    val name: String,
    val modifiers: BuilderClassModifiers?,
    val parentName: String?,
    val interfaceList: List<String>?,
    val isNested: Boolean
) {
    constructor(name: String, isNested: Boolean) : this(null, name, null, null, null, isNested)
    constructor(name: String, isAbstract: Boolean, isNested: Boolean) : this(BuilderImports(), name, BuilderClassModifiers(isAbstract), null, null, isNested)
    constructor(name: String, isAbstract: Boolean, parentName:String?, isNested: Boolean) : this(BuilderImports(), name, BuilderClassModifiers(isAbstract), parentName, null, isNested)
}

data class BuilderInterface(
    val imports: BuilderImports?,
    val name: String,
    val modifiers: BuilderInterfaceModifiers?,
    val parentsNameList: List<String>?,
    val isNested: Boolean
) {
    constructor(name: String, isNested: Boolean) : this(null, name, null, null, isNested)
    constructor(name: String) : this(BuilderImports(), name, BuilderInterfaceModifiers(false, true), null, false)
    constructor(name: String, parentsNameList: List<String>?) : this(BuilderImports(), name, BuilderInterfaceModifiers(false, true), parentsNameList, false)
}

data class BuilderMethod(
    val funType: String,
    val funName: String,
    val typeList: EList<String>,
    val argList: EList<String>,
    val isAbstract: Boolean
)

data class BuilderAttribute (
    val typeName: String,
    val name: String
)

data class BuilderImports (
    val elementImports: List<String> = ArrayList(),
    val packageImports: List<String> = ArrayList(),
    val staticElementImports: List<String> = ArrayList(),
    val staticPackageImports: List<String> = ArrayList(),
)

data class BuilderInterfaceModifiers(
    val isAbstract: Boolean,
    val isPublic: Boolean
)

data class BuilderClassModifiers(
    val isAbstract: Boolean,
    val isStatic: Boolean,
    val isFinal: Boolean,
    val visibility: String?
) {
    constructor(isAbstract: Boolean) : this(isAbstract, false, false, null)
}