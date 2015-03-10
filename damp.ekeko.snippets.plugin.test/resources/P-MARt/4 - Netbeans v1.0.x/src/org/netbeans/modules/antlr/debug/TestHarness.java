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
import antlr.collections.*;
import java.util.Vector;

/**
 *
 * @author  jleppanen
 * @version 
 */
public class TestHarness extends Object {
    public static ScannerGen scannerGen;
    public static ParserGen parserGen;

    static final int TESTING_SCANNER=0;
    static final int TESTING_PARSER=1;
    static final int TESTING_TREEWALKER=2;
    static int testing = TESTING_SCANNER;
    protected static boolean printOnlyFailures = true;

    static int numberOfTestsFailed = 0;
    static int numberOfTestsTaken = 0;

    public TestHarness() {
    }

    public static abstract class ScannerGen {
        abstract public CharScanner scanner(String s);
    }
    public static abstract class ParserGen {
        abstract public AST parseAST(TokenStream in);
    }
    public static class XScannerGen extends ScannerGen {
        java.lang.reflect.Constructor ctor;
        public XScannerGen(Class clazz) throws Exception {
            ctor = clazz.getConstructor(new Class[]{java.io.Reader.class});
        }
        public CharScanner scanner(String s) {
            CharScanner x = null;
            try {
                x = (CharScanner)ctor.newInstance(
                        new Object[]{ new java.io.StringReader(s)});
            } catch (Exception ex) {
                System.out.println("Scanner instantiation failed: "+ex);
            }
            return x;
        }
    }

    static void testFailed(String s) {
        numberOfTestsTaken++;
        numberOfTestsFailed++;
        System.out.println(s);
    }
    static void testSucceeded(String s) {
        numberOfTestsTaken++;
        if (!printOnlyFailures) {
            System.out.println(s);
        }
    }
    static void testFailed(StringBuffer s) { testFailed(s.toString()); }
    static void testSucceeded(StringBuffer s) { testSucceeded(s.toString()); }

    static public void t(String input, int[] tokens) throws TokenStreamException {
        switch (testing) {
        case TESTING_SCANNER: tL_S(input, tokens); break;
        case TESTING_PARSER: break;
        case TESTING_TREEWALKER: break;
        }
    }

    static public void tP(int[] in, AST ast) throws TokenStreamException {
        tP(new TstTokenStream(in), ast);
    }
    static public void tP(String in, AST ast) throws TokenStreamException {
        //verifyScannerIsValid();
        CharScanner scanner = scannerGen.scanner(in);
        tP(scanner, ast);
    }

    static public void tP(TokenStream in, AST ast) throws TokenStreamException {
        StringBuffer s = new StringBuffer(256);
        System.out.println("TESTING PARSER");
        if (parserGen == null) {
            System.out.println("-- NO PARSER GEN --");
            return;
        }
        AST result = parserGen.parseAST(in);
        if (result==null) {
            System.out.println("NOTHING MATCHED");
            return;
        }
        boolean b = result.equals(ast);
        if (b) {
            System.out.println("MATCHED");
        } else {
            System.out.println("FAILED");
            System.out.println("-- Expected tree: ");
            DumpASTVisitor visitor = new DumpASTVisitor();
            visitor.visit(ast);
            System.out.println("-- Got tree: ");
            visitor.visit(result);
            // * print unread input tokens
            System.out.print("unread input tokens: (");
            for (int i=0;i<42;i++) {
                Token token = in.nextToken();
                if (token==null) {break;}
                if (token.getType()==1) {break;} // EOF
                System.out.print(" "+i+": "+token);
            }
            System.out.println(" )");
        }
    }


