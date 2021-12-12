package uml.cpp

import java.io.File
import java.io.FileNotFoundException
import java.io.PrintStream

val nl = System.getProperty("line.separator")

fun makePackageDir(packageDir: String) {
    val d = File(packageDir)
    if (!d.exists()) d.mkdirs()
}

fun clearPackageDir(packageDir: String) {
    val d = File(packageDir)
    if (d.exists())   d.deleteRecursively()
}

fun createFile(packageDir: String, name: String, ext: String = "cpp") = try {
    PrintStream("$packageDir/$name.$ext")
} catch (e: FileNotFoundException) {
    println(e.toString())
    null
}

fun createJava(packageDir: String, name: String, ext: String = "java") = try {
    PrintStream("$packageDir/$name.$ext")
} catch (e: FileNotFoundException) {
    println(e.toString())
    null
}