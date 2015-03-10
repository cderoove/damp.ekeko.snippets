header {
package org.netbeans.modules.antlr.editor.g;
}

{
import java.util.Enumeration;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
}

class ANTLRParser extends Parser;
options {
	exportVocab=ANTLR;
	k=2;
}

tokens {
  C_TEXT="TextColor";
}

grammar
   :
	. { $setType(C_TEXT); }
	;



class ANTLRLexer extends Lexer;
options {
	k=2;
	exportVocab=ANTLR;
	testLiterals=false;
}

tokens {
	"tokens";
	"options";
	DOC_COMMENT;
	OPTIONS;
	TOKENS;
	SEMPRED;
}

{
	/**Convert 'c' to an integer char value. */
	public static int escapeCharValue(String cs) {
		//System.out.println("escapeCharValue("+cs+")");
		if ( cs.charAt(1)!='\\' ) return 0;
		switch ( cs.charAt(2) ) {
		case 'b' : return '\b';
		case 'r' : return '\r';
		case 't' : return '\t';
		case 'n' : return '\n';
		case 'f' : return '\f';
		case '"' : return '\"';
		case '\'' :return '\'';
		case '\\' :return '\\';

		case 'u' :
			// Unicode char
			if (cs.length() != 8) {
				return 0;
			}
			else {
				return
					Character.digit(cs.charAt(3), 16) * 16 * 16 * 16 +
					Character.digit(cs.charAt(4), 16) * 16 * 16 +
					Character.digit(cs.charAt(5), 16) * 16 +
					Character.digit(cs.charAt(6), 16);
			}

		case '0' :
		case '1' :
		case '2' :
		case '3' :
			if ( cs.length()>5 && Character.isDigit(cs.charAt(4)) ) {
				return (cs.charAt(2)-'0')*8*8 + (cs.charAt(3)-'0')*8 + (cs.charAt(4)-'0');
			}
			if ( cs.length()>4 && Character.isDigit(cs.charAt(3)) ) {
				return (cs.charAt(2)-'0')*8 + (cs.charAt(3)-'0');
			}
			return cs.charAt(2)-'0';

		case '4' :
		case '5' :
		case '6' :
		case '7' :
			if ( cs.length()>4 && Character.isDigit(cs.charAt(3)) ) {
				return (cs.charAt(2)-'0')*8 + (cs.charAt(3)-'0');
			}
			return cs.charAt(2)-'0';

		default :
			return 0;
		}
	}
	
	public static int tokenTypeForCharLiteral(String lit) {
		if ( lit.length()>3 ) {  // does char contain escape?
			return escapeCharValue(lit);
		}
		else {
			return lit.charAt(1);
		}
	}
}

WS	:	(	' '
		|	'\t'
		|	'\r' '\n'	{newline();}
		|	'\r'		{newline();}
		|	'\n'		{newline();}
		)
	;

COMMENT : 
	( SL_COMMENT | t:ML_COMMENT {$setType(t.getType());} )
	;

protected
SL_COMMENT :
	"//"
	(~('\n'|'\r'))* ('\n'|'\r'('\n')?)
	{ newline(); }
	;

protected
ML_COMMENT :
	"/*"
	(	{ LA(2)!='/' }? '*' {$setType(DOC_COMMENT);}
	|
	)
	(	{ LA(2)!='/' }? '*'
	|	'\r' '\n'	{newline();}
	|	'\r'		{newline();}
	|	'\n'		{newline();}
	|	~('*'|'\n'|'\r')
	)*
	"*/"
	;

COMMA : ',';

QUESTION :	'?' ;

TREE_BEGIN : "#(" ;

LPAREN:	'(' ;

RPAREN:	')' ;

COLON :	':' ;

STAR:	'*' ;

PLUS:	'+' ;

ASSIGN : '=' ;

IMPLIES : "=>" ;

SEMI:	';' ;

CARET : '^' ;

BANG : '!' ;

OR	:	'|' ;

WILDCARD : '.' ;

RANGE : ".." ;

NOT_OP :	'~' ;

RCURLY:	'}'	;

CHAR_LITERAL
	:	'\'' (ESC|~'\'') '\''
	;

STRING_LITERAL
	:	'"' (ESC|~'"')* '"'
	;

