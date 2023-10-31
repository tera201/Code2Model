package org.tera201.code2uml.uml.decompiler


import org.eclipse.uml2.uml.*
import org.eclipse.uml2.uml.ParameterDirectionKind.RETURN_LITERAL
import org.tera201.code2uml.uml.util.createFile
import org.tera201.code2uml.uml.util.makePackageDir
import org.tera201.code2uml.uml.util.nl


fun Package.generatePackage(packageDir: String) {
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

fun Class.generateClass(packageDir: String) {
    val ps = createFile(packageDir, name, "java") ?: return

    ps.println(packageAsJava)
    ps.println(importsAsJava)

    ps.format("%n${modifiers}class $name$parentsAsJava$interfacesAsJava {%n%n")
    ownedAttributes.forEach { ps.println(it.propertyAsJava) }
    ownedOperations.forEach { ps.println(it.operationAsJava) }
    nestedClassifiers.forEach {ps.println(it.nestedClasses)}
    ps.println("}")
    ps.close()
}

private val Classifier.nestedClasses: String
    get() {
        var body = "class $name$parentsAsJava {"
        attributes.forEach { body += "${it.propertyAsJava}\n" }
        operations.forEach { body += "${it.operationAsJava}\n" }
        body += "}\n"
        return body
    }

private val Class.modifiers: String
    get() {
        var modifiers = visibility.asJava
        if (isAbstract) modifiers += "abstract "
        if (isLeaf) modifiers += "final "
        return modifiers
    }

private val newLine: CharSequence = "\n"

private val VisibilityKind.asJava
    get() = if (this == VisibilityKind.PACKAGE_LITERAL) "" else "$literal "

private val NamedElement.javaName: String
    get() {
        val longName = qualifiedName.replace("::", ".")
        val k = longName.indexOf('.')
        return longName.substring(k + 1)
    }

private val Classifier.packageAsJava
    get() = "package ${nearestPackage.javaName};$nl"

private val Classifier.importsAsJava
    get() = importedMembers
        .map { "import ${it.javaName};" }
        .filter { !it.startsWith("import java.lang") }
        .joinToString(newLine)

private val Classifier.parentsAsJava: String
    get() {
        val parents = generalizations
            .map { it.general }
            .filter { !it.javaName.endsWith("java.lang.Object") }
            .joinToString { it.name }
        return if (parents.isNotEmpty()) " extends $parents" else ""
    }

private val Class.interfacesAsJava: String
    get() {
        val implemented = interfaceRealizations.joinToString { it.contract.name }
        return if (implemented.isNotEmpty()) " implements $implemented" else ""
    }

private val Property.propertyAsJava
    get() = "$modifiers${type.name} $name;"

private val Property.modifiers: String
    get() {
        var modifiers = visibility.asJava
        if (isStatic) modifiers += "static "
        return modifiers
    }

private val Operation.operationAsJava: String
    get() {
        val returns = returnResult?.type?.name ?: "void"
        val tail = if (isAbstract) ";" else " {$newLine}$newLine"

        return "$modifiers$returns $name$parameters$tail"
    }

private val Operation.modifiers: String
    get() {
        var modifiers = visibility.asJava
        if (isStatic) modifiers += "static "
        if (isAbstract) modifiers += "abstract "
        return modifiers
    }

private val Operation.parameters
    get() = ownedParameters
        .filter { it.direction != RETURN_LITERAL }
        .joinToString(prefix = "(", postfix = ")")
        { "${it.type.name} ${it.name}" }

private val Enumeration.modifiers: String
    get() {
        var modifiers = visibility.asJava
        if (isLeaf) modifiers += "final "
        return modifiers
    }

fun Enumeration.generateEnumeration(packageDir: String) {
    val ps = createFile(packageDir, name, "java") ?: return

    ps.println(packageAsJava)
    ps.println(importsAsJava)

    ps.format("%n${modifiers}enum $name$parentsAsJava {%n%n")

    ownedAttributes.forEach { ps.println(it.propertyAsJava) }
    ownedOperations.forEach { ps.println(it.operationAsJava) }
    ps.println("}")
    ps.close()
}

private val Interface.modifiers: String
    get() {
        var modifiers = visibility.asJava
        if (isLeaf) modifiers += "final "
        return modifiers
    }

fun Interface.generateInterface(packageDir: String) {
    val ps = createFile(packageDir, name, "java") ?: return

    ps.println(packageAsJava)
    ps.println(importsAsJava)

    ps.format("%n${modifiers}interface $name$parentsAsJava {%n%n")

    ownedAttributes.forEach { ps.println(it.propertyAsJava) }
    ownedOperations.forEach { ps.println(it.operationAsJava) }
    ps.println("}")
    ps.close()
}

