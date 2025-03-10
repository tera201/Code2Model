package org.tera201.code2uml.java20.parser

import org.tera201.code2uml.java20.parser.generated.Java20Parser
import org.tera201.code2uml.java20.parser.generated.Java20Parser.ClassModifierContext
import org.tera201.code2uml.java20.parser.generated.Java20Parser.InterfaceModifierContext
import org.tera201.code2uml.java20.parser.generated.Java20ParserBaseListener
import org.tera201.code2uml.uml.DBBuilder
import org.tera201.code2uml.uml.helpers.*

/**
 * Listener class for parsing Java 20 source code and building UML representations.
 */
class Java20TreeListener(
    private val parser: Java20Parser,
    private val dbBuilder: DBBuilder,
    private val filePath: String,
    private val checksum: String
) : Java20ParserBaseListener() {

    private var packageNum = 0

    // Lists to store different types of imports
    private val singleTypeImports = mutableListOf<String>()
    private val staticImportsOnDemand = mutableListOf<String>()
    private val singleStaticImports = mutableListOf<String>()
    private val typeImportsOnDemand = mutableListOf<String>()

    /**
     * Handles `import` statements and categorizes them into different lists.
     */
    override fun enterImportDeclaration(ctx: Java20Parser.ImportDeclarationContext?) {
        ctx?.singleTypeImportDeclaration()?.typeName()?.text?.let { singleTypeImports.add(it) }
        ctx?.staticImportOnDemandDeclaration()?.typeName()?.text?.let { staticImportsOnDemand.add(it) }
        ctx?.singleStaticImportDeclaration()?.typeName()?.text?.let { singleStaticImports.add(it) }
        ctx?.typeImportOnDemandDeclaration()?.packageOrTypeName()?.text?.let { typeImportsOnDemand.add(it) }
    }

    /**
     * Clears all stored import lists.
     */
    private fun resetImports() {
        singleTypeImports.clear()
        staticImportsOnDemand.clear()
        singleStaticImports.clear()
        typeImportsOnDemand.clear()
    }

    /**
     * Handles exiting a compilation unit and ensures all open packages are closed.
     */
    override fun exitCompilationUnit(ctx: Java20Parser.CompilationUnitContext?) {
        repeat(packageNum) { dbBuilder.endPackage() }
    }

    /**
     * Processes `package` declarations and registers them in the database builder.
     */
    override fun enterPackageDeclaration(ctx: Java20Parser.PackageDeclarationContext?) {
        ctx?.Identifier()?.forEach {
            dbBuilder.startPackage(it.text, ctx.text?.toByteArray()?.size, filePath, checksum)
        }
        packageNum = ctx?.Identifier()?.size ?: 0
    }

    /**
     * Extracts class modifiers (e.g., `abstract`, `static`, `final`, visibility) into a structured object.
     */
    private fun getBuilderClassModifiers(classModifiers: List<ClassModifierContext>): BuilderClassModifiers {
        val isAbstract = classModifiers.any { it.text == "abstract" }
        val isStatic = classModifiers.any { it.text == "static" }
        val isFinal = classModifiers.any { it.text == "final" }
        val visibility = classModifiers.firstOrNull { it.text in setOf("private", "public", "protected") }?.text
        return BuilderClassModifiers(isAbstract, isStatic, isFinal, visibility)
    }

    /**
     * Processes a `record` declaration and registers it in the UML database.
     */
    override fun enterRecordDeclaration(ctx: Java20Parser.RecordDeclarationContext?) {
        ctx?.let {
            val builderImports = collectImports()
            resetImports()

            val className = it.typeIdentifier().text
            val builderModifiers = getBuilderClassModifiers(it.classModifier())
            val interfaceList = it.classImplements()?.interfaceTypeList()?.interfaceType()?.map { iface -> iface.text }
            val isNested = !it.parent?.parent?.parent?.text?.startsWith("package").orTrue()

            val builderClass = BuilderClass(builderImports, className, builderModifiers, null, interfaceList, isNested)
            dbBuilder.startClass(builderClass, filePath, checksum)
            it.text.toByteArray().size.let(dbBuilder::addClassSize)
        }
    }

    /**
     * Processes a normal class declaration and registers it in the UML database.
     */
    override fun enterNormalClassDeclaration(ctx: Java20Parser.NormalClassDeclarationContext?) {
        ctx?.let {
            val builderImports = collectImports()
            resetImports()

            val className = it.typeIdentifier().text
            val isNested = !it.parent?.parent?.parent?.text?.startsWith("package").orTrue()
            val extendName = it.classExtends()?.classType()?.text
            val interfaceList = it.classImplements()?.interfaceTypeList()?.interfaceType()?.map { iface -> iface.text }
            val builderModifiers = getBuilderClassModifiers(it.classModifier())

            val builderClass = BuilderClass(builderImports, className, builderModifiers, extendName, interfaceList, isNested)
            dbBuilder.startClass(builderClass, filePath, checksum)
            it.text.toByteArray().size.let(dbBuilder::addClassSize)
        }
    }

    override fun exitClassDeclaration(ctx: Java20Parser.ClassDeclarationContext?) {}

    /**
     * Extracts interface modifiers (e.g., `abstract`, `public`) into a structured object.
     */
    private fun getBuilderInterfaceModifiers(interfaceModifiers: List<InterfaceModifierContext>): BuilderInterfaceModifiers {
        val isAbstract = interfaceModifiers.any { it.text == "abstract" }
        val isPublic = interfaceModifiers.any { it.text == "public" }
        return BuilderInterfaceModifiers(isAbstract, isPublic)
    }

    /**
     * Processes an interface declaration and registers it in the UML database.
     */
    override fun enterNormalInterfaceDeclaration(ctx: Java20Parser.NormalInterfaceDeclarationContext?) {
        ctx?.let {
            val builderImports = collectImports()
            resetImports()

            val interfaceName = it.typeIdentifier().text
            val isNested = !it.parent?.parent?.parent?.text?.startsWith("package").orTrue()
            val parentList = it.interfaceExtends()?.interfaceTypeList()?.interfaceType()?.map { iface -> iface.text }
            val modifiers = getBuilderInterfaceModifiers(it.interfaceModifier())

            val builderInterface = BuilderInterface(builderImports, interfaceName, modifiers, parentList, isNested)
            dbBuilder.startInterface(builderInterface, filePath, checksum)
            it.text.toByteArray().size.let(dbBuilder::addClassSize)
        }
    }

    /**
     * Processes an enumeration declaration.
     */
    override fun enterEnumDeclaration(ctx: Java20Parser.EnumDeclarationContext?) {
        ctx?.typeIdentifier()?.text?.let { dbBuilder.startEnumeration(it, filePath, checksum) }
    }

    /**
     * Processes a field (class attribute) declaration.
     */
    override fun enterFieldDeclaration(ctx: Java20Parser.FieldDeclarationContext?) {
        ctx?.let {
            val typeName = it.unannType().text
            val varName = it.variableDeclaratorList().text
            dbBuilder.addAttribute(varName, typeName)
        }
    }

    /**
     * Processes a method declaration.
     */
    override fun enterMethodHeader(ctx: Java20Parser.MethodHeaderContext?) {
        ctx?.let {
            val declarator = it.methodDeclarator()
            val funName = declarator.Identifier().text
            val funType = it.result().text

            val typeList = mutableListOf<String>()
            val argNameList = mutableListOf<String>()
            declarator.formalParameterList()?.formalParameter()?.forEach { param ->
                param.unannType()?.text?.let(typeList::add)
                param.variableDeclaratorId()?.text?.let(argNameList::add)
            }

            dbBuilder.startMethod(funType, funName, typeList, argNameList, false)
        }
    }

    /**
     * Collects current import lists into a `BuilderImports` object.
     */
    private fun collectImports() = BuilderImports(singleTypeImports, typeImportsOnDemand, singleStaticImports, staticImportsOnDemand)

    private fun Boolean?.orTrue() = this ?: true
}
