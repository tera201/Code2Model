package org.tera201.code2uml.cpp.parser

import org.tera201.code2uml.cpp.parser.generated.CPP14Parser
import org.tera201.code2uml.cpp.parser.generated.CPP14Parser.OriginalNamespaceNameContext
import org.tera201.code2uml.cpp.parser.generated.CPP14ParserBaseListener
import org.eclipse.emf.common.util.BasicEList
import org.tera201.code2uml.uml.IUMLBuilder
import org.tera201.code2uml.uml.helpers.BuilderClass
import org.tera201.code2uml.uml.helpers.BuilderImports
import org.tera201.code2uml.uml.helpers.BuilderInterface

class CPP14TreeListener(
    val parser: CPP14Parser,
    private val umlBuilder: IUMLBuilder,
) : CPP14ParserBaseListener() {

    enum class ClassType {
        DEFAULT, ABSTRACT, INTERFACE
    }

    /**
     * translationUnit:
     *    declarationseq? EOF
     * ;
     */
    override fun enterTranslationUnit(ctx: CPP14Parser.TranslationUnitContext?) {
        super.enterTranslationUnit(ctx)
    }

    override fun exitTranslationUnit(ctx: CPP14Parser.TranslationUnitContext?) {
        super.exitTranslationUnit(ctx)
    }

    /**
     * namespaceDefinition:
     *     Inline? Namespace (Identifier | originalNamespaceName)? LeftBrace
     *        namespaceBody = declarationseq
     *     ? RightBrace
     * ;
     */
    override fun enterNamespaceDefinition(ctx: CPP14Parser.NamespaceDefinitionContext?) {
        val packageName = ctx!!.Identifier().text
        umlBuilder.startPackage(packageName, ctx.text?.toByteArray()?.size)
    }

    override fun exitNamespaceDefinition(ctx: CPP14Parser.NamespaceDefinitionContext?) {
        umlBuilder.endPackage()
    }

    /**
     * originalnamespacedefinition
     * :
     *      Inline? Namespace Identifier '{' namespacebody '}'
     * ;
     */
    override fun enterOriginalNamespaceName(ctx: OriginalNamespaceNameContext?) {
        val packageName = ctx!!.Identifier().text
        umlBuilder.startPackage(packageName, 0)
    }

    override fun exitOriginalNamespaceName(ctx: OriginalNamespaceNameContext?) {

        umlBuilder.endPackage()
    }

    override fun enterClassSpecifier(ctx: CPP14Parser.ClassSpecifierContext?) {
        val builderImports = BuilderImports()
        val className = ctx!!.classHead().classHeadName().className().text
        var classType = ClassType.DEFAULT

        // Определение типа класса (стандартный, абстрактный, интерфейс)
        val members = ctx.memberSpecification()
        if (members != null) {
            classType = ClassType.INTERFACE
            if (!members.memberdeclaration().isEmpty()) {
                var numFullVirtualFun = 0
                for (member in members.memberdeclaration()) {
                    if (!member.start.text.equals("virtual")) classType = ClassType.ABSTRACT
                    if (member.functionDefinition() != null) classType = ClassType.ABSTRACT
                    if (member.start.text.equals("virtual") and (member.functionDefinition() == null)) numFullVirtualFun += 1
                }
                if (numFullVirtualFun == 0) classType = ClassType.DEFAULT
            }
        }
        val isAbstact = classType == ClassType.ABSTRACT;
        val builderClass = BuilderClass(className, isAbstact, false)

        // Добавление класса\интерфейса в модель
        if (classType == ClassType.INTERFACE) umlBuilder.startInterface(BuilderInterface(className))
        else umlBuilder.startClass(
            builderClass
        )

        // Добавление родителей классу в моделе
        if (ctx.classHead().baseClause() != null) {
            for (parentClass in ctx.classHead().baseClause().baseSpecifierList().baseSpecifier()) {
                val classParentModyfier = parentClass.accessSpecifier().text
                val classParentName = parentClass.baseTypeSpecifier().text
                val builderClass = BuilderClass(className, isAbstact, classParentName, false)
                if (classType == ClassType.INTERFACE) umlBuilder.startInterface(BuilderInterface(className, listOf(classParentName)))
                else umlBuilder.startClass(builderClass)
            }
        }
        umlBuilder.addClassSize(ctx?.text?.toByteArray()?.size)
    }

    override fun exitClassSpecifier(ctx: CPP14Parser.ClassSpecifierContext?) {
    }

    override fun enterMemberdeclaration(ctx: CPP14Parser.MemberdeclarationContext?) {
        // Пропустить если есть определение функции
        if (ctx!!.functionDefinition() != null) return;
        if (ctx.memberDeclaratorList() != null) {
            // Добавить атрибуты в модель
            if ((ctx.declSpecifierSeq()?.childCount ?: 0) == 1) {
                val type = ctx.declSpecifierSeq()?.text ?: ""
                val childs = ctx.memberDeclaratorList()?.memberDeclarator() ?: listOf()
                for (ind in childs) {
                    umlBuilder.addAttribute(ind.text, type)
                }
            }
            // Добавление виртуальной функции
            else {
                val type = ctx.declSpecifierSeq()?.declSpecifier()?.get(1)?.text ?: ""
                val declarator = ctx.memberDeclaratorList()?.memberDeclarator()?.get(0)?.declarator()
                if (declarator != null) enterfunction(declarator, type, true)
            }
        }
    }

    override fun enterFunctionDefinition(ctx: CPP14Parser.FunctionDefinitionContext?) {
        val size = ctx!!.declSpecifierSeq()?.declSpecifier()?.size ?: 0
        val funType = ctx.declSpecifierSeq()?.declSpecifier()?.getOrNull(size - 1)?.text ?: ""
        val declarator = ctx.declarator()
        enterfunction(declarator, funType, size == 2)
    }

    private fun enterfunction(declarator: CPP14Parser.DeclaratorContext, type:String, isVirtual: Boolean){
        val typeList: BasicEList<String> = BasicEList()
        val argNameList: BasicEList<String> = BasicEList()
        val funName = declarator.pointerDeclarator()?.noPointerDeclarator()?.noPointerDeclarator()?.text ?: ""
        val parameterAndQualifiers = declarator.pointerDeclarator()?.noPointerDeclarator()?.parametersAndQualifiers()?.parameterDeclarationClause()
        if (parameterAndQualifiers != null){
            val args = parameterAndQualifiers.parameterDeclarationList().parameterDeclaration()
            for (arg in args) {
                if(arg.declSpecifierSeq() != null && arg.declarator() != null){
                    typeList.add(arg.declSpecifierSeq().text)
                    argNameList.add(arg.declarator().text)
                }
            }
        }
        umlBuilder.startMethod(type, funName, typeList, argNameList, isVirtual)
    }

    override fun exitMemberdeclaration(ctx: CPP14Parser.MemberdeclarationContext?) {
    }
}
