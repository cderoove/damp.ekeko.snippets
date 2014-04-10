// Generated from ./src/damp/ekeko/snippets/Snippet.g4 by ANTLR 4.1
package damp.ekeko.snippets;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class SnippetParser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		OPEN=1, CLOSE=2, MIDDLE=3, TXT=4;
	public static final String[] tokenNames = {
		"<INVALID>", "'['", "']'", "']@['", "TXT"
	};
	public static final int
		RULE_snippet = 0, RULE_meta = 1, RULE_premeta = 2, RULE_postmeta = 3, 
		RULE_directives = 4;
	public static final String[] ruleNames = {
		"snippet", "meta", "premeta", "postmeta", "directives"
	};

	@Override
	public String getGrammarFileName() { return "Snippet.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public SnippetParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class SnippetContext extends ParserRuleContext {
		public List<TerminalNode> TXT() { return getTokens(SnippetParser.TXT); }
		public TerminalNode TXT(int i) {
			return getToken(SnippetParser.TXT, i);
		}
		public SnippetContext snippet(int i) {
			return getRuleContext(SnippetContext.class,i);
		}
		public MetaContext meta() {
			return getRuleContext(MetaContext.class,0);
		}
		public List<SnippetContext> snippet() {
			return getRuleContexts(SnippetContext.class);
		}
		public SnippetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_snippet; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SnippetListener ) ((SnippetListener)listener).enterSnippet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SnippetListener ) ((SnippetListener)listener).exitSnippet(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SnippetVisitor ) return ((SnippetVisitor<? extends T>)visitor).visitSnippet(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SnippetContext snippet() throws RecognitionException {
		SnippetContext _localctx = new SnippetContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_snippet);
		try {
			int _alt;
			setState(28);
			switch (_input.LA(1)) {
			case OPEN:
				enterOuterAlt(_localctx, 1);
				{
				setState(10); meta();
				setState(14);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
				while ( _alt!=2 && _alt!=-1 ) {
					if ( _alt==1 ) {
						{
						{
						setState(11); snippet();
						}
						} 
					}
					setState(16);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
				}
				}
				break;
			case TXT:
				enterOuterAlt(_localctx, 2);
				{
				setState(18); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(17); match(TXT);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(20); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
				} while ( _alt!=2 && _alt!=-1 );
				setState(25);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
				while ( _alt!=2 && _alt!=-1 ) {
					if ( _alt==1 ) {
						{
						{
						setState(22); snippet();
						}
						} 
					}
					setState(27);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
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

	public static class MetaContext extends ParserRuleContext {
		public PostmetaContext postmeta() {
			return getRuleContext(PostmetaContext.class,0);
		}
		public PremetaContext premeta() {
			return getRuleContext(PremetaContext.class,0);
		}
		public MetaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_meta; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SnippetListener ) ((SnippetListener)listener).enterMeta(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SnippetListener ) ((SnippetListener)listener).exitMeta(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SnippetVisitor ) return ((SnippetVisitor<? extends T>)visitor).visitMeta(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MetaContext meta() throws RecognitionException {
		MetaContext _localctx = new MetaContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_meta);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(30); premeta();
			setState(31); postmeta();
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

	public static class PremetaContext extends ParserRuleContext {
		public TerminalNode OPEN() { return getToken(SnippetParser.OPEN, 0); }
		public TerminalNode MIDDLE() { return getToken(SnippetParser.MIDDLE, 0); }
		public SnippetContext snippet() {
			return getRuleContext(SnippetContext.class,0);
		}
		public PremetaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_premeta; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SnippetListener ) ((SnippetListener)listener).enterPremeta(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SnippetListener ) ((SnippetListener)listener).exitPremeta(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SnippetVisitor ) return ((SnippetVisitor<? extends T>)visitor).visitPremeta(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PremetaContext premeta() throws RecognitionException {
		PremetaContext _localctx = new PremetaContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_premeta);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(33); match(OPEN);
			setState(34); snippet();
			setState(35); match(MIDDLE);
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

	public static class PostmetaContext extends ParserRuleContext {
		public TerminalNode CLOSE() { return getToken(SnippetParser.CLOSE, 0); }
		public DirectivesContext directives() {
			return getRuleContext(DirectivesContext.class,0);
		}
		public PostmetaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_postmeta; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SnippetListener ) ((SnippetListener)listener).enterPostmeta(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SnippetListener ) ((SnippetListener)listener).exitPostmeta(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SnippetVisitor ) return ((SnippetVisitor<? extends T>)visitor).visitPostmeta(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PostmetaContext postmeta() throws RecognitionException {
		PostmetaContext _localctx = new PostmetaContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_postmeta);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(37); directives();
			setState(38); match(CLOSE);
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

	public static class DirectivesContext extends ParserRuleContext {
		public List<TerminalNode> TXT() { return getTokens(SnippetParser.TXT); }
		public TerminalNode TXT(int i) {
			return getToken(SnippetParser.TXT, i);
		}
		public DirectivesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directives; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof SnippetListener ) ((SnippetListener)listener).enterDirectives(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof SnippetListener ) ((SnippetListener)listener).exitDirectives(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof SnippetVisitor ) return ((SnippetVisitor<? extends T>)visitor).visitDirectives(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DirectivesContext directives() throws RecognitionException {
		DirectivesContext _localctx = new DirectivesContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_directives);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(41); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(40); match(TXT);
				}
				}
				setState(43); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==TXT );
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
		"\3\uacf5\uee8c\u4f5d\u8b0d\u4a45\u78bd\u1b2f\u3378\3\6\60\4\2\t\2\4\3"+
		"\t\3\4\4\t\4\4\5\t\5\4\6\t\6\3\2\3\2\7\2\17\n\2\f\2\16\2\22\13\2\3\2\6"+
		"\2\25\n\2\r\2\16\2\26\3\2\7\2\32\n\2\f\2\16\2\35\13\2\5\2\37\n\2\3\3\3"+
		"\3\3\3\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\6\6\6,\n\6\r\6\16\6-\3\6\2\7\2\4"+
		"\6\b\n\2\2/\2\36\3\2\2\2\4 \3\2\2\2\6#\3\2\2\2\b\'\3\2\2\2\n+\3\2\2\2"+
		"\f\20\5\4\3\2\r\17\5\2\2\2\16\r\3\2\2\2\17\22\3\2\2\2\20\16\3\2\2\2\20"+
		"\21\3\2\2\2\21\37\3\2\2\2\22\20\3\2\2\2\23\25\7\6\2\2\24\23\3\2\2\2\25"+
		"\26\3\2\2\2\26\24\3\2\2\2\26\27\3\2\2\2\27\33\3\2\2\2\30\32\5\2\2\2\31"+
		"\30\3\2\2\2\32\35\3\2\2\2\33\31\3\2\2\2\33\34\3\2\2\2\34\37\3\2\2\2\35"+
		"\33\3\2\2\2\36\f\3\2\2\2\36\24\3\2\2\2\37\3\3\2\2\2 !\5\6\4\2!\"\5\b\5"+
		"\2\"\5\3\2\2\2#$\7\3\2\2$%\5\2\2\2%&\7\5\2\2&\7\3\2\2\2\'(\5\n\6\2()\7"+
		"\4\2\2)\t\3\2\2\2*,\7\6\2\2+*\3\2\2\2,-\3\2\2\2-+\3\2\2\2-.\3\2\2\2.\13"+
		"\3\2\2\2\7\20\26\33\36-";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}