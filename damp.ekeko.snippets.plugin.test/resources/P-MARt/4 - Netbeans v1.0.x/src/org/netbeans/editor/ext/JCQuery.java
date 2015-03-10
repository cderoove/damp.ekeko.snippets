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

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import org.netbeans.editor.Analyzer;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.FinderFactory;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.GuardedException;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.Utilities;

/**
* Java completion support finder
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JCQuery {

    public JCQuery() {
    }

    public QueryResult getHelp(JTextComponent c) {
        return getHelp(c, c.getCaret().getDot(), false);
    }

    /** Get the help according to the given position in the document.
    * @param c text component
    * @param pos position in the component's document. The context before this position
    *   is inspected to find the help info.
    * @param sourceHelp find the help that can be used for opening the source code document.
    *   The source help is resolved in a slightly different way in some situations.
    *   e.g. open-source-action etc.
    */
    public QueryResult getHelp(JTextComponent c, int pos, boolean sourceHelp) {
        BaseDocument doc = (BaseDocument)c.getDocument();
        QueryResult ret = null;
        try {
            JCTokenProcessor tp = parseToPosition(doc, pos);
            switch (tp.getLastTokenID()) {
            case JavaSyntax.BLOCK_COMMENT:
            case JavaSyntax.LINE_COMMENT:
                break;

            default:
                ret = resolveQuery(tp, doc, sourceHelp);
                break;
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /** Get the text that is normally filled into the text if enter is pressed. */
    protected String getMainText(QueryResult queryResult, Object dataItem) {
        String text = null;
        if (dataItem instanceof JCPackage) {
            text = ((JCPackage)dataItem).getLastName();
        } else if (dataItem instanceof JCClass) {
            text = ((JCClass)dataItem).getName();
            int cdo = queryResult.getClassDisplayOffset();
            if (cdo > 0 && cdo < text.length()) { // Only the last name for inner classes
                text = text.substring(cdo);
            }
        } else if (dataItem instanceof JCField) {
            text = ((JCField)dataItem).getName();
        } else if (dataItem instanceof JCMethod) {
            JCMethod mtd = (JCMethod)dataItem;
            text = mtd.getName();
        } else if (dataItem instanceof JCConstructor) {
            text = ((JCConstructor)dataItem).getClazz().getName();
        }
        return text;
    }

    /** Get the text that is common to all the entries in the query-result */
    String getCommonText(QueryResult queryResult, String prefix) {
        List data = queryResult.getData();
        int cnt = data.size();
        int prefixLen = prefix.length();
        String commonText = null;
        for (int i = 0; i < cnt; i++) {
            String mainText = getMainText(queryResult, data.get(i));
            if (mainText != null && mainText.startsWith(prefix)) {
                mainText = mainText.substring(prefixLen);
                if (commonText == null) {
                    commonText = mainText;
                }
                // Get largest common part
                int minLen = Math.min(mainText.length(), commonText.length());
                int commonInd;
                for (commonInd = 0; commonInd < minLen; commonInd++) {
                    if (mainText.charAt(commonInd) != commonText.charAt(commonInd)) {
                        break;
                    }
                }
                if (commonInd != 0) {
                    commonText = commonText.substring(0, commonInd);
                } else {
                    return null; // no common text
                }
            }
        }
        return prefix + ((commonText != null) ? commonText : ""); // NOI18N
    }

    /** Update the text in response to pressing TAB key.
    * @return whether the text was successfully updated
    */
    public boolean updateCommonText(JTextComponent c, QueryResult queryResult) {
        if (queryResult == null) {
            return false;
        }

        BaseDocument doc = (BaseDocument)c.getDocument();
        int startPos = queryResult.getSubstitutePosition();
        int substLen = queryResult.getSubstituteLength();
        try {
            String prefix = doc.getText(startPos, substLen);
            String commonText = getCommonText(queryResult, prefix);
            if (commonText != null) {
                doc.atomicLock();
                try {
                    doc.remove(startPos, substLen);
                    doc.insertString(startPos, commonText, null);
                } finally {
                    doc.atomicUnlock();
                }
            }
        } catch (BadLocationException e) {
            // no updating
        }
        return true;
    }

    /** Update the text in response to pressing ENTER.
    * @return whether the text was successfully updated
    */
    protected boolean updateText(JTextComponent c, List data, Object replacement,
                                 QueryResult queryResult) {
        if (queryResult == null) { // must be valid
            return false;
        }

        BaseDocument doc = (BaseDocument)c.getDocument();
        String text = null;
        int selectionStartOffset = -1;
        int selectionEndOffset = -1;

        if (replacement instanceof JCPackage) {
            text = ((JCPackage)replacement).getLastName();

        } else if (replacement instanceof JCClass) {
            text = ((JCClass)replacement).getName();
            int cdo = queryResult.getClassDisplayOffset();
            if (cdo > 0 && cdo < text.length()) { // Only the last name for inner classes
                text = text.substring(cdo);
            }

        } else if (replacement instanceof JCField) {
            text = ((JCField)replacement).getName();

        } else if (replacement instanceof JCConstructor) {
            JCConstructor mtd = (JCConstructor)replacement;
            JCExpression substExp = queryResult.getSubstituteExp();
            switch ((substExp != null) ? substExp.getID() : -1) {
            case JCExpression.METHOD:
                // no substitution
                break;

            case JCExpression.METHOD_OPEN:
                JCParameter[] parms = mtd.getParameters();
                if (parms.length == 0) {
                    text = ")"; // NOI18N
                } else { // one or more parameters
                    int ind = substExp.getParameterCount();
                    boolean addSpace = SettingsUtil.getBoolean(Utilities.getKit(c).getClass(),
                                       ExtSettings.FORMAT_COMMA_ADD_SPACE, true);
                    int sPos = queryResult.getSubstitutePosition();
                    try {
                        if (addSpace && (ind == 0 || (sPos > 0
                                                      && Character.isWhitespace(doc.getText(sPos - 1, 1).charAt(0))))
                           ) {
                            addSpace = false;
                        }
                    } catch (BadLocationException e) {
                    }

                    if (ind < parms.length) {
                        text = addSpace ? " " : ""; // NOI18N
                        selectionStartOffset = text.length();
                        text += parms[ind].getName();
                        selectionEndOffset = text.length();
                    }
                }
                break;

            default:
                text = getMainText(queryResult, replacement);
                boolean addSpace = SettingsUtil.getBoolean(Utilities.getKit(c).getClass(),
                                   ExtSettings.FORMAT_PARENTHESIS_ADD_SPACE, false);
                if (addSpace) {
                    text += ' ';
                }
                text += '(';

                parms = mtd.getParameters();
                if (parms.length > 0) {
                    selectionStartOffset = text.length();
                    text += parms[0].getName();
                    selectionEndOffset = text.length();
                } else {
                    text += ")"; // NOI18N
                }
                break;
            }

        } else if (replacement instanceof JCConstructor) {
            text = ((JCConstructor)replacement).getClazz().getName();
        }

        if (text != null) {
            // Update the text
            doc.atomicLock();
            try {
                int startPos = queryResult.getSubstitutePosition();
                doc.remove(startPos, queryResult.getSubstituteLength());
                doc.insertString(startPos, text, null);
                if (selectionStartOffset >= 0) {
                    c.select(startPos + selectionStartOffset, startPos + selectionEndOffset);
                }
            } catch (BadLocationException e) {
                // Can't update
            } finally {
                doc.atomicUnlock();
            }
        }

        return true;
    }

    /** Get the parsed expression that is at the top of the stack */
    JCTokenProcessor parseToPosition(BaseDocument doc, int pos)
    throws BadLocationException {
        JavaSyntaxSupport sup = (JavaSyntaxSupport)doc.getSyntaxSupport();
        int lastSep = sup.getLastCommandSeparator(pos);
        JCTokenProcessor tp = new JCTokenProcessor();
        sup.tokenizeText(tp, lastSep + 1, pos, true);
        return tp;
    }


    /** Finds the fields, methods and the inner classes.
    */
    static List findFieldsAndMethods(JCFinder finder, JCClass cls, String name,
                                     boolean exactMatch, boolean staticOnly, boolean inspectOuterClasses) {
        // Find inner classes
        List ret = new ArrayList();
        if (staticOnly) {
            JCPackage pkg = finder.getExactPackage(cls.getPackageName());
            if (pkg != null) {
                ret = finder.findClasses(pkg, cls.getName() + '.' + name, false);
            }
        }

        // Add fields
        ret.addAll(finder.findFields(cls, name, exactMatch, staticOnly, inspectOuterClasses));
        // Add methods
        ret.addAll(finder.findMethods(cls, name, exactMatch, staticOnly, inspectOuterClasses));
        return ret;
    }

    protected QueryResult resolveQuery(JCTokenProcessor tp, BaseDocument doc, boolean sourceHelp) {
        JavaSyntaxSupport sup = (JavaSyntaxSupport)doc.getSyntaxSupport();
        sup.refreshCompletion();
        Context ctx = new Context(tp, sup, sourceHelp);
        JCExpression exp = tp.getResultExp();
        ctx.resolveExp(exp);
        return ctx.queryResult;
    }

    public static class Context {

        /** Token processor used for parsing the input */
        private JCTokenProcessor tp;

        /** Syntax support for the given document */
        private JavaSyntaxSupport sup;

        /** Whether get the source help or not. The source help has slightly
        * different handling in some situations.
        */
        private boolean sourceHelp;

        /** If set to true true - find the type of the result expression.
        * It's stored in the lastType variable or lastPkg if it's a package.
        * The result variable is not populated.
        * False means that the code completion output should be collected.
        */
        private boolean findType;

        /** Whether currently scanning either the package or the class name
        * so the results should limit the search to the static fields and methods.
        */
        private boolean staticOnly = true;

        /** Last package found when scanning dot expression */
        private JCPackage lastPkg;

        /** Last type found when scanning dot expression */
        private JCType lastType;

        /** Result list when code completion output is generated */
        private QueryResult queryResult;

        /** Helper flag for recognizing constructors */
        private boolean isConstructor;

        public Context(JCTokenProcessor tp, JavaSyntaxSupport sup, boolean sourceHelp) {
            this.tp = tp;
            this.sup = sup;
            this.sourceHelp = sourceHelp;
        }

        public void setFindType(boolean findType) {
            this.findType = findType;
        }

        protected Object clone() {
            return new Context(tp, sup, sourceHelp);
        }

        /*    private List getBaseHelp(String baseName) {
              if (sourceHelp) {
                JCFinder finder = JCompletion.getFinder();
                List res = finder.findPackages(baseName, false, false); // find all subpackages
                if (res == null) {
                  res = new ArrayList();
                }

                if (baseName != null && baseName.length() > 0) {
                  res.addAll(finder.findClasses(null, baseName, false)); // add matching classes
                }
                return res;
              }
              return null;
            }

            private QueryResult getBaseHelpResult(String baseName, JCExpression exp) {
              List res = getBaseHelp(baseName);
              if (res != null && exp != null) {
                return new QueryResult(res, formatName(baseName, true),
                    exp, exp.getTokenPosition(0), 0, 0);
              }
              return null;
            }
        */

        private String formatName(String name, boolean appendStar) {
            return (name != null) ? (appendStar ? (name + '*') : name)
       : (appendStar ? "*" : ""); // NOI18N

        }

        private String formatType(JCType type, boolean useFullName,
                                  boolean appendDot, boolean appendStar) {
            StringBuffer sb = new StringBuffer();
            if (type != null) {
                sb.append(type.format(useFullName));
            }
            if (appendDot) {
                sb.append('.');
            }
            if (appendStar) {
                sb.append('*');
            }
            return sb.toString();
        }

        private JCType resolveType(JCExpression exp) {
            Context ctx = (Context)clone();
            ctx.setFindType(true);
            JCType typ = null;
            if (ctx.resolveExp(exp)) {
                typ = ctx.lastType;
            }
            return typ;
        }

        boolean resolveExp(JCExpression exp) {
            boolean lastDot = false; // dot at the end of the whole expression?
            JCFinder finder = JCompletion.getFinder();
            boolean ok = true;

            switch (exp.getID()) {
            case JCExpression.DOT_OPEN: // Dot expression with the dot at the end
                lastDot = true;
                // let it flow to DOT
            case JCExpression.DOT: // Dot expression
                int parmCnt = exp.getParameterCount(); // Number of items in the dot exp

                for (int i = 0; i < parmCnt && ok; i++) { // resolve all items in a dot exp
                    ok = resolveItem(exp.getParameter(i), (i == 0),
                                     (!lastDot && i == parmCnt - 1)
                                    );
                }

                if (ok && lastDot) { // Found either type or package help
                    // Need to process dot at the end of the expression
                    int tokenCntM1 = exp.getTokenCount() - 1;
                    int substPos = exp.getTokenPosition(tokenCntM1) + exp.getTokenLength(tokenCntM1);
                    if (lastType != null) { // Found type
                        JCClass cls;
                        if (lastType.getArrayDepth() == 0) { // Not array
                            cls = lastType.getClazz();
                        } else { // Array of some depth
                            cls = JCompletion.OBJECT_CLASS; // Use Object in this case
                        }
                        List res;
                        if (sourceHelp) {
                            res = new ArrayList();
                            res.add(lastType.getClazz());
                        } else { // not source-help
                            res = findFieldsAndMethods(finder, cls, "", false, staticOnly, false); // NOI18N
                        }
                        // Get all fields and methods of the cls
                        queryResult = new QueryResult(res, formatType(lastType, true, true, true),
                                                      exp, substPos, 0, cls.getName().length() + 1);
                    } else { // Found package (otherwise ok would be false)
                        String searchPkg = lastPkg.getName() + '.';
                        List res;
                        if (sourceHelp) {
                            res = new ArrayList();
                            res.add(lastPkg); // return only the package
                        } else {
                            res = finder.findPackages(searchPkg, false, false); // find all subpackages
                            res.addAll(Arrays.asList(lastPkg.getClasses())); // package classes
                        }
                        queryResult = new QueryResult(res, searchPkg + '*',
                                                      exp, substPos, 0, 0);
                    }
                }
                break;

            case JCExpression.NEW: // 'new' keyword
                List res = finder.findClasses(null, "", false); // Find all classes by name // NOI18N
                queryResult = new QueryResult(res, "*", exp, tp.getLastPosition(), 0, 0); // NOI18N
                break;

            default: // The rest of the situations is resolved as a singleton item
                ok = resolveItem(exp, true, true);
                break;
            }

            return ok;
        }

        /** Resolve one item from the expression connected by dots.
        * @param item expression item to resolve
        * @param first whether this expression is the first one in a dot expression
        * @param last whether this expression is the last one in a dot expression
        */
        boolean resolveItem(JCExpression item, boolean first, boolean last) {
            boolean cont = true; // whether parsing should continue or not
            boolean methodOpen = false; // helper flag for unclosed methods
            JCFinder finder = JCompletion.getFinder();

            switch (item.getID()) {
            case JCExpression.CONSTANT: // Constant item
                if (first) {
                    lastType = item.getType(); // Get the constant type
                } else { // Not the first item in a dot exp
                    cont = false; // impossible to have constant inside the expression
                }
                break;

            case JCExpression.VARIABLE: // Variable or special keywords
                if (item.getTokenID(0) == JavaSyntax.KEYWORD) { // special keywords
                    switch (item.getTokenHelperID(0)) {
                    case JavaKeywords.THIS: // 'this' keyword
                        if (first) { // first item in expression
                            JCClass cls = sup.getClass(item.getTokenPosition(0));
                            if (cls != null) {
                                lastType = JCompletion.getType(cls, 0);
                                staticOnly = false;
                            }
                        } else { // 'something.this'
                            staticOnly = false;
                        }
                        break;

                    case JavaKeywords.SUPER: // 'super' keyword
                        if (first) { // only allowed as the first item
                            JCClass cls = sup.getClass(item.getTokenPosition(0));
                            if (cls != null) {
                                cls = finder.getExactClass(cls.getFullName());
                                if (cls != null) {
                                    cls = cls.getSuperclass();
                                    if (cls != null) {
                                        lastType = JCompletion.getType(cls, 0);
                                        staticOnly = false;
                                    }
                                }
                            }
                        } else {
                            cont = false;
                        }
                        break;

                    case JavaKeywords.CLASS: // 'class' keyword
                        if (!first) {
                            lastType = JCompletion.CLASS_TYPE;
                        } else {
                            cont = false;
                        }
                        break;
                    }
                } else { // regular constant
                    String var = item.getTokenText(0);
                    int varPos = item.getTokenPosition(0);
                    if (first) { // try to find variable for the first item
                        if (last && !findType) { // both first and last item
                            List res = new ArrayList();
                            JCClass cls = sup.getClass(varPos); // get document class
                            if (cls != null) {
                                res.addAll(findFieldsAndMethods(finder, cls, var, false,
                                                                sup.isStaticBlock(varPos), true));
                            }
                            if (var.length() > 0 || !sourceHelp) {
                                res.addAll(finder.findPackages(var, false, false)); // add matching packages
                                if (var.length() > 0) { // if at least one char
                                    res.addAll(finder.findClasses(null, var, false)); // add matching classes
                                }
                            }
                            queryResult = new QueryResult(res, var + '*', item, 0);
                        } else { // not last item or finding type
                            lastType = (JCType)sup.findType(var, varPos);
                            if (lastType != null) { // variable found
                                staticOnly = false;
                            } else { // no variable found
                                lastPkg = finder.getExactPackage(var); // try package
                                if (lastPkg == null) { // not package, let's try class name
                                    JCClass cls = sup.getClassFromName(var, true);
                                    if (cls != null) {
                                        lastType = JCompletion.getType(cls, 0);
                                    } else { // class not found
                                        cont = false;
                                    }
                                }
                            }
                        }
                    } else { // not the first item
                        if (lastType != null) { // last was type
                            if (findType || !last) {
                                boolean inner = false;
                                int ad = lastType.getArrayDepth();
                                if (staticOnly && ad == 0) { // can be inner class
                                    JCClass cls = finder.getExactClass(lastType.getClazz().getFullName() + "." + var); // NOI18N
                                    if (cls != null) {
                                        lastType = JCompletion.getType(cls, 0);
                                        inner = true;
                                    }
                                }

                                if (!inner) { // not inner class name
                                    if (ad == 0) { // zero array depth
                                        List fldList = finder.findFields(lastType.getClazz(), var, true, staticOnly, false);
                                        if (fldList.size() > 0) { // match found
                                            JCField fld = (JCField)fldList.get(0);
                                            lastType = fld.getType();
                                            staticOnly = false;
                                        } else { // no match found
                                            lastType = null;
                                            cont = false;
                                        }
                                    } else { // array depth > 0 but no array dereference
                                        cont = false;
                                    }
                                }
                            } else { // last and searching for completion output
                                JCClass cls = lastType.getClazz();
                                queryResult = new QueryResult(
                                                  findFieldsAndMethods(finder, cls, var, false, staticOnly, false),
                                                  lastType.format(false) + '.' + var + '*',
                                                  item,
                                                  cls.getName().length() + 1);
                            }
                        } else { // currently package
                            String searchName = lastPkg.getName() + '.' + var;
                            if (findType || !last) {
                                lastPkg = finder.getExactPackage(searchName);
                                if (lastPkg == null) { // package doesn't exist
                                    JCClass cls = finder.getExactClass(searchName);
                                    if (cls != null) {
                                        lastType = JCompletion.getType(cls, 0);
                                    } else {
                                        lastType = null;
                                        cont = false;
                                    }
                                }
                            } else { // last and searching for completion output
                                if (last) { // get all matching fields/methods/packages
                                    String searchPkg = lastPkg.getName() + '.' + var;
                                    List res = finder.findPackages(searchPkg, false, false); // find matching subpackages
                                    res.addAll(finder.findClasses(lastPkg, var, false)); // matching classes
                                    queryResult = new QueryResult(res, searchPkg + '*', item, 0);
                                }
                            }
                        }
                    }
                }
                break;

            case JCExpression.ARRAY:
                cont = resolveItem(item.getParameter(0), first, false);
                if (cont) {
                    cont = false;
                    if (lastType != null) { // must be type
                        if (item.getParameterCount() == 2) { // index in array follows
                            JCType arrayType = resolveType(item.getParameter(1));
                            if (arrayType != null && arrayType.equals(JCompletion.INT_TYPE)) {
                                lastType = JCompletion.getType(lastType.getClazz(),
                                                               Math.max(lastType.getArrayDepth() - 1, 0));
                                cont = true;
                            }
                        } else { // no index, increase array depth
                            lastType = JCompletion.getType(lastType.getClazz(),
                                                           lastType.getArrayDepth() + 1);
                            cont = true;
                        }
                    }
                }
                break;

            case JCExpression.INSTANCEOF:
                lastType = JCompletion.BOOLEAN_TYPE;
                break;

            case JCExpression.OPERATOR:
                switch (item.getTokenHelperID(0)) {
                case JavaSyntax.EQ: // Assignment operators
                case JavaSyntax.PLUS_EQ:
                case JavaSyntax.MINUS_EQ:
                case JavaSyntax.MUL_EQ:
                case JavaSyntax.DIV_EQ:
                case JavaSyntax.AND_EQ:
                case JavaSyntax.OR_EQ:
                case JavaSyntax.XOR_EQ:
                case JavaSyntax.MOD_EQ:
                case JavaSyntax.LLE:
                case JavaSyntax.GGE:
                case JavaSyntax.GGGE:
                    if (item.getParameterCount() > 0) {
                        lastType = resolveType(item.getParameter(0));
                    }
                    break;

                case JavaSyntax.LT: // Binary, result is boolean
                case JavaSyntax.GT:
                case JavaSyntax.LE:
                case JavaSyntax.GE:
                case JavaSyntax.EQ_EQ:
                case JavaSyntax.NOT_EQ:
                case JavaSyntax.AND_AND: // Binary, result is boolean
                case JavaSyntax.OR_OR:
                    lastType = JCompletion.BOOLEAN_TYPE;
                    break;

                case JavaSyntax.LLT: // Always binary
                case JavaSyntax.GGT:
                case JavaSyntax.GGGT:
                case JavaSyntax.MUL:
                case JavaSyntax.DIV:
                case JavaSyntax.AND:
                case JavaSyntax.OR:
                case JavaSyntax.XOR:
                case JavaSyntax.MOD:

                case JavaSyntax.PLUS:
                case JavaSyntax.MINUS:
                    switch (item.getParameterCount()) {
                    case 2:
                        JCType typ1 = resolveType(item.getParameter(0));
                        JCType typ2 = resolveType(item.getParameter(1));
                        if (typ1 != null && typ2 != null
                                && typ1.getArrayDepth() == 0
                                && typ2.getArrayDepth() == 0
                                && JCompletion.isPrimitiveClass(typ1.getClazz())
                                && JCompletion.isPrimitiveClass(typ2.getClazz())
                           ) {
                            lastType = JCUtilities.getCommonType(typ1, typ2);
                        }
                        break;
                    case 1: // get the only one parameter
                        JCType typ = resolveType(item.getParameter(0));
                        if (typ != null && JCompletion.isPrimitiveClass(typ.getClazz())) {
                            lastType = typ;
                        }
                        break;
                    }
                    break;

                case JavaSyntax.COLON:
                    switch (item.getParameterCount()) {
                    case 2:
                        JCType typ1 = resolveType(item.getParameter(0));
                        JCType typ2 = resolveType(item.getParameter(1));
                        if (typ1 != null && typ2 != null) {
                            lastType = JCUtilities.getCommonType(typ1, typ2);
                        }
                        break;

                    case 1:
                        lastType = resolveType(item.getParameter(0));
                        break;
                    }
                    break;

                case JavaSyntax.QUESTION:
                    if (item.getParameterCount() >= 2) {
                        lastType = resolveType(item.getParameter(1)); // should be colon
                    }
                    break;
                }
                break;

            case JCExpression.UNARY_OPERATOR:
                if (item.getParameterCount() > 0) {
                    lastType = resolveType(item.getParameter(0));
                }
                break;

            case JCExpression.CONVERSION:
                lastType = resolveType(item.getParameter(0));
                staticOnly = false;
                break;

            case JCExpression.PARENTHESIS:
                cont = resolveItem(item.getParameter(0), first, last);
                break;

            case JCExpression.CONSTRUCTOR: // constructor can be part of a DOT expression
                isConstructor = true;
                cont = resolveExp(item.getParameter(0));
                staticOnly = false;
                break;

            case JCExpression.METHOD_OPEN: // Unclosed method
                methodOpen = true;
                // let it flow to method
            case JCExpression.METHOD: // Closed method
                String mtdName = item.getTokenText(0);
                if (isConstructor) { // Help for the constructor
                    JCClass cls = null;
                    if (first) {
                        cls = sup.getClassFromName(mtdName, true);
                    } else { // not first
                        if (lastPkg != null) { // valid package
                            cls = JCUtilities.getExactClass(finder, mtdName, lastPkg.getName());
                        } else if (lastType != null) { // valid type -> will be inner clas
                            cls = JCUtilities.getExactClass(finder, mtdName,
                                                            lastType.getClazz().getFullName());
                        }
                    }
                    if (cls != null) {
                        lastType = JCompletion.getType(cls, 0);
                        List ctrList = JCUtilities.getConstructors(cls);
                        String parmStr = "*"; // NOI18N
                        List typeList = getTypeList(item);
                        List filtered = JCUtilities.filterMethods(ctrList, typeList, methodOpen);
                        if (filtered.size() > 0) {
                            ctrList = filtered;
                            parmStr = formatTypeList(typeList, methodOpen);
                        }
                        queryResult = new QueryResult(ctrList,
                                                      mtdName + '(' + parmStr + ')',
                                                      item, tp.getLastPosition(), 0, 0);
                    }
                } else {
                    // Help for the method
                    if (first) {
                        JCClass cls = sup.getClass(item.getTokenPosition(0));
                        if (cls != null) {
                            lastType = JCompletion.getType(cls, 0);
                        }
                    }

                    if (lastType != null) {
                        List mtdList = finder.findMethods(lastType.getClazz(), mtdName, true, false, first);
                        String parmStr = "*"; // NOI18N
                        List typeList = getTypeList(item);
                        List filtered = JCUtilities.filterMethods(mtdList, typeList, methodOpen);
                        if (filtered.size() > 0) {
                            mtdList = filtered;
                            parmStr = formatTypeList(typeList, methodOpen);
                        }
                        if (mtdList.size() > 0) {
                            if (last && !findType) {
                                queryResult = new QueryResult(mtdList,
                                                              lastType.getClazz().toString() + '.' + mtdName + '(' + parmStr + ')',
                                                              item, tp.getLastPosition(), 0, 0);
                            } else {
                                if (mtdList.size() > 0) {
                                    lastType = ((JCMethod)mtdList.get(0)).getReturnType();
                                    staticOnly = false;
                                }
                            }
                        } else {
                            lastType = null; // no method found
                            cont = false;
                        }
                    } else { // package.method() is invalid
                        lastPkg = null;
                        cont = false;
                    }
                }
                break;
            }

            if (lastType == null && lastPkg == null) { // !!! shouldn't be necessary
                cont = false;
            }

            return cont;
        }

        private List getTypeList(JCExpression item) {
            int parmCnt = item.getParameterCount();
            ArrayList typeList = new ArrayList();
            if (parmCnt > 0) { // will try to filter by parameters
                boolean methodOpen = (item.getID() == JCExpression.METHOD_OPEN);
                for (int i = 0; i < parmCnt; i++) {
                    JCExpression parm = item.getParameter(i);
                    JCType typ = resolveType(parm);
                    typeList.add(typ);
                }
            }
            return typeList;
        }

    }

    private static String formatTypeList(List typeList, boolean methodOpen) {
        StringBuffer sb = new StringBuffer();
        if (typeList.size() > 0) {
            int cntM1 = typeList.size() - 1;
            for (int i = 0; i <= cntM1; i++) {
                JCType t = (JCType)typeList.get(i);
                if (t != null) {
                    sb.append(t.format(false));
                } else {
                    sb.append('?');
                }
                if (i < cntM1) {
                    sb.append(", "); // NOI18N
                }
            }
            if (methodOpen) {
                sb.append(", *"); // NOI18N
            }
        } else { // no parameters
            if (methodOpen) {
                sb.append("*"); // NOI18N
            }
        }
        return sb.toString();
    }

    public static class QueryResult {

        private List data;

        private String title;

        /** First offset in the name of the (inner) class
        * to be displayed. It's used to display the inner classes
        * of the main class to exclude the initial part of the name.
        */
        private int classDisplayOffset;

        /** Expression to substitute */
        private JCExpression substituteExp;

        /** Starting position of the text to substitute */
        private int substitutePosition;

        /** Length of the text to substitute */
        private int substituteLength;

        QueryResult(List data, String title, JCExpression substituteExp, int classDisplayOffset) {
            this(data, title, substituteExp, substituteExp.getTokenPosition(0),
                 substituteExp.getTokenLength(0), classDisplayOffset);
        }

        QueryResult(List data, String title, JCExpression substituteExp, int substitutePosition,
                    int substituteLength, int classDisplayOffset) {
            this.data = data;
            this.title = title;
            this.substituteExp = substituteExp;
            this.substitutePosition = substitutePosition;
            this.substituteLength = substituteLength;
            this.classDisplayOffset = classDisplayOffset;
        }

        public final List getData() {
            return data;
        }

        public final String getTitle() {
            return title;
        }

        public final int getClassDisplayOffset() {
            return classDisplayOffset;
        }

        public final int getSubstitutePosition() {
            return substitutePosition;
        }

        public final int getSubstituteLength() {
            return substituteLength;
        }

        public final JCExpression getSubstituteExp() {
            return substituteExp;
        }

    }

}

/*
 * Log
 *  30   Gandalf-post-FCS1.28.1.0    3/8/00   Miloslav Metelka 
 *  29   Gandalf   1.28        2/15/00  Miloslav Metelka parenthesis instead of 
 *       curly braces
 *  28   Gandalf   1.27        1/15/00  Miloslav Metelka #5270
 *  27   Gandalf   1.26        1/13/00  Miloslav Metelka Localization
 *  26   Gandalf   1.25        1/11/00  Miloslav Metelka 
 *  25   Gandalf   1.24        1/10/00  Miloslav Metelka 
 *  24   Gandalf   1.23        1/4/00   Miloslav Metelka 
 *  23   Gandalf   1.22        12/28/99 Miloslav Metelka 
 *  22   Gandalf   1.21        11/24/99 Miloslav Metelka 
 *  21   Gandalf   1.20        11/14/99 Miloslav Metelka 
 *  20   Gandalf   1.19        11/10/99 Miloslav Metelka 
 *  19   Gandalf   1.18        11/9/99  Miloslav Metelka 
 *  18   Gandalf   1.17        11/8/99  Miloslav Metelka 
 *  17   Gandalf   1.16        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  16   Gandalf   1.15        10/10/99 Miloslav Metelka 
 *  15   Gandalf   1.14        10/4/99  Miloslav Metelka 
 *  14   Gandalf   1.13        9/30/99  Miloslav Metelka 
 *  13   Gandalf   1.12        9/15/99  Miloslav Metelka 
 *  12   Gandalf   1.11        9/10/99  Miloslav Metelka 
 *  11   Gandalf   1.10        8/27/99  Miloslav Metelka 
 *  10   Gandalf   1.9         8/18/99  Miloslav Metelka 
 *  9    Gandalf   1.8         8/18/99  Miloslav Metelka 
 *  8    Gandalf   1.7         7/30/99  Miloslav Metelka 
 *  7    Gandalf   1.6         7/29/99  Miloslav Metelka 
 *  6    Gandalf   1.5         7/22/99  Miloslav Metelka 
 *  5    Gandalf   1.4         7/21/99  Miloslav Metelka 
 *  4    Gandalf   1.3         7/21/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/20/99  Miloslav Metelka 
 *  2    Gandalf   1.1         6/10/99  Miloslav Metelka 
 *  1    Gandalf   1.0         6/8/99   Miloslav Metelka 
 * $
 */



