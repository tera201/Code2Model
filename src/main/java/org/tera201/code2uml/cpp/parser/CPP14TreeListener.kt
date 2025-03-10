package org.tera201.code2uml.cpp.parser

import org.tera201.code2uml.cpp.parser.generated.CPP14Parser
import org.tera201.code2uml.cpp.parser.generated.CPP14Parser.OriginalNamespaceNameContext
import org.tera201.code2uml.cpp.parser.generated.CPP14ParserBaseListener
import org.tera201.code2uml.uml.DBBuilder
import org.tera201.code2uml.uml.helpers.BuilderClass

class CPP14TreeListener(
    private val dbBuilder: DBBuilder,
    private val filePath: String,
    private val checksum: String
) : CPP14ParserBaseListener() {

    // Enum to define the types of classes (e.g., abstract, interface, etc.)
    enum class ClassType {
        DEFAULT, ABSTRACT, INTERFACE
    }

    // Enter translation unit (Not implemented yet, placeholder)
    override fun enterTranslationUnit(ctx: CPP14Parser.TranslationUnitContext?) {
        super.enterTranslationUnit(ctx)
    }

    // Exit translation unit (Not implemented yet, placeholder)
    override fun exitTranslationUnit(ctx: CPP14Parser.TranslationUnitContext?) {
        super.exitTranslationUnit(ctx)
    }

    /**
     * Start processing a namespace definition.
     * Starts a UML package for the namespace.
     */
    override fun enterNamespaceDefinition(ctx: CPP14Parser.NamespaceDefinitionContext?) {
        val packageName = ctx!!.Identifier().text
//        umlBuilder.startPackage(packageName, ctx.text?.toByteArray()?.size, filePath)
    }

    /**
     * End processing a namespace definition.
     * Ends the UML package for the namespace.
     */
    override fun exitNamespaceDefinition(ctx: CPP14Parser.NamespaceDefinitionContext?) {
//        umlBuilder.endPackage()
    }

    /**
     * Start processing an original namespace definition.
     * Starts a UML package for the original namespace.
     */
    override fun enterOriginalNamespaceName(ctx: OriginalNamespaceNameContext?) {
        val packageName = ctx!!.Identifier().text
//        umlBuilder.startPackage(packageName, 0, filePath)
    }

    /**
     * End processing an original namespace definition.
     * Ends the UML package for the original namespace.
     */
    override fun exitOriginalNamespaceName(ctx: OriginalNamespaceNameContext?) {
//        umlBuilder.endPackage()
    }

    /**
     * Start processing a class specification (class, interface, etc.).
     * Identifies whether the class is abstract, interface, or default and starts building it.
     */
    override fun enterClassSpecifier(ctx: CPP14Parser.ClassSpecifierContext?) {
        val className = ctx!!.classHead().classHeadName().className().text
        val classType = determineClassType(ctx)

        // Start building the UML class or interface
        val builderClass = BuilderClass(className, classType == ClassType.ABSTRACT, false)
        startClassOrInterface(classType, builderClass, className)

        // Add parent classes (inheritance)
        processParentClasses(ctx, classType, builderClass)

//        umlBuilder.addClassSize(ctx.text?.toByteArray()?.size)
    }

    // Exit class specification (no action needed at the moment)
    override fun exitClassSpecifier(ctx: CPP14Parser.ClassSpecifierContext?) {
    }

    /**
     * Determines the type of the class (Default, Abstract, Interface) based on its members.
     */
    private fun determineClassType(ctx: CPP14Parser.ClassSpecifierContext): ClassType {
        var classType = ClassType.DEFAULT

        val members = ctx.memberSpecification()
        if (members != null) {
            // If there are members, determine if it's an interface or abstract class
            classType = ClassType.INTERFACE
            if (members.memberdeclaration().isNotEmpty()) {
                classType = checkIfAbstract(members)
            }
        }
        return classType
    }

    /**
     * Checks if the class should be treated as abstract based on its members.
     */
    private fun checkIfAbstract(members: CPP14Parser.MemberSpecificationContext): ClassType {
        var classType = ClassType.ABSTRACT
        var numFullVirtualFun = 0
        for (member in members.memberdeclaration()) {
            if (member.start.text != "virtual") classType = ClassType.ABSTRACT
            if (member.functionDefinition() != null) classType = ClassType.ABSTRACT
            if (member.start.text == "virtual" && member.functionDefinition() == null) numFullVirtualFun++
        }
        if (numFullVirtualFun == 0) classType = ClassType.DEFAULT
        return classType
    }

    /**
     * Starts the UML class or interface depending on the class type.
     */
    private fun startClassOrInterface(classType: ClassType, builderClass: BuilderClass, className: String) {
//        when (classType) {
//            ClassType.INTERFACE -> umlBuilder.startInterface(BuilderInterface(className), filePath)
//            else -> umlBuilder.startClass(builderClass, filePath)
//        }
    }

    /**
     * Processes parent classes (base classes) for inheritance and adds them to the UML model.
     */
    private fun processParentClasses(ctx: CPP14Parser.ClassSpecifierContext, classType: ClassType, builderClass: BuilderClass) {
        val baseClause = ctx.classHead().baseClause()
        if (baseClause != null) {
            for (parentClass in baseClause.baseSpecifierList().baseSpecifier()) {
                val parentClassName = parentClass.baseTypeSpecifier().text
                val classParentModifier = parentClass.accessSpecifier().text

                val builderClassWithParent = BuilderClass(builderClass.name, builderClass.modifiers.isAbstract, parentClassName, false)
                startClassOrInterface(classType, builderClassWithParent, builderClass.name)
            }
        }
    }

    /**
     * Processes a member declaration (e.g., attribute or function).
     * If it's a function definition, it's skipped here and handled elsewhere.
     */
    override fun enterMemberdeclaration(ctx: CPP14Parser.MemberdeclarationContext?) {
        // Skip function definitions as they are handled separately
        if (ctx!!.functionDefinition() != null) return

        if (ctx.memberDeclaratorList() != null) {
            processMemberDeclarators(ctx)
        }
    }

    /**
     * Processes member declarators (attributes or virtual functions).
     * Adds attributes or virtual functions to the UML model.
     */
    private fun processMemberDeclarators(ctx: CPP14Parser.MemberdeclarationContext) {
        val declSpecifierSeq = ctx.declSpecifierSeq()
        val memberDeclarators = ctx.memberDeclaratorList()?.memberDeclarator() ?: return

        if (declSpecifierSeq?.childCount == 1) {
            // Add attributes to the UML model
            val type = declSpecifierSeq.text
//            memberDeclarators.forEach { umlBuilder.addAttribute(it.text, type) }
        } else {
            // Add virtual function to the UML model
            val type = declSpecifierSeq?.declSpecifier()?.get(1)?.text ?: ""
            val declarator = memberDeclarators[0].declarator()
            declarator?.let { enterFunction(it, type, true) }
        }
    }

    /**
     * Handles function definition.
     * Extracts the function type, name, and arguments, then adds the function to the UML model.
     */
    override fun enterFunctionDefinition(ctx: CPP14Parser.FunctionDefinitionContext?) {
        val declSpecifierSeq = ctx!!.declSpecifierSeq()
        val funType = declSpecifierSeq?.declSpecifier()?.lastOrNull()?.text ?: ""
        val declarator = ctx.declarator()
        enterFunction(declarator, funType, declSpecifierSeq?.declSpecifier()?.size == 2)
    }

    /**
     * Handles function entry and adds the function's details to the UML model.
     * @param declarator Function declarator context.
     * @param type Function return type.
     * @param isVirtual Whether the function is virtual.
     */
    private fun enterFunction(declarator: CPP14Parser.DeclaratorContext, type: String, isVirtual: Boolean) {
        val typeList = mutableListOf<String>()
        val argNameList = mutableListOf<String>()
        val funName = declarator.pointerDeclarator()?.noPointerDeclarator()?.noPointerDeclarator()?.text ?: ""
        val parameterClause = declarator.pointerDeclarator()?.noPointerDeclarator()?.parametersAndQualifiers()?.parameterDeclarationClause()

        parameterClause?.parameterDeclarationList()?.parameterDeclaration()?.forEach { arg ->
            arg.declSpecifierSeq()?.let { typeList.add(it.text) }
            arg.declarator()?.let { argNameList.add(it.text) }
        }
    }

    // Exit member declaration (No action needed at the moment)
    override fun exitMemberdeclaration(ctx: CPP14Parser.MemberdeclarationContext?) {
    }
}
