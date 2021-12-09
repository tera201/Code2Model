package uml.cpp

import org.eclipse.uml2.uml.*
import java.io.PrintStream

fun Package.generateCpp(targetPath: String) {

    makePackageDir(targetPath)
    val ps = createFile(targetPath, name) ?: return
    ps.println("namespace $name {")
    ownedMembers.
    filter {!it.hasKeyword("unknown") }.
    forEach {
        when(it) {
            is Class -> it.generateClass(ps)
            //is Namespace -> it.generateNamespace(targetPath)
            is Package -> it.generateCpp(targetPath)
        }
    }
    ps.println("}")
    ps.close()
}
fun Namespace.generateNamespace(packageDir: String){
    println("namespace $name {")
    println("} // $name ")
}
fun Class.generateClass(file: PrintStream) {
    file.println("class $name {")
    ownedAttributes.forEach { file.println(it.propertyAsJava) }
    file.println("}")
}

private val Property.propertyAsJava
    get() = "$name;"