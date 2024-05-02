package org.tera201.code2uml.uml.db

data class PackageDB(
    val id: Int,
    val name: String,
    val packageName: String,
    val size: Long,
    val childrenId: MutableList<Int> = mutableListOf(),
    val parentId: MutableList<Int> = mutableListOf(),
    val checksumList: MutableList<String> = mutableListOf()
)
