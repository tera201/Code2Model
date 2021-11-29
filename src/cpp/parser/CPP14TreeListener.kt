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
}