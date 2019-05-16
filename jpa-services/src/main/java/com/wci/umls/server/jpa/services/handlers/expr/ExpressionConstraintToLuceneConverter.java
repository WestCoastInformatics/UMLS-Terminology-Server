package com.wci.umls.server.jpa.services.handlers.expr;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * The Class ExpressionConstraintToLuceneConverter.
 * 
 * NOTE: Adapted from Kai Kewley's SNOMED Query Service at
 * https://github.com/IHTSDO/snomed-query-service
 */
@SuppressWarnings("all")
public class ExpressionConstraintToLuceneConverter {

  /**
   * The Enum InternalFunction. NOTE: Changed to public from SQS and comments
   * added
   */
  public enum InternalFunction {

    /** The attribute descendant of. */
    ATTRIBUTE_DESCENDANT_OF(true, false, false),

    /** The attribute descendant or self of. */
    ATTRIBUTE_DESCENDANT_OR_SELF_OF(true, false, true),

    /** The attribute ancestor of. */
    ATTRIBUTE_ANCESTOR_OF(true, true, false),

    /** The attribute ancestor or self of. */
    ATTRIBUTE_ANCESTOR_OR_SELF_OF(true, true, true),

    /** The ancestor or self of. */
    ANCESTOR_OR_SELF_OF(false, true, true),

    /** The ancestor of. */
    ANCESTOR_OF(false, true, false);

    /** The attribute type. */
    private boolean attributeType;

    /** The ancestor type. */
    private boolean ancestorType;

    /** The include self. */
    private boolean includeSelf;

    /**
     * Instantiates a new internal function.
     *
     * @param attributeType the attribute type
     * @param ancestorType the ancestor type
     * @param includeSelf the include self
     */
    InternalFunction(boolean attributeType, boolean ancestorType,
        boolean includeSelf) {
      this.attributeType = attributeType;
      this.ancestorType = ancestorType;
      this.includeSelf = includeSelf;
    }

    /**
     * Checks if is attribute type.
     *
     * @return true, if is attribute type
     */
    public boolean isAttributeType() {
      return attributeType;
    }

    /**
     * Checks if is ancestor type.
     *
     * @return true, if is ancestor type
     */
    public boolean isAncestorType() {
      return ancestorType;
    }

    /**
     * Checks if is include self.
     *
     * @return true, if is include self
     */
    public boolean isIncludeSelf() {
      return includeSelf;
    }

  }

  /**
   * Parse.
   *
   * @param ecQuery the ec query
   * @return the string
   * @throws RecognitionException the recognition exception
   */
  public String parse(String ecQuery) throws RecognitionException {
    final ExpressionConstraintLexer lexer =
        new ExpressionConstraintLexer(CharStreams.fromString(ecQuery));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    final ExpressionConstraintParser parser =
        new ExpressionConstraintParser(tokens);
    final List<RecognitionException> exceptions = new ArrayList<>();
    parser.setErrorHandler(new ANTLRErrorStrategy() {
      @Override
      public void reportError(Parser parser, RecognitionException e) {
        exceptions.add(e);
      }

      @Override
      public void reset(Parser parser) {
      }

      @Override
      public Token recoverInline(Parser parser) throws RecognitionException {
        return null;
      }

      @Override
      public void recover(Parser parser, RecognitionException e)
        throws RecognitionException {
      }

      @Override
      public void sync(Parser parser) throws RecognitionException {
      }

      @Override
      public boolean inErrorRecoveryMode(Parser parser) {
        return false;
      }

      @Override
      public void reportMatch(Parser parser) {
      }
    });
    ParserRuleContext tree = parser.expressionconstraint();

    final ParseTreeWalker walker = new ParseTreeWalker();
    final ExpressionConstraintListener listener =
        new ExpressionConstraintListener();
    walker.walk(listener, tree);
    if (exceptions.isEmpty()) {
      return listener.getLuceneQuery();
    } else {
      final RecognitionException recognitionException = exceptions.get(0);
      throw recognitionException;
    }
  }

