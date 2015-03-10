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
public class HTMLLexer extends antlr.CharScanner implements HTMLTokenTypes, TokenStream
{
    public HTMLLexer(InputStream in) {
        this(new ByteBuffer(in));
    }
    public HTMLLexer(Reader in) {
        this(new CharBuffer(in));
    }
    public HTMLLexer(InputBuffer ib) {
        this(new LexerSharedInputState(ib));
    }
    public HTMLLexer(LexerSharedInputState state) {
        super(state);
        literals = new Hashtable();
        caseSensitiveLiterals = true;
        setCaseSensitive(false);
    }

    public Token nextToken() throws IOException {
        Token _rettoken=null;
tryAgain:
        for (;;) {
            Token _token = null;
            int _ttype = Token.INVALID_TYPE;
            setCommitToPath(false);
            int _m;
            _m = mark();
            resetText();
            try {   // for error handling
                if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='h') && (LA(4)=='t')) {
                    mCHTML(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='h') && (LA(4)=='e')) {
                    mCHEAD(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='b') && (LA(4)=='o')) {
                    mCBODY(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='t') && (LA(4)=='i')) {
                    mCTITLE(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='s') && (LA(4)=='c')) {
                    mCSCRIPT(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='m') && (LA(3)=='e') && (LA(4)=='t')) {
                    mMETA(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='l') && (LA(3)=='i') && (LA(4)=='n')) {
                    mLINK(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='h') && (LA(4)=='1')) {
                    mCH1(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='h') && (LA(4)=='2')) {
                    mCH2(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='h') && (LA(4)=='3')) {
                    mCH3(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='h') && (LA(4)=='4')) {
                    mCH4(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='h') && (LA(4)=='5')) {
                    mCH5(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='h') && (LA(4)=='6')) {
                    mCH6(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='a') && (LA(4)=='d')) {
                    mCADDRESS(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='p') && (LA(4)=='>')) {
                    mCPARA(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='u') && (LA(4)=='l')) {
                    mCULIST(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='l') && (LA(3)=='i') && (_tokenSet_0.member(LA(4)))) {
                    mOLITEM(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='d') && (LA(4)=='l')) {
                    mCDLIST(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='d') && (LA(4)=='t')) {
                    mCDTERM(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='d') && (LA(4)=='d')) {
                    mCDDEF(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='d') && (LA(3)=='i') && (LA(4)=='r')) {
                    mODIR(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='d') && (LA(4)=='i')) {
                    mCDIR_OR_CDIV(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='d') && (LA(3)=='i') && (LA(4)=='v')) {
                    mODIV(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='m') && (LA(3)=='e') && (LA(4)=='n')) {
                    mOMENU(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='m') && (LA(4)=='e')) {
                    mCMENU(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='p'||LA(3)=='x') && (LA(4)=='m'||LA(4)=='r')) {
                    mCPRE(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='c') && (LA(4)=='e')) {
                    mCCENTER(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='b') && (LA(4)=='l')) {
                    mCBQUOTE(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='t') && (LA(4)=='a')) {
                    mCTABLE(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='c') && (LA(4)=='a')) {
                    mCCAP(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='t') && (LA(4)=='r')) {
                    mC_TR(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='t') && (LA(4)=='d'||LA(4)=='h')) {
                    mC_TH_OR_TD(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='t') && (LA(4)=='t')) {
                    mCTTYPE(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='b') && (LA(4)=='>')) {
                    mCBOLD(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='u') && (LA(4)=='>')) {
                    mCUNDER(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='s') && (LA(3)=='t') && (LA(4)=='r')) {
                    mOSTRIKE_OR_OSTRONG(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='s') && (LA(4)=='t')) {
                    mCST_LEFT_FACTORED(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='s') && (LA(3)=='t') && (LA(4)=='y')) {
                    mOSTYLE(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='b') && (LA(4)=='i')) {
                    mCBIG(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='s') && (LA(4)=='m')) {
                    mCSMALL(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='s') && (LA(3)=='u') && (LA(4)=='b')) {
                    mOSUB(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='s') && (LA(3)=='u') && (LA(4)=='p')) {
                    mOSUP(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='s') && (LA(4)=='u')) {
                    mCSUB_OR_CSUP(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='d') && (LA(4)=='f')) {
                    mCDFN(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='c') && (LA(4)=='o')) {
                    mCCODE(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='s') && (LA(4)=='a')) {
                    mCSAMP(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='c') && (LA(4)=='i')) {
                    mCCITE(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='s') && (LA(4)=='e')) {
                    mCSELECT(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='t') && (LA(4)=='e')) {
                    mCTAREA(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='a') && (LA(4)=='>')) {
                    mCANCHOR(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='a') && (LA(4)=='p')) {
                    mAPPLET(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='f') && (LA(3)=='o') && (LA(4)=='r')) {
                    mOFORM(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='f') && (LA(3)=='o') && (LA(4)=='n')) {
                    mOFONT(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='m') && (LA(4)=='a')) {
                    mCMAP(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='!') && (LA(3)=='d')) {
                    mDOCTYPE(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='h') && (LA(3)=='t')) {
                    mOHTML(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='h') && (LA(3)=='e')) {
                    mOHEAD(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='b') && (LA(3)=='o')) {
                    mOBODY(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='t') && (LA(3)=='i')) {
                    mOTITLE(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='s') && (LA(3)=='c')) {
                    mOSCRIPT(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='i') && (LA(3)=='s')) {
                    mISINDEX(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='h') && (LA(3)=='1')) {
                    mOH1(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='h') && (LA(3)=='2')) {
                    mOH2(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='h') && (LA(3)=='3')) {
                    mOH3(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='h') && (LA(3)=='4')) {
                    mOH4(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='h') && (LA(3)=='5')) {
                    mOH5(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='h') && (LA(3)=='6')) {
                    mOH6(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='a') && (LA(3)=='d')) {
                    mOADDRESS(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='p') && (_tokenSet_0.member(LA(3)))) {
                    mOPARA(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='u') && (LA(3)=='l')) {
                    mOULIST(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='o') && (LA(3)=='l')) {
                    mOOLIST(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='o')) {
                    mCOLIST(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='l')) {
                    mCLITEM(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='d') && (LA(3)=='l')) {
                    mODLIST(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='d') && (LA(3)=='t')) {
                    mODTERM(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='d') && (LA(3)=='d')) {
                    mODDEF(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='p'||LA(2)=='x') && (LA(3)=='m'||LA(3)=='r')) {
                    mOPRE(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='c') && (LA(3)=='e')) {
                    mOCENTER(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='b') && (LA(3)=='l')) {
                    mOBQUOTE(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='h') && (LA(3)=='r')) {
                    mHR(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='t') && (LA(3)=='a')) {
                    mOTABLE(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='c') && (LA(3)=='a')) {
                    mOCAP(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='t') && (LA(3)=='r')) {
                    mO_TR(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='t') && (LA(3)=='d'||LA(3)=='h')) {
                    mO_TH_OR_TD(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='t') && (LA(3)=='t')) {
                    mOTTYPE(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='i') && (LA(3)=='>')) {
                    mOITALIC(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='i')) {
                    mCITALIC(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='b') && (LA(3)=='>')) {
                    mOBOLD(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='u') && (LA(3)=='>')) {
                    mOUNDER(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='b') && (LA(3)=='i')) {
                    mOBIG(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='s') && (LA(3)=='m')) {
                    mOSMALL(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='e')) {
                    mCEM(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='d') && (LA(3)=='f')) {
                    mODFN(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='c') && (LA(3)=='o')) {
                    mOCODE(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='s') && (LA(3)=='a')) {
                    mOSAMP(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='k')) {
                    mCKBD(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='v')) {
                    mCVAR(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='c') && (LA(3)=='i')) {
                    mOCITE(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='i') && (LA(3)=='n')) {
                    mINPUT(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='s') && (LA(3)=='e')) {
                    mOSELECT(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='t') && (LA(3)=='e')) {
                    mOTAREA(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='o') && (LA(3)=='p')) {
                    mSELOPT(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='a') && (_tokenSet_1.member(LA(3)))) {
                    mOANCHOR(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='i') && (LA(3)=='m')) {
                    mIMG(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='a') && (LA(3)=='p')) {
                    mOAPPLET(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='p') && (LA(3)=='a')) {
                    mAPARM(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='f')) {
                    mCFORM_OR_CFONT(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='b') && (LA(3)=='a')) {
                    mBFONT_OR_BASE(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='b') && (LA(3)=='r')) {
                    mBR(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='m') && (LA(3)=='a')) {
                    mOMAP(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='a') && (LA(3)=='r')) {
                    mAREA(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='!') && (LA(3)=='-')) {
                    mCOMMENT(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='e')) {
                    mOEM(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='k')) {
                    mOKBD(true);
                    _rettoken=_returnToken;
                }
                else if ((LA(1)=='<') && (LA(2)=='v')) {
                    mOVAR(true);
                    _rettoken=_returnToken;
                }
                else if ((_tokenSet_2.member(LA(1)))) {
                    mPCDATA(true);
                    _rettoken=_returnToken;
                }
                else {
                    if (LA(1)==EOF_CHAR) {_returnToken = makeToken(Token.EOF_TYPE);}
                    else {
                        commit();
                        try {mUNDEFINED_TOKEN(false);}
                        catch(ScannerException e) {
                            // catastrophic failure
                            reportError(e);
                            consume();
                        }
                        continue tryAgain;
                    }
                }

                commit();
                if ( _returnToken==null ) continue tryAgain; // found SKIP token
                _ttype = _returnToken.getType();
                _ttype = testLiteralsTable(_ttype);
                _returnToken.setType(_ttype);
                return _returnToken;
            }
            catch (ScannerException e) {
                if ( !getCommitToPath() ) {
                    rewind(_m);
                    resetText();
                    try {mUNDEFINED_TOKEN(false);}
                    catch(ScannerException ee) {
                        // horrendous failure: error in filter rule
                        reportError(ee);
                        consume();
                    }
                    continue tryAgain;
                }
                reportError(e);
                consume();
            }
        }
    }

    public final void mDOCTYPE(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = DOCTYPE;
        int _saveIndex;

        match("<!doctype");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    _saveIndex=text.length();
                    mWS(false);
                    text.setLength(_saveIndex);
                    break;
                }
            case 'h':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match("html");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    _saveIndex=text.length();
                    mWS(false);
                    text.setLength(_saveIndex);
                    break;
                }
            case 'p':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match("public");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    _saveIndex=text.length();
                    mWS(false);
                    text.setLength(_saveIndex);
                    break;
                }
        case '"':  case '\'':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        mSTRING(false);
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    _saveIndex=text.length();
                    mWS(false);
                    text.setLength(_saveIndex);
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    protected final void mWS(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = WS;
        int _saveIndex;

        {
            int _cnt421=0;
_loop421:
            do {
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
                        newline();
                        break;
                    }
                default:
                    if ((LA(1)=='\r') && (LA(2)=='\n') && (_tokenSet_3.member(LA(3)))) {
                        match("\r\n");
                        newline();
                    }
                    else if ((LA(1)=='\r') && (_tokenSet_3.member(LA(2)))) {
                        match('\r');
                        newline();
                    }
                    else {
                        if ( _cnt421>=1 ) { break _loop421; } else {throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());}
                    }
                }
                _cnt421++;
            } while (true);
        }
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    protected final void mSTRING(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = STRING;
        int _saveIndex;

        switch ( LA(1)) {
        case '"':
            {
                match('"');
                {
_loop441:
                    do {
                        if ((_tokenSet_4.member(LA(1)))) {
                            matchNot('"');
                        }
                        else {
                            break _loop441;
                        }

                    } while (true);
                }
                match('"');
                break;
            }
        case '\'':
            {
                match('\'');
                {
_loop443:
                    do {
                        if ((_tokenSet_5.member(LA(1)))) {
                            matchNot('\'');
                        }
                        else {
                            break _loop443;
                        }

                    } while (true);
                }
                match('\'');
                break;
            }
        default:
            {
                throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
            }
        }
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOHTML(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OHTML;
        int _saveIndex;

        match("<html>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCHTML(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CHTML;
        int _saveIndex;

        match("</html>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOHEAD(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OHEAD;
        int _saveIndex;

        match("<head>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCHEAD(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CHEAD;
        int _saveIndex;

        match("</head>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOBODY(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OBODY;
        int _saveIndex;

        match("<body");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    {
_loop234:
                        do {
                            if ((_tokenSet_6.member(LA(1)))) {
                                mATTR(false);
                            }
                            else {
                                break _loop234;
                            }

                        } while (true);
                    }
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    protected final void mATTR(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = ATTR;
        int _saveIndex;

        mWORD(false);
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    _saveIndex=text.length();
                    mWS(false);
                    text.setLength(_saveIndex);
                    break;
                }
case '.':  case '=':  case '>':  case 'a':
case 'b':  case 'c':  case 'd':  case 'e':
case 'f':  case 'g':  case 'h':  case 'i':
case 'j':  case 'k':  case 'l':  case 'm':
case 'n':  case 'o':  case 'p':  case 'q':
case 'r':  case 's':  case 't':  case 'u':
case 'v':  case 'w':  case 'x':  case 'y':
            case 'z':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        {
            switch ( LA(1)) {
            case '=':
                {
                    match('=');
                    {
                        switch ( LA(1)) {
            case '\t':  case '\n':  case '\r':  case ' ':
                            {
                                _saveIndex=text.length();
                                mWS(false);
                                text.setLength(_saveIndex);
                                break;
                            }
            case '"':  case '#':  case '\'':  case '-':
            case '.':  case '0':  case '1':  case '2':
            case '3':  case '4':  case '5':  case '6':
            case '7':  case '8':  case '9':  case 'a':
            case 'b':  case 'c':  case 'd':  case 'e':
            case 'f':  case 'g':  case 'h':  case 'i':
            case 'j':  case 'k':  case 'l':  case 'm':
            case 'n':  case 'o':  case 'p':  case 'q':
            case 'r':  case 's':  case 't':  case 'u':
            case 'v':  case 'w':  case 'x':  case 'y':
                        case 'z':
                            {
                                break;
                            }
                        default:
                            {
                                throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                            }
                        }
                    }
                    {
                        switch ( LA(1)) {
            case '.':  case 'a':  case 'b':  case 'c':
            case 'd':  case 'e':  case 'f':  case 'g':
            case 'h':  case 'i':  case 'j':  case 'k':
            case 'l':  case 'm':  case 'n':  case 'o':
            case 'p':  case 'q':  case 'r':  case 's':
            case 't':  case 'u':  case 'v':  case 'w':
                case 'x':  case 'y':  case 'z':
                            {
                                mWORD(false);
                                {
                                    switch ( LA(1)) {
                        case '\t':  case '\n':  case '\r':  case ' ':
                                        {
                                            _saveIndex=text.length();
                                            mWS(false);
                                            text.setLength(_saveIndex);
                                            break;
                                        }
                        case '%':  case '.':  case '>':  case 'a':
                        case 'b':  case 'c':  case 'd':  case 'e':
                        case 'f':  case 'g':  case 'h':  case 'i':
                        case 'j':  case 'k':  case 'l':  case 'm':
                        case 'n':  case 'o':  case 'p':  case 'q':
                        case 'r':  case 's':  case 't':  case 'u':
                        case 'v':  case 'w':  case 'x':  case 'y':
                                    case 'z':
                                        {
                                            break;
                                        }
                                    default:
                                        {
                                            throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                                        }
                                    }
                                }
                                {
                                    switch ( LA(1)) {
                                    case '%':
                                        {
                                            match('%');
                                            {
                                                switch ( LA(1)) {
                                    case '\t':  case '\n':  case '\r':  case ' ':
                                                    {
                                                        _saveIndex=text.length();
                                                        mWS(false);
                                                        text.setLength(_saveIndex);
                                                        break;
                                                    }
                                    case '.':  case '>':  case 'a':  case 'b':
                                    case 'c':  case 'd':  case 'e':  case 'f':
                                    case 'g':  case 'h':  case 'i':  case 'j':
                                    case 'k':  case 'l':  case 'm':  case 'n':
                                    case 'o':  case 'p':  case 'q':  case 'r':
                                    case 's':  case 't':  case 'u':  case 'v':
                                    case 'w':  case 'x':  case 'y':  case 'z':
                                                    {
                                                        break;
                                                    }
                                                default:
                                                    {
                                                        throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                                                    }
                                                }
                                            }
                                            break;
                                        }
                        case '.':  case '>':  case 'a':  case 'b':
                        case 'c':  case 'd':  case 'e':  case 'f':
                        case 'g':  case 'h':  case 'i':  case 'j':
                        case 'k':  case 'l':  case 'm':  case 'n':
                        case 'o':  case 'p':  case 'q':  case 'r':
                        case 's':  case 't':  case 'u':  case 'v':
                        case 'w':  case 'x':  case 'y':  case 'z':
                                        {
                                            break;
                                        }
                                    default:
                                        {
                                            throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                                        }
                                    }
                                }
                                break;
                            }
            case '-':  case '0':  case '1':  case '2':
            case '3':  case '4':  case '5':  case '6':
                case '7':  case '8':  case '9':
                            {
                                {
                                    switch ( LA(1)) {
                                    case '-':
                                        {
                                            match('-');
                                            {
                                                switch ( LA(1)) {
                                    case '\t':  case '\n':  case '\r':  case ' ':
                                                    {
                                                        _saveIndex=text.length();
                                                        mWS(false);
                                                        text.setLength(_saveIndex);
                                                        break;
                                                    }
                                    case '0':  case '1':  case '2':  case '3':
                                    case '4':  case '5':  case '6':  case '7':
                                            case '8':  case '9':
                                                    {
                                                        break;
                                                    }
                                                default:
                                                    {
                                                        throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                                                    }
                                                }
                                            }
                                            break;
                                        }
                        case '0':  case '1':  case '2':  case '3':
                        case '4':  case '5':  case '6':  case '7':
                                case '8':  case '9':
                                        {
                                            break;
                                        }
                                    default:
                                        {
                                            throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                                        }
                                    }
                                }
                                mINT(false);
                                {
                                    switch ( LA(1)) {
                        case '\t':  case '\n':  case '\r':  case ' ':
                                        {
                                            _saveIndex=text.length();
                                            mWS(false);
                                            text.setLength(_saveIndex);
                                            break;
                                        }
                        case '.':  case '>':  case 'a':  case 'b':
                        case 'c':  case 'd':  case 'e':  case 'f':
                        case 'g':  case 'h':  case 'i':  case 'j':
                        case 'k':  case 'l':  case 'm':  case 'n':
                        case 'o':  case 'p':  case 'q':  case 'r':
                        case 's':  case 't':  case 'u':  case 'v':
                        case 'w':  case 'x':  case 'y':  case 'z':
                                        {
                                            break;
                                        }
                                    default:
                                        {
                                            throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                                        }
                                    }
                                }
                                break;
                            }
                    case '"':  case '\'':
                            {
                                mSTRING(false);
                                {
                                    switch ( LA(1)) {
                        case '\t':  case '\n':  case '\r':  case ' ':
                                        {
                                            _saveIndex=text.length();
                                            mWS(false);
                                            text.setLength(_saveIndex);
                                            break;
                                        }
                        case '.':  case '>':  case 'a':  case 'b':
                        case 'c':  case 'd':  case 'e':  case 'f':
                        case 'g':  case 'h':  case 'i':  case 'j':
                        case 'k':  case 'l':  case 'm':  case 'n':
                        case 'o':  case 'p':  case 'q':  case 'r':
                        case 's':  case 't':  case 'u':  case 'v':
                        case 'w':  case 'x':  case 'y':  case 'z':
                                        {
                                            break;
                                        }
                                    default:
                                        {
                                            throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                                        }
                                    }
                                }
                                break;
                            }
                        case '#':
                            {
                                mHEXNUM(false);
                                {
                                    switch ( LA(1)) {
                        case '\t':  case '\n':  case '\r':  case ' ':
                                        {
                                            _saveIndex=text.length();
                                            mWS(false);
                                            text.setLength(_saveIndex);
                                            break;
                                        }
                        case '.':  case '>':  case 'a':  case 'b':
                        case 'c':  case 'd':  case 'e':  case 'f':
                        case 'g':  case 'h':  case 'i':  case 'j':
                        case 'k':  case 'l':  case 'm':  case 'n':
                        case 'o':  case 'p':  case 'q':  case 'r':
                        case 's':  case 't':  case 'u':  case 'v':
                        case 'w':  case 'x':  case 'y':  case 'z':
                                        {
                                            break;
                                        }
                                    default:
                                        {
                                            throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                                        }
                                    }
                                }
                                break;
                            }
                        default:
                            {
                                throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                            }
                        }
                    }
                    break;
                }
case '.':  case '>':  case 'a':  case 'b':
case 'c':  case 'd':  case 'e':  case 'f':
case 'g':  case 'h':  case 'i':  case 'j':
case 'k':  case 'l':  case 'm':  case 'n':
case 'o':  case 'p':  case 'q':  case 'r':
case 's':  case 't':  case 'u':  case 'v':
case 'w':  case 'x':  case 'y':  case 'z':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCBODY(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CBODY;
        int _saveIndex;

        match("</body>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOTITLE(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OTITLE;
        int _saveIndex;

        match("<title>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCTITLE(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CTITLE;
        int _saveIndex;

        match("</title>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOSCRIPT(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OSCRIPT;
        int _saveIndex;

        match("<script>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCSCRIPT(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CSCRIPT;
        int _saveIndex;

        match("</script>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mISINDEX(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = ISINDEX;
        int _saveIndex;

        match("<isindex");
        mWS(false);
        mATTR(false);
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mMETA(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = META;
        int _saveIndex;

        match("<meta");
        mWS(false);
        {
            int _cnt243=0;
_loop243:
            do {
                if ((_tokenSet_6.member(LA(1)))) {
                    mATTR(false);
                }
                else {
                    if ( _cnt243>=1 ) { break _loop243; } else {throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());}
                }

                _cnt243++;
            } while (true);
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mLINK(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = LINK;
        int _saveIndex;

        match("<link");
        mWS(false);
        {
            int _cnt246=0;
_loop246:
            do {
                if ((_tokenSet_6.member(LA(1)))) {
                    mATTR(false);
                }
                else {
                    if ( _cnt246>=1 ) { break _loop246; } else {throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());}
                }

                _cnt246++;
            } while (true);
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOH1(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OH1;
        int _saveIndex;

        match("<h1");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    mATTR(false);
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCH1(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CH1;
        int _saveIndex;

        match("</h1>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOH2(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OH2;
        int _saveIndex;

        match("<h2");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    mATTR(false);
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCH2(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CH2;
        int _saveIndex;

        match("</h2>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOH3(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OH3;
        int _saveIndex;

        match("<h3");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    mATTR(false);
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCH3(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CH3;
        int _saveIndex;

        match("</h3>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOH4(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OH4;
        int _saveIndex;

        match("<h4");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    mATTR(false);
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCH4(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CH4;
        int _saveIndex;

        match("</h4>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOH5(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OH5;
        int _saveIndex;

        match("<h5");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    mATTR(false);
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCH5(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CH5;
        int _saveIndex;

        match("</h5>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOH6(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OH6;
        int _saveIndex;

        match("<h6");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    mATTR(false);
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCH6(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CH6;
        int _saveIndex;

        match("</h6>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOADDRESS(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OADDRESS;
        int _saveIndex;

        match("<address>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCADDRESS(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CADDRESS;
        int _saveIndex;

        match("</address>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOPARA(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OPARA;
        int _saveIndex;

        match("<p");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    mATTR(false);
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCPARA(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CPARA;
        int _saveIndex;

        match("</p>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOULIST(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OULIST;
        int _saveIndex;

        match("<ul");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    mATTR(false);
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCULIST(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CULIST;
        int _saveIndex;

        match("</ul>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOOLIST(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OOLIST;
        int _saveIndex;

        match("<ol");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    mATTR(false);
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCOLIST(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = COLIST;
        int _saveIndex;

        match("</ol>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOLITEM(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OLITEM;
        int _saveIndex;

        match("<li");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    mATTR(false);
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCLITEM(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CLITEM;
        int _saveIndex;

        match("</li>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mODLIST(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = ODLIST;
        int _saveIndex;

        match("<dl");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    mATTR(false);
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCDLIST(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CDLIST;
        int _saveIndex;

        match("</dl>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mODTERM(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = ODTERM;
        int _saveIndex;

        match("<dt>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCDTERM(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CDTERM;
        int _saveIndex;

        match("</dt>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mODDEF(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = ODDEF;
        int _saveIndex;

        match("<dd>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCDDEF(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CDDEF;
        int _saveIndex;

        match("</dd>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mODIR(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = ODIR;
        int _saveIndex;

        match("<dir>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCDIR_OR_CDIV(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CDIR_OR_CDIV;
        int _saveIndex;

        match("</di");
        {
            switch ( LA(1)) {
            case 'r':
                {
                    match('r');
                    _ttype = CDIR;
                    break;
                }
            case 'v':
                {
                    match('v');
                    _ttype = CDIV;
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mODIV(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = ODIV;
        int _saveIndex;

        match("<div");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    mATTR(false);
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOMENU(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OMENU;
        int _saveIndex;

        match("<menu>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCMENU(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CMENU;
        int _saveIndex;

        match("</menu>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOPRE(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OPRE;
        int _saveIndex;

        {
            if ((LA(1)=='<') && (LA(2)=='p')) {
                match("<pre>");
            }
            else if ((LA(1)=='<') && (LA(2)=='x')) {
                match("<xmp>");
            }
            else {
                throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
            }

        }
        {
            if ((LA(1)=='\n')) {
                match('\n');
            }
            else {
            }

        }
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCPRE(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CPRE;
        int _saveIndex;

        if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='p')) {
            match("</pre>");
        }
        else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='x')) {
            match("</xmp>");
        }
        else {
            throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
        }

        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOCENTER(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OCENTER;
        int _saveIndex;

        match("<center>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCCENTER(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CCENTER;
        int _saveIndex;

        match("</center>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOBQUOTE(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OBQUOTE;
        int _saveIndex;

        match("<blockquote>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCBQUOTE(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CBQUOTE;
        int _saveIndex;

        match("</blockquote>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mHR(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = HR;
        int _saveIndex;

        match("<hr");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    {
_loop304:
                        do {
                            if ((_tokenSet_6.member(LA(1)))) {
                                mATTR(false);
                            }
                            else {
                                break _loop304;
                            }

                        } while (true);
                    }
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOTABLE(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OTABLE;
        int _saveIndex;

        match("<table");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    {
_loop308:
                        do {
                            if ((_tokenSet_6.member(LA(1)))) {
                                mATTR(false);
                            }
                            else {
                                break _loop308;
                            }

                        } while (true);
                    }
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCTABLE(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CTABLE;
        int _saveIndex;

        match("</table>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOCAP(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OCAP;
        int _saveIndex;

        match("<caption");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    {
_loop313:
                        do {
                            if ((_tokenSet_6.member(LA(1)))) {
                                mATTR(false);
                            }
                            else {
                                break _loop313;
                            }

                        } while (true);
                    }
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCCAP(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CCAP;
        int _saveIndex;

        match("</caption>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mO_TR(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = O_TR;
        int _saveIndex;

        match("<tr");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    {
_loop318:
                        do {
                            if ((_tokenSet_6.member(LA(1)))) {
                                mATTR(false);
                            }
                            else {
                                break _loop318;
                            }

                        } while (true);
                    }
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mC_TR(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = C_TR;
        int _saveIndex;

        match("</tr>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mO_TH_OR_TD(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = O_TH_OR_TD;
        int _saveIndex;

        {
            if ((LA(1)=='<') && (LA(2)=='t') && (LA(3)=='h')) {
                match("<th");
            }
            else if ((LA(1)=='<') && (LA(2)=='t') && (LA(3)=='d')) {
                match("<td");
            }
            else {
                throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
            }

        }
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    {
_loop324:
                        do {
                            if ((_tokenSet_6.member(LA(1)))) {
                                mATTR(false);
                            }
                            else {
                                break _loop324;
                            }

                        } while (true);
                    }
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mC_TH_OR_TD(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = C_TH_OR_TD;
        int _saveIndex;

        if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='t') && (LA(4)=='h')) {
            match("</th>");
        }
        else if ((LA(1)=='<') && (LA(2)=='/') && (LA(3)=='t') && (LA(4)=='d')) {
            match("</td>");
        }
        else {
            throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
        }

        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOTTYPE(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OTTYPE;
        int _saveIndex;

        match("<tt>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCTTYPE(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CTTYPE;
        int _saveIndex;

        match("</tt>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOITALIC(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OITALIC;
        int _saveIndex;

        match("<i>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCITALIC(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CITALIC;
        int _saveIndex;

        match("</i>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOBOLD(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OBOLD;
        int _saveIndex;

        match("<b>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCBOLD(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CBOLD;
        int _saveIndex;

        match("</b>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOUNDER(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OUNDER;
        int _saveIndex;

        match("<u>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCUNDER(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CUNDER;
        int _saveIndex;

        match("</u>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    /** Left-factor <strike> and <strong> to reduce lookahead */
    public final void mOSTRIKE_OR_OSTRONG(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OSTRIKE_OR_OSTRONG;
        int _saveIndex;

        match("<str");
        {
            switch ( LA(1)) {
            case 'i':
                {
                    match("ike");
                    _ttype = OSTRIKE;
                    break;
                }
            case 'o':
                {
                    match("ong");
                    _ttype = OSTRONG;
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCST_LEFT_FACTORED(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CST_LEFT_FACTORED;
        int _saveIndex;

        match("</st");
        {
            if ((LA(1)=='r') && (LA(2)=='i')) {
                match("rike");
                _ttype = CSTRIKE;
            }
            else if ((LA(1)=='r') && (LA(2)=='o')) {
                match("rong");
                _ttype = CSTRONG;
            }
            else if ((LA(1)=='y')) {
                match("yle");
                _ttype = CSTYLE;
            }
            else {
                throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
            }

        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOSTYLE(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OSTYLE;
        int _saveIndex;

        match("<style>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOBIG(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OBIG;
        int _saveIndex;

        match("<big>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCBIG(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CBIG;
        int _saveIndex;

        match("</big>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOSMALL(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OSMALL;
        int _saveIndex;

        match("<small>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCSMALL(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CSMALL;
        int _saveIndex;

        match("</small>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOSUB(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OSUB;
        int _saveIndex;

        match("<sub>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOSUP(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OSUP;
        int _saveIndex;

        match("<sup>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCSUB_OR_CSUP(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CSUB_OR_CSUP;
        int _saveIndex;

        match("</su");
        {
            switch ( LA(1)) {
            case 'b':
                {
                    match('b');
                    _ttype = CSUB;
                    break;
                }
            case 'p':
                {
                    match('p');
                    _ttype = CSUP;
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOEM(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OEM;
        int _saveIndex;

        match("<em>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCEM(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CEM;
        int _saveIndex;

        match("</em>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mODFN(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = ODFN;
        int _saveIndex;

        match("<dfn>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCDFN(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CDFN;
        int _saveIndex;

        match("</dfn>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOCODE(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OCODE;
        int _saveIndex;

        match("<code>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCCODE(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CCODE;
        int _saveIndex;

        match("</code>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOSAMP(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OSAMP;
        int _saveIndex;

        match("<samp>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCSAMP(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CSAMP;
        int _saveIndex;

        match("</samp>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOKBD(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OKBD;
        int _saveIndex;

        match("<kbd>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCKBD(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CKBD;
        int _saveIndex;

        match("</kbd>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOVAR(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OVAR;
        int _saveIndex;

        match("<var>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCVAR(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CVAR;
        int _saveIndex;

        match("</var>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOCITE(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OCITE;
        int _saveIndex;

        match("<cite>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCCITE(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CCITE;
        int _saveIndex;

        match("</cite>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mINPUT(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = INPUT;
        int _saveIndex;

        match("<input");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    {
_loop364:
                        do {
                            if ((_tokenSet_6.member(LA(1)))) {
                                mATTR(false);
                            }
                            else {
                                break _loop364;
                            }

                        } while (true);
                    }
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOSELECT(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OSELECT;
        int _saveIndex;

        match("<select");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    {
_loop368:
                        do {
                            if ((_tokenSet_6.member(LA(1)))) {
                                mATTR(false);
                            }
                            else {
                                break _loop368;
                            }

                        } while (true);
                    }
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCSELECT(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CSELECT;
        int _saveIndex;

        match("</select>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOTAREA(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OTAREA;
        int _saveIndex;

        match("<textarea");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    {
_loop373:
                        do {
                            if ((_tokenSet_6.member(LA(1)))) {
                                mATTR(false);
                            }
                            else {
                                break _loop373;
                            }

                        } while (true);
                    }
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCTAREA(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CTAREA;
        int _saveIndex;

        match("</textarea>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mSELOPT(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = SELOPT;
        int _saveIndex;

        match("<option");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    {
_loop378:
                        do {
                            if ((_tokenSet_6.member(LA(1)))) {
                                mATTR(false);
                            }
                            else {
                                break _loop378;
                            }

                        } while (true);
                    }
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOANCHOR(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OANCHOR;
        int _saveIndex;

        match("<a");
        mWS(false);
        {
            int _cnt381=0;
_loop381:
            do {
                if ((_tokenSet_6.member(LA(1)))) {
                    mATTR(false);
                }
                else {
                    if ( _cnt381>=1 ) { break _loop381; } else {throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());}
                }

                _cnt381++;
            } while (true);
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCANCHOR(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CANCHOR;
        int _saveIndex;

        match("</a>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mIMG(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = IMG;
        int _saveIndex;

        match("<img");
        mWS(false);
        {
            int _cnt385=0;
_loop385:
            do {
                if ((_tokenSet_6.member(LA(1)))) {
                    mATTR(false);
                }
                else {
                    if ( _cnt385>=1 ) { break _loop385; } else {throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());}
                }

                _cnt385++;
            } while (true);
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOAPPLET(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OAPPLET;
        int _saveIndex;

        match("<applet");
        mWS(false);
        {
            int _cnt388=0;
_loop388:
            do {
                if ((_tokenSet_6.member(LA(1)))) {
                    mATTR(false);
                }
                else {
                    if ( _cnt388>=1 ) { break _loop388; } else {throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());}
                }

                _cnt388++;
            } while (true);
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mAPPLET(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = APPLET;
        int _saveIndex;

        match("</applet>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mAPARM(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = APARM;
        int _saveIndex;

        match("<param");
        mWS(false);
        {
            int _cnt392=0;
_loop392:
            do {
                if ((_tokenSet_6.member(LA(1)))) {
                    mATTR(false);
                }
                else {
                    if ( _cnt392>=1 ) { break _loop392; } else {throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());}
                }

                _cnt392++;
            } while (true);
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOFORM(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OFORM;
        int _saveIndex;

        match("<form");
        mWS(false);
        {
            int _cnt395=0;
_loop395:
            do {
                if ((_tokenSet_6.member(LA(1)))) {
                    mATTR(false);
                }
                else {
                    if ( _cnt395>=1 ) { break _loop395; } else {throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());}
                }

                _cnt395++;
            } while (true);
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOFONT(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OFONT;
        int _saveIndex;

        match("<font");
        mWS(false);
        {
            int _cnt398=0;
_loop398:
            do {
                if ((_tokenSet_6.member(LA(1)))) {
                    mATTR(false);
                }
                else {
                    if ( _cnt398>=1 ) { break _loop398; } else {throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());}
                }

                _cnt398++;
            } while (true);
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCFORM_OR_CFONT(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CFORM_OR_CFONT;
        int _saveIndex;

        match("</fo");
        {
            switch ( LA(1)) {
            case 'r':
                {
                    match("rm");
                    _ttype = CFORM;
                    break;
                }
            case 'n':
                {
                    match("nt");
                    _ttype = CFONT;
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mBFONT_OR_BASE(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = BFONT_OR_BASE;
        int _saveIndex;

        match("<base");
        {
            switch ( LA(1)) {
            case 'f':
                {
                    match("font");
                    mWS(false);
                    mATTR(false);
                    _ttype = BFONT;
                    break;
                }
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    mATTR(false);
                    _ttype = BASE;
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mBR(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = BR;
        int _saveIndex;

        match("<br");
        {
            switch ( LA(1)) {
case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(false);
                    mATTR(false);
                    break;
                }
            case '>':
                {
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mOMAP(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = OMAP;
        int _saveIndex;

        match("<map");
        mWS(false);
        mATTR(false);
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCMAP(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CMAP;
        int _saveIndex;

        match("</map>");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mAREA(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = AREA;
        int _saveIndex;

        match("<area");
        mWS(false);
        {
            int _cnt409=0;
_loop409:
            do {
                if ((_tokenSet_6.member(LA(1)))) {
                    mATTR(false);
                }
                else {
                    if ( _cnt409>=1 ) { break _loop409; } else {throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());}
                }

                _cnt409++;
            } while (true);
        }
        match('>');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mPCDATA(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = PCDATA;
        int _saveIndex;

        {
            int _cnt413=0;
_loop413:
            do {
                switch ( LA(1)) {
                case '\n':
                    {
                        match('\n');
                        newline();
                        break;
                    }
    case '\3':  case '\4':  case '\5':  case '\6':
    case '\7':  case '\10':  case '\t':  case '\13':
    case '\14':  case '\16':  case '\17':  case '\20':
    case '\21':  case '\22':  case '\23':  case '\24':
    case '\25':  case '\26':  case '\27':  case '\30':
    case '\31':  case '\32':  case '\33':  case '\34':
    case '\35':  case '\36':  case '\37':  case ' ':
    case '!':  case '#':  case '$':  case '%':
    case '&':  case '\'':  case '(':  case ')':
    case '*':  case '+':  case ',':  case '-':
    case '.':  case '/':  case '0':  case '1':
    case '2':  case '3':  case '4':  case '5':
    case '6':  case '7':  case '8':  case '9':
    case ':':  case ';':  case '=':  case '?':
    case '@':  case 'A':  case 'B':  case 'C':
    case 'D':  case 'E':  case 'F':  case 'G':
    case 'H':  case 'I':  case 'J':  case 'K':
    case 'L':  case 'M':  case 'N':  case 'O':
    case 'P':  case 'Q':  case 'R':  case 'S':
    case 'T':  case 'U':  case 'V':  case 'W':
    case 'X':  case 'Y':  case 'Z':  case '[':
    case '\\':  case ']':  case '^':  case '_':
    case '`':  case 'a':  case 'b':  case 'c':
    case 'd':  case 'e':  case 'f':  case 'g':
    case 'h':  case 'i':  case 'j':  case 'k':
    case 'l':  case 'm':  case 'n':  case 'o':
    case 'p':  case 'q':  case 'r':  case 's':
    case 't':  case 'u':  case 'v':  case 'w':
    case 'x':  case 'y':  case 'z':  case '{':
    case '|':  case '}':  case '~':  case '\177':
    case '\200':  case '\201':  case '\202':  case '\203':
    case '\204':  case '\205':  case '\206':  case '\207':
    case '\210':  case '\211':  case '\212':  case '\213':
    case '\214':  case '\215':  case '\216':  case '\217':
    case '\220':  case '\221':  case '\222':  case '\223':
    case '\224':  case '\225':  case '\226':  case '\227':
    case '\230':  case '\231':  case '\232':  case '\233':
    case '\234':  case '\235':  case '\236':  case '\237':
    case '\240':  case '\241':  case '\242':  case '\243':
    case '\244':  case '\245':  case '\246':  case '\247':
    case '\250':  case '\251':  case '\252':  case '\253':
    case '\254':  case '\255':  case '\256':  case '\257':
    case '\260':  case '\261':  case '\262':  case '\263':
    case '\264':  case '\265':  case '\266':  case '\267':
    case '\270':  case '\271':  case '\272':  case '\273':
    case '\274':  case '\275':  case '\276':  case '\277':
    case '\300':  case '\301':  case '\302':  case '\303':
    case '\304':  case '\305':  case '\306':  case '\307':
    case '\310':  case '\311':  case '\312':  case '\313':
    case '\314':  case '\315':  case '\316':  case '\317':
    case '\320':  case '\321':  case '\322':  case '\323':
    case '\324':  case '\325':  case '\326':  case '\327':
    case '\330':  case '\331':  case '\332':  case '\333':
    case '\334':  case '\335':  case '\336':  case '\337':
    case '\340':  case '\341':  case '\342':  case '\343':
    case '\344':  case '\345':  case '\346':  case '\347':
    case '\350':  case '\351':  case '\352':  case '\353':
    case '\354':  case '\355':  case '\356':  case '\357':
    case '\360':  case '\361':  case '\362':  case '\363':
    case '\364':  case '\365':  case '\366':  case '\367':
    case '\370':  case '\371':  case '\372':  case '\373':
    case '\374':  case '\375':  case '\376':  case '\377':
                    {
                        {
                            match(_tokenSet_7);
                        }
                        break;
                    }
                default:
                    if ((LA(1)=='\r') && (LA(2)=='\n')) {
                        match('\r');
                        match('\n');
                        newline();
                    }
                    else if ((LA(1)=='\r')) {
                        match('\r');
                        newline();
                    }
                    else {
                        if ( _cnt413>=1 ) { break _loop413; } else {throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());}
                    }
                }
                _cnt413++;
            } while (true);
        }
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    protected final void mCOMMENT_DATA(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = COMMENT_DATA;
        int _saveIndex;

        {
_loop417:
            do {
                switch ( LA(1)) {
                case '\n':
                    {
                        match('\n');
                        newline();
                        break;
                    }
    case '\3':  case '\4':  case '\5':  case '\6':
    case '\7':  case '\10':  case '\t':  case '\13':
    case '\14':  case '\16':  case '\17':  case '\20':
    case '\21':  case '\22':  case '\23':  case '\24':
    case '\25':  case '\26':  case '\27':  case '\30':
    case '\31':  case '\32':  case '\33':  case '\34':
    case '\35':  case '\36':  case '\37':  case ' ':
    case '!':  case '"':  case '#':  case '$':
    case '%':  case '&':  case '\'':  case '(':
    case ')':  case '*':  case '+':  case ',':
    case '.':  case '/':  case '0':  case '1':
    case '2':  case '3':  case '4':  case '5':
    case '6':  case '7':  case '8':  case '9':
    case ':':  case ';':  case '<':  case '=':
    case '>':  case '?':  case '@':  case 'A':
    case 'B':  case 'C':  case 'D':  case 'E':
    case 'F':  case 'G':  case 'H':  case 'I':
    case 'J':  case 'K':  case 'L':  case 'M':
    case 'N':  case 'O':  case 'P':  case 'Q':
    case 'R':  case 'S':  case 'T':  case 'U':
    case 'V':  case 'W':  case 'X':  case 'Y':
    case 'Z':  case '[':  case '\\':  case ']':
    case '^':  case '_':  case '`':  case 'a':
    case 'b':  case 'c':  case 'd':  case 'e':
    case 'f':  case 'g':  case 'h':  case 'i':
    case 'j':  case 'k':  case 'l':  case 'm':
    case 'n':  case 'o':  case 'p':  case 'q':
    case 'r':  case 's':  case 't':  case 'u':
    case 'v':  case 'w':  case 'x':  case 'y':
    case 'z':  case '{':  case '|':  case '}':
    case '~':  case '\177':  case '\200':  case '\201':
    case '\202':  case '\203':  case '\204':  case '\205':
    case '\206':  case '\207':  case '\210':  case '\211':
    case '\212':  case '\213':  case '\214':  case '\215':
    case '\216':  case '\217':  case '\220':  case '\221':
    case '\222':  case '\223':  case '\224':  case '\225':
    case '\226':  case '\227':  case '\230':  case '\231':
    case '\232':  case '\233':  case '\234':  case '\235':
    case '\236':  case '\237':  case '\240':  case '\241':
    case '\242':  case '\243':  case '\244':  case '\245':
    case '\246':  case '\247':  case '\250':  case '\251':
    case '\252':  case '\253':  case '\254':  case '\255':
    case '\256':  case '\257':  case '\260':  case '\261':
    case '\262':  case '\263':  case '\264':  case '\265':
    case '\266':  case '\267':  case '\270':  case '\271':
    case '\272':  case '\273':  case '\274':  case '\275':
    case '\276':  case '\277':  case '\300':  case '\301':
    case '\302':  case '\303':  case '\304':  case '\305':
    case '\306':  case '\307':  case '\310':  case '\311':
    case '\312':  case '\313':  case '\314':  case '\315':
    case '\316':  case '\317':  case '\320':  case '\321':
    case '\322':  case '\323':  case '\324':  case '\325':
    case '\326':  case '\327':  case '\330':  case '\331':
    case '\332':  case '\333':  case '\334':  case '\335':
    case '\336':  case '\337':  case '\340':  case '\341':
    case '\342':  case '\343':  case '\344':  case '\345':
    case '\346':  case '\347':  case '\350':  case '\351':
    case '\352':  case '\353':  case '\354':  case '\355':
    case '\356':  case '\357':  case '\360':  case '\361':
    case '\362':  case '\363':  case '\364':  case '\365':
    case '\366':  case '\367':  case '\370':  case '\371':
    case '\372':  case '\373':  case '\374':  case '\375':
            case '\376':  case '\377':
                    {
                        {
                            match(_tokenSet_8);
                        }
                        break;
                    }
                default:
                    if (((LA(1)=='-') && ((LA(2) >= '\3' && LA(2) <= '\377')) && ((LA(3) >= '\3' && LA(3) <= '\377')) && ((LA(4) >= '\3' && LA(4) <= '\377')))&&(LA(2)!='-' && LA(3)!='>')) {
                        match('-');
                    }
                    else if ((LA(1)=='\r') && (LA(2)=='\n') && ((LA(3) >= '\3' && LA(3) <= '\377')) && ((LA(4) >= '\3' && LA(4) <= '\377'))) {
                        match('\r');
                        match('\n');
                        newline();
                    }
                    else if ((LA(1)=='\r') && ((LA(2) >= '\3' && LA(2) <= '\377')) && ((LA(3) >= '\3' && LA(3) <= '\377')) && ((LA(4) >= '\3' && LA(4) <= '\377'))) {
                        match('\r');
                        newline();
                    }
                    else {
                        break _loop417;
                    }
                }
            } while (true);
        }
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    public final void mCOMMENT(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = COMMENT;
        int _saveIndex;

        match("<!--");
        mCOMMENT_DATA(false);
        match("-->");
        _ttype = Token.SKIP;
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    protected final void mWORD(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = WORD;
        int _saveIndex;

        {
            switch ( LA(1)) {
case 'a':  case 'b':  case 'c':  case 'd':
case 'e':  case 'f':  case 'g':  case 'h':
case 'i':  case 'j':  case 'k':  case 'l':
case 'm':  case 'n':  case 'o':  case 'p':
case 'q':  case 'r':  case 's':  case 't':
case 'u':  case 'v':  case 'w':  case 'x':
        case 'y':  case 'z':
                {
                    mLCLETTER(false);
                    break;
                }
            case '.':
                {
                    match('.');
                    break;
                }
            default:
                {
                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                }
            }
        }
        {
            int _cnt438=0;
_loop438:
            do {
                if (((LA(1) >= 'a' && LA(1) <= 'z')) && (_tokenSet_9.member(LA(2)))) {
                    mLCLETTER(false);
                }
                else if ((LA(1)=='.') && (_tokenSet_9.member(LA(2)))) {
                    match('.');
                }
                else if (((LA(1) >= '0' && LA(1) <= '9'))) {
                    mDIGIT(false);
                }
                else {
                    if ( _cnt438>=1 ) { break _loop438; } else {throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());}
                }

                _cnt438++;
            } while (true);
        }
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    protected final void mINT(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = INT;
        int _saveIndex;

        {
            int _cnt449=0;
_loop449:
            do {
                if (((LA(1) >= '0' && LA(1) <= '9'))) {
                    mDIGIT(false);
                }
                else {
                    if ( _cnt449>=1 ) { break _loop449; } else {throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());}
                }

                _cnt449++;
            } while (true);
        }
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    protected final void mHEXNUM(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = HEXNUM;
        int _saveIndex;

        match('#');
        mHEXINT(false);
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    protected final void mLCLETTER(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = LCLETTER;
        int _saveIndex;

        matchRange('a','z');
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

    protected final void mWSCHARS(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = WSCHARS;
        int _saveIndex;

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
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    protected final void mSPECIAL(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = SPECIAL;
        int _saveIndex;

        switch ( LA(1)) {
        case '<':
            {
                match('<');
                break;
            }
        case '~':
            {
                match('~');
                break;
            }
        default:
            {
                throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
            }
        }
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    protected final void mHEXINT(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = HEXINT;
        int _saveIndex;

        {
            int _cnt452=0;
_loop452:
            do {
                if ((_tokenSet_10.member(LA(1))) && (_tokenSet_11.member(LA(2)))) {
                    mHEXDIGIT(false);
                }
                else {
                    if ( _cnt452>=1 ) { break _loop452; } else {throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());}
                }

                _cnt452++;
            } while (true);
        }
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    protected final void mHEXDIGIT(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = HEXDIGIT;
        int _saveIndex;

        switch ( LA(1)) {
case '0':  case '1':  case '2':  case '3':
case '4':  case '5':  case '6':  case '7':
    case '8':  case '9':
            {
                matchRange('0','9');
                break;
            }
case 'a':  case 'b':  case 'c':  case 'd':
    case 'e':  case 'f':
            {
                matchRange('a','f');
                break;
            }
        default:
            {
                throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
            }
        }
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }

    protected final void mUNDEFINED_TOKEN(boolean _createToken) throws ScannerException, IOException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = UNDEFINED_TOKEN;
        int _saveIndex;

        switch ( LA(1)) {
        case '<':
            {
                match('<');
                {
_loop458:
                    do {
                        if ((_tokenSet_12.member(LA(1)))) {
                            matchNot('>');
                        }
                        else {
                            break _loop458;
                        }

                    } while (true);
                }
                match('>');
                {
_loop461:
                    do {
                        if ((LA(1)=='\n'||LA(1)=='\r')) {
                            {
                                if ((LA(1)=='\r') && (LA(2)=='\n')) {
                                    match("\r\n");
                                }
                                else if ((LA(1)=='\r')) {
                                    match('\r');
                                }
                                else if ((LA(1)=='\n')) {
                                    match('\n');
                                }
                                else {
                                    throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                                }

                            }
                            newline();
                        }
                        else {
                            break _loop461;
                        }

                    } while (true);
                }
                System.err.println("invalid tag: "+new String(text.getBuffer(),_begin,text.length()-_begin));
                break;
            }
    case '\n':  case '\r':
            {
                {
                    if ((LA(1)=='\r') && (LA(2)=='\n')) {
                        match("\r\n");
                    }
                    else if ((LA(1)=='\r')) {
                        match('\r');
                    }
                    else if ((LA(1)=='\n')) {
                        match('\n');
                    }
                    else {
                        throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
                    }

                }
                newline();
                break;
            }
case '\3':  case '\4':  case '\5':  case '\6':
case '\7':  case '\10':  case '\t':  case '\13':
case '\14':  case '\16':  case '\17':  case '\20':
case '\21':  case '\22':  case '\23':  case '\24':
case '\25':  case '\26':  case '\27':  case '\30':
case '\31':  case '\32':  case '\33':  case '\34':
case '\35':  case '\36':  case '\37':  case ' ':
case '!':  case '"':  case '#':  case '$':
case '%':  case '&':  case '\'':  case '(':
case ')':  case '*':  case '+':  case ',':
case '-':  case '.':  case '/':  case '0':
case '1':  case '2':  case '3':  case '4':
case '5':  case '6':  case '7':  case '8':
case '9':  case ':':  case ';':  case '=':
case '>':  case '?':  case '@':  case 'A':
case 'B':  case 'C':  case 'D':  case 'E':
case 'F':  case 'G':  case 'H':  case 'I':
case 'J':  case 'K':  case 'L':  case 'M':
case 'N':  case 'O':  case 'P':  case 'Q':
case 'R':  case 'S':  case 'T':  case 'U':
case 'V':  case 'W':  case 'X':  case 'Y':
case 'Z':  case '[':  case '\\':  case ']':
case '^':  case '_':  case '`':  case 'a':
case 'b':  case 'c':  case 'd':  case 'e':
case 'f':  case 'g':  case 'h':  case 'i':
case 'j':  case 'k':  case 'l':  case 'm':
case 'n':  case 'o':  case 'p':  case 'q':
case 'r':  case 's':  case 't':  case 'u':
case 'v':  case 'w':  case 'x':  case 'y':
case 'z':  case '{':  case '|':  case '}':
case '~':  case '\177':  case '\200':  case '\201':
case '\202':  case '\203':  case '\204':  case '\205':
case '\206':  case '\207':  case '\210':  case '\211':
case '\212':  case '\213':  case '\214':  case '\215':
case '\216':  case '\217':  case '\220':  case '\221':
case '\222':  case '\223':  case '\224':  case '\225':
case '\226':  case '\227':  case '\230':  case '\231':
case '\232':  case '\233':  case '\234':  case '\235':
case '\236':  case '\237':  case '\240':  case '\241':
case '\242':  case '\243':  case '\244':  case '\245':
case '\246':  case '\247':  case '\250':  case '\251':
case '\252':  case '\253':  case '\254':  case '\255':
case '\256':  case '\257':  case '\260':  case '\261':
case '\262':  case '\263':  case '\264':  case '\265':
case '\266':  case '\267':  case '\270':  case '\271':
case '\272':  case '\273':  case '\274':  case '\275':
case '\276':  case '\277':  case '\300':  case '\301':
case '\302':  case '\303':  case '\304':  case '\305':
case '\306':  case '\307':  case '\310':  case '\311':
case '\312':  case '\313':  case '\314':  case '\315':
case '\316':  case '\317':  case '\320':  case '\321':
case '\322':  case '\323':  case '\324':  case '\325':
case '\326':  case '\327':  case '\330':  case '\331':
case '\332':  case '\333':  case '\334':  case '\335':
case '\336':  case '\337':  case '\340':  case '\341':
case '\342':  case '\343':  case '\344':  case '\345':
case '\346':  case '\347':  case '\350':  case '\351':
case '\352':  case '\353':  case '\354':  case '\355':
case '\356':  case '\357':  case '\360':  case '\361':
case '\362':  case '\363':  case '\364':  case '\365':
case '\366':  case '\367':  case '\370':  case '\371':
case '\372':  case '\373':  case '\374':  case '\375':
    case '\376':  case '\377':
            {
                matchNot(EOF_CHAR);
                break;
            }
        default:
            {
                throw new ScannerException("no viable alt for char: "+(char)LA(1),getLine());
            }
        }
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }


    private static final long _tokenSet_0_data_[] = { 4611686022722364928L, 0L, 0L, 0L, 0L };
    public static final BitSet _tokenSet_0 = new BitSet(_tokenSet_0_data_);
    private static final long _tokenSet_1_data_[] = { 4294977024L, 0L, 0L, 0L, 0L };
    public static final BitSet _tokenSet_1 = new BitSet(_tokenSet_1_data_);
    private static final long _tokenSet_2_data_[] = { -5764607540214104072L, -1L, -1L, -1L, 0L, 0L, 0L, 0L };
    public static final BitSet _tokenSet_2 = new BitSet(_tokenSet_2_data_);
    private static final long _tokenSet_3_data_[] = { 4611756391466542592L, 576460743713488896L, 0L, 0L, 0L };
    public static final BitSet _tokenSet_3 = new BitSet(_tokenSet_3_data_);
    private static final long _tokenSet_4_data_[] = { -17179869192L, -1L, -1L, -1L, 0L, 0L, 0L, 0L };
    public static final BitSet _tokenSet_4 = new BitSet(_tokenSet_4_data_);
    private static final long _tokenSet_5_data_[] = { -549755813896L, -1L, -1L, -1L, 0L, 0L, 0L, 0L };
    public static final BitSet _tokenSet_5 = new BitSet(_tokenSet_5_data_);
    private static final long _tokenSet_6_data_[] = { 70368744177664L, 576460743713488896L, 0L, 0L, 0L };
    public static final BitSet _tokenSet_6 = new BitSet(_tokenSet_6_data_);
    private static final long _tokenSet_7_data_[] = { -5764607540214113288L, -1L, -1L, -1L, 0L, 0L, 0L, 0L };
    public static final BitSet _tokenSet_7 = new BitSet(_tokenSet_7_data_);
    private static final long _tokenSet_8_data_[] = { -35184372098056L, -1L, -1L, -1L, 0L, 0L, 0L, 0L };
    public static final BitSet _tokenSet_8 = new BitSet(_tokenSet_8_data_);
    private static final long _tokenSet_9_data_[] = { 7205548439294191104L, 576460743713488896L, 0L, 0L, 0L };
    public static final BitSet _tokenSet_9 = new BitSet(_tokenSet_9_data_);
    private static final long _tokenSet_10_data_[] = { 287948901175001088L, 541165879296L, 0L, 0L, 0L };
    public static final BitSet _tokenSet_10 = new BitSet(_tokenSet_10_data_);
    private static final long _tokenSet_11_data_[] = { 4899705292641543680L, 576460743713488896L, 0L, 0L, 0L };
    public static final BitSet _tokenSet_11 = new BitSet(_tokenSet_11_data_);
    private static final long _tokenSet_12_data_[] = { -4611686018427387912L, -1L, -1L, -1L, 0L, 0L, 0L, 0L };
    public static final BitSet _tokenSet_12 = new BitSet(_tokenSet_12_data_);

}
