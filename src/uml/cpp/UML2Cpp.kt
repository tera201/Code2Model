package uml.cpp

import org.eclipse.uml2.uml.Package

fun Package.generateCpp(targetPath: String) {
    println("namespace $name {")

    nestedPackages.forEach { it.generateCpp(targetPath) }

    println("} // $name ")
}