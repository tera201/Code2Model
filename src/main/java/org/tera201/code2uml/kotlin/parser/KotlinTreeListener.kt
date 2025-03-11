package org.tera201.code2uml.kotlin.parser

import org.tera201.code2uml.kotlin.parser.generated.KotlinParser
import org.tera201.code2uml.kotlin.parser.generated.KotlinParserBaseListener
import org.tera201.code2uml.db.DBBuilder
import org.tera201.code2uml.uml.helpers.*

class KotlinTreeListener(
    private val dbBuilder: DBBuilder,
    private val filePath: String,
    private val checksum: String
) : KotlinParserBaseListener() {

    private var fileName = ""
    private var isUtilFile = true
    private var packageNum = 0
    private var fileSize = 0

    // Lists to store different types of imports
    private val singleTypeImports = mutableListOf<String>()
    private val staticImportsOnDemand = mutableListOf<String>()
    private val singleStaticImports = mutableListOf<String>()
    private val typeImportsOnDemand = mutableListOf<String>()

    /**
     * Handles `import` statements and categorizes them into different lists.
     */
    override fun enterImportList(ctx: KotlinParser.ImportListContext?) {
        ctx?.let {
            it.importHeader()?.map { it?.identifier()?.text?.let { singleTypeImports.add(it) } }
        }
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

    override fun enterKotlinFile(ctx: KotlinParser.KotlinFileContext?) {
        ctx?.let {
            fileSize = it.text.toByteArray().size
            fileName = filePath.substringAfterLast("/").substringBefore(".")
        }
    }

    override fun enterPackageHeader(ctx: KotlinParser.PackageHeaderContext?) {
        ctx?.identifier()?.let {
            val packages = it.text.split(".")
            packages.forEach { dbBuilder.startPackage(it, fileSize, filePath, checksum) }
            packageNum = packages.size
        }
    }

    override fun exitKotlinFile(ctx: KotlinParser.KotlinFileContext?) {
        repeat(packageNum) { dbBuilder.endPackage() }
    }

    /**
     * Extracts class modifiers (e.g., `abstract`, `static`, `final`, visibility) into a structured object.
     */
    private fun getBuilderClassModifiers(classModifiers: List<String?>?): BuilderClassModifiers {
        val isAbstract = classModifiers?.any { it == "abstract" }.orFalse()
        val isStatic = classModifiers?.any { it == "object" }.orFalse()
        val isFinal = classModifiers?.any { it == "open" }?.not().orTrue()
        val visibility = classModifiers?.firstOrNull { it in setOf("private", "public", "protected") }
        return BuilderClassModifiers(isAbstract, isStatic, isFinal, visibility)
    }

    override fun enterClassDeclaration(ctx: KotlinParser.ClassDeclarationContext?) {
        ctx?.let {
            isUtilFile = false
            val builderImports = collectImports()
            resetImports()
            val className = it.simpleIdentifier().text
            val extendName = it.delegationSpecifiers()?.text
            val modifiers = it.modifiers()?.modifier()?.map { it?.text }
            if (it.CLASS() != null) {
                if (modifiers?.any {it == "enum" } == true) {
                    dbBuilder.startEnumeration(className, filePath, checksum)
                } else {
                val classModifiers = getBuilderClassModifiers(modifiers)
                val builderClass = BuilderClass(builderImports, className, classModifiers, extendName, listOf(), false)
                dbBuilder.startClass(builderClass, filePath, checksum)
                }

            } else if (it.INTERFACE() != null) {

                val builderInterface = BuilderInterface(builderImports, className, BuilderInterfaceModifiers(false, false), listOf(), false)
                dbBuilder.startInterface(builderInterface, filePath, checksum)

            }
            it.text.toByteArray().size.let(dbBuilder::addClassSize)
        }
    }

    override fun enterFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext?) {
        ctx?.text
        ctx?.let {
            val receiverClass = it.receiverType()?.text
            val name = it.simpleIdentifier().text
            val modifiers = it.modifiers()?.modifier()
            val a = it.typeParameters()

            dbBuilder.startMethod("", name, listOf(), listOf(), false)
        }
    }

    private fun collectImports() = BuilderImports(singleTypeImports, typeImportsOnDemand, singleStaticImports, staticImportsOnDemand)

    private fun Boolean?.orTrue() = this ?: true

    private fun Boolean?.orFalse() = this ?: false
}