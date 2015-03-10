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

import java.util.Map;
import java.util.HashMap;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.TokenProcessor;

/**
* Support methods for syntax analyzes
*
* @author Miloslav Metelka
* @version 1.00
*/

public class ExtSyntaxSupport extends SyntaxSupport {

    /** Listens for the changes on the document. Children can override
    * the documentModified() method to perform some processing.
    */
    private DocumentListener docL;

    /** Map holding the [position, local-variable-map] pairs 
     * @associates Map*/
    private HashMap localVarMaps = new HashMap();

    /** Map holding the [position, global-variable-map] pairs 
     * @associates Map*/
    private HashMap globalVarMaps = new HashMap();

    public ExtSyntaxSupport(BaseDocument doc) {
        super(doc);

        // Create listener to listen on document changes
        docL = new DocumentListener() {
                   public void insertUpdate(DocumentEvent evt) {
                       documentModified(evt);
                   }

                   public void removeUpdate(DocumentEvent evt) {
                       documentModified(evt);
                   }

                   public void changedUpdate(DocumentEvent evt) {
                   }
               };
        doc.addDocumentListener(docL);
    }

    /** Called when the document was modified by either the insert or removal.
    * @param evt event received with the modification notification. getType()
    *   can be used to obtain the type of the event.
    */
    protected void documentModified(DocumentEvent evt) {
        // Invalidate variable maps
        localVarMaps.clear();
        globalVarMaps.clear();
    }

    /** Find the type of the variable. The default behavior is to first
    * search for the local variable declaration and then possibly for
    * the global declaration and if the declaration position is found
    * to get the first word on that position.
    * @return it returns Object to enable the custom implementations
    *   to return the appropriate instances.
    */
    public Object findType(String varName, int varPos) {
        Object type = null;
        Map varMap = getLocalVariableMap(varPos); // first try local vars
        if (varMap != null) {
            type = varMap.get(varName);
        }

        if (type == null) {
            varMap = getGlobalVariableMap(varPos); // try global vars
            if (varMap != null) {
                type = varMap.get(varName);
            }
        }

        return type;
    }

    public Map getLocalVariableMap(int pos) {
        Integer posI = new Integer(pos);
        Map varMap = (Map)localVarMaps.get(posI);
        if (varMap == null) {
            varMap = buildLocalVariableMap(pos);
            localVarMaps.put(posI, varMap);
        }
        return varMap;
    }

    protected Map buildLocalVariableMap(int pos) {
        int methodStartPos = getMethodStartPosition(pos);
        if (methodStartPos >= 0 && methodStartPos < pos) {
            VariableMapTokenProcessor vmtp = createVariableMapTokenProcessor(methodStartPos, pos);
            try {
                tokenizeText(vmtp, methodStartPos, pos, true);
                return vmtp.getVariableMap();
            } catch (BadLocationException e) {
                // will default null
            }
        }
        return null;
    }

    public Map getGlobalVariableMap(int pos) {
        Integer posI = new Integer(pos);
        Map varMap = (Map)globalVarMaps.get(posI);
        if (varMap == null) {
            varMap = buildGlobalVariableMap(pos);
            globalVarMaps.put(posI, varMap);
        }
        return varMap;
    }

    protected Map buildGlobalVariableMap(int pos) {
        int docLen = doc.getLength();
        VariableMapTokenProcessor vmtp = createVariableMapTokenProcessor(0, docLen);
        if (vmtp != null) {
            try {
                tokenizeText(vmtp, 0, docLen, true);
                return vmtp.getVariableMap();
            } catch (BadLocationException e) {
                // will default null
            }
        }
        return null;
    }

    /** Get the start position of the method or the area
    * where the declaration can start.
    */
    protected int getMethodStartPosition(int pos) {
        return 0; // return begining of the document by default
    }

    /** Find either the local or global declaration position. First
    * try the local declaration and if it doesn't succeed, then
    * try the global declaration.
    */
    public int findDeclarationPosition(String varName, int varPos) {
        int pos = findLocalDeclarationPosition(varName, varPos);
        if (pos < 0) {
            pos = findGlobalDeclarationPosition(varName, varPos);
        }
        return pos;
    }

    public int findLocalDeclarationPosition(String varName, int varPos) {
        int methodStartPos = getMethodStartPosition(varPos);
        if (methodStartPos >= 0 && methodStartPos < varPos) {
            return findDeclarationPositionImpl(varName, methodStartPos, varPos);
        }
        return -1;
    }

    /** Get the position of the global declaration of a given variable.
    * By default it's implemented to use the same token processor as for the local
    * variables but the whole file is searched.
    */
    public int findGlobalDeclarationPosition(String varName, int varPos) {
        return findDeclarationPositionImpl(varName, 0, doc.getLength());
    }

    private int findDeclarationPositionImpl(String varName, int startPos, int endPos) {
        DeclarationTokenProcessor dtp = createDeclarationTokenProcessor(varName, startPos, endPos);
        if (dtp != null) {
            try {
                tokenizeText(dtp, startPos, endPos, true);
                return dtp.getDeclarationPosition();
            } catch (BadLocationException e) {
                // will default to -1
            }
        }
        return -1;
    }

    protected DeclarationTokenProcessor createDeclarationTokenProcessor(
        String varName, int startPos, int endPos) {
        return null;
    }

    protected VariableMapTokenProcessor createVariableMapTokenProcessor(
        int startPos, int endPos) {
        return null;
    }

    /** Token processor extended to get declaration position
    * of the given variable.
    */
    public interface DeclarationTokenProcessor extends TokenProcessor {

        /** Get the declaration position. */
        public int getDeclarationPosition();

    }

    public interface VariableMapTokenProcessor extends TokenProcessor {

        /** Get the map that contains the pairs [variable-name, variable-type]. */
        public Map getVariableMap();

    }

}

/*
 * Log
 *  3    Gandalf   1.2         1/10/00  Miloslav Metelka 
 *  2    Gandalf   1.1         11/14/99 Miloslav Metelka 
 *  1    Gandalf   1.0         11/8/99  Miloslav Metelka 
 * $
 */

