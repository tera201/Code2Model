package java20.parser

import java20.parser.generated.Java20Parser
import java20.parser.generated.Java20ParserBaseListener
import org.eclipse.emf.common.util.BasicEList
import uml.IUMLBuilder

class Java20TreeListener(
    val parser: Java20Parser,
    private val umlBuilder: IUMLBuilder,
) : Java20ParserBaseListener() {

    enum class ClassType {
        DEFAULT, ABSTRACT, INTERFACE
    }

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

    override fun exitPackageDeclaration(ctx: Java20Parser.PackageDeclarationContext?) {
//        ctx!!.Identifier().forEach{umlBuilder.endPackage()}
    }

    override fun enterClassDeclaration(ctx: Java20Parser.ClassDeclarationContext?) {
        val className = ctx!!.normalClassDeclaration()?.typeIdentifier()?.text
        var classType = ClassType.DEFAULT
        val isNested =  ctx.getParent()?.getParent()?.text?.startsWith("package")?.not()

        val parent = ctx.normalClassDeclaration()?.classExtends()?.classType()?.text
        val interfaceList = ctx.normalClassDeclaration()?.classImplements()?.interfaceTypeList()?.interfaceType()?.
            stream()?.map { it.text }?.toList()

        // Определение типа класса (стандартный, абстрактный, интерфейс)
        val modifiers = ctx.normalClassDeclaration()?.classModifier()?.stream()?.filter { it.text in setOf("private", "public", "protected", "static", "final") }?.map { it.text }?.toList()
        val isAbstract = ctx.normalClassDeclaration()?.classModifier()?.stream()?.anyMatch{it.text == "abstract"}
        if (isAbstract != null && isAbstract) classType = ClassType.ABSTRACT

        // Добавление класса\интерфейса в модель
        if (className != null) {
            if (classType == ClassType.INTERFACE) umlBuilder.startInterface(className)
            else umlBuilder.startClass(
                className, parent, modifiers, isAbstract = classType == ClassType.ABSTRACT,
                interfaceList, isNested
            )
        }

        umlBuilder.addClassSize(ctx.text?.toByteArray()?.size)
    }

    override fun exitClassDeclaration(ctx: Java20Parser.ClassDeclarationContext?) {
    }

    override fun enterInterfaceDeclaration(ctx: Java20Parser.InterfaceDeclarationContext?) {
        val interfaceName = ctx!!.normalInterfaceDeclaration()?.typeIdentifier()?.text
        if (interfaceName != null) umlBuilder.startInterface(interfaceName)
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
