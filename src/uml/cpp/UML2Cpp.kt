package uml.cpp

import org.eclipse.uml2.uml.*
import uml.util.createFile
import uml.util.makePackageDir
import java.io.PrintStream

fun Package.generateCpp(targetPath: String) {
    makePackageDir(targetPath)

    val ps = createFile(targetPath, name) ?: return
    generateNamespace(ps)
    ps.close()
}

fun Namespace.generateNamespace(file: PrintStream) {
    file.println("namespace $name {\n")

    ownedMembers.filter { !it.hasKeyword("unknown") }
        .forEach {
            when (it) {
                is Class -> it.generateClass(file)
                is Namespace -> it.generateNamespace(file)
//            is Package -> it.generateCpp(targetPath)
            }
        }
    file.println("} // $name ")
}

fun Class.generateClass(file: PrintStream) {
    file.println("class $name$parentsAsCPP {")
    ownedAttributes.forEach { file.println(it.propertyAsJava) }
    ownedOperations.forEach { it.generateOperation(file)}
    file.println("}\n")
}

private val Classifier.parentsAsCPP: String
    get() {
        val parents = generalizations
            .map { it.general }
            .joinToString { "${it.visibility.literal} ${it.name}" }
        return if (parents.isNotEmpty()) ": $parents" else ""
    }

fun Operation.generateOperation(file: PrintStream) {
    file.print("\t$virtualModify${type.name} $name(")
    val opsList = ownedParameters.filter { it.name != null }.map { "${it.type.name} ${it.name}" }
    file.println("${opsList.joinToString(", ")})")
}

val Operation.virtualModify: String
    get() {
        if (isAbstract) return "virtual "
        return ""
    }

private val Property.propertyAsJava
    get() = "\t${type.name} $name;"