protected
ESC	:	'\\'
		(	'n'
		|	'r'
		|	't'
		|	'b'
		|	'f'
		|	'"'
		|	'\''
		|	'\\'
		|	('0'..'3')
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:
	('0'..'9')
				(	
					options {
						warnWhenFollowAmbig = false;
					}
				:
	'0'..'9'
				)?
			)?
		|	('4'..'7')
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:
	('0'..'9')
			)?
		|	'u' XDIGIT XDIGIT XDIGIT XDIGIT
		)
	;

protected
DIGIT
	:	'0'..'9'
	;

protected
XDIGIT :
		'0' .. '9'
	|	'a' .. 'f'
	|	'A' .. 'F'
	;

protected
VOCAB
	:	'\3'..'\176'	// common ASCII
	;

INT	:	('0'..'9')+
	;


ARG_ACTION 
   :
	NESTED_ARG_ACTION
	{ 
///*setText(Tool.stripFrontBack(getText(), "[", "]"));*/ 
	}
	;

protected
NESTED_ARG_ACTION :
	'['
	(	NESTED_ARG_ACTION
	|	'\r' '\n'	{newline();}
	|	'\r'		{newline();}
	|	'\n'		{newline();}
	|	CHAR_LITERAL 
	|	STRING_LITERAL 
	|	~']'
	)* 
	']'
	;


ACTION 
{int actionLine=getLine();}
	:	NESTED_ACTION
		(	'?'	{_ttype = SEMPRED;} )?
		{
			if ( _ttype==ACTION ) {
				///*setText(Tool.stripFrontBack(getText(), "{", "}"));*/
			}
			else {
				///*setText(Tool.stripFrontBack(getText(), "{", "}?"));*/
			}
			CommonToken t = new CommonToken(_ttype,$getText);
			t.setLine(actionLine);	// set action line to start
			$setToken(t);
		}
	;

/**
Note that the predicate "{true} was added before '~}'.  The reason is that
ANTLR's code generation put "COMMENT" in the default case of a switch, but
the '~}' was in a case, so '~}' was always matched for '/'.  Adding the
sempred caused '~}' to put put in the default case as well.  Finally, the
extra '/' alternative had to be added, because '~}' does not match the '/'.
 The '/' alt also has a sempred, to force it into the default case.  Ack!
This seems too hard, but it works.
 */
protected
NESTED_ACTION : 
	'{'
	(	'\r' '\n'	{newline();}
	|	'\r' 		{newline();}
	|	'\n'		{newline();}
	|	NESTED_ACTION
	|	CHAR_LITERAL
	|	{LA(2)=='/'||LA(2)=='*'}? COMMENT
	|	STRING_LITERAL
	|	{true}? ~'}'
	|	{true}? '/'
	)*
	'}'
   ;


TOKEN_REF
options { testLiterals = true; }
	:	'A'..'Z'
		(	// scarf as many letters/numbers as you can
			options {
				warnWhenFollowAmbig=false;
			}
		:
			'a'..'z'|'A'..'Z'|'_'|'0'..'9'
		)*
	;

// we get a warning here when looking for options '{', but it works right
RULE_REF
{
	int t=0;
}
	:	t=INTERNAL_RULE_REF {_ttype=t;}
		(	{t==LITERAL_options}? WS_LOOP ('{' {_ttype = OPTIONS;})?
		|	{t==LITERAL_tokens}? WS_LOOP ('{' {_ttype = TOKENS;})?
		|
		)
	;

protected
WS_LOOP
	:	(	// grab as much WS as you can
			options {
				warnWhenFollowAmbig=false;
			}
		:
			WS
		)*
	;

protected
INTERNAL_RULE_REF returns [int t]
{
	t = RULE_REF;
}
	:	'a'..'z'
		(	// scarf as many letters/numbers as you can
			options {
				warnWhenFollowAmbig=false;
			}
		:
			'a'..'z'|'A'..'Z'|'_'|'0'..'9'
		)*
		{t = testLiteralsTable(t);}
	;

protected 
WS_OPT :
	(WS)?
	;

// remove after the class variants of the scanners/parsers go
// away. this rule, just forces the lexer to use the most
// complicated class so I can get rid of the others.
protected
NOT_USEFUL
	:	('a')=>'a'
	|	'a'
	;