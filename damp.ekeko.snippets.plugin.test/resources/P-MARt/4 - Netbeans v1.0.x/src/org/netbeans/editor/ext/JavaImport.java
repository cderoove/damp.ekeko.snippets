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

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenProcessor;

/**
* Mapping of colorings to particular token types
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaImport implements TokenProcessor {

    /** Initial length of the document to be scanned. It should be big enough
    * so that only one pass is necessary. If the initial section is too
    * long, then this value is doubled and the whole parsing restarted.
    */
    private static final int INIT_SCAN_LEN = 4096;

    private static final int INIT = 0; // at the line begining before import kwd
    private static final int AFTER_IMPORT = 1; // right after the import kwd
    private static final int INSIDE_EXP = 2; // inside import expression
    // inside import expression mixed from several different tokens
    // exp string buffer is used in this case
    private static final int INSIDE_MIXED_EXP = 3;

    /** Short names to classes map 
     * @associates JCClass*/
    private HashMap name2Class = new HashMap(501);

    private char[] buffer;

    /**
     * @associates Info 
     */
    private ArrayList infoList = new ArrayList();

    /** Current state of the imports parsing */
    private int state;

    /** Whether parsing package statement instead of import statment.
    * They have similair syntax so only this flag distinguishes them.
    */
    private boolean parsingPackage;

    /** Start of the whole import statement */
    private int startPos;

    /** Start position of the particular import expression */
    private int expPos;

    private boolean eotReached;

    private StringBuffer exp = new StringBuffer();

    /** Whether the star was found at the end of package expression */
    private boolean star;



    JavaSyntax debugSyntax = new JavaSyntax(); // !!! debugging syntax

    public JavaImport() {
    }

    public synchronized void update(BaseDocument doc) {
        doc.readLock();
        try {
            int scanLen = INIT_SCAN_LEN;
            int docLen = doc.getLength();
            boolean wholeDoc = false;
            do {
                if (scanLen >= docLen) {
                    scanLen = docLen;
                    wholeDoc = true;
                }
                eotReached = false;
                init();
                try {
                    doc.getSyntaxSupport().tokenizeText(this, 0, scanLen, false);
                } catch (BadLocationException e) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) { // NOI18N
                        e.printStackTrace();
                    }
                }
                scanLen *= 4; // increase the scanning size
            } while (!wholeDoc && eotReached);
        } finally {
            doc.readUnlock();
        }
        buffer = null;
    }

    protected void init() {
        exp.setLength(0);
        star = false;
        parsingPackage = false;
        infoList.clear();

        name2Class.clear(); // clear current mappings
        // add java.lang package by default
        JCPackage pkg = JCompletion.getFinder().getExactPackage("java.lang"); // NOI18N
        if (pkg != null) {
            JCClass[] classes = pkg.getClasses();
            for (int i = 0; i < classes.length; i++) {
                name2Class.put(classes[i].getName(), classes[i]);
            }
        }

    }

    public JCClass getClazz(String className) {
        JCFinder finder = JCompletion.getFinder();
        JCClass ret = finder.getExactClass(className); // first try exact match
        if (ret == null) {
            ret = (JCClass)name2Class.get(className);
        }
        return ret;
    }

    protected void packageStatementFound(int packageStartPos, int packageEndPos, String packageExp) {
        JCPackage pkg = JCompletion.getFinder().getExactPackage(packageExp);
        if (pkg != null) {
            JCClass[] classes = pkg.getClasses();
            for (int i = 0; i < classes.length; i++) {
                name2Class.put(classes[i].getName(), classes[i]);
            }
        }
    }

    protected void importStatementFound(int importStartPos, int importEndPos, String importExp, boolean starAtEnd) {
        JCFinder finder = JCompletion.getFinder();
        Info info = new Info(importStartPos, importEndPos, starAtEnd);
        JCClass cls = finder.getExactClass(importExp);
        if (cls != null) {
            info.cls = cls;
            if (star) { // !!! dodelat
            } else { // only this single class
                name2Class.put(cls.getName(), cls);
            }
        } else { // not a direct class, try package
            JCPackage pkg = finder.getExactPackage(importExp);
            if (pkg != null) {
                info.pkg = pkg;
                if (starAtEnd) { // only useful with star
                    JCClass[] classes = pkg.getClasses();
                    for (int i = 0; i < classes.length; i++) {
                        name2Class.put(classes[i].getName(), classes[i]);
                    }
                }
            } else { // not package, will be class
                String pkgName = importExp;
                String simplePkgName = null;
                int ind;
                while((ind = pkgName.lastIndexOf('.')) >= 0) {
                    pkgName = pkgName.substring(0, ind);
                    if (simplePkgName == null) {
                        simplePkgName = pkgName;
                    }
                    pkg = finder.getExactPackage(pkgName);
                    if (pkg != null) { // found valid package, but unknown class
                        cls = JCompletion.getSimpleClass(importExp, pkgName.length());
                        info.cls = cls;
                        if (star) {
                            // don't add in this case, can change in the future
                        } else {
                            name2Class.put(cls.getName(), cls);
                        }
                        break;
                    }
                }

                if (cls == null) {
                    // didn't found a direct package, assume last is class name
                    if (simplePkgName != null) { // at least one dot in importExp
                        cls = JCompletion.getSimpleClass(importExp, simplePkgName.length());
                        if (star) {
                            // don't add in this case, can change in the future
                        } else {
                            name2Class.put(cls.getName(), cls);
                        }
                    }
                }
            }
        }
        infoList.add(info);
    }

    public boolean token(int tokenID, int helperID, int offset, int tokenLen) {
        boolean cont = true;
        switch (tokenID) {
        case JavaSyntax.IDENTIFIER:
            switch (state) {
            case AFTER_IMPORT:
                expPos = offset;
                state = INSIDE_EXP;
                break;

            case INSIDE_MIXED_EXP:
                exp.append(buffer, offset, tokenLen);
                // let it flow to INSIDE_EXP
            case INSIDE_EXP:
                if (star) { // not allowed after star was found
                    cont = false;
                }
                break;
            }
            break;

        case JavaSyntax.OPERATOR:
            switch (helperID) {
            case JavaSyntax.DOT:
                switch (state) {
                case INIT: // ignore standalone dot
                    break;

                case AFTER_IMPORT:
                    cont = false; // dot after import keyword
                    break;

                case INSIDE_MIXED_EXP:
                    exp.append('.');
                    // let it flow to INSIDE_EXP
                case INSIDE_EXP:
                    if (star) { // not allowed after star was found
                        cont = false;
                    }
                    break;
                }
                break;

            case JavaSyntax.SEMICOLON:
                String impExp = null;
                switch (state) {
                case INIT: // ignore semicolon
                    break;

                case AFTER_IMPORT: // semicolon after import kwd
                    cont = false;
                    break;

                case INSIDE_EXP:
                    impExp = new String(buffer, expPos,
                                        (star ? (offset - 2) : offset) - expPos);
                    break;

                case INSIDE_MIXED_EXP:
                    impExp = exp.toString();
                    exp.setLength(0);
                    break;
                }

                if (impExp != null) {
                    if (parsingPackage) {
                        packageStatementFound(startPos, offset + 1, impExp);
                    } else { // parsing import statement
                        importStatementFound(startPos, offset + 1, impExp, star);
                    }
                    star = false;
                    parsingPackage = false;
                    state = INIT;
                }
                break;

            case JavaSyntax.MUL:
                if (star || parsingPackage) {
                    cont = false;
                } else {
                    switch (state) {
                    case INIT: // ignore star at the begining
                        break;

                    case AFTER_IMPORT:
                        cont = false; // star after import kwd
                        break;

                    case INSIDE_EXP:
                        star = true;
                        if (offset == 0 || buffer[offset - 1] != '.') {
                            cont = false;
                        }
                        break;

                    case INSIDE_MIXED_EXP:
                        int len = exp.length();
                        if (len > 0 && exp.charAt(len - 1) == '.') {
                            exp.setLength(len - 1); // remove ending dot
                            star = true;
                        } else { // error
                            cont = false;
                        }
                        break;
                    }
                }
                break;

            default:
                cont = false;
                break;
            }
            break;

        case JavaSyntax.KEYWORD: // KEYWORD found
            switch (helperID) {
            case JavaKeywords.PACKAGE:
                switch (state) {
                case INIT:
                    parsingPackage = true;
                    state = AFTER_IMPORT; // the same state is used
                    break;

                default:
                    cont = false; // error in other states
                    break;
                }
                break;

            case JavaKeywords.IMPORT: // IMPORT keyword
                switch (state) {
                case INIT:
                    parsingPackage = false;
                    state = AFTER_IMPORT;
                    startPos = offset;
                    break;

                default:
                    cont = false; // error in other states
                    break;
                }
                break;

            default:
                cont = false;
            }
            break;

        case JavaSyntax.TEXT:
        case JavaSyntax.EOL:
        case JavaSyntax.LINE_COMMENT:
        case JavaSyntax.BLOCK_COMMENT:
            switch (state) {
            case INSIDE_EXP:
                // Need to continue as string
                exp.append(buffer, expPos, offset - expPos);
                state = INSIDE_MIXED_EXP;
                break;
            }
            break;

        default:
            cont = false;
        }

        return cont;
    }

    private String debugState(int state) {
        switch (state) {
        case INIT:
            return "INIT"; // NOI18N
        case AFTER_IMPORT:
            return "AFTER_IMPORT"; // NOI18N
        case INSIDE_EXP:
            return "INSIDE_EXP"; // NOI18N
        case INSIDE_MIXED_EXP:
            return "INSIDE_MIXED_EXP"; // NOI18N
        }
        return "UNKNOWN STATE"; // NOI18N
    }

    public int eot(int offset) {
        eotReached = true; // will be rescanned
        return 0;
    }

    public void nextBuffer(char[] buffer, int offset, int len,
                           int startPos, int preScan, boolean lastBuffer) {
        this.buffer = buffer;
    }

    class Info {

        Info(int startPos, int endPos, boolean star) {
            this.startPos = startPos;
            this.endPos = endPos;
            this.star = star;
        }

        int startPos;

        int endPos;

        boolean star;

        JCPackage pkg;

        JCClass cls;

    }

}

/*
 * Log
 *  11   Gandalf-post-FCS1.9.1.0     3/8/00   Miloslav Metelka 
 *  10   Gandalf   1.9         1/13/00  Miloslav Metelka Localization
 *  9    Gandalf   1.8         1/10/00  Miloslav Metelka 
 *  8    Gandalf   1.7         11/14/99 Miloslav Metelka 
 *  7    Gandalf   1.6         11/8/99  Miloslav Metelka 
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         10/10/99 Miloslav Metelka 
 *  4    Gandalf   1.3         9/30/99  Miloslav Metelka 
 *  3    Gandalf   1.2         9/15/99  Miloslav Metelka 
 *  2    Gandalf   1.1         9/10/99  Miloslav Metelka 
 *  1    Gandalf   1.0         8/27/99  Miloslav Metelka 
 * $
 */

