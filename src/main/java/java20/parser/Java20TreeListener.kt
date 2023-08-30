package java20.parser

import java20.parser.generated.Java20Parser
import java20.parser.generated.Java20ParserBaseListener
import org.eclipse.emf.common.util.BasicEList
import uml.IUMLBuilder
import java.util.function.Predicate

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
//        val packageName = ctx!!.Identifier().joinToString(".")
        ctx!!.Identifier().forEach{umlBuilder.startPackage(it.text, ctx.text?.toByteArray()?.size)}
        packageNum = ctx.Identifier().size

    }

    override fun exitPackageDeclaration(ctx: Java20Parser.PackageDeclarationContext?) {
//        ctx!!.Identifier().forEach{umlBuilder.endPackage()}
    }

//    /**
//     * originalnamespacedefinition
//     * :
//     *      Inline? Namespace Identifier '{' namespacebody '}'
//     * ;
//     */
//    override fun enterenterOriginalNamespaceName(ctx: OriginalNamespaceNameContext?) {
//        val packageName = ctx!!.Identifier().text
//        umlBuilder.startPackage(packageName, 0)
//    }
//
//    override fun exitOriginalNamespaceName(ctx: OriginalNamespaceNameContext?) {
//
//        umlBuilder.endPackage()
//    }

    override fun enterClassDeclaration(ctx: Java20Parser.ClassDeclarationContext?) {
        val className = ctx!!.normalClassDeclaration().typeIdentifier().text
        var classType = ClassType.DEFAULT

        // Определение типа класса (стандартный, абстрактный, интерфейс)
        val isAbstract = ctx.normalClassDeclaration().classModifier().stream().anyMatch{it.text == "abstract"}
        if (isAbstract) classType = ClassType.ABSTRACT
//        if (members != null) {
//            classType = ClassType.INTERFACE
//            if (!members.memberdeclaration().isEmpty()) {
//                var numFullVirtualFun = 0
//                for (member in members.memberdeclaration()) {
//                    if (!member.start.text.equals("virtual")) classType = ClassType.ABSTRACT
//                    if (member.functionDefinition() != null) classType = ClassType.ABSTRACT
//                    if (member.start.text.equals("virtual") and (member.functionDefinition() == null)) numFullVirtualFun += 1
//                }
//                if (numFullVirtualFun == 0) classType = ClassType.DEFAULT
//            }
//        }

        // Добавление класса\интерфейса в модель
        if (classType == ClassType.INTERFACE) umlBuilder.startInterface(className)
        else umlBuilder.startClass(className, isAbstract = classType == ClassType.ABSTRACT)

//        // Добавление родителей классу в моделе
//        if (ctx.normalClassDeclaration().baseClause() != null) {
//            for (parentClass in ctx.classHead().baseClause().baseSpecifierList().baseSpecifier()) {
//                val classParentModyfier = parentClass.accessSpecifier().text
//                val classParentName = parentClass.baseTypeSpecifier().text
//                if (classType == ClassType.INTERFACE) umlBuilder.startInterface(className, classParentName, classParentModyfier)
//                else umlBuilder.startClass(className, classParentName, classParentModyfier, isAbstract = classType == ClassType.ABSTRACT)
//            }
//        }
        umlBuilder.addClassSize(ctx.text?.toByteArray()?.size)
    }

    override fun exitClassDeclaration(ctx: Java20Parser.ClassDeclarationContext?) {
    }

//    override fun enterMemberdeclaration(ctx: Java20Parser.MemberdeclarationContext?) {
//        // Пропустить если есть определение функции
//        if (ctx!!.functionDefinition() != null) return;
//        if (ctx.memberDeclaratorList() != null) {
//            // Добавить атрибуты в модель
//            if (ctx.declSpecifierSeq().childCount == 1) {
//                val type = ctx.declSpecifierSeq().text
//                val childs = ctx.memberDeclaratorList()!!.memberDeclarator()
//                for (ind in childs) {
//                    umlBuilder.addAttribute(ind.text, type)
//                }
//            }
//            // Добавление виртуальной функции
//            else {
//                val type = ctx.declSpecifierSeq().declSpecifier().get(1).text
//                val declarator = ctx.memberDeclaratorList()!!.memberDeclarator().get(0).declarator()
//                enterfunction(declarator, type, true)
//            }
//        }
//    }
//
//    override fun enterFunctionDefinition(ctx: Java20Parser.FunctionDefinitionContext?) {
//        val size = ctx!!.declSpecifierSeq().declSpecifier().size
//        val funType = ctx.declSpecifierSeq().declSpecifier(size - 1).text
//        val declarator = ctx.declarator()
//        enterfunction(declarator, funType, size == 2)
//    }
//
//    private fun enterfunction(declarator: Java20Parser.DeclaratorContext, type:String, isVirtual: Boolean){
//        val typeList: BasicEList<String> = BasicEList()
//        val argNameList: BasicEList<String> = BasicEList()
//        val funName = declarator.pointerDeclarator().noPointerDeclarator().noPointerDeclarator().text
//        val parameterAndQualifiers = declarator.pointerDeclarator().noPointerDeclarator().parametersAndQualifiers().parameterDeclarationClause()
//        if (parameterAndQualifiers != null){
//            val args = parameterAndQualifiers.parameterDeclarationList().parameterDeclaration()
//            for (arg in args) {
//                if(arg.declSpecifierSeq() != null && arg.declarator() != null){
//                    typeList.add(arg.declSpecifierSeq().text)
//                    argNameList.add(arg.declarator().text)
//                }
//            }
//        }
//        umlBuilder.startMethod(type, funName, typeList, argNameList, isVirtual)
//    }
//
//    override fun exitMemberdeclaration(ctx: Java20Parser.MemberdeclarationContext?) {
//    }
}
