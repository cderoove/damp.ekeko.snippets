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

package org.netbeans.editor.ext;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.swing.text.BadLocationException;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.TextBatchProcessor;
import org.netbeans.editor.FinderFactory;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.Analyzer;

/**
* Support methods for syntax analyzes
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaSyntaxSupport extends ExtSyntaxSupport {

    // Internal java declaration token processor states
    static final int INIT = 0;
    static final int AFTER_TYPE = 1;
    static final int AFTER_VARIABLE = 2;
    static final int AFTER_COMMA = 3;
    static final int AFTER_DOT = 4;
    static final int AFTER_TYPE_LSB = 5;
    static final int AFTER_MATCHING_VARIABLE_LSB = 6;
    static final int AFTER_MATCHING_VARIABLE = 7;

    private static final int[] COMMENT_TOKENS = new int[] {
                JavaSyntax.LINE_COMMENT,
                JavaSyntax.BLOCK_COMMENT
            };

    private static final int[] BRACKET_SKIP_TOKENS = new int[] {
                JavaSyntax.LINE_COMMENT,
                JavaSyntax.BLOCK_COMMENT,
                JavaSyntax.CHAR,
                JavaSyntax.STRING
            };

    private static final char[] COMMAND_SEPARATOR_CHARS = new char[] {
                ';', '{', '}'
            };

    private JavaImport javaImport;

    private boolean javaImportValid;


    public JavaSyntaxSupport(BaseDocument doc) {
        super(doc);
        endVarAcceptor = AcceptorFactory.JAVA_IDENTIFIER;
        varAcceptor = AcceptorFactory.JAVA_IDENTIFIER_DOT;
        javaImport = new JavaImport();
    }

    protected void documentModified(DocumentEvent evt) {
        super.documentModified(evt);
        javaImportValid = false;
    }

    public int[] getCommentTokens() {
        return COMMENT_TOKENS;
    }

    public int[] getBracketSkipTokens() {
        return BRACKET_SKIP_TOKENS;
    }

    public int getLastCommandSeparator(int pos)
    throws BadLocationException {
        TextBatchProcessor tbp = new TextBatchProcessor() {
                                     public int processTextBatch(BaseDocument doc, int startPos, int endPos,
                                                                 boolean lastBatch) {
                                         try {
                                             int[] blks = getCommentBlocks(endPos, startPos);
                                             FinderFactory.CharArrayBwdFinder cmdFinder
                                             = new FinderFactory.CharArrayBwdFinder(COMMAND_SEPARATOR_CHARS);
                                             return findOutsideBlocks(cmdFinder, startPos, endPos, blks);
                                         } catch (BadLocationException e) {
                                             e.printStackTrace();
                                             return -1;
                                         }
                                     }
                                 };
        return doc.processText(tbp, pos, 0);
    }

    /** Get the class from name. The import sections are consulted to find
    * the proper package for the name. If the search in import sections fails
    * the method can ask the finder to search just by the given name.
    * @param className name to resolve. It can be either the full name
    *   or just the name without the package.
    * @param searchByName if true and the resolving through the import sections fails
    *   the finder is asked to find the class just by the given name
    */
    public JCClass getClassFromName(String className, boolean searchByName) {
        refreshJavaImport();
        JCClass ret = JCompletion.getPrimitiveClass(className);
        if (ret == null) {
            ret = javaImport.getClazz(className);
        }
        if (ret == null && searchByName) {
            List clsList = JCompletion.getFinder().findClasses(null, className, true);
            if (clsList != null && clsList.size() > 0) {
                if (clsList.size() > 0) { // more matching classes
                    ret = (JCClass)clsList.get(0); // get the first one
                }
            }

        }
        return ret;
    }

    protected void refreshJavaImport() {
        if (!javaImportValid) {
            javaImport.update(doc);
        }
    }

    protected void refreshCompletion() {
    }

    /** Get the class that belongs to the given position */
    public JCClass getClass(int pos) {
        return null;
    }

    public boolean isStaticBlock(int pos) {
        return false;
    }

    protected DeclarationTokenProcessor createDeclarationTokenProcessor(
        String varName, int startPos, int endPos) {
        return new JavaDeclarationTokenProcessor(varName);
    }

    protected VariableMapTokenProcessor createVariableMapTokenProcessor(
        int startPos, int endPos) {
        return new JavaDeclarationTokenProcessor(null);
    }

    class JavaDeclarationTokenProcessor
        implements DeclarationTokenProcessor, VariableMapTokenProcessor {

        /** Position of the begining of the declaration to be returned */
        int decStartPos = -1;

        int decArrayDepth;

        /** Starting position of the declaration type */
        int typeStartPos;

        /** Position of the end of the type */
        int typeEndPos;

        /** Offset of the name of the variable */
        int decVarNameOffset;

        /** Length of the name of the variable */
        int decVarNameLen;

        /** Currently inside parenthesis, i.e. comma delimits declarations */
        boolean inParenthesis;

        /** Depth of the array when there is an array declaration */
        int arrayDepth;

        char[] buffer;

        int bufferStartPos;

        String varName;

        int state;

        /** Map filled with the [varName, type] pairs 
         * @associates JCType*/
        HashMap varMap;


        /** Construct new token processor
        * @param varName it contains valid varName name or null to search
        *   for all variables and construct the variable map.
        */
        JavaDeclarationTokenProcessor(String varName) {
            this.varName = varName;
            if (varName == null) {
                varMap = new HashMap();
            }
        }

        public int getDeclarationPosition() {
            return decStartPos;
        }

        public Map getVariableMap() {
            return varMap;
        }

        private void processDeclaration() {
            if (varName == null) { // collect all variables
                String decType = new String(buffer, typeStartPos - bufferStartPos,
                                            typeEndPos - typeStartPos);
                if (decType.indexOf(' ') >= 0) {
                    decType = Analyzer.removeSpaces(decType);
                }
                String decVarName = new String(buffer, decVarNameOffset, decVarNameLen);
                JCClass cls = getClassFromName(decType, true);
                if (cls != null) {
                    varMap.put(decVarName, JCompletion.getType(cls, decArrayDepth));
                }
            } else {
                decStartPos = typeStartPos;
            }
        }

        public boolean token(int tokenID, int helperID, int offset, int tokenLen) {
            int pos = bufferStartPos + offset;
            switch (tokenID) {
            case JavaSyntax.KEYWORD:
                switch (helperID) {
                case JavaKeywords.BOOLEAN:
                case JavaKeywords.BYTE:
                case JavaKeywords.CHAR:
                case JavaKeywords.DOUBLE:
                case JavaKeywords.FLOAT:
                case JavaKeywords.INT:
                case JavaKeywords.LONG:
                case JavaKeywords.SHORT:
                case JavaKeywords.VOID:
                    typeStartPos = pos;
                    arrayDepth = 0;
                    typeEndPos = pos + tokenLen;
                    state = AFTER_TYPE;
                    break;

                default:
                    state = INIT;
                    break;
                }
                break;

            case JavaSyntax.OPERATOR:
                switch (helperID) {
                case JavaSyntax.DOT:
                    switch (state) {
                    case AFTER_TYPE: // allowed only inside type
                        state = AFTER_DOT;
                        typeEndPos = pos + tokenLen;
                        break;

                    default:
                        state = INIT;
                        break;
                    }
                    break;

                case JavaSyntax.LEFT_SQUARE_BRACKET:
                    switch (state) {
                    case AFTER_TYPE:
                        state = AFTER_TYPE_LSB;
                        arrayDepth++;
                        break;

                    case AFTER_MATCHING_VARIABLE:
                        state = AFTER_MATCHING_VARIABLE_LSB;
                        decArrayDepth++;
                        break;

                    default:
                        state = INIT;
                        break;
                    }
                    break;

                case JavaSyntax.RIGHT_SQUARE_BRACKET:
                    switch (state) {
                    case AFTER_TYPE_LSB:
                        state = AFTER_TYPE;
                        break;

                    case AFTER_MATCHING_VARIABLE_LSB:
                        state = AFTER_MATCHING_VARIABLE;
                        break;

                    default:
                        state = INIT;
                        break;
                    }
                    break; // both in type and varName

                case JavaSyntax.LEFT_PARENTHESES:
                    inParenthesis = true;
                    state = INIT;
                    break;

                case JavaSyntax.RIGHT_PARENTHESES:
                    if (state == AFTER_MATCHING_VARIABLE) {
                        processDeclaration();
                    }
                    inParenthesis = false;
                    state = INIT;
                    break;

                case JavaSyntax.LEFT_BRACE:
                case JavaSyntax.RIGHT_BRACE:
                    inParenthesis = false; // to tolerate opened parenthesis
                    state = INIT;
                    break;

                case JavaSyntax.COMMA:
                    if (inParenthesis) { // comma is declaration separator in parenthesis
                        if (state == AFTER_MATCHING_VARIABLE) {
                            processDeclaration();
                        }
                        state = INIT;
                    } else { // not in parenthesis
                        switch (state) {
                        case AFTER_MATCHING_VARIABLE:
                            processDeclaration();
                            // let it flow to AFTER_VARIABLE
                        case AFTER_VARIABLE:
                            state = AFTER_COMMA;
                            break;

                        default:
                            state = INIT;
                            break;
                        }
                    }
                    break;

                case JavaSyntax.EQ:
                    // let it flow to SEMICOLON
                case JavaSyntax.SEMICOLON:
                    if (state == AFTER_MATCHING_VARIABLE) {
                        processDeclaration();
                    }
                    state = INIT;
                    break;

                default:
                    state = INIT;
                    break;
                }
                break;

            case JavaSyntax.IDENTIFIER:
                switch (state) {
                case AFTER_TYPE:
                case AFTER_COMMA:
                    if (varName == null || Analyzer.equals(varName, buffer, offset, tokenLen)) {
                        decArrayDepth = arrayDepth;
                        decVarNameOffset = offset;
                        decVarNameLen = tokenLen;
                        state = AFTER_MATCHING_VARIABLE;
                    } else {
                        state = AFTER_VARIABLE;
                    }
                    break;

                case AFTER_VARIABLE: // error
                    state = INIT;
                    break;

                case AFTER_DOT:
                    typeEndPos = pos + tokenLen;
                    state = AFTER_TYPE;
                    break;

                case INIT:
                    typeStartPos = pos;
                    arrayDepth = 0;
                    typeEndPos = pos + tokenLen;
                    state = AFTER_TYPE;
                    break;

                default:
                    state = INIT;
                    break;
                }
                break;

            case JavaSyntax.TEXT: // whitespace ignored
                break;

            default:
                state = INIT;
                break;
            }

            return true;
        }

        public int eot(int offset) {
            return 0;
        }

        public void nextBuffer(char[] buffer, int offset, int len,
                               int startPos, int preScan, boolean lastBuffer) {
            this.buffer = buffer;
            bufferStartPos = startPos - offset;
        }

    }

}

/*
 * Log
 *  15   Gandalf   1.14        12/28/99 Miloslav Metelka 
 *  14   Gandalf   1.13        11/14/99 Miloslav Metelka 
 *  13   Gandalf   1.12        11/10/99 Miloslav Metelka 
 *  12   Gandalf   1.11        11/9/99  Miloslav Metelka 
 *  11   Gandalf   1.10        11/8/99  Miloslav Metelka 
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         10/10/99 Miloslav Metelka 
 *  8    Gandalf   1.7         9/15/99  Miloslav Metelka 
 *  7    Gandalf   1.6         9/10/99  Miloslav Metelka 
 *  6    Gandalf   1.5         8/27/99  Miloslav Metelka 
 *  5    Gandalf   1.4         8/18/99  Miloslav Metelka 
 *  4    Gandalf   1.3         7/30/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/20/99  Miloslav Metelka 
 *  2    Gandalf   1.1         6/10/99  Miloslav Metelka 
 *  1    Gandalf   1.0         6/8/99   Miloslav Metelka 
 * $
 */

