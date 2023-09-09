package uml.decompiler

import uml.util.makePackageDir
import org.eclipse.uml2.uml.Class
import org.eclipse.uml2.uml.Enumeration
import org.eclipse.uml2.uml.Interface
import org.eclipse.uml2.uml.Package

fun Package.toKotlin(packageDir: String) {
    makePackageDir(packageDir)

    ownedMembers
        .filter { !it.hasKeyword("unknown") }
        .forEach {
            when (it) {
                is Class -> it.generateClass(packageDir)
                is Interface -> it.generateInterface(packageDir)
                is Enumeration -> it.generateEnumeration(packageDir)
                is Package -> it.generatePackage("$packageDir/${it.name}")
            }
        }
}
