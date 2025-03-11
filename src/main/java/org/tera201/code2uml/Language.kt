package org.tera201.code2uml

enum class Language(val displayName: String, val index: Int) {
    JAVA("Java", 0),
    KOTLIN("Kotlin", 1),
    JAVA_KOTLIN("Java&Kotlin", 2);

    override fun toString(): String = displayName
}