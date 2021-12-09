package cpp.parser

import cpp.parser.generated.CPP14Parser
import cpp.parser.generated.CPP14Parser.OriginalNamespaceNameContext
import cpp.parser.generated.CPP14ParserBaseListener
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
        println("namespace $packageName {")

        umlBuilder.startPackage(packageName)
    }

    override fun exitNamespaceDefinition(ctx: CPP14Parser.NamespaceDefinitionContext?) {
        println()
        println("}")

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
        println("namespace $packageName {")

        umlBuilder.startPackage(packageName)
    }

    override fun exitOriginalNamespaceName(ctx: OriginalNamespaceNameContext?) {
        println()
        println("}")

        umlBuilder.endPackage()
    }

    override fun enterClassSpecifier(ctx: CPP14Parser.ClassSpecifierContext?) {
        val className = ctx!!.classHead().classHeadName().className().text
        umlBuilder.startClass(className)
        println("class $className {")
    }

    override fun exitClassSpecifier(ctx: CPP14Parser.ClassSpecifierContext?) {
        println("}")
    }

    override fun enterMemberdeclaration(ctx: CPP14Parser.MemberdeclarationContext?) {
        val type = ctx!!.getChild(0).text
        print("$type ")
        val childs = ctx.memberDeclaratorList().memberDeclarator()
        if (childs.size > 1){
            for (ind in childs){
                umlBuilder.addAttribute(ind.text, type)
                print("${ind.text} ")
            }}
    }

    override fun exitMemberdeclaration(ctx: CPP14Parser.MemberdeclarationContext?) {
        println(";")
    }
}