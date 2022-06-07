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
                is Interface -> it.generateClass(file)
                is Namespace -> it.generateNamespace(file)
//            is Package -> it.generateCpp(targetPath)
            }
        }
    file.println("} // $name ")
}

fun Class.generateClass(file: PrintStream) {
    file.println("class $name$parentsAsCPP {")
    ownedAttributes.forEach { file.println(it.propertyAsJava) }
    ownedOperations.forEach { it.generateOperation(file, false)}
    file.println("}\n")
}

fun Interface.generateClass(file: PrintStream) {
    file.println("class $name$parentsAsCPP {")
    ownedOperations.forEach { it.generateOperation(file, true)}
    file.println("}\n")
}

private val Classifier.parentsAsCPP: String
    get() {
        val parents = generalizations
            .map { it.general }
            .joinToString { "${it.visibility.literal} ${it.name}" }
        return if (parents.isNotEmpty()) ": $parents" else ""
    }

fun Operation.generateOperation(file: PrintStream, isInterface: Boolean) {
    file.print("\t$virtualModify${type.name} $name(")
    val opsList = ownedParameters.filter { it.name != null }.map { "${it.type.name} ${it.name}" }
    val fullyAbstract = if (isInterface) " = 0;" else ""
    file.println("${opsList.joinToString(", ")})$fullyAbstract")
}

val Operation.virtualModify: String
    get() {
        if (isAbstract) return "virtual "
        return ""
    }

private val Property.propertyAsJava
    get() = "\t${type.name} $name;"