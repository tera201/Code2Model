package cpp.parser

import cpp.parser.generated.CPP14Parser
import cpp.parser.generated.CPP14Parser.OriginalNamespaceNameContext
import cpp.parser.generated.CPP14ParserBaseListener
import org.eclipse.emf.common.util.BasicEList
import uml.IUMLBuilder

class CPP14TreeListener(
    val parser: CPP14Parser,
    private val umlBuilder: IUMLBuilder,
) : CPP14ParserBaseListener() {

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
        umlBuilder.startPackage(packageName)
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
        umlBuilder.startPackage(packageName)
    }

    override fun exitOriginalNamespaceName(ctx: OriginalNamespaceNameContext?) {

        umlBuilder.endPackage()
    }

    override fun enterClassSpecifier(ctx: CPP14Parser.ClassSpecifierContext?) {
        val className = ctx!!.classHead().classHeadName().className().text
        var classParentName: String? = null
        var classParentModyfier: String? = null
        umlBuilder.startClass(className, classParentName, classParentModyfier)
        if (ctx.classHead().baseClause() != null) {
            for (parentClass in ctx.classHead().baseClause().baseSpecifierList().baseSpecifier()) {
                classParentModyfier = parentClass.accessSpecifier().text
                classParentName = parentClass.baseTypeSpecifier().text
                umlBuilder.startClass(className, classParentName, classParentModyfier)
            }
        }
    }

    override fun exitClassSpecifier(ctx: CPP14Parser.ClassSpecifierContext?) {
    }

    override fun enterMemberdeclaration(ctx: CPP14Parser.MemberdeclarationContext?) {
        if (ctx!!.functionDefinition() != null) return;
        if (ctx.memberDeclaratorList() != null) {
            if (ctx.declSpecifierSeq().childCount == 1) {
                val type = ctx.declSpecifierSeq().text
                val childs = ctx.memberDeclaratorList()!!.memberDeclarator()
                for (ind in childs) {
                    umlBuilder.addAttribute(ind.text, type)
                }
            }
            else {
                val type = ctx.declSpecifierSeq().declSpecifier().get(1).text
                val declarator = ctx.memberDeclaratorList()!!.memberDeclarator().get(0).declarator()
                enterfunction(declarator, type, true)
            }
        }
    }

    override fun enterFunctionDefinition(ctx: CPP14Parser.FunctionDefinitionContext?) {
        val funType = ctx!!.declSpecifierSeq().text
        val declarator = ctx.declarator()
        enterfunction(declarator, funType, false)
    }

    private fun enterfunction(declarator: CPP14Parser.DeclaratorContext, type:String, isVirtual: Boolean){
        val typeList: BasicEList<String> = BasicEList()
        val argNameList: BasicEList<String> = BasicEList()
        val funName = declarator.pointerDeclarator().noPointerDeclarator().noPointerDeclarator().text
        val parameterAndQualifiers = declarator.pointerDeclarator().noPointerDeclarator().parametersAndQualifiers().parameterDeclarationClause()
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