    static public void tL_S(String input, int[] tokens) throws TokenStreamException {
        StringBuffer s = new StringBuffer(256);
        CharScanner charScanner = scannerGen.scanner(input);
        s.append("TESTING if input:\""+input+"\" produces tokens "+printTokens(tokens)+"\n");
        tL(charScanner, tokens,s);
    }
    static public void tL(TokenStream input, int[] tokens,StringBuffer s)
    throws TokenStreamException {
        Token scannedToken;
        int i=0;
        while ((scannedToken = input.nextToken()) != null) {
            s.append("scannedToken: "+scannedToken+"\n");
            if (i >= tokens.length) {
                if (scannedToken.getType()==1) { break; }
                s.append("!!!!!TEST FAILED!!!!! expected too few tokens ["+tokens.length+"]\n");
                testFailed(s);
                return;
            }
            int tokenId = tokens[i++];

            if (!(tokenId == scannedToken.getType())) {
                s.append("!!!!!TEST FAILED!!!!! at "+i+":"+tokenId+" got "+scannedToken);
                testFailed(s);
                return;
            }
        }
        if (i < tokens.length) {
            s.append("!!!!!TEST FAILED!!!!! expected too many ["+tokens.length+"]tokens and got"+i);
            testFailed(s);
            return;
        }
        if (!printOnlyFailures) {
            s.append("TEST SUCCEEDED");
            testSucceeded(s);
        }
    }

    static public void t(String input, String expected_s) throws TokenStreamException {
        StringBuffer s = new StringBuffer(256);
        CharScanner scanner = scannerGen.scanner(input);
        Token t = scanner.nextToken();

        s.append("TESTING if input:\""+input+"\" produces \""+expected_s+"\"\n");
        boolean testFailed = !(expected_s.compareTo(t.getText()) == 0);
        if (testFailed) {
            s.append("FAILED "+t);
            testFailed(s);
        } else {
            s.append("PASSED");
            testSucceeded(s);
        }
    }

    static public void t(String input, String[] expected_strings)
    throws TokenStreamException {
        StringBuffer s = new StringBuffer(256);
        CharScanner scanner = scannerGen.scanner(input);

        s.append("TESTING if input:\""+input+"\" produces \""+
                 printStringArray(expected_strings)+"\"\n");
        for (int i=0; i<expected_strings.length; i++) {
            String expected_s = expected_strings[i];
            Token t = scanner.nextToken();
            if ( (t==null) | (t.getText()==null) ) {
                s.append("FAILED at "+i+" expectedL '"+expected_s+"'");
                testFailed(s);  return;
            }

            boolean testFailed = !(expected_s.compareTo(t.getText()) == 0);
            if (testFailed) {
                s.append("FAILED at "+i+" expected: '"+expected_s+"'"+" got: '"+
                         t.getText()+"'");
                testFailed(s); return;
            }
        }
        s.append("PASSED");
        testSucceeded(s);
    }

    static String printTokens(int[] tokens) {
        String s = "";
        s+="(";
        for (int i=0;i<tokens.length;i++) {
            s+=(""+ tokens[i] + ",");
        }
        s+=(")");
        return s;
    }

    static String printStringArray(String[] strings) {
        String s = "";
        s+="(";
        for (int i=0;i<strings.length;i++) {
            s+=(""+ strings[i] + ",");
        }
        s+=(")");
        return s;
    }

    public static class TstTokenStream implements TokenStream {
        /**
         * @associates Integer 
         */
        Vector tokens;
        int   i = 0;

        public TstTokenStream(int[] tokens) {
            this.tokens = new Vector();
            for (int n=0; n<tokens.length;n++) {
                this.tokens.addElement(new Integer(tokens[n]));
            }
        }
        public TstTokenStream(CharScanner scanner) throws Exception {
            tokens = new Vector();
            while (true) {
                Token t = scanner.nextToken();
                if ( (t==null) | (t.getText()==null) ) { break; }
                tokens.addElement(new Integer(t.getType()));
            }
        }

        public Token nextToken() throws TokenStreamException {
            if (i>=tokens.size()) { return null; }
            Token token = new Token( ((Integer)tokens.elementAt(i)).intValue() );
            i++;
            return token;
        }
    }

    public static void printTestStatistics() {
        System.out.println("");
        System.out.println("Made "+numberOfTestsTaken+" test(s) of which "+
                           numberOfTestsFailed + " failed!");
    }
}