  /**
   * The listener interface for receiving expressionConstraint events. The class
   * that is interested in processing a expressionConstraint event implements
   * this interface, and the object created with that class is registered with a
   * component using the component's
   * addExpressionConstraintListener method. When
   * the expressionConstraint event occurs, that object's appropriate
   * method is invoked.
   *
   */
  protected static final class ExpressionConstraintListener extends
      ExpressionConstraintBaseListener {

    /** The lucene query. */
    private String luceneQuery = "";

    /** The in attribute. */
    private boolean inAttribute;

    @Override
    public void visitErrorNode(ErrorNode node) {
      super.visitErrorNode(node);
    }

    @Override
    public void enterSimpleexpressionconstraint(
      ExpressionConstraintParser.SimpleexpressionconstraintContext ctx) {
      final ExpressionConstraintParser.FocusconceptContext focusconcept =
          ctx.focusconcept();
      if (focusconcept.wildcard() != null) {
        if (!inAttribute) {
          luceneQuery += EclConceptFieldNames.ID + ":*";
        } else {
          luceneQuery += "*";
        }
      } else if (focusconcept.memberof() != null) {
        luceneQuery +=
            EclConceptFieldNames.MEMBER_OF + ":"
                + focusconcept.conceptreference().conceptid().getText();
      } else {
        final String conceptId =
            focusconcept.conceptreference().conceptid().getText();
        final ExpressionConstraintParser.ConstraintoperatorContext constraintoperator =
            ctx.constraintoperator();
        if (constraintoperator == null) {
          if (!inAttribute) {
            luceneQuery += EclConceptFieldNames.ID + ":";
          }
          luceneQuery += conceptId;
        } else {
          if (constraintoperator.descendantof() != null) {
            if (!inAttribute) {
              luceneQuery += EclConceptFieldNames.ANCESTOR + ":" + conceptId;
            } else {
              luceneQuery +=
                  InternalFunction.ATTRIBUTE_DESCENDANT_OF + "(" + conceptId
                      + ")";
            }
          } else if (constraintoperator.descendantorselfof() != null) {
            if (!inAttribute) {
              luceneQuery +=
                  "(" + EclConceptFieldNames.ID + ":" + conceptId + " OR "
                      + EclConceptFieldNames.ANCESTOR + ":" + conceptId + ")";
            } else {
              luceneQuery +=
                  InternalFunction.ATTRIBUTE_DESCENDANT_OR_SELF_OF + "("
                      + conceptId + ")";
            }
          } else if (constraintoperator.ancestororselfof() != null) {
            if (!inAttribute) {
              luceneQuery +=
                  InternalFunction.ANCESTOR_OR_SELF_OF + "(" + conceptId + ")";
            } else {
              luceneQuery +=
                  InternalFunction.ATTRIBUTE_ANCESTOR_OR_SELF_OF + "("
                      + conceptId + ")";
            }
          } else if (constraintoperator.ancestorof() != null) {
            if (!inAttribute) {
              luceneQuery +=
                  InternalFunction.ANCESTOR_OF + "(" + conceptId + ")";
            } else {
              luceneQuery +=
                  InternalFunction.ATTRIBUTE_ANCESTOR_OF + "(" + conceptId
                      + ")";
            }
          }
        }
      }
    }

    @Override
    public void enterRefinement(ExpressionConstraintParser.RefinementContext ctx) {
      luceneQuery += " AND ";
    }

    @Override
    public void enterAttribute(ExpressionConstraintParser.AttributeContext ctx) {
      inAttribute = true;
    }

   
    @Override
    public void exitAttribute(ExpressionConstraintParser.AttributeContext ctx) {
      inAttribute = false;
    }

    @Override
    public void enterAttributename(
      ExpressionConstraintParser.AttributenameContext ctx) {
      final ExpressionConstraintParser.ConceptreferenceContext conceptreference =
          ctx.conceptreference();
      if (conceptreference != null) {
        luceneQuery += conceptreference.conceptid().getText();
      } else {
        throwUnsupported("wildcard attributeName");
      }
    }

    @Override
    public void enterExpressioncomparisonoperator(
      ExpressionConstraintParser.ExpressioncomparisonoperatorContext ctx) {
      if (ctx.getText().equals("=")) {
        luceneQuery += ":";
      } else {
        throwUnsupported("not-equal expressionComparisonOperator");
      }
    }

    @Override
    public void enterConjunction(
      ExpressionConstraintParser.ConjunctionContext ctx) {
      luceneQuery += " AND ";
    }

    @Override
    public void enterDisjunction(
      ExpressionConstraintParser.DisjunctionContext ctx) {
      luceneQuery += " OR ";
    }

    @Override
    public void enterExclusion(ExpressionConstraintParser.ExclusionContext ctx) {
      luceneQuery += " NOT ";
    }

    @Override
    public void enterSubexpressionconstraint(
      ExpressionConstraintParser.SubexpressionconstraintContext ctx) {
      addLeftParenthesisIfNotNull(ctx.LEFT_PAREN());
    }

    @Override
    public void exitSubexpressionconstraint(
      ExpressionConstraintParser.SubexpressionconstraintContext ctx) {
      addRightParenthesisIfNotNull(ctx.RIGHT_PAREN());
    }

    @Override
    public void enterSubrefinement(
      ExpressionConstraintParser.SubrefinementContext ctx) {
      addLeftParenthesisIfNotNull(ctx.LEFT_PAREN());
    }

    @Override
    public void exitSubrefinement(
      ExpressionConstraintParser.SubrefinementContext ctx) {
      addRightParenthesisIfNotNull(ctx.RIGHT_PAREN());
    }

    @Override
    public void enterSubattributeset(
      ExpressionConstraintParser.SubattributesetContext ctx) {
      addLeftParenthesisIfNotNull(ctx.LEFT_PAREN());
    }

    @Override
    public void enterExpressionconstraintvalue(
      ExpressionConstraintParser.ExpressionconstraintvalueContext ctx) {
      if (ctx.refinedexpressionconstraint() != null
          || (ctx.compoundexpressionconstraint() != null && ctx
              .compoundexpressionconstraint().getText().contains(":"))) {
        throw new UnsupportedOperationException(
            "Within an expressionConstraintValue refinedExpressionConstraint is not currently supported.");
      }
      addLeftParenthesisIfNotNull(ctx.LEFT_PAREN());
    }

    @Override
    public void exitExpressionconstraintvalue(
      ExpressionConstraintParser.ExpressionconstraintvalueContext ctx) {
      addRightParenthesisIfNotNull(ctx.RIGHT_PAREN());
    }

    @Override
    public void exitSubattributeset(
      ExpressionConstraintParser.SubattributesetContext ctx) {
      addRightParenthesisIfNotNull(ctx.RIGHT_PAREN());
    }

    /**
     * Add left parenthesis if not null.
     *
     * @param terminalNode the terminal node
     */
    private void addLeftParenthesisIfNotNull(TerminalNode terminalNode) {
      if (terminalNode != null) {
        luceneQuery += " ( ";
      }
    }

    /**
     * Add right parenthesis if not null.
     *
     * @param terminalNode the terminal node
     */
    private void addRightParenthesisIfNotNull(TerminalNode terminalNode) {
      if (terminalNode != null) {
        luceneQuery += " ) ";
      }
    }

    // Unsupported enter methods below this line

    @Override
    public void enterCardinality(
      ExpressionConstraintParser.CardinalityContext ctx) {
      throwUnsupported("cardinality");
    }

    @Override
    public void enterAttributegroup(
      ExpressionConstraintParser.AttributegroupContext ctx) {
      throwUnsupported("attributeGroup");
    }


    @Override
    public void enterStringcomparisonoperator(
      ExpressionConstraintParser.StringcomparisonoperatorContext ctx) {
      throwUnsupported("stringComparisonOperator");
    }

    @Override
    public void enterNumericcomparisonoperator(
      ExpressionConstraintParser.NumericcomparisonoperatorContext ctx) {
      throwUnsupported("numericComparisonOperator");
    }

    @Override
    public void enterReverseflag(
      ExpressionConstraintParser.ReverseflagContext ctx) {
      throwUnsupported("reverseFlag");
    }

    /**
     * Throw unsupported.
     *
     * @param feature the feature
     */
    private void throwUnsupported(String feature) {
      throw new UnsupportedOperationException(feature
          + " is not currently supported.");
    }

    /**
     * Gets the lucene query.
     *
     * @return the lucene query
     */
    public String getLuceneQuery() {
      return luceneQuery;
    }
  }

}