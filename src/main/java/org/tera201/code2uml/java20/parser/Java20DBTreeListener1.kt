package org.tera201.code2uml.java20.parser

import org.tera201.code2uml.java20.parser.generated.Java20Parser
import org.tera201.code2uml.java20.parser.generated.Java20ParserBaseListener
import org.tera201.code2uml.uml.DBBuilder
import org.tera201.code2uml.uml.helpers.*

class Java20DBTreeListener1(
    val parser: Java20Parser,
    private val dbBuilder: DBBuilder,
    private val filePath: String,
    private val checksum: String
) : Java20ParserBaseListener() {

    private var packageNum = 0

    /**
     * translationUnit:
     *    declarationseq? EOF
     * ;
     */
    override fun enterCompilationUnit(ctx: Java20Parser.CompilationUnitContext?) {
        super.enterCompilationUnit(ctx)
    }

    override fun exitCompilationUnit(ctx: Java20Parser.CompilationUnitContext?) {
        for (i in 1..packageNum) dbBuilder.endPackage()
    }

    /**
     * namespaceDefinition:
     *     Inline? Namespace (Identifier | originalNamespaceName)? LeftBrace
     *        namespaceBody = declarationseq
     *     ? RightBrace
     * ;
     */
    override fun enterPackageDeclaration(ctx: Java20Parser.PackageDeclarationContext?) {
        ctx!!.Identifier().forEach{dbBuilder.startPackage(it.text, ctx.text?.toByteArray()?.size, filePath, checksum)}
        packageNum = ctx.Identifier().size

    }

    override fun enterRecordDeclaration(ctx: Java20Parser.RecordDeclarationContext?) {
        val className = ctx!!.typeIdentifier()!!.text
        val isNested =  ctx.getParent()?.getParent()?.getParent()?.text?.startsWith("package")?.not() == true
        val builderClass = BuilderClass(className, isNested)
        dbBuilder.startClass(builderClass, filePath, checksum)
        ctx.text?.toByteArray()?.size?.let { dbBuilder.addClassSize(it) }
    }

    override fun enterNormalClassDeclaration(ctx: Java20Parser.NormalClassDeclarationContext?) {
        val className = ctx!!.typeIdentifier()!!.text
        val isNested =  ctx.getParent()?.getParent()?.getParent()?.text?.startsWith("package")?.not() == true
        val builderClass = BuilderClass(className, isNested)
        dbBuilder.startClass(builderClass, filePath, checksum)
        ctx.text?.toByteArray()?.size?.let { dbBuilder.addClassSize(it) }
    }

    override fun enterNormalInterfaceDeclaration(ctx: Java20Parser.NormalInterfaceDeclarationContext?) {
        val interfaceName = ctx!!.typeIdentifier().text
        val isNested =  ctx.getParent()?.getParent()?.getParent()?.text?.startsWith("package")?.not() == true
        val builderInterface = BuilderInterface(interfaceName, isNested)
        dbBuilder.startInterface(builderInterface, filePath, checksum)
        ctx.text?.toByteArray()?.size?.let { dbBuilder.addClassSize(it) }
    }

    override fun enterEnumDeclaration(ctx: Java20Parser.EnumDeclarationContext?) {
        val enumName = ctx!!.typeIdentifier().text
        if (enumName != null) dbBuilder.startEnumeration(enumName, filePath, checksum)
    }
}
