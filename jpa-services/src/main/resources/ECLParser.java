// Generated from ECL.g4 by ANTLR 4.4
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ECLParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.4", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__35=1, T__34=2, T__33=3, T__32=4, T__31=5, T__30=6, T__29=7, T__28=8, 
		T__27=9, T__26=10, T__25=11, T__24=12, T__23=13, T__22=14, T__21=15, T__20=16, 
		T__19=17, T__18=18, T__17=19, T__16=20, T__15=21, T__14=22, T__13=23, 
		T__12=24, T__11=25, T__10=26, T__9=27, T__8=28, T__7=29, T__6=30, T__5=31, 
		T__4=32, T__3=33, T__2=34, T__1=35, T__0=36, MEMBEROF=37, WILDCARD=38, 
		DESCENDANTOF=39, DESCENDANTORSELFOF=40, ANCESTOROF=41, ANCESTORORSELFOF=42, 
		TO=43, MANY=44, REVERSEFLAG=45, EXPRESSIONCOMPARISONOPERATOR=46, NUMERICCOMPARISONOPERATOR=47, 
		STRINGCOMPARISONOPERATOR=48, SP=49, HTAB=50, CR=51, LF=52, QM=53, BS=54, 
		ALPHA=55, DIGIT=56, ZERO=57, DIGITNONZERO=58, WS1=59, WS2=60, ANY1=61, 
		ANY2=62, ANY3=63, UTF2=64, UTF3_HELPER1=65, UTF3_HELPER2=66, UTF3_HELPER3=67, 
		UTF3_HELPER4=68, UTF4_HELPER1=69, UTF4_HELPER2=70, UTF4_HELPER3=71, UTF8_TAIL=72;
	public static final String[] tokenNames = {
		"<INVALID>", "'o'", "'s'", "'u'", "'\\u002D'", "'{'", "'}'", "'A'", "'d'", 
		"'('", "'I'", "','", "'M'", "'.'", "'n'", "'O'", "'r'", "'S'", "'\\u002E'", 
		"'U'", "'\\u00F4'", "':'", "'['", "'|'", "'\\u00E0'", "']'", "'\\u00F0'", 
		"'a'", "'#'", "'D'", "'\\u00ED'", "'i'", "')'", "'+'", "'-'", "'m'", "'N'", 
		"'^'", "WILDCARD", "'<'", "'<<'", "'>'", "'>>'", "'..'", "MANY", "'R'", 
		"EXPRESSIONCOMPARISONOPERATOR", "NUMERICCOMPARISONOPERATOR", "STRINGCOMPARISONOPERATOR", 
		"' '", "'\\u0009'", "'\\u000D'", "'\\u000A'", "'\\u0022'", "'\\'", "ALPHA", 
		"DIGIT", "'\\u0030'", "DIGITNONZERO", "WS1", "WS2", "ANY1", "ANY2", "ANY3", 
		"UTF2", "UTF3_HELPER1", "UTF3_HELPER2", "UTF3_HELPER3", "UTF3_HELPER4", 
		"UTF4_HELPER1", "UTF4_HELPER2", "UTF4_HELPER3", "UTF8_TAIL"
	};
	public static final int
		RULE_expressionconstraint = 0, RULE_simpleexpressionconstraint = 1, RULE_refinedexpressionconstraint = 2, 
		RULE_compoundexpressionconstraint = 3, RULE_conjunctionexpressionconstraint = 4, 
		RULE_disjunctionexpressionconstraint = 5, RULE_exclusionexpressionconstraint = 6, 
		RULE_subexpressionconstraint = 7, RULE_focusconcept = 8, RULE_conceptreference = 9, 
		RULE_conceptid = 10, RULE_term = 11, RULE_constraintoperator = 12, RULE_conjunction = 13, 
		RULE_disjunction = 14, RULE_exclusion = 15, RULE_refinement = 16, RULE_conjunctionrefinementset = 17, 
		RULE_disjunctionrefinementset = 18, RULE_subrefinement = 19, RULE_attributeset = 20, 
		RULE_conjunctionattributeset = 21, RULE_disjunctionattributeset = 22, 
		RULE_subattributeset = 23, RULE_attributegroup = 24, RULE_attribute = 25, 
		RULE_cardinality = 26, RULE_attributeoperator = 27, RULE_attributename = 28, 
		RULE_expressionconstraintvalue = 29, RULE_numericvalue = 30, RULE_stringvalue = 31, 
		RULE_integervalue = 32, RULE_decimalvalue = 33, RULE_nonnegativeintegervalue = 34, 
		RULE_sctid = 35, RULE_ws = 36, RULE_mws = 37, RULE_alphanumericnonus = 38, 
		RULE_alphanumeric = 39, RULE_nonwsnonpipe = 40, RULE_anynonescapedchar = 41, 
		RULE_escapedchar = 42, RULE_utf8_2 = 43, RULE_utf8_3 = 44, RULE_utf8_4 = 45;
	public static final String[] ruleNames = {
		"expressionconstraint", "simpleexpressionconstraint", "refinedexpressionconstraint", 
		"compoundexpressionconstraint", "conjunctionexpressionconstraint", "disjunctionexpressionconstraint", 
		"exclusionexpressionconstraint", "subexpressionconstraint", "focusconcept", 
		"conceptreference", "conceptid", "term", "constraintoperator", "conjunction", 
		"disjunction", "exclusion", "refinement", "conjunctionrefinementset", 
		"disjunctionrefinementset", "subrefinement", "attributeset", "conjunctionattributeset", 
		"disjunctionattributeset", "subattributeset", "attributegroup", "attribute", 
		"cardinality", "attributeoperator", "attributename", "expressionconstraintvalue", 
		"numericvalue", "stringvalue", "integervalue", "decimalvalue", "nonnegativeintegervalue", 
		"sctid", "ws", "mws", "alphanumericnonus", "alphanumeric", "nonwsnonpipe", 
		"anynonescapedchar", "escapedchar", "utf8_2", "utf8_3", "utf8_4"
	};

	@Override
	public String getGrammarFileName() { return "ECL.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ECLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ExpressionconstraintContext extends ParserRuleContext {
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public SimpleexpressionconstraintContext simpleexpressionconstraint() {
			return getRuleContext(SimpleexpressionconstraintContext.class,0);
		}
		public CompoundexpressionconstraintContext compoundexpressionconstraint() {
			return getRuleContext(CompoundexpressionconstraintContext.class,0);
		}
		public RefinedexpressionconstraintContext refinedexpressionconstraint() {
			return getRuleContext(RefinedexpressionconstraintContext.class,0);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public ExpressionconstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionconstraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterExpressionconstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitExpressionconstraint(this);
		}
	}

	public final ExpressionconstraintContext expressionconstraint() throws RecognitionException {
		ExpressionconstraintContext _localctx = new ExpressionconstraintContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_expressionconstraint);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(92); ws();
			setState(96);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(93); refinedexpressionconstraint();
				}
				break;
			case 2:
				{
				setState(94); compoundexpressionconstraint();
				}
				break;
			case 3:
				{
				setState(95); simpleexpressionconstraint();
				}
				break;
			}
			setState(98); ws();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SimpleexpressionconstraintContext extends ParserRuleContext {
		public FocusconceptContext focusconcept() {
			return getRuleContext(FocusconceptContext.class,0);
		}
		public WsContext ws() {
			return getRuleContext(WsContext.class,0);
		}
		public ConstraintoperatorContext constraintoperator() {
			return getRuleContext(ConstraintoperatorContext.class,0);
		}
		public SimpleexpressionconstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleexpressionconstraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterSimpleexpressionconstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitSimpleexpressionconstraint(this);
		}
	}

	public final SimpleexpressionconstraintContext simpleexpressionconstraint() throws RecognitionException {
		SimpleexpressionconstraintContext _localctx = new SimpleexpressionconstraintContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_simpleexpressionconstraint);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DESCENDANTOF) | (1L << DESCENDANTORSELFOF) | (1L << ANCESTOROF) | (1L << ANCESTORORSELFOF))) != 0)) {
				{
				setState(100); constraintoperator();
				setState(101); ws();
				}
			}

			setState(105); focusconcept();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RefinedexpressionconstraintContext extends ParserRuleContext {
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public SimpleexpressionconstraintContext simpleexpressionconstraint() {
			return getRuleContext(SimpleexpressionconstraintContext.class,0);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public RefinementContext refinement() {
			return getRuleContext(RefinementContext.class,0);
		}
		public RefinedexpressionconstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_refinedexpressionconstraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterRefinedexpressionconstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitRefinedexpressionconstraint(this);
		}
	}

	public final RefinedexpressionconstraintContext refinedexpressionconstraint() throws RecognitionException {
		RefinedexpressionconstraintContext _localctx = new RefinedexpressionconstraintContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_refinedexpressionconstraint);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(107); simpleexpressionconstraint();
			setState(108); ws();
			setState(109); match(T__15);
			setState(110); ws();
			setState(111); refinement();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CompoundexpressionconstraintContext extends ParserRuleContext {
		public ConjunctionexpressionconstraintContext conjunctionexpressionconstraint() {
			return getRuleContext(ConjunctionexpressionconstraintContext.class,0);
		}
		public DisjunctionexpressionconstraintContext disjunctionexpressionconstraint() {
			return getRuleContext(DisjunctionexpressionconstraintContext.class,0);
		}
		public ExclusionexpressionconstraintContext exclusionexpressionconstraint() {
			return getRuleContext(ExclusionexpressionconstraintContext.class,0);
		}
		public CompoundexpressionconstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_compoundexpressionconstraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterCompoundexpressionconstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitCompoundexpressionconstraint(this);
		}
	}

	public final CompoundexpressionconstraintContext compoundexpressionconstraint() throws RecognitionException {
		CompoundexpressionconstraintContext _localctx = new CompoundexpressionconstraintContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_compoundexpressionconstraint);
		try {
			setState(116);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(113); conjunctionexpressionconstraint();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(114); disjunctionexpressionconstraint();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(115); exclusionexpressionconstraint();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConjunctionexpressionconstraintContext extends ParserRuleContext {
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public List<SubexpressionconstraintContext> subexpressionconstraint() {
			return getRuleContexts(SubexpressionconstraintContext.class);
		}
		public SubexpressionconstraintContext subexpressionconstraint(int i) {
			return getRuleContext(SubexpressionconstraintContext.class,i);
		}
		public List<ConjunctionContext> conjunction() {
			return getRuleContexts(ConjunctionContext.class);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public ConjunctionContext conjunction(int i) {
			return getRuleContext(ConjunctionContext.class,i);
		}
		public ConjunctionexpressionconstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conjunctionexpressionconstraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterConjunctionexpressionconstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitConjunctionexpressionconstraint(this);
		}
	}

	public final ConjunctionexpressionconstraintContext conjunctionexpressionconstraint() throws RecognitionException {
		ConjunctionexpressionconstraintContext _localctx = new ConjunctionexpressionconstraintContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_conjunctionexpressionconstraint);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(118); subexpressionconstraint();
			setState(124); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(119); ws();
					setState(120); conjunction();
					setState(121); ws();
					setState(122); subexpressionconstraint();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(126); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DisjunctionexpressionconstraintContext extends ParserRuleContext {
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public DisjunctionContext disjunction(int i) {
			return getRuleContext(DisjunctionContext.class,i);
		}
		public List<SubexpressionconstraintContext> subexpressionconstraint() {
			return getRuleContexts(SubexpressionconstraintContext.class);
		}
		public SubexpressionconstraintContext subexpressionconstraint(int i) {
			return getRuleContext(SubexpressionconstraintContext.class,i);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public List<DisjunctionContext> disjunction() {
			return getRuleContexts(DisjunctionContext.class);
		}
		public DisjunctionexpressionconstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_disjunctionexpressionconstraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterDisjunctionexpressionconstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitDisjunctionexpressionconstraint(this);
		}
	}

	public final DisjunctionexpressionconstraintContext disjunctionexpressionconstraint() throws RecognitionException {
		DisjunctionexpressionconstraintContext _localctx = new DisjunctionexpressionconstraintContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_disjunctionexpressionconstraint);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(128); subexpressionconstraint();
			setState(134); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(129); ws();
					setState(130); disjunction();
					setState(131); ws();
					setState(132); subexpressionconstraint();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(136); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExclusionexpressionconstraintContext extends ParserRuleContext {
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public ExclusionContext exclusion() {
			return getRuleContext(ExclusionContext.class,0);
		}
		public List<SubexpressionconstraintContext> subexpressionconstraint() {
			return getRuleContexts(SubexpressionconstraintContext.class);
		}
		public SubexpressionconstraintContext subexpressionconstraint(int i) {
			return getRuleContext(SubexpressionconstraintContext.class,i);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public ExclusionexpressionconstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exclusionexpressionconstraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterExclusionexpressionconstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitExclusionexpressionconstraint(this);
		}
	}

	public final ExclusionexpressionconstraintContext exclusionexpressionconstraint() throws RecognitionException {
		ExclusionexpressionconstraintContext _localctx = new ExclusionexpressionconstraintContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_exclusionexpressionconstraint);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(138); subexpressionconstraint();
			setState(139); ws();
			setState(140); exclusion();
			setState(141); ws();
			setState(142); subexpressionconstraint();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SubexpressionconstraintContext extends ParserRuleContext {
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public SimpleexpressionconstraintContext simpleexpressionconstraint() {
			return getRuleContext(SimpleexpressionconstraintContext.class,0);
		}
		public CompoundexpressionconstraintContext compoundexpressionconstraint() {
			return getRuleContext(CompoundexpressionconstraintContext.class,0);
		}
		public RefinedexpressionconstraintContext refinedexpressionconstraint() {
			return getRuleContext(RefinedexpressionconstraintContext.class,0);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public SubexpressionconstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subexpressionconstraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterSubexpressionconstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitSubexpressionconstraint(this);
		}
	}

	public final SubexpressionconstraintContext subexpressionconstraint() throws RecognitionException {
		SubexpressionconstraintContext _localctx = new SubexpressionconstraintContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_subexpressionconstraint);
		try {
			setState(154);
			switch (_input.LA(1)) {
			case T__18:
			case MEMBEROF:
			case WILDCARD:
			case DESCENDANTOF:
			case DESCENDANTORSELFOF:
			case ANCESTOROF:
			case ANCESTORORSELFOF:
			case ALPHA:
			case DIGIT:
				enterOuterAlt(_localctx, 1);
				{
				setState(144); simpleexpressionconstraint();
				}
				break;
			case T__27:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(145); match(T__27);
				setState(146); ws();
				setState(149);
				switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
				case 1:
					{
					setState(147); compoundexpressionconstraint();
					}
					break;
				case 2:
					{
					setState(148); refinedexpressionconstraint();
					}
					break;
				}
				setState(151); ws();
				setState(152); match(T__4);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FocusconceptContext extends ParserRuleContext {
		public TerminalNode MEMBEROF() { return getToken(ECLParser.MEMBEROF, 0); }
		public WsContext ws() {
			return getRuleContext(WsContext.class,0);
		}
		public ConceptreferenceContext conceptreference() {
			return getRuleContext(ConceptreferenceContext.class,0);
		}
		public TerminalNode WILDCARD() { return getToken(ECLParser.WILDCARD, 0); }
		public FocusconceptContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_focusconcept; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterFocusconcept(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitFocusconcept(this);
		}
	}

	public final FocusconceptContext focusconcept() throws RecognitionException {
		FocusconceptContext _localctx = new FocusconceptContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_focusconcept);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(158);
			_la = _input.LA(1);
			if (_la==MEMBEROF) {
				{
				setState(156); match(MEMBEROF);
				setState(157); ws();
				}
			}

			setState(162);
			switch (_input.LA(1)) {
			case T__18:
			case ALPHA:
			case DIGIT:
				{
				setState(160); conceptreference();
				}
				break;
			case WILDCARD:
				{
				setState(161); match(WILDCARD);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConceptreferenceContext extends ParserRuleContext {
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public ConceptidContext conceptid() {
			return getRuleContext(ConceptidContext.class,0);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public ConceptreferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conceptreference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterConceptreference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitConceptreference(this);
		}
	}

	public final ConceptreferenceContext conceptreference() throws RecognitionException {
		ConceptreferenceContext _localctx = new ConceptreferenceContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_conceptreference);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(164); conceptid();
			setState(172);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				{
				setState(165); ws();
				setState(166); match(T__13);
				setState(167); ws();
				setState(168); term();
				setState(169); ws();
				setState(170); match(T__13);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConceptidContext extends ParserRuleContext {
		public SctidContext sctid() {
			return getRuleContext(SctidContext.class,0);
		}
		public ConceptidContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conceptid; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterConceptid(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitConceptid(this);
		}
	}

	public final ConceptidContext conceptid() throws RecognitionException {
		ConceptidContext _localctx = new ConceptidContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_conceptid);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(174); sctid();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TermContext extends ParserRuleContext {
		public TerminalNode SP(int i) {
			return getToken(ECLParser.SP, i);
		}
		public List<NonwsnonpipeContext> nonwsnonpipe() {
			return getRuleContexts(NonwsnonpipeContext.class);
		}
		public NonwsnonpipeContext nonwsnonpipe(int i) {
			return getRuleContext(NonwsnonpipeContext.class,i);
		}
		public List<TerminalNode> SP() { return getTokens(ECLParser.SP); }
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitTerm(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_term);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(177); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(176); nonwsnonpipe();
				}
				}
				setState(179); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( ((((_la - 20)) & ~0x3f) == 0 && ((1L << (_la - 20)) & ((1L << (T__16 - 20)) | (1L << (T__12 - 20)) | (1L << (T__10 - 20)) | (1L << (T__6 - 20)) | (1L << (WS1 - 20)) | (1L << (WS2 - 20)) | (1L << (UTF2 - 20)) | (1L << (UTF3_HELPER2 - 20)) | (1L << (UTF3_HELPER4 - 20)) | (1L << (UTF4_HELPER2 - 20)))) != 0) );
			setState(193);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(182); 
					_errHandler.sync(this);
					_la = _input.LA(1);
					do {
						{
						{
						setState(181); match(SP);
						}
						}
						setState(184); 
						_errHandler.sync(this);
						_la = _input.LA(1);
					} while ( _la==SP );
					setState(187); 
					_errHandler.sync(this);
					_la = _input.LA(1);
					do {
						{
						{
						setState(186); nonwsnonpipe();
						}
						}
						setState(189); 
						_errHandler.sync(this);
						_la = _input.LA(1);
					} while ( ((((_la - 20)) & ~0x3f) == 0 && ((1L << (_la - 20)) & ((1L << (T__16 - 20)) | (1L << (T__12 - 20)) | (1L << (T__10 - 20)) | (1L << (T__6 - 20)) | (1L << (WS1 - 20)) | (1L << (WS2 - 20)) | (1L << (UTF2 - 20)) | (1L << (UTF3_HELPER2 - 20)) | (1L << (UTF3_HELPER4 - 20)) | (1L << (UTF4_HELPER2 - 20)))) != 0) );
					}
					} 
				}
				setState(195);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConstraintoperatorContext extends ParserRuleContext {
		public TerminalNode ANCESTORORSELFOF() { return getToken(ECLParser.ANCESTORORSELFOF, 0); }
		public TerminalNode ANCESTOROF() { return getToken(ECLParser.ANCESTOROF, 0); }
		public TerminalNode DESCENDANTORSELFOF() { return getToken(ECLParser.DESCENDANTORSELFOF, 0); }
		public TerminalNode DESCENDANTOF() { return getToken(ECLParser.DESCENDANTOF, 0); }
		public ConstraintoperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraintoperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterConstraintoperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitConstraintoperator(this);
		}
	}

	public final ConstraintoperatorContext constraintoperator() throws RecognitionException {
		ConstraintoperatorContext _localctx = new ConstraintoperatorContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_constraintoperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(196);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << DESCENDANTOF) | (1L << DESCENDANTORSELFOF) | (1L << ANCESTOROF) | (1L << ANCESTORORSELFOF))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConjunctionContext extends ParserRuleContext {
		public MwsContext mws() {
			return getRuleContext(MwsContext.class,0);
		}
		public ConjunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conjunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterConjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitConjunction(this);
		}
	}

	public final ConjunctionContext conjunction() throws RecognitionException {
		ConjunctionContext _localctx = new ConjunctionContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_conjunction);
		int _la;
		try {
			setState(203);
			switch (_input.LA(1)) {
			case T__29:
			case T__9:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(198);
				_la = _input.LA(1);
				if ( !(_la==T__29 || _la==T__9) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				setState(199);
				_la = _input.LA(1);
				if ( !(_la==T__22 || _la==T__0) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				setState(200);
				_la = _input.LA(1);
				if ( !(_la==T__28 || _la==T__7) ) {
				_errHandler.recoverInline(this);
				}
				consume();
				setState(201); mws();
				}
				}
				break;
			case T__25:
				enterOuterAlt(_localctx, 2);
				{
				setState(202); match(T__25);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DisjunctionContext extends ParserRuleContext {
		public MwsContext mws() {
			return getRuleContext(MwsContext.class,0);
		}
		public DisjunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_disjunction; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterDisjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitDisjunction(this);
		}
	}

	public final DisjunctionContext disjunction() throws RecognitionException {
		DisjunctionContext _localctx = new DisjunctionContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_disjunction);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(205);
			_la = _input.LA(1);
			if ( !(_la==T__35 || _la==T__21) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			setState(206);
			_la = _input.LA(1);
			if ( !(_la==T__20 || _la==REVERSEFLAG) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			setState(207); mws();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExclusionContext extends ParserRuleContext {
		public MwsContext mws() {
			return getRuleContext(MwsContext.class,0);
		}
		public ExclusionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exclusion; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterExclusion(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitExclusion(this);
		}
	}

	public final ExclusionContext exclusion() throws RecognitionException {
		ExclusionContext _localctx = new ExclusionContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_exclusion);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(209);
			_la = _input.LA(1);
			if ( !(_la==T__24 || _la==T__1) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			setState(210);
			_la = _input.LA(1);
			if ( !(_la==T__26 || _la==T__5) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			setState(211);
			_la = _input.LA(1);
			if ( !(_la==T__22 || _la==T__0) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			setState(212);
			_la = _input.LA(1);
			if ( !(_la==T__33 || _la==T__17) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			setState(213);
			_la = _input.LA(1);
			if ( !(_la==T__34 || _la==T__19) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			setState(214); mws();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RefinementContext extends ParserRuleContext {
		public DisjunctionrefinementsetContext disjunctionrefinementset() {
			return getRuleContext(DisjunctionrefinementsetContext.class,0);
		}
		public SubrefinementContext subrefinement() {
			return getRuleContext(SubrefinementContext.class,0);
		}
		public ConjunctionrefinementsetContext conjunctionrefinementset() {
			return getRuleContext(ConjunctionrefinementsetContext.class,0);
		}
		public WsContext ws() {
			return getRuleContext(WsContext.class,0);
		}
		public RefinementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_refinement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterRefinement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitRefinement(this);
		}
	}

	public final RefinementContext refinement() throws RecognitionException {
		RefinementContext _localctx = new RefinementContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_refinement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(216); subrefinement();
			setState(217); ws();
			setState(220);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				{
				setState(218); conjunctionrefinementset();
				}
				break;
			case 2:
				{
				setState(219); disjunctionrefinementset();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConjunctionrefinementsetContext extends ParserRuleContext {
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public List<SubrefinementContext> subrefinement() {
			return getRuleContexts(SubrefinementContext.class);
		}
		public List<ConjunctionContext> conjunction() {
			return getRuleContexts(ConjunctionContext.class);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public ConjunctionContext conjunction(int i) {
			return getRuleContext(ConjunctionContext.class,i);
		}
		public SubrefinementContext subrefinement(int i) {
			return getRuleContext(SubrefinementContext.class,i);
		}
		public ConjunctionrefinementsetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conjunctionrefinementset; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterConjunctionrefinementset(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitConjunctionrefinementset(this);
		}
	}

	public final ConjunctionrefinementsetContext conjunctionrefinementset() throws RecognitionException {
		ConjunctionrefinementsetContext _localctx = new ConjunctionrefinementsetContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_conjunctionrefinementset);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(227); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(222); ws();
					setState(223); conjunction();
					setState(224); ws();
					setState(225); subrefinement();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(229); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DisjunctionrefinementsetContext extends ParserRuleContext {
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public DisjunctionContext disjunction(int i) {
			return getRuleContext(DisjunctionContext.class,i);
		}
		public List<SubrefinementContext> subrefinement() {
			return getRuleContexts(SubrefinementContext.class);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public List<DisjunctionContext> disjunction() {
			return getRuleContexts(DisjunctionContext.class);
		}
		public SubrefinementContext subrefinement(int i) {
			return getRuleContext(SubrefinementContext.class,i);
		}
		public DisjunctionrefinementsetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_disjunctionrefinementset; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterDisjunctionrefinementset(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitDisjunctionrefinementset(this);
		}
	}

	public final DisjunctionrefinementsetContext disjunctionrefinementset() throws RecognitionException {
		DisjunctionrefinementsetContext _localctx = new DisjunctionrefinementsetContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_disjunctionrefinementset);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(236); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(231); ws();
					setState(232); disjunction();
					setState(233); ws();
					setState(234); subrefinement();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(238); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SubrefinementContext extends ParserRuleContext {
		public AttributegroupContext attributegroup() {
			return getRuleContext(AttributegroupContext.class,0);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public AttributesetContext attributeset() {
			return getRuleContext(AttributesetContext.class,0);
		}
		public RefinementContext refinement() {
			return getRuleContext(RefinementContext.class,0);
		}
		public SubrefinementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subrefinement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterSubrefinement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitSubrefinement(this);
		}
	}

	public final SubrefinementContext subrefinement() throws RecognitionException {
		SubrefinementContext _localctx = new SubrefinementContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_subrefinement);
		try {
			setState(248);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(240); attributeset();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(241); attributegroup();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				{
				setState(242); match(T__27);
				setState(243); ws();
				setState(244); refinement();
				setState(245); ws();
				setState(246); match(T__4);
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AttributesetContext extends ParserRuleContext {
		public SubattributesetContext subattributeset() {
			return getRuleContext(SubattributesetContext.class,0);
		}
		public WsContext ws() {
			return getRuleContext(WsContext.class,0);
		}
		public ConjunctionattributesetContext conjunctionattributeset() {
			return getRuleContext(ConjunctionattributesetContext.class,0);
		}
		public DisjunctionattributesetContext disjunctionattributeset() {
			return getRuleContext(DisjunctionattributesetContext.class,0);
		}
		public AttributesetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeset; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterAttributeset(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitAttributeset(this);
		}
	}

	public final AttributesetContext attributeset() throws RecognitionException {
		AttributesetContext _localctx = new AttributesetContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_attributeset);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(250); subattributeset();
			setState(251); ws();
			setState(254);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				{
				setState(252); conjunctionattributeset();
				}
				break;
			case 2:
				{
				setState(253); disjunctionattributeset();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConjunctionattributesetContext extends ParserRuleContext {
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public List<SubattributesetContext> subattributeset() {
			return getRuleContexts(SubattributesetContext.class);
		}
		public SubattributesetContext subattributeset(int i) {
			return getRuleContext(SubattributesetContext.class,i);
		}
		public List<ConjunctionContext> conjunction() {
			return getRuleContexts(ConjunctionContext.class);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public ConjunctionContext conjunction(int i) {
			return getRuleContext(ConjunctionContext.class,i);
		}
		public ConjunctionattributesetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conjunctionattributeset; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterConjunctionattributeset(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitConjunctionattributeset(this);
		}
	}

	public final ConjunctionattributesetContext conjunctionattributeset() throws RecognitionException {
		ConjunctionattributesetContext _localctx = new ConjunctionattributesetContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_conjunctionattributeset);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(261); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(256); ws();
					setState(257); conjunction();
					setState(258); ws();
					setState(259); subattributeset();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(263); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DisjunctionattributesetContext extends ParserRuleContext {
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public DisjunctionContext disjunction(int i) {
			return getRuleContext(DisjunctionContext.class,i);
		}
		public List<SubattributesetContext> subattributeset() {
			return getRuleContexts(SubattributesetContext.class);
		}
		public SubattributesetContext subattributeset(int i) {
			return getRuleContext(SubattributesetContext.class,i);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public List<DisjunctionContext> disjunction() {
			return getRuleContexts(DisjunctionContext.class);
		}
		public DisjunctionattributesetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_disjunctionattributeset; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterDisjunctionattributeset(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitDisjunctionattributeset(this);
		}
	}

	public final DisjunctionattributesetContext disjunctionattributeset() throws RecognitionException {
		DisjunctionattributesetContext _localctx = new DisjunctionattributesetContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_disjunctionattributeset);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(270); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(265); ws();
					setState(266); disjunction();
					setState(267); ws();
					setState(268); subattributeset();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(272); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SubattributesetContext extends ParserRuleContext {
		public AttributeContext attribute() {
			return getRuleContext(AttributeContext.class,0);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public AttributesetContext attributeset() {
			return getRuleContext(AttributesetContext.class,0);
		}
		public SubattributesetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_subattributeset; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterSubattributeset(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitSubattributeset(this);
		}
	}

	public final SubattributesetContext subattributeset() throws RecognitionException {
		SubattributesetContext _localctx = new SubattributesetContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_subattributeset);
		try {
			setState(281);
			switch (_input.LA(1)) {
			case T__18:
			case T__14:
			case WILDCARD:
			case DESCENDANTOF:
			case DESCENDANTORSELFOF:
			case REVERSEFLAG:
			case ALPHA:
			case DIGIT:
				enterOuterAlt(_localctx, 1);
				{
				setState(274); attribute();
				}
				break;
			case T__27:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(275); match(T__27);
				setState(276); ws();
				setState(277); attributeset();
				setState(278); ws();
				setState(279); match(T__4);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AttributegroupContext extends ParserRuleContext {
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public CardinalityContext cardinality() {
			return getRuleContext(CardinalityContext.class,0);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public AttributesetContext attributeset() {
			return getRuleContext(AttributesetContext.class,0);
		}
		public AttributegroupContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributegroup; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterAttributegroup(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitAttributegroup(this);
		}
	}

	public final AttributegroupContext attributegroup() throws RecognitionException {
		AttributegroupContext _localctx = new AttributegroupContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_attributegroup);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(286);
			_la = _input.LA(1);
			if (_la==T__14) {
				{
				setState(283); cardinality();
				setState(284); ws();
				}
			}

			setState(288); match(T__31);
			setState(289); ws();
			setState(290); attributeset();
			setState(291); ws();
			setState(292); match(T__30);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AttributeContext extends ParserRuleContext {
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public TerminalNode STRINGCOMPARISONOPERATOR() { return getToken(ECLParser.STRINGCOMPARISONOPERATOR, 0); }
		public StringvalueContext stringvalue() {
			return getRuleContext(StringvalueContext.class,0);
		}
		public CardinalityContext cardinality() {
			return getRuleContext(CardinalityContext.class,0);
		}
		public TerminalNode REVERSEFLAG() { return getToken(ECLParser.REVERSEFLAG, 0); }
		public AttributeoperatorContext attributeoperator() {
			return getRuleContext(AttributeoperatorContext.class,0);
		}
		public ExpressionconstraintvalueContext expressionconstraintvalue() {
			return getRuleContext(ExpressionconstraintvalueContext.class,0);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public TerminalNode NUMERICCOMPARISONOPERATOR() { return getToken(ECLParser.NUMERICCOMPARISONOPERATOR, 0); }
		public NumericvalueContext numericvalue() {
			return getRuleContext(NumericvalueContext.class,0);
		}
		public AttributenameContext attributename() {
			return getRuleContext(AttributenameContext.class,0);
		}
		public TerminalNode EXPRESSIONCOMPARISONOPERATOR() { return getToken(ECLParser.EXPRESSIONCOMPARISONOPERATOR, 0); }
		public AttributeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attribute; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterAttribute(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitAttribute(this);
		}
	}

	public final AttributeContext attribute() throws RecognitionException {
		AttributeContext _localctx = new AttributeContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_attribute);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(297);
			_la = _input.LA(1);
			if (_la==T__14) {
				{
				setState(294); cardinality();
				setState(295); ws();
				}
			}

			setState(301);
			_la = _input.LA(1);
			if (_la==REVERSEFLAG) {
				{
				setState(299); match(REVERSEFLAG);
				setState(300); ws();
				}
			}

			setState(306);
			_la = _input.LA(1);
			if (_la==DESCENDANTOF || _la==DESCENDANTORSELFOF) {
				{
				setState(303); attributeoperator();
				setState(304); ws();
				}
			}

			setState(308); attributename();
			setState(309); ws();
			setState(322);
			switch (_input.LA(1)) {
			case EXPRESSIONCOMPARISONOPERATOR:
				{
				{
				setState(310); match(EXPRESSIONCOMPARISONOPERATOR);
				setState(311); ws();
				setState(312); expressionconstraintvalue();
				}
				}
				break;
			case NUMERICCOMPARISONOPERATOR:
				{
				{
				setState(314); match(NUMERICCOMPARISONOPERATOR);
				setState(315); ws();
				setState(316); numericvalue();
				}
				}
				break;
			case STRINGCOMPARISONOPERATOR:
				{
				{
				setState(318); match(STRINGCOMPARISONOPERATOR);
				setState(319); ws();
				setState(320); stringvalue();
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CardinalityContext extends ParserRuleContext {
		public NonnegativeintegervalueContext nonnegativeintegervalue(int i) {
			return getRuleContext(NonnegativeintegervalueContext.class,i);
		}
		public List<NonnegativeintegervalueContext> nonnegativeintegervalue() {
			return getRuleContexts(NonnegativeintegervalueContext.class);
		}
		public TerminalNode MANY() { return getToken(ECLParser.MANY, 0); }
		public TerminalNode TO() { return getToken(ECLParser.TO, 0); }
		public CardinalityContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_cardinality; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterCardinality(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitCardinality(this);
		}
	}

	public final CardinalityContext cardinality() throws RecognitionException {
		CardinalityContext _localctx = new CardinalityContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_cardinality);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(324); match(T__14);
			setState(325); nonnegativeintegervalue();
			setState(326); match(TO);
			setState(329);
			switch (_input.LA(1)) {
			case ZERO:
			case DIGITNONZERO:
				{
				setState(327); nonnegativeintegervalue();
				}
				break;
			case MANY:
				{
				setState(328); match(MANY);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(331); match(T__11);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AttributeoperatorContext extends ParserRuleContext {
		public TerminalNode DESCENDANTORSELFOF() { return getToken(ECLParser.DESCENDANTORSELFOF, 0); }
		public TerminalNode DESCENDANTOF() { return getToken(ECLParser.DESCENDANTOF, 0); }
		public AttributeoperatorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributeoperator; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterAttributeoperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitAttributeoperator(this);
		}
	}

	public final AttributeoperatorContext attributeoperator() throws RecognitionException {
		AttributeoperatorContext _localctx = new AttributeoperatorContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_attributeoperator);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(333);
			_la = _input.LA(1);
			if ( !(_la==DESCENDANTOF || _la==DESCENDANTORSELFOF) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AttributenameContext extends ParserRuleContext {
		public ConceptreferenceContext conceptreference() {
			return getRuleContext(ConceptreferenceContext.class,0);
		}
		public TerminalNode WILDCARD() { return getToken(ECLParser.WILDCARD, 0); }
		public AttributenameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_attributename; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterAttributename(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitAttributename(this);
		}
	}

	public final AttributenameContext attributename() throws RecognitionException {
		AttributenameContext _localctx = new AttributenameContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_attributename);
		try {
			setState(337);
			switch (_input.LA(1)) {
			case T__18:
			case ALPHA:
			case DIGIT:
				enterOuterAlt(_localctx, 1);
				{
				setState(335); conceptreference();
				}
				break;
			case WILDCARD:
				enterOuterAlt(_localctx, 2);
				{
				setState(336); match(WILDCARD);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionconstraintvalueContext extends ParserRuleContext {
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public SimpleexpressionconstraintContext simpleexpressionconstraint() {
			return getRuleContext(SimpleexpressionconstraintContext.class,0);
		}
		public CompoundexpressionconstraintContext compoundexpressionconstraint() {
			return getRuleContext(CompoundexpressionconstraintContext.class,0);
		}
		public RefinedexpressionconstraintContext refinedexpressionconstraint() {
			return getRuleContext(RefinedexpressionconstraintContext.class,0);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public ExpressionconstraintvalueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionconstraintvalue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterExpressionconstraintvalue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitExpressionconstraintvalue(this);
		}
	}

	public final ExpressionconstraintvalueContext expressionconstraintvalue() throws RecognitionException {
		ExpressionconstraintvalueContext _localctx = new ExpressionconstraintvalueContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_expressionconstraintvalue);
		try {
			setState(349);
			switch (_input.LA(1)) {
			case T__18:
			case MEMBEROF:
			case WILDCARD:
			case DESCENDANTOF:
			case DESCENDANTORSELFOF:
			case ANCESTOROF:
			case ANCESTORORSELFOF:
			case ALPHA:
			case DIGIT:
				enterOuterAlt(_localctx, 1);
				{
				setState(339); simpleexpressionconstraint();
				}
				break;
			case T__27:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(340); match(T__27);
				setState(341); ws();
				setState(344);
				switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
				case 1:
					{
					setState(342); refinedexpressionconstraint();
					}
					break;
				case 2:
					{
					setState(343); compoundexpressionconstraint();
					}
					break;
				}
				setState(346); ws();
				setState(347); match(T__4);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumericvalueContext extends ParserRuleContext {
		public DecimalvalueContext decimalvalue() {
			return getRuleContext(DecimalvalueContext.class,0);
		}
		public IntegervalueContext integervalue() {
			return getRuleContext(IntegervalueContext.class,0);
		}
		public NumericvalueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_numericvalue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterNumericvalue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitNumericvalue(this);
		}
	}

	public final NumericvalueContext numericvalue() throws RecognitionException {
		NumericvalueContext _localctx = new NumericvalueContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_numericvalue);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(351); match(T__8);
			setState(354);
			switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
			case 1:
				{
				setState(352); decimalvalue();
				}
				break;
			case 2:
				{
				setState(353); integervalue();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StringvalueContext extends ParserRuleContext {
		public TerminalNode QM(int i) {
			return getToken(ECLParser.QM, i);
		}
		public List<EscapedcharContext> escapedchar() {
			return getRuleContexts(EscapedcharContext.class);
		}
		public List<AnynonescapedcharContext> anynonescapedchar() {
			return getRuleContexts(AnynonescapedcharContext.class);
		}
		public List<TerminalNode> QM() { return getTokens(ECLParser.QM); }
		public EscapedcharContext escapedchar(int i) {
			return getRuleContext(EscapedcharContext.class,i);
		}
		public AnynonescapedcharContext anynonescapedchar(int i) {
			return getRuleContext(AnynonescapedcharContext.class,i);
		}
		public StringvalueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringvalue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterStringvalue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitStringvalue(this);
		}
	}

	public final StringvalueContext stringvalue() throws RecognitionException {
		StringvalueContext _localctx = new StringvalueContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_stringvalue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(356); match(QM);
			setState(359); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				setState(359);
				switch (_input.LA(1)) {
				case T__16:
				case T__12:
				case T__10:
				case T__6:
				case HTAB:
				case CR:
				case LF:
				case ANY1:
				case ANY2:
				case ANY3:
				case UTF2:
				case UTF3_HELPER2:
				case UTF3_HELPER4:
				case UTF4_HELPER2:
					{
					setState(357); anynonescapedchar();
					}
					break;
				case BS:
					{
					setState(358); escapedchar();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(361); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( ((((_la - 20)) & ~0x3f) == 0 && ((1L << (_la - 20)) & ((1L << (T__16 - 20)) | (1L << (T__12 - 20)) | (1L << (T__10 - 20)) | (1L << (T__6 - 20)) | (1L << (HTAB - 20)) | (1L << (CR - 20)) | (1L << (LF - 20)) | (1L << (BS - 20)) | (1L << (ANY1 - 20)) | (1L << (ANY2 - 20)) | (1L << (ANY3 - 20)) | (1L << (UTF2 - 20)) | (1L << (UTF3_HELPER2 - 20)) | (1L << (UTF3_HELPER4 - 20)) | (1L << (UTF4_HELPER2 - 20)))) != 0) );
			setState(363); match(QM);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IntegervalueContext extends ParserRuleContext {
		public TerminalNode ZERO() { return getToken(ECLParser.ZERO, 0); }
		public TerminalNode DIGITNONZERO() { return getToken(ECLParser.DIGITNONZERO, 0); }
		public TerminalNode DIGIT(int i) {
			return getToken(ECLParser.DIGIT, i);
		}
		public List<TerminalNode> DIGIT() { return getTokens(ECLParser.DIGIT); }
		public IntegervalueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integervalue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterIntegervalue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitIntegervalue(this);
		}
	}

	public final IntegervalueContext integervalue() throws RecognitionException {
		IntegervalueContext _localctx = new IntegervalueContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_integervalue);
		int _la;
		try {
			setState(376);
			switch (_input.LA(1)) {
			case T__3:
			case T__2:
			case DIGITNONZERO:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(366);
				_la = _input.LA(1);
				if (_la==T__3 || _la==T__2) {
					{
					setState(365);
					_la = _input.LA(1);
					if ( !(_la==T__3 || _la==T__2) ) {
					_errHandler.recoverInline(this);
					}
					consume();
					}
				}

				setState(368); match(DIGITNONZERO);
				setState(372);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==DIGIT) {
					{
					{
					setState(369); match(DIGIT);
					}
					}
					setState(374);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				}
				break;
			case ZERO:
				enterOuterAlt(_localctx, 2);
				{
				setState(375); match(ZERO);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DecimalvalueContext extends ParserRuleContext {
		public TerminalNode DIGIT(int i) {
			return getToken(ECLParser.DIGIT, i);
		}
		public List<TerminalNode> DIGIT() { return getTokens(ECLParser.DIGIT); }
		public IntegervalueContext integervalue() {
			return getRuleContext(IntegervalueContext.class,0);
		}
		public DecimalvalueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_decimalvalue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterDecimalvalue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitDecimalvalue(this);
		}
	}

	public final DecimalvalueContext decimalvalue() throws RecognitionException {
		DecimalvalueContext _localctx = new DecimalvalueContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_decimalvalue);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(378); integervalue();
			setState(379); match(T__23);
			setState(381); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(380); match(DIGIT);
				}
				}
				setState(383); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==DIGIT );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NonnegativeintegervalueContext extends ParserRuleContext {
		public TerminalNode ZERO() { return getToken(ECLParser.ZERO, 0); }
		public TerminalNode DIGITNONZERO() { return getToken(ECLParser.DIGITNONZERO, 0); }
		public TerminalNode DIGIT(int i) {
			return getToken(ECLParser.DIGIT, i);
		}
		public List<TerminalNode> DIGIT() { return getTokens(ECLParser.DIGIT); }
		public NonnegativeintegervalueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonnegativeintegervalue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterNonnegativeintegervalue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitNonnegativeintegervalue(this);
		}
	}

	public final NonnegativeintegervalueContext nonnegativeintegervalue() throws RecognitionException {
		NonnegativeintegervalueContext _localctx = new NonnegativeintegervalueContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_nonnegativeintegervalue);
		int _la;
		try {
			setState(393);
			switch (_input.LA(1)) {
			case DIGITNONZERO:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(385); match(DIGITNONZERO);
				setState(389);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==DIGIT) {
					{
					{
					setState(386); match(DIGIT);
					}
					}
					setState(391);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				}
				break;
			case ZERO:
				enterOuterAlt(_localctx, 2);
				{
				setState(392); match(ZERO);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SctidContext extends ParserRuleContext {
		public AlphanumericnonusContext alphanumericnonus() {
			return getRuleContext(AlphanumericnonusContext.class,0);
		}
		public List<AlphanumericContext> alphanumeric() {
			return getRuleContexts(AlphanumericContext.class);
		}
		public AlphanumericContext alphanumeric(int i) {
			return getRuleContext(AlphanumericContext.class,i);
		}
		public SctidContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sctid; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterSctid(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitSctid(this);
		}
	}

	public final SctidContext sctid() throws RecognitionException {
		SctidContext _localctx = new SctidContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_sctid);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(395); alphanumericnonus();
			{
			setState(396); alphanumeric();
			}
			{
			setState(397); alphanumeric();
			}
			{
			setState(398); alphanumeric();
			}
			setState(519);
			switch ( getInterpreter().adaptivePredict(_input,42,_ctx) ) {
			case 1:
				{
				setState(400);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__32) | (1L << T__18) | (1L << ALPHA) | (1L << DIGIT))) != 0)) {
					{
					setState(399); alphanumeric();
					}
				}

				}
				break;
			case 2:
				{
				{
				{
				setState(402); alphanumeric();
				}
				{
				setState(403); alphanumeric();
				}
				}
				}
				break;
			case 3:
				{
				{
				{
				setState(405); alphanumeric();
				}
				{
				setState(406); alphanumeric();
				}
				{
				setState(407); alphanumeric();
				}
				}
				}
				break;
			case 4:
				{
				{
				{
				setState(409); alphanumeric();
				}
				{
				setState(410); alphanumeric();
				}
				{
				setState(411); alphanumeric();
				}
				{
				setState(412); alphanumeric();
				}
				}
				}
				break;
			case 5:
				{
				{
				{
				setState(414); alphanumeric();
				}
				{
				setState(415); alphanumeric();
				}
				{
				setState(416); alphanumeric();
				}
				{
				setState(417); alphanumeric();
				}
				{
				setState(418); alphanumeric();
				}
				}
				}
				break;
			case 6:
				{
				{
				{
				setState(420); alphanumeric();
				}
				{
				setState(421); alphanumeric();
				}
				{
				setState(422); alphanumeric();
				}
				{
				setState(423); alphanumeric();
				}
				{
				setState(424); alphanumeric();
				}
				{
				setState(425); alphanumeric();
				}
				}
				}
				break;
			case 7:
				{
				{
				{
				setState(427); alphanumeric();
				}
				{
				setState(428); alphanumeric();
				}
				{
				setState(429); alphanumeric();
				}
				{
				setState(430); alphanumeric();
				}
				{
				setState(431); alphanumeric();
				}
				{
				setState(432); alphanumeric();
				}
				{
				setState(433); alphanumeric();
				}
				}
				}
				break;
			case 8:
				{
				{
				{
				setState(435); alphanumeric();
				}
				{
				setState(436); alphanumeric();
				}
				{
				setState(437); alphanumeric();
				}
				{
				setState(438); alphanumeric();
				}
				{
				setState(439); alphanumeric();
				}
				{
				setState(440); alphanumeric();
				}
				{
				setState(441); alphanumeric();
				}
				{
				setState(442); alphanumeric();
				}
				}
				}
				break;
			case 9:
				{
				{
				{
				setState(444); alphanumeric();
				}
				{
				setState(445); alphanumeric();
				}
				{
				setState(446); alphanumeric();
				}
				{
				setState(447); alphanumeric();
				}
				{
				setState(448); alphanumeric();
				}
				{
				setState(449); alphanumeric();
				}
				{
				setState(450); alphanumeric();
				}
				{
				setState(451); alphanumeric();
				}
				{
				setState(452); alphanumeric();
				}
				}
				}
				break;
			case 10:
				{
				{
				{
				setState(454); alphanumeric();
				}
				{
				setState(455); alphanumeric();
				}
				{
				setState(456); alphanumeric();
				}
				{
				setState(457); alphanumeric();
				}
				{
				setState(458); alphanumeric();
				}
				{
				setState(459); alphanumeric();
				}
				{
				setState(460); alphanumeric();
				}
				{
				setState(461); alphanumeric();
				}
				{
				setState(462); alphanumeric();
				}
				{
				setState(463); alphanumeric();
				}
				}
				}
				break;
			case 11:
				{
				{
				{
				setState(465); alphanumeric();
				}
				{
				setState(466); alphanumeric();
				}
				{
				setState(467); alphanumeric();
				}
				{
				setState(468); alphanumeric();
				}
				{
				setState(469); alphanumeric();
				}
				{
				setState(470); alphanumeric();
				}
				{
				setState(471); alphanumeric();
				}
				{
				setState(472); alphanumeric();
				}
				{
				setState(473); alphanumeric();
				}
				{
				setState(474); alphanumeric();
				}
				{
				setState(475); alphanumeric();
				}
				}
				}
				break;
			case 12:
				{
				{
				{
				setState(477); alphanumeric();
				}
				{
				setState(478); alphanumeric();
				}
				{
				setState(479); alphanumeric();
				}
				{
				setState(480); alphanumeric();
				}
				{
				setState(481); alphanumeric();
				}
				{
				setState(482); alphanumeric();
				}
				{
				setState(483); alphanumeric();
				}
				{
				setState(484); alphanumeric();
				}
				{
				setState(485); alphanumeric();
				}
				{
				setState(486); alphanumeric();
				}
				{
				setState(487); alphanumeric();
				}
				{
				setState(488); alphanumeric();
				}
				}
				}
				break;
			case 13:
				{
				{
				{
				setState(490); alphanumeric();
				}
				{
				setState(491); alphanumeric();
				}
				{
				setState(492); alphanumeric();
				}
				{
				setState(493); alphanumeric();
				}
				{
				setState(494); alphanumeric();
				}
				{
				setState(495); alphanumeric();
				}
				{
				setState(496); alphanumeric();
				}
				{
				setState(497); alphanumeric();
				}
				{
				setState(498); alphanumeric();
				}
				{
				setState(499); alphanumeric();
				}
				{
				setState(500); alphanumeric();
				}
				{
				setState(501); alphanumeric();
				}
				{
				setState(502); alphanumeric();
				}
				}
				}
				break;
			case 14:
				{
				{
				{
				setState(504); alphanumeric();
				}
				{
				setState(505); alphanumeric();
				}
				{
				setState(506); alphanumeric();
				}
				{
				setState(507); alphanumeric();
				}
				{
				setState(508); alphanumeric();
				}
				{
				setState(509); alphanumeric();
				}
				{
				setState(510); alphanumeric();
				}
				{
				setState(511); alphanumeric();
				}
				{
				setState(512); alphanumeric();
				}
				{
				setState(513); alphanumeric();
				}
				{
				setState(514); alphanumeric();
				}
				{
				setState(515); alphanumeric();
				}
				{
				setState(516); alphanumeric();
				}
				{
				setState(517); alphanumeric();
				}
				}
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class WsContext extends ParserRuleContext {
		public List<TerminalNode> LF() { return getTokens(ECLParser.LF); }
		public TerminalNode SP(int i) {
			return getToken(ECLParser.SP, i);
		}
		public TerminalNode HTAB(int i) {
			return getToken(ECLParser.HTAB, i);
		}
		public TerminalNode LF(int i) {
			return getToken(ECLParser.LF, i);
		}
		public List<TerminalNode> CR() { return getTokens(ECLParser.CR); }
		public List<TerminalNode> SP() { return getTokens(ECLParser.SP); }
		public List<TerminalNode> HTAB() { return getTokens(ECLParser.HTAB); }
		public TerminalNode CR(int i) {
			return getToken(ECLParser.CR, i);
		}
		public WsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ws; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterWs(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitWs(this);
		}
	}

	public final WsContext ws() throws RecognitionException {
		WsContext _localctx = new WsContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_ws);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(524);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,43,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(521);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << SP) | (1L << HTAB) | (1L << CR) | (1L << LF))) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					consume();
					}
					} 
				}
				setState(526);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,43,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class MwsContext extends ParserRuleContext {
		public List<TerminalNode> LF() { return getTokens(ECLParser.LF); }
		public TerminalNode SP(int i) {
			return getToken(ECLParser.SP, i);
		}
		public TerminalNode HTAB(int i) {
			return getToken(ECLParser.HTAB, i);
		}
		public TerminalNode LF(int i) {
			return getToken(ECLParser.LF, i);
		}
		public List<TerminalNode> CR() { return getTokens(ECLParser.CR); }
		public List<TerminalNode> SP() { return getTokens(ECLParser.SP); }
		public List<TerminalNode> HTAB() { return getTokens(ECLParser.HTAB); }
		public TerminalNode CR(int i) {
			return getToken(ECLParser.CR, i);
		}
		public MwsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mws; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterMws(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitMws(this);
		}
	}

	public final MwsContext mws() throws RecognitionException {
		MwsContext _localctx = new MwsContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_mws);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(528); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(527);
					_la = _input.LA(1);
					if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << SP) | (1L << HTAB) | (1L << CR) | (1L << LF))) != 0)) ) {
					_errHandler.recoverInline(this);
					}
					consume();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(530); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,44,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AlphanumericnonusContext extends ParserRuleContext {
		public TerminalNode DIGIT() { return getToken(ECLParser.DIGIT, 0); }
		public TerminalNode ALPHA() { return getToken(ECLParser.ALPHA, 0); }
		public AlphanumericnonusContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alphanumericnonus; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterAlphanumericnonus(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitAlphanumericnonus(this);
		}
	}

	public final AlphanumericnonusContext alphanumericnonus() throws RecognitionException {
		AlphanumericnonusContext _localctx = new AlphanumericnonusContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_alphanumericnonus);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(532);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__18) | (1L << ALPHA) | (1L << DIGIT))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AlphanumericContext extends ParserRuleContext {
		public TerminalNode DIGIT() { return getToken(ECLParser.DIGIT, 0); }
		public TerminalNode ALPHA() { return getToken(ECLParser.ALPHA, 0); }
		public AlphanumericContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alphanumeric; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterAlphanumeric(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitAlphanumeric(this);
		}
	}

	public final AlphanumericContext alphanumeric() throws RecognitionException {
		AlphanumericContext _localctx = new AlphanumericContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_alphanumeric);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(534);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__32) | (1L << T__18) | (1L << ALPHA) | (1L << DIGIT))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			consume();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NonwsnonpipeContext extends ParserRuleContext {
		public Utf8_4Context utf8_4() {
			return getRuleContext(Utf8_4Context.class,0);
		}
		public TerminalNode WS1() { return getToken(ECLParser.WS1, 0); }
		public TerminalNode WS2() { return getToken(ECLParser.WS2, 0); }
		public Utf8_3Context utf8_3() {
			return getRuleContext(Utf8_3Context.class,0);
		}
		public Utf8_2Context utf8_2() {
			return getRuleContext(Utf8_2Context.class,0);
		}
		public NonwsnonpipeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nonwsnonpipe; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterNonwsnonpipe(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitNonwsnonpipe(this);
		}
	}

	public final NonwsnonpipeContext nonwsnonpipe() throws RecognitionException {
		NonwsnonpipeContext _localctx = new NonwsnonpipeContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_nonwsnonpipe);
		try {
			setState(541);
			switch (_input.LA(1)) {
			case WS1:
				enterOuterAlt(_localctx, 1);
				{
				setState(536); match(WS1);
				}
				break;
			case WS2:
				enterOuterAlt(_localctx, 2);
				{
				setState(537); match(WS2);
				}
				break;
			case UTF2:
				enterOuterAlt(_localctx, 3);
				{
				setState(538); utf8_2();
				}
				break;
			case T__12:
			case T__6:
			case UTF3_HELPER2:
			case UTF3_HELPER4:
				enterOuterAlt(_localctx, 4);
				{
				setState(539); utf8_3();
				}
				break;
			case T__16:
			case T__10:
			case UTF4_HELPER2:
				enterOuterAlt(_localctx, 5);
				{
				setState(540); utf8_4();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AnynonescapedcharContext extends ParserRuleContext {
		public TerminalNode LF() { return getToken(ECLParser.LF, 0); }
		public TerminalNode ANY3() { return getToken(ECLParser.ANY3, 0); }
		public TerminalNode ANY1() { return getToken(ECLParser.ANY1, 0); }
		public Utf8_4Context utf8_4() {
			return getRuleContext(Utf8_4Context.class,0);
		}
		public TerminalNode CR() { return getToken(ECLParser.CR, 0); }
		public TerminalNode ANY2() { return getToken(ECLParser.ANY2, 0); }
		public Utf8_3Context utf8_3() {
			return getRuleContext(Utf8_3Context.class,0);
		}
		public TerminalNode HTAB() { return getToken(ECLParser.HTAB, 0); }
		public Utf8_2Context utf8_2() {
			return getRuleContext(Utf8_2Context.class,0);
		}
		public AnynonescapedcharContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_anynonescapedchar; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterAnynonescapedchar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitAnynonescapedchar(this);
		}
	}

	public final AnynonescapedcharContext anynonescapedchar() throws RecognitionException {
		AnynonescapedcharContext _localctx = new AnynonescapedcharContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_anynonescapedchar);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(552);
			switch (_input.LA(1)) {
			case HTAB:
				{
				setState(543); match(HTAB);
				}
				break;
			case CR:
				{
				setState(544); match(CR);
				}
				break;
			case LF:
				{
				setState(545); match(LF);
				}
				break;
			case ANY1:
				{
				setState(546); match(ANY1);
				}
				break;
			case ANY2:
				{
				setState(547); match(ANY2);
				}
				break;
			case ANY3:
				{
				setState(548); match(ANY3);
				}
				break;
			case UTF2:
				{
				setState(549); utf8_2();
				}
				break;
			case T__12:
			case T__6:
			case UTF3_HELPER2:
			case UTF3_HELPER4:
				{
				setState(550); utf8_3();
				}
				break;
			case T__16:
			case T__10:
			case UTF4_HELPER2:
				{
				setState(551); utf8_4();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class EscapedcharContext extends ParserRuleContext {
		public List<TerminalNode> BS() { return getTokens(ECLParser.BS); }
		public TerminalNode BS(int i) {
			return getToken(ECLParser.BS, i);
		}
		public TerminalNode QM() { return getToken(ECLParser.QM, 0); }
		public EscapedcharContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_escapedchar; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterEscapedchar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitEscapedchar(this);
		}
	}

	public final EscapedcharContext escapedchar() throws RecognitionException {
		EscapedcharContext _localctx = new EscapedcharContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_escapedchar);
		try {
			setState(558);
			switch ( getInterpreter().adaptivePredict(_input,47,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(554); match(BS);
				setState(555); match(QM);
				}
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(556); match(BS);
				setState(557); match(BS);
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Utf8_2Context extends ParserRuleContext {
		public TerminalNode UTF2() { return getToken(ECLParser.UTF2, 0); }
		public TerminalNode UTF8_TAIL() { return getToken(ECLParser.UTF8_TAIL, 0); }
		public Utf8_2Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_utf8_2; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterUtf8_2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitUtf8_2(this);
		}
	}

	public final Utf8_2Context utf8_2() throws RecognitionException {
		Utf8_2Context _localctx = new Utf8_2Context(_ctx, getState());
		enterRule(_localctx, 86, RULE_utf8_2);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(560); match(UTF2);
			setState(561); match(UTF8_TAIL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Utf8_3Context extends ParserRuleContext {
		public TerminalNode UTF3_HELPER2() { return getToken(ECLParser.UTF3_HELPER2, 0); }
		public TerminalNode UTF3_HELPER3() { return getToken(ECLParser.UTF3_HELPER3, 0); }
		public TerminalNode UTF3_HELPER4() { return getToken(ECLParser.UTF3_HELPER4, 0); }
		public TerminalNode UTF8_TAIL(int i) {
			return getToken(ECLParser.UTF8_TAIL, i);
		}
		public List<TerminalNode> UTF8_TAIL() { return getTokens(ECLParser.UTF8_TAIL); }
		public TerminalNode UTF3_HELPER1() { return getToken(ECLParser.UTF3_HELPER1, 0); }
		public Utf8_3Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_utf8_3; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterUtf8_3(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitUtf8_3(this);
		}
	}

	public final Utf8_3Context utf8_3() throws RecognitionException {
		Utf8_3Context _localctx = new Utf8_3Context(_ctx, getState());
		enterRule(_localctx, 88, RULE_utf8_3);
		try {
			setState(575);
			switch (_input.LA(1)) {
			case T__12:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(563); match(T__12);
				setState(564); match(UTF3_HELPER1);
				setState(565); match(UTF8_TAIL);
				}
				}
				break;
			case UTF3_HELPER2:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(566); match(UTF3_HELPER2);
				{
				setState(567); match(UTF8_TAIL);
				}
				{
				setState(568); match(UTF8_TAIL);
				}
				}
				}
				break;
			case T__6:
				enterOuterAlt(_localctx, 3);
				{
				{
				setState(569); match(T__6);
				setState(570); match(UTF3_HELPER3);
				setState(571); match(UTF8_TAIL);
				}
				}
				break;
			case UTF3_HELPER4:
				enterOuterAlt(_localctx, 4);
				{
				{
				setState(572); match(UTF3_HELPER4);
				{
				setState(573); match(UTF8_TAIL);
				}
				{
				setState(574); match(UTF8_TAIL);
				}
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class Utf8_4Context extends ParserRuleContext {
		public TerminalNode UTF4_HELPER1() { return getToken(ECLParser.UTF4_HELPER1, 0); }
		public TerminalNode UTF4_HELPER3() { return getToken(ECLParser.UTF4_HELPER3, 0); }
		public TerminalNode UTF8_TAIL(int i) {
			return getToken(ECLParser.UTF8_TAIL, i);
		}
		public TerminalNode UTF4_HELPER2() { return getToken(ECLParser.UTF4_HELPER2, 0); }
		public List<TerminalNode> UTF8_TAIL() { return getTokens(ECLParser.UTF8_TAIL); }
		public Utf8_4Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_utf8_4; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).enterUtf8_4(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ECLListener ) ((ECLListener)listener).exitUtf8_4(this);
		}
	}

	public final Utf8_4Context utf8_4() throws RecognitionException {
		Utf8_4Context _localctx = new Utf8_4Context(_ctx, getState());
		enterRule(_localctx, 90, RULE_utf8_4);
		try {
			setState(589);
			switch (_input.LA(1)) {
			case T__10:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(577); match(T__10);
				setState(578); match(UTF4_HELPER1);
				{
				setState(579); match(UTF8_TAIL);
				}
				{
				setState(580); match(UTF8_TAIL);
				}
				}
				}
				break;
			case UTF4_HELPER2:
				enterOuterAlt(_localctx, 2);
				{
				{
				setState(581); match(UTF4_HELPER2);
				{
				setState(582); match(UTF8_TAIL);
				}
				{
				setState(583); match(UTF8_TAIL);
				}
				{
				setState(584); match(UTF8_TAIL);
				}
				}
				}
				break;
			case T__16:
				enterOuterAlt(_localctx, 3);
				{
				{
				setState(585); match(T__16);
				setState(586); match(UTF4_HELPER3);
				{
				setState(587); match(UTF8_TAIL);
				}
				{
				setState(588); match(UTF8_TAIL);
				}
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3J\u0252\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\3\2\3\2\3\2\3\2\5\2c\n\2\3\2\3\2\3\3\3\3\3\3\5"+
		"\3j\n\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\5\5w\n\5\3\6\3\6\3"+
		"\6\3\6\3\6\3\6\6\6\177\n\6\r\6\16\6\u0080\3\7\3\7\3\7\3\7\3\7\3\7\6\7"+
		"\u0089\n\7\r\7\16\7\u008a\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t"+
		"\5\t\u0098\n\t\3\t\3\t\3\t\5\t\u009d\n\t\3\n\3\n\5\n\u00a1\n\n\3\n\3\n"+
		"\5\n\u00a5\n\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u00af\n\13"+
		"\3\f\3\f\3\r\6\r\u00b4\n\r\r\r\16\r\u00b5\3\r\6\r\u00b9\n\r\r\r\16\r\u00ba"+
		"\3\r\6\r\u00be\n\r\r\r\16\r\u00bf\7\r\u00c2\n\r\f\r\16\r\u00c5\13\r\3"+
		"\16\3\16\3\17\3\17\3\17\3\17\3\17\5\17\u00ce\n\17\3\20\3\20\3\20\3\20"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\5\22\u00df\n\22"+
		"\3\23\3\23\3\23\3\23\3\23\6\23\u00e6\n\23\r\23\16\23\u00e7\3\24\3\24\3"+
		"\24\3\24\3\24\6\24\u00ef\n\24\r\24\16\24\u00f0\3\25\3\25\3\25\3\25\3\25"+
		"\3\25\3\25\3\25\5\25\u00fb\n\25\3\26\3\26\3\26\3\26\5\26\u0101\n\26\3"+
		"\27\3\27\3\27\3\27\3\27\6\27\u0108\n\27\r\27\16\27\u0109\3\30\3\30\3\30"+
		"\3\30\3\30\6\30\u0111\n\30\r\30\16\30\u0112\3\31\3\31\3\31\3\31\3\31\3"+
		"\31\3\31\5\31\u011c\n\31\3\32\3\32\3\32\5\32\u0121\n\32\3\32\3\32\3\32"+
		"\3\32\3\32\3\32\3\33\3\33\3\33\5\33\u012c\n\33\3\33\3\33\5\33\u0130\n"+
		"\33\3\33\3\33\3\33\5\33\u0135\n\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33"+
		"\3\33\3\33\3\33\3\33\3\33\3\33\3\33\5\33\u0145\n\33\3\34\3\34\3\34\3\34"+
		"\3\34\5\34\u014c\n\34\3\34\3\34\3\35\3\35\3\36\3\36\5\36\u0154\n\36\3"+
		"\37\3\37\3\37\3\37\3\37\5\37\u015b\n\37\3\37\3\37\3\37\5\37\u0160\n\37"+
		"\3 \3 \3 \5 \u0165\n \3!\3!\3!\6!\u016a\n!\r!\16!\u016b\3!\3!\3\"\5\""+
		"\u0171\n\"\3\"\3\"\7\"\u0175\n\"\f\"\16\"\u0178\13\"\3\"\5\"\u017b\n\""+
		"\3#\3#\3#\6#\u0180\n#\r#\16#\u0181\3$\3$\7$\u0186\n$\f$\16$\u0189\13$"+
		"\3$\5$\u018c\n$\3%\3%\3%\3%\3%\5%\u0193\n%\3%\3%\3%\3%\3%\3%\3%\3%\3%"+
		"\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%"+
		"\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%"+
		"\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%"+
		"\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%"+
		"\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\5%\u020a\n%\3&\7&\u020d"+
		"\n&\f&\16&\u0210\13&\3\'\6\'\u0213\n\'\r\'\16\'\u0214\3(\3(\3)\3)\3*\3"+
		"*\3*\3*\3*\5*\u0220\n*\3+\3+\3+\3+\3+\3+\3+\3+\3+\5+\u022b\n+\3,\3,\3"+
		",\3,\5,\u0231\n,\3-\3-\3-\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\3.\5.\u0242"+
		"\n.\3/\3/\3/\3/\3/\3/\3/\3/\3/\3/\3/\3/\5/\u0250\n/\3/\2\2\60\2\4\6\b"+
		"\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJLNPRTVX"+
		"Z\\\2\21\3\2),\4\2\t\t\35\35\4\2\20\20&&\4\2\n\n\37\37\4\2\3\3\21\21\4"+
		"\2\22\22//\4\2\16\16%%\4\2\f\f!!\4\2\5\5\25\25\4\2\4\4\23\23\3\2)*\3\2"+
		"#$\3\2\63\66\4\2\24\249:\5\2\6\6\24\249:\u0274\2^\3\2\2\2\4i\3\2\2\2\6"+
		"m\3\2\2\2\bv\3\2\2\2\nx\3\2\2\2\f\u0082\3\2\2\2\16\u008c\3\2\2\2\20\u009c"+
		"\3\2\2\2\22\u00a0\3\2\2\2\24\u00a6\3\2\2\2\26\u00b0\3\2\2\2\30\u00b3\3"+
		"\2\2\2\32\u00c6\3\2\2\2\34\u00cd\3\2\2\2\36\u00cf\3\2\2\2 \u00d3\3\2\2"+
		"\2\"\u00da\3\2\2\2$\u00e5\3\2\2\2&\u00ee\3\2\2\2(\u00fa\3\2\2\2*\u00fc"+
		"\3\2\2\2,\u0107\3\2\2\2.\u0110\3\2\2\2\60\u011b\3\2\2\2\62\u0120\3\2\2"+
		"\2\64\u012b\3\2\2\2\66\u0146\3\2\2\28\u014f\3\2\2\2:\u0153\3\2\2\2<\u015f"+
		"\3\2\2\2>\u0161\3\2\2\2@\u0166\3\2\2\2B\u017a\3\2\2\2D\u017c\3\2\2\2F"+
		"\u018b\3\2\2\2H\u018d\3\2\2\2J\u020e\3\2\2\2L\u0212\3\2\2\2N\u0216\3\2"+
		"\2\2P\u0218\3\2\2\2R\u021f\3\2\2\2T\u022a\3\2\2\2V\u0230\3\2\2\2X\u0232"+
		"\3\2\2\2Z\u0241\3\2\2\2\\\u024f\3\2\2\2^b\5J&\2_c\5\6\4\2`c\5\b\5\2ac"+
		"\5\4\3\2b_\3\2\2\2b`\3\2\2\2ba\3\2\2\2cd\3\2\2\2de\5J&\2e\3\3\2\2\2fg"+
		"\5\32\16\2gh\5J&\2hj\3\2\2\2if\3\2\2\2ij\3\2\2\2jk\3\2\2\2kl\5\22\n\2"+
		"l\5\3\2\2\2mn\5\4\3\2no\5J&\2op\7\27\2\2pq\5J&\2qr\5\"\22\2r\7\3\2\2\2"+
		"sw\5\n\6\2tw\5\f\7\2uw\5\16\b\2vs\3\2\2\2vt\3\2\2\2vu\3\2\2\2w\t\3\2\2"+
		"\2x~\5\20\t\2yz\5J&\2z{\5\34\17\2{|\5J&\2|}\5\20\t\2}\177\3\2\2\2~y\3"+
		"\2\2\2\177\u0080\3\2\2\2\u0080~\3\2\2\2\u0080\u0081\3\2\2\2\u0081\13\3"+
		"\2\2\2\u0082\u0088\5\20\t\2\u0083\u0084\5J&\2\u0084\u0085\5\36\20\2\u0085"+
		"\u0086\5J&\2\u0086\u0087\5\20\t\2\u0087\u0089\3\2\2\2\u0088\u0083\3\2"+
		"\2\2\u0089\u008a\3\2\2\2\u008a\u0088\3\2\2\2\u008a\u008b\3\2\2\2\u008b"+
		"\r\3\2\2\2\u008c\u008d\5\20\t\2\u008d\u008e\5J&\2\u008e\u008f\5 \21\2"+
		"\u008f\u0090\5J&\2\u0090\u0091\5\20\t\2\u0091\17\3\2\2\2\u0092\u009d\5"+
		"\4\3\2\u0093\u0094\7\13\2\2\u0094\u0097\5J&\2\u0095\u0098\5\b\5\2\u0096"+
		"\u0098\5\6\4\2\u0097\u0095\3\2\2\2\u0097\u0096\3\2\2\2\u0098\u0099\3\2"+
		"\2\2\u0099\u009a\5J&\2\u009a\u009b\7\"\2\2\u009b\u009d\3\2\2\2\u009c\u0092"+
		"\3\2\2\2\u009c\u0093\3\2\2\2\u009d\21\3\2\2\2\u009e\u009f\7\'\2\2\u009f"+
		"\u00a1\5J&\2\u00a0\u009e\3\2\2\2\u00a0\u00a1\3\2\2\2\u00a1\u00a4\3\2\2"+
		"\2\u00a2\u00a5\5\24\13\2\u00a3\u00a5\7(\2\2\u00a4\u00a2\3\2\2\2\u00a4"+
		"\u00a3\3\2\2\2\u00a5\23\3\2\2\2\u00a6\u00ae\5\26\f\2\u00a7\u00a8\5J&\2"+
		"\u00a8\u00a9\7\31\2\2\u00a9\u00aa\5J&\2\u00aa\u00ab\5\30\r\2\u00ab\u00ac"+
		"\5J&\2\u00ac\u00ad\7\31\2\2\u00ad\u00af\3\2\2\2\u00ae\u00a7\3\2\2\2\u00ae"+
		"\u00af\3\2\2\2\u00af\25\3\2\2\2\u00b0\u00b1\5H%\2\u00b1\27\3\2\2\2\u00b2"+
		"\u00b4\5R*\2\u00b3\u00b2\3\2\2\2\u00b4\u00b5\3\2\2\2\u00b5\u00b3\3\2\2"+
		"\2\u00b5\u00b6\3\2\2\2\u00b6\u00c3\3\2\2\2\u00b7\u00b9\7\63\2\2\u00b8"+
		"\u00b7\3\2\2\2\u00b9\u00ba\3\2\2\2\u00ba\u00b8\3\2\2\2\u00ba\u00bb\3\2"+
		"\2\2\u00bb\u00bd\3\2\2\2\u00bc\u00be\5R*\2\u00bd\u00bc\3\2\2\2\u00be\u00bf"+
		"\3\2\2\2\u00bf\u00bd\3\2\2\2\u00bf\u00c0\3\2\2\2\u00c0\u00c2\3\2\2\2\u00c1"+
		"\u00b8\3\2\2\2\u00c2\u00c5\3\2\2\2\u00c3\u00c1\3\2\2\2\u00c3\u00c4\3\2"+
		"\2\2\u00c4\31\3\2\2\2\u00c5\u00c3\3\2\2\2\u00c6\u00c7\t\2\2\2\u00c7\33"+
		"\3\2\2\2\u00c8\u00c9\t\3\2\2\u00c9\u00ca\t\4\2\2\u00ca\u00cb\t\5\2\2\u00cb"+
		"\u00ce\5L\'\2\u00cc\u00ce\7\r\2\2\u00cd\u00c8\3\2\2\2\u00cd\u00cc\3\2"+
		"\2\2\u00ce\35\3\2\2\2\u00cf\u00d0\t\6\2\2\u00d0\u00d1\t\7\2\2\u00d1\u00d2"+
		"\5L\'\2\u00d2\37\3\2\2\2\u00d3\u00d4\t\b\2\2\u00d4\u00d5\t\t\2\2\u00d5"+
		"\u00d6\t\4\2\2\u00d6\u00d7\t\n\2\2\u00d7\u00d8\t\13\2\2\u00d8\u00d9\5"+
		"L\'\2\u00d9!\3\2\2\2\u00da\u00db\5(\25\2\u00db\u00de\5J&\2\u00dc\u00df"+
		"\5$\23\2\u00dd\u00df\5&\24\2\u00de\u00dc\3\2\2\2\u00de\u00dd\3\2\2\2\u00de"+
		"\u00df\3\2\2\2\u00df#\3\2\2\2\u00e0\u00e1\5J&\2\u00e1\u00e2\5\34\17\2"+
		"\u00e2\u00e3\5J&\2\u00e3\u00e4\5(\25\2\u00e4\u00e6\3\2\2\2\u00e5\u00e0"+
		"\3\2\2\2\u00e6\u00e7\3\2\2\2\u00e7\u00e5\3\2\2\2\u00e7\u00e8\3\2\2\2\u00e8"+
		"%\3\2\2\2\u00e9\u00ea\5J&\2\u00ea\u00eb\5\36\20\2\u00eb\u00ec\5J&\2\u00ec"+
		"\u00ed\5(\25\2\u00ed\u00ef\3\2\2\2\u00ee\u00e9\3\2\2\2\u00ef\u00f0\3\2"+
		"\2\2\u00f0\u00ee\3\2\2\2\u00f0\u00f1\3\2\2\2\u00f1\'\3\2\2\2\u00f2\u00fb"+
		"\5*\26\2\u00f3\u00fb\5\62\32\2\u00f4\u00f5\7\13\2\2\u00f5\u00f6\5J&\2"+
		"\u00f6\u00f7\5\"\22\2\u00f7\u00f8\5J&\2\u00f8\u00f9\7\"\2\2\u00f9\u00fb"+
		"\3\2\2\2\u00fa\u00f2\3\2\2\2\u00fa\u00f3\3\2\2\2\u00fa\u00f4\3\2\2\2\u00fb"+
		")\3\2\2\2\u00fc\u00fd\5\60\31\2\u00fd\u0100\5J&\2\u00fe\u0101\5,\27\2"+
		"\u00ff\u0101\5.\30\2\u0100\u00fe\3\2\2\2\u0100\u00ff\3\2\2\2\u0100\u0101"+
		"\3\2\2\2\u0101+\3\2\2\2\u0102\u0103\5J&\2\u0103\u0104\5\34\17\2\u0104"+
		"\u0105\5J&\2\u0105\u0106\5\60\31\2\u0106\u0108\3\2\2\2\u0107\u0102\3\2"+
		"\2\2\u0108\u0109\3\2\2\2\u0109\u0107\3\2\2\2\u0109\u010a\3\2\2\2\u010a"+
		"-\3\2\2\2\u010b\u010c\5J&\2\u010c\u010d\5\36\20\2\u010d\u010e\5J&\2\u010e"+
		"\u010f\5\60\31\2\u010f\u0111\3\2\2\2\u0110\u010b\3\2\2\2\u0111\u0112\3"+
		"\2\2\2\u0112\u0110\3\2\2\2\u0112\u0113\3\2\2\2\u0113/\3\2\2\2\u0114\u011c"+
		"\5\64\33\2\u0115\u0116\7\13\2\2\u0116\u0117\5J&\2\u0117\u0118\5*\26\2"+
		"\u0118\u0119\5J&\2\u0119\u011a\7\"\2\2\u011a\u011c\3\2\2\2\u011b\u0114"+
		"\3\2\2\2\u011b\u0115\3\2\2\2\u011c\61\3\2\2\2\u011d\u011e\5\66\34\2\u011e"+
		"\u011f\5J&\2\u011f\u0121\3\2\2\2\u0120\u011d\3\2\2\2\u0120\u0121\3\2\2"+
		"\2\u0121\u0122\3\2\2\2\u0122\u0123\7\7\2\2\u0123\u0124\5J&\2\u0124\u0125"+
		"\5*\26\2\u0125\u0126\5J&\2\u0126\u0127\7\b\2\2\u0127\63\3\2\2\2\u0128"+
		"\u0129\5\66\34\2\u0129\u012a\5J&\2\u012a\u012c\3\2\2\2\u012b\u0128\3\2"+
		"\2\2\u012b\u012c\3\2\2\2\u012c\u012f\3\2\2\2\u012d\u012e\7/\2\2\u012e"+
		"\u0130\5J&\2\u012f\u012d\3\2\2\2\u012f\u0130\3\2\2\2\u0130\u0134\3\2\2"+
		"\2\u0131\u0132\58\35\2\u0132\u0133\5J&\2\u0133\u0135\3\2\2\2\u0134\u0131"+
		"\3\2\2\2\u0134\u0135\3\2\2\2\u0135\u0136\3\2\2\2\u0136\u0137\5:\36\2\u0137"+
		"\u0144\5J&\2\u0138\u0139\7\60\2\2\u0139\u013a\5J&\2\u013a\u013b\5<\37"+
		"\2\u013b\u0145\3\2\2\2\u013c\u013d\7\61\2\2\u013d\u013e\5J&\2\u013e\u013f"+
		"\5> \2\u013f\u0145\3\2\2\2\u0140\u0141\7\62\2\2\u0141\u0142\5J&\2\u0142"+
		"\u0143\5@!\2\u0143\u0145\3\2\2\2\u0144\u0138\3\2\2\2\u0144\u013c\3\2\2"+
		"\2\u0144\u0140\3\2\2\2\u0145\65\3\2\2\2\u0146\u0147\7\30\2\2\u0147\u0148"+
		"\5F$\2\u0148\u014b\7-\2\2\u0149\u014c\5F$\2\u014a\u014c\7.\2\2\u014b\u0149"+
		"\3\2\2\2\u014b\u014a\3\2\2\2\u014c\u014d\3\2\2\2\u014d\u014e\7\33\2\2"+
		"\u014e\67\3\2\2\2\u014f\u0150\t\f\2\2\u01509\3\2\2\2\u0151\u0154\5\24"+
		"\13\2\u0152\u0154\7(\2\2\u0153\u0151\3\2\2\2\u0153\u0152\3\2\2\2\u0154"+
		";\3\2\2\2\u0155\u0160\5\4\3\2\u0156\u0157\7\13\2\2\u0157\u015a\5J&\2\u0158"+
		"\u015b\5\6\4\2\u0159\u015b\5\b\5\2\u015a\u0158\3\2\2\2\u015a\u0159\3\2"+
		"\2\2\u015b\u015c\3\2\2\2\u015c\u015d\5J&\2\u015d\u015e\7\"\2\2\u015e\u0160"+
		"\3\2\2\2\u015f\u0155\3\2\2\2\u015f\u0156\3\2\2\2\u0160=\3\2\2\2\u0161"+
		"\u0164\7\36\2\2\u0162\u0165\5D#\2\u0163\u0165\5B\"\2\u0164\u0162\3\2\2"+
		"\2\u0164\u0163\3\2\2\2\u0165?\3\2\2\2\u0166\u0169\7\67\2\2\u0167\u016a"+
		"\5T+\2\u0168\u016a\5V,\2\u0169\u0167\3\2\2\2\u0169\u0168\3\2\2\2\u016a"+
		"\u016b\3\2\2\2\u016b\u0169\3\2\2\2\u016b\u016c\3\2\2\2\u016c\u016d\3\2"+
		"\2\2\u016d\u016e\7\67\2\2\u016eA\3\2\2\2\u016f\u0171\t\r\2\2\u0170\u016f"+
		"\3\2\2\2\u0170\u0171\3\2\2\2\u0171\u0172\3\2\2\2\u0172\u0176\7<\2\2\u0173"+
		"\u0175\7:\2\2\u0174\u0173\3\2\2\2\u0175\u0178\3\2\2\2\u0176\u0174\3\2"+
		"\2\2\u0176\u0177\3\2\2\2\u0177\u017b\3\2\2\2\u0178\u0176\3\2\2\2\u0179"+
		"\u017b\7;\2\2\u017a\u0170\3\2\2\2\u017a\u0179\3\2\2\2\u017bC\3\2\2\2\u017c"+
		"\u017d\5B\"\2\u017d\u017f\7\17\2\2\u017e\u0180\7:\2\2\u017f\u017e\3\2"+
		"\2\2\u0180\u0181\3\2\2\2\u0181\u017f\3\2\2\2\u0181\u0182\3\2\2\2\u0182"+
		"E\3\2\2\2\u0183\u0187\7<\2\2\u0184\u0186\7:\2\2\u0185\u0184\3\2\2\2\u0186"+
		"\u0189\3\2\2\2\u0187\u0185\3\2\2\2\u0187\u0188\3\2\2\2\u0188\u018c\3\2"+
		"\2\2\u0189\u0187\3\2\2\2\u018a\u018c\7;\2\2\u018b\u0183\3\2\2\2\u018b"+
		"\u018a\3\2\2\2\u018cG\3\2\2\2\u018d\u018e\5N(\2\u018e\u018f\5P)\2\u018f"+
		"\u0190\5P)\2\u0190\u0209\5P)\2\u0191\u0193\5P)\2\u0192\u0191\3\2\2\2\u0192"+
		"\u0193\3\2\2\2\u0193\u020a\3\2\2\2\u0194\u0195\5P)\2\u0195\u0196\5P)\2"+
		"\u0196\u020a\3\2\2\2\u0197\u0198\5P)\2\u0198\u0199\5P)\2\u0199\u019a\5"+
		"P)\2\u019a\u020a\3\2\2\2\u019b\u019c\5P)\2\u019c\u019d\5P)\2\u019d\u019e"+
		"\5P)\2\u019e\u019f\5P)\2\u019f\u020a\3\2\2\2\u01a0\u01a1\5P)\2\u01a1\u01a2"+
		"\5P)\2\u01a2\u01a3\5P)\2\u01a3\u01a4\5P)\2\u01a4\u01a5\5P)\2\u01a5\u020a"+
		"\3\2\2\2\u01a6\u01a7\5P)\2\u01a7\u01a8\5P)\2\u01a8\u01a9\5P)\2\u01a9\u01aa"+
		"\5P)\2\u01aa\u01ab\5P)\2\u01ab\u01ac\5P)\2\u01ac\u020a\3\2\2\2\u01ad\u01ae"+
		"\5P)\2\u01ae\u01af\5P)\2\u01af\u01b0\5P)\2\u01b0\u01b1\5P)\2\u01b1\u01b2"+
		"\5P)\2\u01b2\u01b3\5P)\2\u01b3\u01b4\5P)\2\u01b4\u020a\3\2\2\2\u01b5\u01b6"+
		"\5P)\2\u01b6\u01b7\5P)\2\u01b7\u01b8\5P)\2\u01b8\u01b9\5P)\2\u01b9\u01ba"+
		"\5P)\2\u01ba\u01bb\5P)\2\u01bb\u01bc\5P)\2\u01bc\u01bd\5P)\2\u01bd\u020a"+
		"\3\2\2\2\u01be\u01bf\5P)\2\u01bf\u01c0\5P)\2\u01c0\u01c1\5P)\2\u01c1\u01c2"+
		"\5P)\2\u01c2\u01c3\5P)\2\u01c3\u01c4\5P)\2\u01c4\u01c5\5P)\2\u01c5\u01c6"+
		"\5P)\2\u01c6\u01c7\5P)\2\u01c7\u020a\3\2\2\2\u01c8\u01c9\5P)\2\u01c9\u01ca"+
		"\5P)\2\u01ca\u01cb\5P)\2\u01cb\u01cc\5P)\2\u01cc\u01cd\5P)\2\u01cd\u01ce"+
		"\5P)\2\u01ce\u01cf\5P)\2\u01cf\u01d0\5P)\2\u01d0\u01d1\5P)\2\u01d1\u01d2"+
		"\5P)\2\u01d2\u020a\3\2\2\2\u01d3\u01d4\5P)\2\u01d4\u01d5\5P)\2\u01d5\u01d6"+
		"\5P)\2\u01d6\u01d7\5P)\2\u01d7\u01d8\5P)\2\u01d8\u01d9\5P)\2\u01d9\u01da"+
		"\5P)\2\u01da\u01db\5P)\2\u01db\u01dc\5P)\2\u01dc\u01dd\5P)\2\u01dd\u01de"+
		"\5P)\2\u01de\u020a\3\2\2\2\u01df\u01e0\5P)\2\u01e0\u01e1\5P)\2\u01e1\u01e2"+
		"\5P)\2\u01e2\u01e3\5P)\2\u01e3\u01e4\5P)\2\u01e4\u01e5\5P)\2\u01e5\u01e6"+
		"\5P)\2\u01e6\u01e7\5P)\2\u01e7\u01e8\5P)\2\u01e8\u01e9\5P)\2\u01e9\u01ea"+
		"\5P)\2\u01ea\u01eb\5P)\2\u01eb\u020a\3\2\2\2\u01ec\u01ed\5P)\2\u01ed\u01ee"+
		"\5P)\2\u01ee\u01ef\5P)\2\u01ef\u01f0\5P)\2\u01f0\u01f1\5P)\2\u01f1\u01f2"+
		"\5P)\2\u01f2\u01f3\5P)\2\u01f3\u01f4\5P)\2\u01f4\u01f5\5P)\2\u01f5\u01f6"+
		"\5P)\2\u01f6\u01f7\5P)\2\u01f7\u01f8\5P)\2\u01f8\u01f9\5P)\2\u01f9\u020a"+
		"\3\2\2\2\u01fa\u01fb\5P)\2\u01fb\u01fc\5P)\2\u01fc\u01fd\5P)\2\u01fd\u01fe"+
		"\5P)\2\u01fe\u01ff\5P)\2\u01ff\u0200\5P)\2\u0200\u0201\5P)\2\u0201\u0202"+
		"\5P)\2\u0202\u0203\5P)\2\u0203\u0204\5P)\2\u0204\u0205\5P)\2\u0205\u0206"+
		"\5P)\2\u0206\u0207\5P)\2\u0207\u0208\5P)\2\u0208\u020a\3\2\2\2\u0209\u0192"+
		"\3\2\2\2\u0209\u0194\3\2\2\2\u0209\u0197\3\2\2\2\u0209\u019b\3\2\2\2\u0209"+
		"\u01a0\3\2\2\2\u0209\u01a6\3\2\2\2\u0209\u01ad\3\2\2\2\u0209\u01b5\3\2"+
		"\2\2\u0209\u01be\3\2\2\2\u0209\u01c8\3\2\2\2\u0209\u01d3\3\2\2\2\u0209"+
		"\u01df\3\2\2\2\u0209\u01ec\3\2\2\2\u0209\u01fa\3\2\2\2\u020aI\3\2\2\2"+
		"\u020b\u020d\t\16\2\2\u020c\u020b\3\2\2\2\u020d\u0210\3\2\2\2\u020e\u020c"+
		"\3\2\2\2\u020e\u020f\3\2\2\2\u020fK\3\2\2\2\u0210\u020e\3\2\2\2\u0211"+
		"\u0213\t\16\2\2\u0212\u0211\3\2\2\2\u0213\u0214\3\2\2\2\u0214\u0212\3"+
		"\2\2\2\u0214\u0215\3\2\2\2\u0215M\3\2\2\2\u0216\u0217\t\17\2\2\u0217O"+
		"\3\2\2\2\u0218\u0219\t\20\2\2\u0219Q\3\2\2\2\u021a\u0220\7=\2\2\u021b"+
		"\u0220\7>\2\2\u021c\u0220\5X-\2\u021d\u0220\5Z.\2\u021e\u0220\5\\/\2\u021f"+
		"\u021a\3\2\2\2\u021f\u021b\3\2\2\2\u021f\u021c\3\2\2\2\u021f\u021d\3\2"+
		"\2\2\u021f\u021e\3\2\2\2\u0220S\3\2\2\2\u0221\u022b\7\64\2\2\u0222\u022b"+
		"\7\65\2\2\u0223\u022b\7\66\2\2\u0224\u022b\7?\2\2\u0225\u022b\7@\2\2\u0226"+
		"\u022b\7A\2\2\u0227\u022b\5X-\2\u0228\u022b\5Z.\2\u0229\u022b\5\\/\2\u022a"+
		"\u0221\3\2\2\2\u022a\u0222\3\2\2\2\u022a\u0223\3\2\2\2\u022a\u0224\3\2"+
		"\2\2\u022a\u0225\3\2\2\2\u022a\u0226\3\2\2\2\u022a\u0227\3\2\2\2\u022a"+
		"\u0228\3\2\2\2\u022a\u0229\3\2\2\2\u022bU\3\2\2\2\u022c\u022d\78\2\2\u022d"+
		"\u0231\7\67\2\2\u022e\u022f\78\2\2\u022f\u0231\78\2\2\u0230\u022c\3\2"+
		"\2\2\u0230\u022e\3\2\2\2\u0231W\3\2\2\2\u0232\u0233\7B\2\2\u0233\u0234"+
		"\7J\2\2\u0234Y\3\2\2\2\u0235\u0236\7\32\2\2\u0236\u0237\7C\2\2\u0237\u0242"+
		"\7J\2\2\u0238\u0239\7D\2\2\u0239\u023a\7J\2\2\u023a\u0242\7J\2\2\u023b"+
		"\u023c\7 \2\2\u023c\u023d\7E\2\2\u023d\u0242\7J\2\2\u023e\u023f\7F\2\2"+
		"\u023f\u0240\7J\2\2\u0240\u0242\7J\2\2\u0241\u0235\3\2\2\2\u0241\u0238"+
		"\3\2\2\2\u0241\u023b\3\2\2\2\u0241\u023e\3\2\2\2\u0242[\3\2\2\2\u0243"+
		"\u0244\7\34\2\2\u0244\u0245\7G\2\2\u0245\u0246\7J\2\2\u0246\u0250\7J\2"+
		"\2\u0247\u0248\7H\2\2\u0248\u0249\7J\2\2\u0249\u024a\7J\2\2\u024a\u0250"+
		"\7J\2\2\u024b\u024c\7\26\2\2\u024c\u024d\7I\2\2\u024d\u024e\7J\2\2\u024e"+
		"\u0250\7J\2\2\u024f\u0243\3\2\2\2\u024f\u0247\3\2\2\2\u024f\u024b\3\2"+
		"\2\2\u0250]\3\2\2\2\64biv\u0080\u008a\u0097\u009c\u00a0\u00a4\u00ae\u00b5"+
		"\u00ba\u00bf\u00c3\u00cd\u00de\u00e7\u00f0\u00fa\u0100\u0109\u0112\u011b"+
		"\u0120\u012b\u012f\u0134\u0144\u014b\u0153\u015a\u015f\u0164\u0169\u016b"+
		"\u0170\u0176\u017a\u0181\u0187\u018b\u0192\u0209\u020e\u0214\u021f\u022a"+
		"\u0230\u0241\u024f";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}