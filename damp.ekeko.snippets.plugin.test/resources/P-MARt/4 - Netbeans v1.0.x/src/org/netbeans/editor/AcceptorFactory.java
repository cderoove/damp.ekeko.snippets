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

package org.netbeans.editor;

/** Mostly used acceptors
*
* @author Miloslav Metelka
* @version 1.00
*/


public class AcceptorFactory {

    public static final Acceptor TRUE
    = new Acceptor() {
          public final boolean accept(char ch) {
              return true;
          }
      };

    public static final Acceptor FALSE
    = new Acceptor() {
          public final boolean accept(char ch) {
              return false;
          }
      };

    public static final Acceptor SPACE
    = new Acceptor() {
          public final boolean accept(char ch) {
              return ch == ' ';
          }
      };

    public static final Acceptor SPACE_TAB
    = new Acceptor() {
          public final boolean accept(char ch) {
              return ch == ' ' || ch == '\t';
          }
      };

    public static final Acceptor SPACE_NL
    = new Acceptor() {
          public final boolean accept(char ch) {
              return ch == ' ' || ch == '\n';
          }
      };

    public static final Acceptor NL
    = new Acceptor() {
          public final boolean accept(char ch) {
              return ch == '\n';
          }
      };

    public static final Acceptor WHITESPACE
    = new Acceptor() {
          public final boolean accept(char ch) {
              return Character.isWhitespace(ch);
          }
      };

    public static final Acceptor NON_WHITESPACE
    = new Acceptor() {
          public final boolean accept(char ch) {
              return !Character.isWhitespace(ch);
          }
      };

    public static final Acceptor LETTER_DIGIT
    = new Acceptor() {
          public final boolean accept(char ch) {
              return Character.isLetterOrDigit(ch);
          }
      };

    public static final Acceptor NON_LETTER_DIGIT
    = new Acceptor() {
          public final boolean accept(char ch) {
              return !Character.isLetterOrDigit(ch);
          }
      };

    public static final Acceptor JAVA_IDENTIFIER
    = new Acceptor() {
          public final boolean accept(char ch) {
              return Character.isJavaIdentifierPart(ch);
          }
      };

    public static final Acceptor NON_JAVA_IDENTIFIER
    = new Acceptor() {
          public final boolean accept(char ch) {
              return !Character.isJavaIdentifierPart(ch);
          }
      };

    public static final Acceptor JAVA_IDENTIFIER_DOT
    = new Acceptor() {
          public final boolean accept(char ch) {
              return Character.isJavaIdentifierPart(ch) || ch == '.';
          }
      };

    public static final Acceptor DOT
    = new Acceptor() {
          public final boolean accept(char ch) {
              return ch == '.';
          }
      };

}

/*
 * Log
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         9/15/99  Miloslav Metelka 
 *  1    Gandalf   1.0         6/10/99  Miloslav Metelka 
 * $
 */

