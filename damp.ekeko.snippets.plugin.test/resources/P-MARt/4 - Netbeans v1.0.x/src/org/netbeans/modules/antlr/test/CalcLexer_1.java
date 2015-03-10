/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

import java.io.InputStream;
import java.io.Reader;
import java.io.IOException;
import java.util.Hashtable;
import antlr.CharScanner;
import antlr.InputBuffer;
import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.Token;
import antlr.CommonToken;
import antlr.ScannerException;
import antlr.TokenStream;
import antlr.ANTLRHashString;
import antlr.LexerSharedInputState;
import antlr.collections.impl.BitSet;
public class CalcLexer extends antlr.CharScanner implements CalcParserTokenTypes, TokenStream
{
    public CalcLexer(InputStream in) {
        this(new ByteBuffer(in));
    }
    public CalcLexer(Reader in) {
        this(new CharBuffer(in));
    }
    public CalcLexer(InputBuffer ib) {
        this(new LexerSharedInputState(ib));
    }
    public CalcLexer(LexerSharedInputState state) {
        super(state);
        literals = new Hashtable();
        caseSensitiveLiterals = true;
        setCaseSensitive(true);
    }

    public Token nextToken() throws IOException {
        Token _rettoken=null;
tryAgain:
        for (;;) {
            Token _token = null;
            int _ttype = Token.INVALID_TYPE;
            resetText();
            try {   // for error handling
                switch ( LA(1)) {
    case '\t':  case '\n':  case '\r':  case ' ':
                    {
                        mWS(true);
                        _rettoken=_returnToken;
                        break;
                    }
                case '(':
                    {
                        mLPAREN(true);
                        _rettoken=_returnToken;
                        break;
                    }
                case ')':
                    {
                        mRPAREN(true);
                        _rettoken=_returnToken;
                        break;
                    }
                case '*':
                    {
                        mSTAR(true);
                        _rettoken=_returnToken;
                        break;
                    }
                case '+':
                    {
                        mPLUS(true);
                        _rettoken=_returnToken;
                        break;
                    }
                case ';':
                    {
                        mSEMI(true);
                        _rettoken=_returnToken;
                        break;
                    }
    case '0':  case '1':  case '2':  case '3':
    case '4':  case '5':  case '6':  case '7':
            case '8':  case '9':
                    {
                        mINT(true);
                        _rettoken=_returnToken;
                        break;
                    }
                default:
                    {
                        if (LA(1)==EOF_CHAR) {_returnToken = makeToken(Token.EOF_TYPE);}
                        else {throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());}
                    }
                }
                if ( _returnToken==null ) continue tryAgain; // found SKIP token
                _ttype = _returnToken.getType();
                _ttype = testLiteralsTable(_ttype);
                _returnToken.setType(_ttype);
                return _returnToken;
            }
            catch (ScannerException e) {
                reportError(e);
                consume();
            }
        }
    }

    public final void mWS(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = WS;
        int _saveIndex;

        {
            switch ( LA(1)) {
            case ' ':
                {
                    match(' ');
                    break;
                }
            case '\t':
                {
                    match('\t');
                    break;
                }
            case '\n':
                {
                    match('\n');
                    break;
                }
            case '\r':
                {
                    match('\r');
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        _ttype = Token.SKIP;
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mLPAREN(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = LPAREN;
        int _saveIndex;

        match('(');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mRPAREN(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = RPAREN;
        int _saveIndex;

        match(')');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mSTAR(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = STAR;
        int _saveIndex;

        match('*');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mPLUS(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = PLUS;
        int _saveIndex;

        match('+');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mSEMI(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = SEMI;
        int _saveIndex;

        match(';');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    protected final void mDIGIT(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = DIGIT;
        int _saveIndex;

        matchRange('0','9');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mINT(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = INT;
        int _saveIndex;

        {
            int _cnt18=0;
_loop18:
            do {
                if (((LA(1) >= '0' && LA(1) <= '9'))) {
                    mDIGIT(false);
                }
                else {
                    if ( _cnt18>=1 ) { break _loop18; } else {throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());}
                }

                _cnt18++;
            } while (true);
        }
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }



}
