package org.tera201.code2uml.uml.db

data class InterfaceDB(
    val id: Int,
    val name: String,
    val filePath: String,
    val size: Long,
    val packageName: String,
    var extendsId: MutableList<Int> = mutableListOf(),
    val methodCount: Int = 0,
    )
