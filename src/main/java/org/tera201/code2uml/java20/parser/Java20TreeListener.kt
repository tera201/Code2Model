package org.tera201.code2uml.java20.parser

import org.tera201.code2uml.java20.parser.generated.Java20Parser
import org.tera201.code2uml.java20.parser.generated.Java20Parser.ClassModifierContext
import org.tera201.code2uml.java20.parser.generated.Java20Parser.InterfaceModifierContext
import org.tera201.code2uml.java20.parser.generated.Java20ParserBaseListener
import org.eclipse.emf.common.util.BasicEList
import org.tera201.code2uml.uml.IUMLBuilder
import org.tera201.code2uml.uml.helpers.*
import kotlin.jvm.optionals.getOrNull

@Deprecated("Use Java20DBTreeListener instead")
class Java20TreeListener(
    val parser: Java20Parser,
    private val umlBuilder: IUMLBuilder,
    private val filePath: String
) : Java20ParserBaseListener() {

    private var packageNum = 0
    private var singleTypeImportDeclarationList = ArrayList<String>()
    private var staticImportOnDemandDeclarationList = ArrayList<String>()
    private var singleStaticImportDeclarationList = ArrayList<String>()
    private var typeImportOnDemandDeclarationList = ArrayList<String>()

    override fun enterImportDeclaration(ctx: Java20Parser.ImportDeclarationContext?) {
        val singleTypeImportDeclaration = ctx?.singleTypeImportDeclaration()?.typeName()?.text
        val staticImportOnDemandDeclaration = ctx?.staticImportOnDemandDeclaration()?.typeName()?.text
        val singleStaticImportDeclaration = ctx?.singleStaticImportDeclaration()?.typeName()?.text
        val typeImportOnDemandDeclaration = ctx?.typeImportOnDemandDeclaration()?.packageOrTypeName()?.text
        if (singleTypeImportDeclaration != null) singleTypeImportDeclarationList.add(singleTypeImportDeclaration)
        if (staticImportOnDemandDeclaration != null) staticImportOnDemandDeclarationList.add(staticImportOnDemandDeclaration)
        if (singleStaticImportDeclaration != null) singleStaticImportDeclarationList.add(singleStaticImportDeclaration)
        if (typeImportOnDemandDeclaration != null) typeImportOnDemandDeclarationList.add(typeImportOnDemandDeclaration)
    }

    fun resetImports() {
        singleTypeImportDeclarationList.clear()
        staticImportOnDemandDeclarationList.clear()
        singleStaticImportDeclarationList.clear()
        typeImportOnDemandDeclarationList.clear()
    }

    /**
     * translationUnit:
     *    declarationseq? EOF
     * ;
     */
    override fun enterCompilationUnit(ctx: Java20Parser.CompilationUnitContext?) {
        super.enterCompilationUnit(ctx)
    }

    override fun exitCompilationUnit(ctx: Java20Parser.CompilationUnitContext?) {
        for (i in 1..packageNum) umlBuilder.endPackage()
    }

    /**
     * namespaceDefinition:
     *     Inline? Namespace (Identifier | originalNamespaceName)? LeftBrace
     *        namespaceBody = declarationseq
     *     ? RightBrace
     * ;
     */
    override fun enterPackageDeclaration(ctx: Java20Parser.PackageDeclarationContext?) {
        ctx!!.Identifier().forEach{umlBuilder.startPackage(it.text, ctx.text?.toByteArray()?.size, filePath)}
        packageNum = ctx.Identifier().size

    }

    fun getBuilderClassModifier(classModifiers: List<ClassModifierContext>): BuilderClassModifiers {
        val isAbstract = classModifiers.stream().anyMatch{it.text  == "abstract"}
        val isStatic = classModifiers.stream().anyMatch{it.text  == "static"}
        val isFinal = classModifiers.stream().anyMatch{it.text  == "final"}
        val visibility = classModifiers.stream().filter{it.text in setOf("private", "public", "protected")}.map { it.text }.findAny().getOrNull()
        return BuilderClassModifiers(isAbstract, isStatic, isFinal, visibility)
    }

    override fun enterRecordDeclaration(ctx: Java20Parser.RecordDeclarationContext?) {
        val builderImports = BuilderImports(singleTypeImportDeclarationList, typeImportOnDemandDeclarationList, singleStaticImportDeclarationList, staticImportOnDemandDeclarationList);
        resetImports()
        val className = ctx!!.typeIdentifier()!!.text
        val builderModifiers = getBuilderClassModifier(ctx.classModifier())
        val interfaceList = ctx.classImplements()?.interfaceTypeList()?.interfaceType()?.stream()?.map { it.text }?.toList() // start from 1
        val isNested =  ctx.getParent()?.getParent()?.getParent()?.text?.startsWith("package")?.not() == true
        val builderClass = BuilderClass(builderImports, className, builderModifiers, null, interfaceList, isNested)
        umlBuilder.startClass(builderClass, filePath)
        umlBuilder.addClassSize(ctx.text?.toByteArray()?.size)
    }

    override fun enterNormalClassDeclaration(ctx: Java20Parser.NormalClassDeclarationContext?) {
        val builderImports = BuilderImports(singleTypeImportDeclarationList, typeImportOnDemandDeclarationList, singleStaticImportDeclarationList, staticImportOnDemandDeclarationList);
        resetImports()
        val className = ctx!!.typeIdentifier()!!.text
        val isNested =  ctx.getParent()?.getParent()?.getParent()?.text?.startsWith("package")?.not() == true
        val extendName = ctx.classExtends()?.classType()?.text
        val interfaceList = ctx.classImplements()?.interfaceTypeList()?.interfaceType()?.stream()?.map { it.text }?.toList()
        val builderModifiers = getBuilderClassModifier(ctx.classModifier())
        val builderClass = BuilderClass(builderImports, className, builderModifiers, extendName, interfaceList, isNested)
        umlBuilder.startClass(builderClass, filePath)
        umlBuilder.addClassSize(ctx.text?.toByteArray()?.size)
    }

    override fun exitClassDeclaration(ctx: Java20Parser.ClassDeclarationContext?) {
    }

    fun getBuilderInterfaceModifier(interfaceModifiers: List<InterfaceModifierContext>): BuilderInterfaceModifiers {
        val isAbstract = interfaceModifiers.stream().anyMatch{it.text == "abstract"}
        val isPublic = interfaceModifiers.stream().anyMatch{it.text == "public"}
        return BuilderInterfaceModifiers(isAbstract, isPublic)
    }

    override fun enterNormalInterfaceDeclaration(ctx: Java20Parser.NormalInterfaceDeclarationContext?) {
        val builderImports = BuilderImports(singleTypeImportDeclarationList, typeImportOnDemandDeclarationList, singleStaticImportDeclarationList, staticImportOnDemandDeclarationList);
        resetImports()
        val interfaceName = ctx!!.typeIdentifier().text
        val isNested =  ctx.getParent()?.getParent()?.getParent()?.text?.startsWith("package")?.not() == true
        val parentList = ctx.interfaceExtends()?.interfaceTypeList()?.interfaceType()?.stream()?.map { it.text }?.toList()
        val modifiers = getBuilderInterfaceModifier(ctx.interfaceModifier())
        val builderInterface = BuilderInterface(builderImports, interfaceName, modifiers, parentList, isNested)
        umlBuilder.startInterface(builderInterface, filePath)
        umlBuilder.addClassSize(ctx.text?.toByteArray()?.size)
    }

    override fun enterEnumDeclaration(ctx: Java20Parser.EnumDeclarationContext?) {
        val enumName = ctx!!.typeIdentifier().text
        if (enumName != null) umlBuilder.startEnumeration(enumName, filePath)
    }

    override fun enterFieldDeclaration(ctx: Java20Parser.FieldDeclarationContext?) {
        val typeName = ctx!!.unannType().text
        val varName = ctx!!.variableDeclaratorList().text
        val builderAttribute = BuilderAttribute(typeName, varName)
        if (typeName != null && varName != null) umlBuilder.addAttribute(varName, typeName)
    }

    override fun enterMethodHeader(ctx: Java20Parser.MethodHeaderContext?) {
        val declarator = ctx!!.methodDeclarator();
        val funName = declarator.Identifier().text
        val funType = ctx.result().text
        val typeList: BasicEList<String> = BasicEList()
        val argNameList: BasicEList<String> = BasicEList()
        if (declarator.formalParameterList()?.formalParameter() != null) {
            declarator.formalParameterList().formalParameter()?.forEach { if (it.unannType() != null) {
                typeList.add(it.unannType().text);
                argNameList.add(it.variableDeclaratorId().text) }
            }
        }
        val builderMethod = BuilderMethod(funType, funName, typeList, argNameList, false)
        if (funName != null && funType != null) umlBuilder.startMethod(funType, funName, typeList,
            argNameList, false)
    }
}
