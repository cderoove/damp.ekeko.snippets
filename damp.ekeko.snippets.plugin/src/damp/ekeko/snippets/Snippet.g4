grammar Snippet;

snippet :
        meta snippet*
		| TXT+ snippet*
		;
		
meta : premeta postmeta;

premeta : OPEN snippet MIDDLE;

postmeta :  directives CLOSE;

directives : TXT+;

OPEN: '[' ;
CLOSE: ']' ;
MIDDLE: ']@[' ;
TXT : .;

//CHAR : ~[MIDDLE];
