package org.tera201.code2uml.uml.db

data class ClassDB(
    val id: Int,
    val name: String,
    val filePath: String,
    val size: Long,
    val packageName: String,
    val type: Int,
    val modificator: Int,
    val methodCount: Int = 0,
    val parentClassIdList: MutableList<Int> = mutableListOf(),
    val interfaceIdList: MutableList<Int> = mutableListOf()
    )
