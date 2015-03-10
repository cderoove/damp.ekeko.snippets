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
import java.io.*;
import com.jguru.util.StringUtils;

/**
 *
 * @author  jleppanen
 * @version 
 */
public class SyntaxToHTML extends TestHarness
{
    public static boolean isPrintTagTypeOn = true;

    final static PrintStream out = System.out;

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) throws java.lang.Exception {
        String file = args[1];

        out.println("<html><body>");
        outputSyntaxToHtml_TokenLevel(file);

        outputSyntaxToHtml_ParseLevel(file);
        out.println("</body></html>");
    }

    public static void outputSyntaxToHtml_ParseLevel(String file)
    throws Exception
    {
        Reader reader = new FileReader(file);
        HTMLLexer lexer = new HTMLLexer( reader );
        HTMLParser parser = new HTMLParser( lexer );

        try {
            parser.parseHTML();
        } catch (Exception ex) {
            out.println("Caught exception: ");
            ex.printStackTrace();
            return;
        }
        AST t = parser.getAST();
        out.println("<h2>Parser</h2><pre>");
        printSyntaxTreeToHtml(t);
        out.println("</pre>");
    }

    public static void printSyntaxTreeToHtml(AST t) {
        if (t==null) { return; }
        System.out.print("<ul>");
        System.out.print("<li>");
        printColoredToken(t.getType(), t.getText());
        System.out.print("</li>");

        printSyntaxTreeToHtml(t.getFirstChild());

        // NOW The siblings
        AST sibling = t.getNextSibling();
        while (sibling != null) {
            if (sibling.getType() == 1) { break; }
            System.out.print("<li>");
            printColoredToken(sibling.getType(),sibling.getText());
            printSyntaxTreeToHtml(sibling.getFirstChild());
            sibling = sibling.getNextSibling();
        }
        System.out.print("</ul>");
    }

    public static void printColoredToken(int type,String text) {
        System.out.print(
            "<font color="+colorForType(type)+">"
            + StringUtils.escapeHTMLStuff(text) +
            "</font>"
        );
    }

    public static void outputSyntaxToHtml_TokenLevel(String file)
    throws Exception
    {
        out.println("<h2>Lexer</h2><pre>");
        outputSyntaxToHtml_TokenLevel2(file);
        out.println("</pre>");
    }

    private static void outputSyntaxToHtml_TokenLevel2(String file)
    throws Exception
    {
        Reader reader = new FileReader(file);
        HTMLLexer lexer = new HTMLLexer( reader );
        HTMLParser parser = new HTMLParser( lexer );

        boolean toggle = true;
        try {
            Token t;
            while ((t = lexer.nextToken()) != null) {
                if (t.getType() == 1) { break; }
                //toggle = !toggle;

                if (isPrintTagTypeOn) {
                    System.out.print("<b>["+t.getType()+":"
                                     +parser._tokenNames[t.getType()]+"&gt;&gt;</b>");
                }
                System.out.print(
                    ((toggle) ? "<font color="+colorForType(t.getType())+">" : "")
                    + StringUtils.escapeHTMLStuff(t.getText()) +
                    ((toggle) ? "</font>" : "")
                );
                if (isPrintTagTypeOn) {
                    System.out.print("<b>&lt;&lt;"+t.getType()+"]</b>");
                }
            }
        } catch (Exception ex) {
        }
    }

    public static String colorForType(int type) {
        return "red";
    }
}
