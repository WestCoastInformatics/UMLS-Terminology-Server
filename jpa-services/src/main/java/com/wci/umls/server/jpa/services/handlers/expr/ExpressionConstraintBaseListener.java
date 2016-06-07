package com.wci.umls.server.jpa.services.handlers.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * The listener interface for receiving expressionConstraint events. The class
 * that is interested in processing a expressionConstraint event implements this
 * interface, and the object created with that class is registered with a
 * component using the component's ddExpressionConstraintListener
 * method. When the expressionConstraint event occurs, that object's
 * appropriate method is invoked.
 */
@SuppressWarnings("all")
public class ExpressionConstraintBaseListener implements
    ExpressionConstraintListener {

  /**
   * Enter expressionconstraint.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterExpressionconstraint(
    ExpressionConstraintParser.ExpressionconstraintContext ctx) {
  }

  /**
   * Exit expressionconstraint.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitExpressionconstraint(
    ExpressionConstraintParser.ExpressionconstraintContext ctx) {
  }

  /**
   * Enter simpleexpressionconstraint.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterSimpleexpressionconstraint(
    ExpressionConstraintParser.SimpleexpressionconstraintContext ctx) {
  }

  /**
   * Exit simpleexpressionconstraint.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitSimpleexpressionconstraint(
    ExpressionConstraintParser.SimpleexpressionconstraintContext ctx) {
  }

  /**
   * Enter refinedexpressionconstraint.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterRefinedexpressionconstraint(
    ExpressionConstraintParser.RefinedexpressionconstraintContext ctx) {
  }

  /**
   * Exit refinedexpressionconstraint.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitRefinedexpressionconstraint(
    ExpressionConstraintParser.RefinedexpressionconstraintContext ctx) {
  }

  /**
   * Enter compoundexpressionconstraint.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterCompoundexpressionconstraint(
    ExpressionConstraintParser.CompoundexpressionconstraintContext ctx) {
  }

  /**
   * Exit compoundexpressionconstraint.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitCompoundexpressionconstraint(
    ExpressionConstraintParser.CompoundexpressionconstraintContext ctx) {
  }

  /**
   * Enter conjunctionexpressionconstraint.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterConjunctionexpressionconstraint(
    ExpressionConstraintParser.ConjunctionexpressionconstraintContext ctx) {
  }

  /**
   * Exit conjunctionexpressionconstraint.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitConjunctionexpressionconstraint(
    ExpressionConstraintParser.ConjunctionexpressionconstraintContext ctx) {
  }

  /**
   * Enter disjunctionexpressionconstraint.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterDisjunctionexpressionconstraint(
    ExpressionConstraintParser.DisjunctionexpressionconstraintContext ctx) {
  }

  /**
   * Exit disjunctionexpressionconstraint.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitDisjunctionexpressionconstraint(
    ExpressionConstraintParser.DisjunctionexpressionconstraintContext ctx) {
  }

  /**
   * Enter exclusionexpressionconstraint.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterExclusionexpressionconstraint(
    ExpressionConstraintParser.ExclusionexpressionconstraintContext ctx) {
  }

  /**
   * Exit exclusionexpressionconstraint.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitExclusionexpressionconstraint(
    ExpressionConstraintParser.ExclusionexpressionconstraintContext ctx) {
  }

  /**
   * Enter subexpressionconstraint.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterSubexpressionconstraint(
    ExpressionConstraintParser.SubexpressionconstraintContext ctx) {
  }

  /**
   * Exit subexpressionconstraint.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitSubexpressionconstraint(
    ExpressionConstraintParser.SubexpressionconstraintContext ctx) {
  }

  /**
   * Enter focusconcept.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterFocusconcept(
    ExpressionConstraintParser.FocusconceptContext ctx) {
  }

  /**
   * Exit focusconcept.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitFocusconcept(
    ExpressionConstraintParser.FocusconceptContext ctx) {
  }

  /**
   * Enter memberof.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterMemberof(ExpressionConstraintParser.MemberofContext ctx) {
  }

  /**
   * Exit memberof.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitMemberof(ExpressionConstraintParser.MemberofContext ctx) {
  }

  /**
   * Enter conceptreference.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterConceptreference(
    ExpressionConstraintParser.ConceptreferenceContext ctx) {
  }

  /**
   * Exit conceptreference.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitConceptreference(
    ExpressionConstraintParser.ConceptreferenceContext ctx) {
  }

  /**
   * Enter conceptid.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterConceptid(ExpressionConstraintParser.ConceptidContext ctx) {
  }

  /**
   * Exit conceptid.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitConceptid(ExpressionConstraintParser.ConceptidContext ctx) {
  }

  /**
   * Enter term.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterTerm(ExpressionConstraintParser.TermContext ctx) {
  }

  /**
   * Exit term.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitTerm(ExpressionConstraintParser.TermContext ctx) {
  }

  /**
   * Enter wildcard.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterWildcard(ExpressionConstraintParser.WildcardContext ctx) {
  }

  /**
   * Exit wildcard.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitWildcard(ExpressionConstraintParser.WildcardContext ctx) {
  }

  /**
   * Enter constraintoperator.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterConstraintoperator(
    ExpressionConstraintParser.ConstraintoperatorContext ctx) {
  }

  /**
   * Exit constraintoperator.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitConstraintoperator(
    ExpressionConstraintParser.ConstraintoperatorContext ctx) {
  }

  /**
   * Enter descendantof.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterDescendantof(
    ExpressionConstraintParser.DescendantofContext ctx) {
  }

  /**
   * Exit descendantof.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitDescendantof(
    ExpressionConstraintParser.DescendantofContext ctx) {
  }

  /**
   * Enter descendantorselfof.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterDescendantorselfof(
    ExpressionConstraintParser.DescendantorselfofContext ctx) {
  }

  /**
   * Exit descendantorselfof.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitDescendantorselfof(
    ExpressionConstraintParser.DescendantorselfofContext ctx) {
  }

  /**
   * Enter ancestorof.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterAncestorof(ExpressionConstraintParser.AncestorofContext ctx) {
  }

  /**
   * Exit ancestorof.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitAncestorof(ExpressionConstraintParser.AncestorofContext ctx) {
  }

  /**
   * Enter ancestororselfof.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterAncestororselfof(
    ExpressionConstraintParser.AncestororselfofContext ctx) {
  }

  /**
   * Exit ancestororselfof.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitAncestororselfof(
    ExpressionConstraintParser.AncestororselfofContext ctx) {
  }

  /**
   * Enter conjunction.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterConjunction(ExpressionConstraintParser.ConjunctionContext ctx) {
  }

  /**
   * Exit conjunction.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitConjunction(ExpressionConstraintParser.ConjunctionContext ctx) {
  }

  /**
   * Enter disjunction.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterDisjunction(ExpressionConstraintParser.DisjunctionContext ctx) {
  }

  /**
   * Exit disjunction.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitDisjunction(ExpressionConstraintParser.DisjunctionContext ctx) {
  }

  /**
   * Enter exclusion.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterExclusion(ExpressionConstraintParser.ExclusionContext ctx) {
  }

  /**
   * Exit exclusion.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitExclusion(ExpressionConstraintParser.ExclusionContext ctx) {
  }

  /**
   * Enter refinement.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterRefinement(ExpressionConstraintParser.RefinementContext ctx) {
  }

  /**
   * Exit refinement.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitRefinement(ExpressionConstraintParser.RefinementContext ctx) {
  }

  /**
   * Enter conjunctionrefinementset.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterConjunctionrefinementset(
    ExpressionConstraintParser.ConjunctionrefinementsetContext ctx) {
  }

  /**
   * Exit conjunctionrefinementset.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitConjunctionrefinementset(
    ExpressionConstraintParser.ConjunctionrefinementsetContext ctx) {
  }

  /**
   * Enter disjunctionrefinementset.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterDisjunctionrefinementset(
    ExpressionConstraintParser.DisjunctionrefinementsetContext ctx) {
  }

  /**
   * Exit disjunctionrefinementset.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitDisjunctionrefinementset(
    ExpressionConstraintParser.DisjunctionrefinementsetContext ctx) {
  }

  /**
   * Enter subrefinement.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterSubrefinement(
    ExpressionConstraintParser.SubrefinementContext ctx) {
  }

  /**
   * Exit subrefinement.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitSubrefinement(
    ExpressionConstraintParser.SubrefinementContext ctx) {
  }

  /**
   * Enter attributeset.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterAttributeset(
    ExpressionConstraintParser.AttributesetContext ctx) {
  }

  /**
   * Exit attributeset.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitAttributeset(
    ExpressionConstraintParser.AttributesetContext ctx) {
  }

  /**
   * Enter conjunctionattributeset.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterConjunctionattributeset(
    ExpressionConstraintParser.ConjunctionattributesetContext ctx) {
  }

  /**
   * Exit conjunctionattributeset.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitConjunctionattributeset(
    ExpressionConstraintParser.ConjunctionattributesetContext ctx) {
  }

  /**
   * Enter disjunctionattributeset.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterDisjunctionattributeset(
    ExpressionConstraintParser.DisjunctionattributesetContext ctx) {
  }

  /**
   * Exit disjunctionattributeset.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitDisjunctionattributeset(
    ExpressionConstraintParser.DisjunctionattributesetContext ctx) {
  }

  /**
   * Enter subattributeset.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterSubattributeset(
    ExpressionConstraintParser.SubattributesetContext ctx) {
  }

  /**
   * Exit subattributeset.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitSubattributeset(
    ExpressionConstraintParser.SubattributesetContext ctx) {
  }

  /**
   * Enter attributegroup.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterAttributegroup(
    ExpressionConstraintParser.AttributegroupContext ctx) {
  }

  /**
   * Exit attributegroup.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitAttributegroup(
    ExpressionConstraintParser.AttributegroupContext ctx) {
  }

  /**
   * Enter attribute.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterAttribute(ExpressionConstraintParser.AttributeContext ctx) {
  }

  /**
   * Exit attribute.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitAttribute(ExpressionConstraintParser.AttributeContext ctx) {
  }

  /**
   * Enter cardinality.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterCardinality(ExpressionConstraintParser.CardinalityContext ctx) {
  }

  /**
   * Exit cardinality.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitCardinality(ExpressionConstraintParser.CardinalityContext ctx) {
  }

  /**
   * Enter to.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterTo(ExpressionConstraintParser.ToContext ctx) {
  }

  /**
   * Exit to.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitTo(ExpressionConstraintParser.ToContext ctx) {
  }

  /**
   * Enter many.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterMany(ExpressionConstraintParser.ManyContext ctx) {
  }

  /**
   * Exit many.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitMany(ExpressionConstraintParser.ManyContext ctx) {
  }

  /**
   * Enter reverseflag.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterReverseflag(ExpressionConstraintParser.ReverseflagContext ctx) {
  }

  /**
   * Exit reverseflag.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitReverseflag(ExpressionConstraintParser.ReverseflagContext ctx) {
  }

  /**
   * Enter attributeoperator.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterAttributeoperator(
    ExpressionConstraintParser.AttributeoperatorContext ctx) {
  }

  /**
   * Exit attributeoperator.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitAttributeoperator(
    ExpressionConstraintParser.AttributeoperatorContext ctx) {
  }

  /**
   * Enter attributename.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterAttributename(
    ExpressionConstraintParser.AttributenameContext ctx) {
  }

  /**
   * Exit attributename.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitAttributename(
    ExpressionConstraintParser.AttributenameContext ctx) {
  }

  /**
   * Enter expressionconstraintvalue.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterExpressionconstraintvalue(
    ExpressionConstraintParser.ExpressionconstraintvalueContext ctx) {
  }

  /**
   * Exit expressionconstraintvalue.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitExpressionconstraintvalue(
    ExpressionConstraintParser.ExpressionconstraintvalueContext ctx) {
  }

  /**
   * Enter expressioncomparisonoperator.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterExpressioncomparisonoperator(
    ExpressionConstraintParser.ExpressioncomparisonoperatorContext ctx) {
  }

  /**
   * Exit expressioncomparisonoperator.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitExpressioncomparisonoperator(
    ExpressionConstraintParser.ExpressioncomparisonoperatorContext ctx) {
  }

  /**
   * Enter numericcomparisonoperator.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterNumericcomparisonoperator(
    ExpressionConstraintParser.NumericcomparisonoperatorContext ctx) {
  }

  /**
   * Exit numericcomparisonoperator.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitNumericcomparisonoperator(
    ExpressionConstraintParser.NumericcomparisonoperatorContext ctx) {
  }

  /**
   * Enter stringcomparisonoperator.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterStringcomparisonoperator(
    ExpressionConstraintParser.StringcomparisonoperatorContext ctx) {
  }

  /**
   * Exit stringcomparisonoperator.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitStringcomparisonoperator(
    ExpressionConstraintParser.StringcomparisonoperatorContext ctx) {
  }

  /**
   * Enter numericvalue.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterNumericvalue(
    ExpressionConstraintParser.NumericvalueContext ctx) {
  }

  /**
   * Exit numericvalue.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitNumericvalue(
    ExpressionConstraintParser.NumericvalueContext ctx) {
  }

  /**
   * Enter stringvalue.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterStringvalue(ExpressionConstraintParser.StringvalueContext ctx) {
  }

  /**
   * Exit stringvalue.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitStringvalue(ExpressionConstraintParser.StringvalueContext ctx) {
  }

  /**
   * Enter integervalue.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterIntegervalue(
    ExpressionConstraintParser.IntegervalueContext ctx) {
  }

  /**
   * Exit integervalue.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitIntegervalue(
    ExpressionConstraintParser.IntegervalueContext ctx) {
  }

  /**
   * Enter decimalvalue.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterDecimalvalue(
    ExpressionConstraintParser.DecimalvalueContext ctx) {
  }

  /**
   * Exit decimalvalue.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitDecimalvalue(
    ExpressionConstraintParser.DecimalvalueContext ctx) {
  }

  /**
   * Enter nonnegativeintegervalue.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterNonnegativeintegervalue(
    ExpressionConstraintParser.NonnegativeintegervalueContext ctx) {
  }

  /**
   * Exit nonnegativeintegervalue.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitNonnegativeintegervalue(
    ExpressionConstraintParser.NonnegativeintegervalueContext ctx) {
  }

  /**
   * Enter sctid.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterSctid(ExpressionConstraintParser.SctidContext ctx) {
  }

  /**
   * Exit sctid.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitSctid(ExpressionConstraintParser.SctidContext ctx) {
  }

  /**
   * Enter ws.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterWs(ExpressionConstraintParser.WsContext ctx) {
  }

  /**
   * Exit ws.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitWs(ExpressionConstraintParser.WsContext ctx) {
  }

  /**
   * Enter mws.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterMws(ExpressionConstraintParser.MwsContext ctx) {
  }

  /**
   * Exit mws.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitMws(ExpressionConstraintParser.MwsContext ctx) {
  }

  /**
   * Enter sp.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterSp(ExpressionConstraintParser.SpContext ctx) {
  }

  /**
   * Exit sp.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitSp(ExpressionConstraintParser.SpContext ctx) {
  }

  /**
   * Enter htab.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterHtab(ExpressionConstraintParser.HtabContext ctx) {
  }

  /**
   * Exit htab.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitHtab(ExpressionConstraintParser.HtabContext ctx) {
  }

  /**
   * Enter cr.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterCr(ExpressionConstraintParser.CrContext ctx) {
  }

  /**
   * Exit cr.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitCr(ExpressionConstraintParser.CrContext ctx) {
  }

  /**
   * Enter lf.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterLf(ExpressionConstraintParser.LfContext ctx) {
  }

  /**
   * Exit lf.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitLf(ExpressionConstraintParser.LfContext ctx) {
  }

  /**
   * Enter qm.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterQm(ExpressionConstraintParser.QmContext ctx) {
  }

  /**
   * Exit qm.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitQm(ExpressionConstraintParser.QmContext ctx) {
  }

  /**
   * Enter bs.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterBs(ExpressionConstraintParser.BsContext ctx) {
  }

  /**
   * Exit bs.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitBs(ExpressionConstraintParser.BsContext ctx) {
  }

  /**
   * Enter digit.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterDigit(ExpressionConstraintParser.DigitContext ctx) {
  }

  /**
   * Exit digit.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitDigit(ExpressionConstraintParser.DigitContext ctx) {
  }

  /**
   * Enter zero.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterZero(ExpressionConstraintParser.ZeroContext ctx) {
  }

  /**
   * Exit zero.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitZero(ExpressionConstraintParser.ZeroContext ctx) {
  }

  /**
   * Enter digitnonzero.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterDigitnonzero(
    ExpressionConstraintParser.DigitnonzeroContext ctx) {
  }

  /**
   * Exit digitnonzero.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitDigitnonzero(
    ExpressionConstraintParser.DigitnonzeroContext ctx) {
  }

  /**
   * Enter nonwsnonpipe.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterNonwsnonpipe(
    ExpressionConstraintParser.NonwsnonpipeContext ctx) {
  }

  /**
   * Exit nonwsnonpipe.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitNonwsnonpipe(
    ExpressionConstraintParser.NonwsnonpipeContext ctx) {
  }

  /**
   * Enter anynonescapedchar.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterAnynonescapedchar(
    ExpressionConstraintParser.AnynonescapedcharContext ctx) {
  }

  /**
   * Exit anynonescapedchar.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitAnynonescapedchar(
    ExpressionConstraintParser.AnynonescapedcharContext ctx) {
  }

  /**
   * Enter escapedchar.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterEscapedchar(ExpressionConstraintParser.EscapedcharContext ctx) {
  }

  /**
   * Exit escapedchar.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitEscapedchar(ExpressionConstraintParser.EscapedcharContext ctx) {
  }

  /**
   * Enter utf8_2.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterUtf8_2(ExpressionConstraintParser.Utf8_2Context ctx) {
  }

  /**
   * Exit utf8_2.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitUtf8_2(ExpressionConstraintParser.Utf8_2Context ctx) {
  }

  /**
   * Enter utf8_3.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterUtf8_3(ExpressionConstraintParser.Utf8_3Context ctx) {
  }

  /**
   * Exit utf8_3.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitUtf8_3(ExpressionConstraintParser.Utf8_3Context ctx) {
  }

  /**
   * Enter utf8_4.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterUtf8_4(ExpressionConstraintParser.Utf8_4Context ctx) {
  }

  /**
   * Exit utf8_4.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitUtf8_4(ExpressionConstraintParser.Utf8_4Context ctx) {
  }

  /**
   * Enter utf8_tail.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterUtf8_tail(ExpressionConstraintParser.Utf8_tailContext ctx) {
  }

  /**
   * Exit utf8_tail.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitUtf8_tail(ExpressionConstraintParser.Utf8_tailContext ctx) {
  }

  /**
   * Enter every rule.
   *
   * @param ctx the ctx
   */
  @Override
  public void enterEveryRule(ParserRuleContext ctx) {
  }

  /**
   * Exit every rule.
   *
   * @param ctx the ctx
   */
  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
  }

  /**
   * Visit terminal.
   *
   * @param node the node
   */
  @Override
  public void visitTerminal(TerminalNode node) {
  }

  /**
   * Visit error node.
   *
   * @param node the node
   */
  @Override
  public void visitErrorNode(ErrorNode node) {
  }
}
