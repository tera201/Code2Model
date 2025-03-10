package org.tera201.code2uml.kotlin.parser

import org.tera201.code2uml.java20.parser.generated.Java20Parser.ClassModifierContext
import org.tera201.code2uml.kotlin.parser.generated.KotlinParser
import org.tera201.code2uml.kotlin.parser.generated.KotlinParserBaseListener
import org.tera201.code2uml.uml.DBBuilder
import org.tera201.code2uml.uml.helpers.BuilderClass
import org.tera201.code2uml.uml.helpers.BuilderClassModifiers
import org.tera201.code2uml.uml.helpers.BuilderImports

class KotlinTreeListener(
    private val parser: KotlinParser,
    private val dbBuilder: DBBuilder,
    private val filePath: String,
    private val checksum: String
) : KotlinParserBaseListener() {

    private var packageNum = 0

    // Lists to store different types of imports
    private val singleTypeImports = mutableListOf<String>()
    private val staticImportsOnDemand = mutableListOf<String>()
    private val singleStaticImports = mutableListOf<String>()
    private val typeImportsOnDemand = mutableListOf<String>()

    /**
     * Handles `import` statements and categorizes them into different lists.
     */
    override fun enterImportList(ctx: KotlinParser.ImportListContext?) {}

    /**
     * Clears all stored import lists.
     */
    private fun resetImports() {
        singleTypeImports.clear()
        staticImportsOnDemand.clear()
        singleStaticImports.clear()
        typeImportsOnDemand.clear()
    }

    override fun enterPackageHeader(ctx: KotlinParser.PackageHeaderContext?) {
        ctx?.identifier()?.let {
            it.text.split(".").forEach { dbBuilder.startPackage(it, ctx.text.toByteArray().size, filePath, checksum) }
            packageNum = 1
        }
    }

    override fun exitPackageHeader(ctx: KotlinParser.PackageHeaderContext?) {
        dbBuilder.endPackage()
    }

    override fun enterClassDeclaration(ctx: KotlinParser.ClassDeclarationContext?) {
        ctx?.let {
            val builderImports = collectImports()
            val className = it.simpleIdentifier().text
            val extendName = it.delegationSpecifiers()?.text
            parser.importList().text
            it.modifiers()?.modifier()?.map { it?.text }
            val builderClass = BuilderClass(builderImports, className, BuilderClassModifiers(false, false, false), extendName, listOf(), false)
            dbBuilder.startClass(builderClass, filePath, checksum)
            it.text.toByteArray().size.let(dbBuilder::addClassSize)
        }
    }

    override fun enterFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext?) {
        super.enterFunctionDeclaration(ctx)
    }

    private fun collectImports() = BuilderImports(singleTypeImports, typeImportsOnDemand, singleStaticImports, staticImportsOnDemand)
}