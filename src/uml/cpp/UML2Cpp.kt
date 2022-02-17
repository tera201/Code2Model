package uml.cpp

import org.eclipse.uml2.uml.Class
import org.eclipse.uml2.uml.Namespace
import org.eclipse.uml2.uml.Package
import org.eclipse.uml2.uml.Property
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
    file.println("namespace $name {")

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
    file.println("class $name {")
    ownedAttributes.forEach { file.println(it.propertyAsJava) }
    file.println("}")
}

private val Property.propertyAsJava
    get() = "${type.name} $name;"