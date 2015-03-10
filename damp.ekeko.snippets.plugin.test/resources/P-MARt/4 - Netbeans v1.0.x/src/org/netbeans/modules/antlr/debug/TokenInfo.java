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

package org.netbeans.modules.antlr.debug;

import antlr.*;
import java.io.*;

/**
 *
 * @author  jleppanen
 * @version 
 */
public class TokenInfo extends SimpleTokenManager implements Cloneable  {

    /** Creates new TokenInfo */
    public TokenInfo() {
        // Read a file with lines of the form ID=number
        try {
            // SAS: changed the following for proper text io
            FileReader fileIn = new FileReader(filename);
            ANTLRTokdefLexer tokdefLexer = new ANTLRTokdefLexer(fileIn);
            ANTLRTokdefParser tokdefParser = new ANTLRTokdefParser(tokdefLexer);
            tokdefParser.setFilename(filename);
            tokdefParser.file(this);
        }
        catch (ParserException ex) {
            tool.panic("Error parsing importVocab file '" + filename + "': " + ex.toString());
        }
        catch (IOException ex) {
            tool.panic("Error reading importVocab file '" + filename + "'");
        }
    }

    public String getName(int tokenId) {
        return "!!! NO NAME FOR TOKEN: "+tokenId+" !!!";
    }

    public String getName(Token token) {
        return "!!! NO NAME FOR TOKEN: "+"?"+" !!!";
    }

    public void init(File tokenFile) throws java.io.IOException {
        FileReader reader = new FileReader(tokenFile);
        ANTLRTokdefLexer l = new ANTLRTokdefLexer(reader);
        ANTLRTokdefParser p = new ANTLRTokdefParser(l);

    }

    public Object clone() {
        ImportVocabTokenManager tm;
        tm = (ImportVocabTokenManager)super.clone();
        tm.filename = this.filename;
        tm.grammar = this.grammar;
        return tm;
    }
    /** define a token. */
    public void define(TokenSymbol ts) {
        super.define(ts);
    }
    /** define a token.  Intended for use only when reading the importVocab file. */
    public void define(String s, int ttype) {
        TokenSymbol ts=null;
        if ( s.startsWith("\"") ) {
            ts = new StringLiteralSymbol(s);
        }
        else {
            ts = new TokenSymbol(s);
        }
        ts.setTokenType(ttype);
        super.define(ts);
        maxToken = (ttype+1)>maxToken ? (ttype+1) : maxToken;	// record maximum token type
    }
    /** importVocab token manager is read-only if output would be same as input */
    public boolean isReadOnly() {
        return readOnly;
    }
    /** Get the next unused token type. */
    public int nextTokenType() {
        return super.nextTokenType();
    }
}