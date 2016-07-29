// Generated from ECL.g4 by ANTLR 4.4
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ECLParser}.
 */
public interface ECLListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ECLParser#conjunctionattributeset}.
	 * @param ctx the parse tree
	 */
	void enterConjunctionattributeset(@NotNull ECLParser.ConjunctionattributesetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#conjunctionattributeset}.
	 * @param ctx the parse tree
	 */
	void exitConjunctionattributeset(@NotNull ECLParser.ConjunctionattributesetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#disjunctionattributeset}.
	 * @param ctx the parse tree
	 */
	void enterDisjunctionattributeset(@NotNull ECLParser.DisjunctionattributesetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#disjunctionattributeset}.
	 * @param ctx the parse tree
	 */
	void exitDisjunctionattributeset(@NotNull ECLParser.DisjunctionattributesetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#constraintoperator}.
	 * @param ctx the parse tree
	 */
	void enterConstraintoperator(@NotNull ECLParser.ConstraintoperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#constraintoperator}.
	 * @param ctx the parse tree
	 */
	void exitConstraintoperator(@NotNull ECLParser.ConstraintoperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#mws}.
	 * @param ctx the parse tree
	 */
	void enterMws(@NotNull ECLParser.MwsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#mws}.
	 * @param ctx the parse tree
	 */
	void exitMws(@NotNull ECLParser.MwsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#simpleexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void enterSimpleexpressionconstraint(@NotNull ECLParser.SimpleexpressionconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#simpleexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void exitSimpleexpressionconstraint(@NotNull ECLParser.SimpleexpressionconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#exclusion}.
	 * @param ctx the parse tree
	 */
	void enterExclusion(@NotNull ECLParser.ExclusionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#exclusion}.
	 * @param ctx the parse tree
	 */
	void exitExclusion(@NotNull ECLParser.ExclusionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#conjunctionexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void enterConjunctionexpressionconstraint(@NotNull ECLParser.ConjunctionexpressionconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#conjunctionexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void exitConjunctionexpressionconstraint(@NotNull ECLParser.ConjunctionexpressionconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#attributename}.
	 * @param ctx the parse tree
	 */
	void enterAttributename(@NotNull ECLParser.AttributenameContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#attributename}.
	 * @param ctx the parse tree
	 */
	void exitAttributename(@NotNull ECLParser.AttributenameContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#expressionconstraintvalue}.
	 * @param ctx the parse tree
	 */
	void enterExpressionconstraintvalue(@NotNull ECLParser.ExpressionconstraintvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#expressionconstraintvalue}.
	 * @param ctx the parse tree
	 */
	void exitExpressionconstraintvalue(@NotNull ECLParser.ExpressionconstraintvalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#conjunction}.
	 * @param ctx the parse tree
	 */
	void enterConjunction(@NotNull ECLParser.ConjunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#conjunction}.
	 * @param ctx the parse tree
	 */
	void exitConjunction(@NotNull ECLParser.ConjunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#nonnegativeintegervalue}.
	 * @param ctx the parse tree
	 */
	void enterNonnegativeintegervalue(@NotNull ECLParser.NonnegativeintegervalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#nonnegativeintegervalue}.
	 * @param ctx the parse tree
	 */
	void exitNonnegativeintegervalue(@NotNull ECLParser.NonnegativeintegervalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#disjunctionrefinementset}.
	 * @param ctx the parse tree
	 */
	void enterDisjunctionrefinementset(@NotNull ECLParser.DisjunctionrefinementsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#disjunctionrefinementset}.
	 * @param ctx the parse tree
	 */
	void exitDisjunctionrefinementset(@NotNull ECLParser.DisjunctionrefinementsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(@NotNull ECLParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(@NotNull ECLParser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#refinement}.
	 * @param ctx the parse tree
	 */
	void enterRefinement(@NotNull ECLParser.RefinementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#refinement}.
	 * @param ctx the parse tree
	 */
	void exitRefinement(@NotNull ECLParser.RefinementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#attribute}.
	 * @param ctx the parse tree
	 */
	void enterAttribute(@NotNull ECLParser.AttributeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#attribute}.
	 * @param ctx the parse tree
	 */
	void exitAttribute(@NotNull ECLParser.AttributeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#attributeoperator}.
	 * @param ctx the parse tree
	 */
	void enterAttributeoperator(@NotNull ECLParser.AttributeoperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#attributeoperator}.
	 * @param ctx the parse tree
	 */
	void exitAttributeoperator(@NotNull ECLParser.AttributeoperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#ws}.
	 * @param ctx the parse tree
	 */
	void enterWs(@NotNull ECLParser.WsContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#ws}.
	 * @param ctx the parse tree
	 */
	void exitWs(@NotNull ECLParser.WsContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#utf8_2}.
	 * @param ctx the parse tree
	 */
	void enterUtf8_2(@NotNull ECLParser.Utf8_2Context ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#utf8_2}.
	 * @param ctx the parse tree
	 */
	void exitUtf8_2(@NotNull ECLParser.Utf8_2Context ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#escapedchar}.
	 * @param ctx the parse tree
	 */
	void enterEscapedchar(@NotNull ECLParser.EscapedcharContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#escapedchar}.
	 * @param ctx the parse tree
	 */
	void exitEscapedchar(@NotNull ECLParser.EscapedcharContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#utf8_3}.
	 * @param ctx the parse tree
	 */
	void enterUtf8_3(@NotNull ECLParser.Utf8_3Context ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#utf8_3}.
	 * @param ctx the parse tree
	 */
	void exitUtf8_3(@NotNull ECLParser.Utf8_3Context ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#utf8_4}.
	 * @param ctx the parse tree
	 */
	void enterUtf8_4(@NotNull ECLParser.Utf8_4Context ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#utf8_4}.
	 * @param ctx the parse tree
	 */
	void exitUtf8_4(@NotNull ECLParser.Utf8_4Context ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#alphanumeric}.
	 * @param ctx the parse tree
	 */
	void enterAlphanumeric(@NotNull ECLParser.AlphanumericContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#alphanumeric}.
	 * @param ctx the parse tree
	 */
	void exitAlphanumeric(@NotNull ECLParser.AlphanumericContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#subrefinement}.
	 * @param ctx the parse tree
	 */
	void enterSubrefinement(@NotNull ECLParser.SubrefinementContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#subrefinement}.
	 * @param ctx the parse tree
	 */
	void exitSubrefinement(@NotNull ECLParser.SubrefinementContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#attributegroup}.
	 * @param ctx the parse tree
	 */
	void enterAttributegroup(@NotNull ECLParser.AttributegroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#attributegroup}.
	 * @param ctx the parse tree
	 */
	void exitAttributegroup(@NotNull ECLParser.AttributegroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#focusconcept}.
	 * @param ctx the parse tree
	 */
	void enterFocusconcept(@NotNull ECLParser.FocusconceptContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#focusconcept}.
	 * @param ctx the parse tree
	 */
	void exitFocusconcept(@NotNull ECLParser.FocusconceptContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#disjunction}.
	 * @param ctx the parse tree
	 */
	void enterDisjunction(@NotNull ECLParser.DisjunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#disjunction}.
	 * @param ctx the parse tree
	 */
	void exitDisjunction(@NotNull ECLParser.DisjunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#refinedexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void enterRefinedexpressionconstraint(@NotNull ECLParser.RefinedexpressionconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#refinedexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void exitRefinedexpressionconstraint(@NotNull ECLParser.RefinedexpressionconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#subexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void enterSubexpressionconstraint(@NotNull ECLParser.SubexpressionconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#subexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void exitSubexpressionconstraint(@NotNull ECLParser.SubexpressionconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#conceptreference}.
	 * @param ctx the parse tree
	 */
	void enterConceptreference(@NotNull ECLParser.ConceptreferenceContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#conceptreference}.
	 * @param ctx the parse tree
	 */
	void exitConceptreference(@NotNull ECLParser.ConceptreferenceContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#expressionconstraint}.
	 * @param ctx the parse tree
	 */
	void enterExpressionconstraint(@NotNull ECLParser.ExpressionconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#expressionconstraint}.
	 * @param ctx the parse tree
	 */
	void exitExpressionconstraint(@NotNull ECLParser.ExpressionconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#decimalvalue}.
	 * @param ctx the parse tree
	 */
	void enterDecimalvalue(@NotNull ECLParser.DecimalvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#decimalvalue}.
	 * @param ctx the parse tree
	 */
	void exitDecimalvalue(@NotNull ECLParser.DecimalvalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#conjunctionrefinementset}.
	 * @param ctx the parse tree
	 */
	void enterConjunctionrefinementset(@NotNull ECLParser.ConjunctionrefinementsetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#conjunctionrefinementset}.
	 * @param ctx the parse tree
	 */
	void exitConjunctionrefinementset(@NotNull ECLParser.ConjunctionrefinementsetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#anynonescapedchar}.
	 * @param ctx the parse tree
	 */
	void enterAnynonescapedchar(@NotNull ECLParser.AnynonescapedcharContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#anynonescapedchar}.
	 * @param ctx the parse tree
	 */
	void exitAnynonescapedchar(@NotNull ECLParser.AnynonescapedcharContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#attributeset}.
	 * @param ctx the parse tree
	 */
	void enterAttributeset(@NotNull ECLParser.AttributesetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#attributeset}.
	 * @param ctx the parse tree
	 */
	void exitAttributeset(@NotNull ECLParser.AttributesetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#cardinality}.
	 * @param ctx the parse tree
	 */
	void enterCardinality(@NotNull ECLParser.CardinalityContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#cardinality}.
	 * @param ctx the parse tree
	 */
	void exitCardinality(@NotNull ECLParser.CardinalityContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#nonwsnonpipe}.
	 * @param ctx the parse tree
	 */
	void enterNonwsnonpipe(@NotNull ECLParser.NonwsnonpipeContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#nonwsnonpipe}.
	 * @param ctx the parse tree
	 */
	void exitNonwsnonpipe(@NotNull ECLParser.NonwsnonpipeContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#compoundexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void enterCompoundexpressionconstraint(@NotNull ECLParser.CompoundexpressionconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#compoundexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void exitCompoundexpressionconstraint(@NotNull ECLParser.CompoundexpressionconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#numericvalue}.
	 * @param ctx the parse tree
	 */
	void enterNumericvalue(@NotNull ECLParser.NumericvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#numericvalue}.
	 * @param ctx the parse tree
	 */
	void exitNumericvalue(@NotNull ECLParser.NumericvalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#stringvalue}.
	 * @param ctx the parse tree
	 */
	void enterStringvalue(@NotNull ECLParser.StringvalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#stringvalue}.
	 * @param ctx the parse tree
	 */
	void exitStringvalue(@NotNull ECLParser.StringvalueContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#alphanumericnonus}.
	 * @param ctx the parse tree
	 */
	void enterAlphanumericnonus(@NotNull ECLParser.AlphanumericnonusContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#alphanumericnonus}.
	 * @param ctx the parse tree
	 */
	void exitAlphanumericnonus(@NotNull ECLParser.AlphanumericnonusContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#exclusionexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void enterExclusionexpressionconstraint(@NotNull ECLParser.ExclusionexpressionconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#exclusionexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void exitExclusionexpressionconstraint(@NotNull ECLParser.ExclusionexpressionconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#conceptid}.
	 * @param ctx the parse tree
	 */
	void enterConceptid(@NotNull ECLParser.ConceptidContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#conceptid}.
	 * @param ctx the parse tree
	 */
	void exitConceptid(@NotNull ECLParser.ConceptidContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#subattributeset}.
	 * @param ctx the parse tree
	 */
	void enterSubattributeset(@NotNull ECLParser.SubattributesetContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#subattributeset}.
	 * @param ctx the parse tree
	 */
	void exitSubattributeset(@NotNull ECLParser.SubattributesetContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#disjunctionexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void enterDisjunctionexpressionconstraint(@NotNull ECLParser.DisjunctionexpressionconstraintContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#disjunctionexpressionconstraint}.
	 * @param ctx the parse tree
	 */
	void exitDisjunctionexpressionconstraint(@NotNull ECLParser.DisjunctionexpressionconstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#sctid}.
	 * @param ctx the parse tree
	 */
	void enterSctid(@NotNull ECLParser.SctidContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#sctid}.
	 * @param ctx the parse tree
	 */
	void exitSctid(@NotNull ECLParser.SctidContext ctx);
	/**
	 * Enter a parse tree produced by {@link ECLParser#integervalue}.
	 * @param ctx the parse tree
	 */
	void enterIntegervalue(@NotNull ECLParser.IntegervalueContext ctx);
	/**
	 * Exit a parse tree produced by {@link ECLParser#integervalue}.
	 * @param ctx the parse tree
	 */
	void exitIntegervalue(@NotNull ECLParser.IntegervalueContext ctx);
}