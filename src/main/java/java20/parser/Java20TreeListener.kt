package java20.parser

import java20.parser.generated.Java20Parser
import java20.parser.generated.Java20Parser.ClassModifierContext
import java20.parser.generated.Java20ParserBaseListener
import org.eclipse.emf.common.util.BasicEList
import uml.IUMLBuilder
import uml.helpers.BuilderClass
import uml.helpers.BuilderImports
import uml.helpers.BuilderClassModifiers

class Java20TreeListener(
    val parser: Java20Parser,
    private val umlBuilder: IUMLBuilder,
) : Java20ParserBaseListener() {

    enum class ClassType {
        DEFAULT, ABSTRACT, INTERFACE
    }

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
        ctx!!.Identifier().forEach{umlBuilder.startPackage(it.text, ctx.text?.toByteArray()?.size)}
        packageNum = ctx.Identifier().size

    }

    fun getBuilderModifier(classModifiers: List<ClassModifierContext>): BuilderClassModifiers {
        val modifiers = classModifiers.stream()
            .filter { it.text in setOf("private", "public", "protected", "static", "final") }.map { it.text }.toList()
        val isAbstract = modifiers.stream().anyMatch{it == "abstract"}
        val isStatic = modifiers.stream().anyMatch{it == "static"}
        val isFinal = modifiers.stream().anyMatch{it == "final"}
        val visibility = modifiers.stream().filter{it in setOf("private", "public", "protected")}.findAny().get()
        return BuilderClassModifiers(isAbstract, isStatic, isFinal, visibility)
    }

    override fun enterRecordDeclaration(ctx: Java20Parser.RecordDeclarationContext?) {
        val builderImports = BuilderImports(singleTypeImportDeclarationList, typeImportOnDemandDeclarationList, singleStaticImportDeclarationList, staticImportOnDemandDeclarationList);
        resetImports()
        val className = ctx!!.typeIdentifier()!!.text
        val builderModifiers = getBuilderModifier(ctx.classModifier())
        val interfaceList = ctx.classImplements()?.interfaceTypeList()?.interfaceType()?.stream()?.map { it.text }?.toList() // start from 1
        val isNested =  ctx.getParent()?.getParent()?.getParent()?.text?.startsWith("package")?.not() == true
        val builderClass = BuilderClass(builderImports, className, builderModifiers, null, interfaceList, isNested)
        umlBuilder.startClass(builderClass)
        umlBuilder.addClassSize(ctx.text?.toByteArray()?.size)
    }

    override fun enterNormalClassDeclaration(ctx: Java20Parser.NormalClassDeclarationContext?) {
        val builderImports = BuilderImports(singleTypeImportDeclarationList, typeImportOnDemandDeclarationList, singleStaticImportDeclarationList, staticImportOnDemandDeclarationList);
        resetImports()
        val className = ctx!!.typeIdentifier()!!.text
        val isNested =  ctx.getParent()?.getParent()?.text?.startsWith("package")?.not() == true
        val extendName = ctx.classExtends()?.classType()?.text
        val interfaceList = ctx.classImplements()?.interfaceTypeList()?.interfaceType()?.stream()?.map { it.text }?.toList()
        val builderModifiers = getBuilderModifier(ctx.classModifier())
        val builderClass = BuilderClass(builderImports, className, builderModifiers, extendName, interfaceList, isNested)
        umlBuilder.startClass(builderClass)
        umlBuilder.addClassSize(ctx.text?.toByteArray()?.size)
    }

    override fun exitClassDeclaration(ctx: Java20Parser.ClassDeclarationContext?) {
    }

    override fun enterInterfaceDeclaration(ctx: Java20Parser.InterfaceDeclarationContext?) {
        val builderImports = BuilderImports(singleTypeImportDeclarationList, typeImportOnDemandDeclarationList, singleStaticImportDeclarationList, staticImportOnDemandDeclarationList);
        resetImports()
        val interfaceName = ctx!!.normalInterfaceDeclaration()?.typeIdentifier()?.text
        if (interfaceName != null) umlBuilder.startInterface(interfaceName)
        umlBuilder.addClassSize(ctx.text?.toByteArray()?.size)
    }

    override fun enterEnumDeclaration(ctx: Java20Parser.EnumDeclarationContext?) {
        val enumName = ctx!!.typeIdentifier().text
        if (enumName != null) umlBuilder.startEnumeration(enumName)
    }

    override fun enterClassMemberDeclaration(ctx: Java20Parser.ClassMemberDeclarationContext?) {
        val typeName = ctx?.fieldDeclaration()?.unannType()?.text
        val varName = ctx?.fieldDeclaration()?.variableDeclaratorList()?.text
        if (typeName != null && varName != null) umlBuilder.addAttribute(varName, typeName)
    }

    override fun enterMethodHeader(ctx: Java20Parser.MethodHeaderContext?) {
        val declarator = ctx?.methodDeclarator();
        val funName = declarator?.Identifier()?.text
        val funType = ctx?.result()?.text
        val typeList: BasicEList<String> = BasicEList()
        val argNameList: BasicEList<String> = BasicEList()
        if (declarator?.formalParameterList()?.formalParameter() != null) {
            declarator.formalParameterList().formalParameter()?.forEach { if (it.unannType() != null) {
                typeList.add(it.unannType().text);
                argNameList.add(it.variableDeclaratorId().text) }
            }
        }
        if (funName != null && funType != null) umlBuilder.startMethod(funType, funName, typeList,
            argNameList, false)
    }
}
