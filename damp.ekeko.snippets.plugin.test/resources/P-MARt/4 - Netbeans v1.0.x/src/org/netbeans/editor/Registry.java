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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.text.JTextComponent;

/**
* All the documents and components register here so that
* they become available to the processing that crosses
* different components and documents such as cross document
* position stack or word matching.
*
* @author Miloslav Metelka
* @version 1.00
*/
public class Registry {

    /** Shared iregistry nstance */
    private static Registry registry;

    /** Array list of weak references to documents */
    private ArrayList docRefs = new ArrayList();

    /** Array of activated document numbers */
    private ArrayList docAct = new ArrayList();

    /** Array list of weak references to components */
    private ArrayList compRefs = new ArrayList();

    /** Array of activated component numbers */
    private ArrayList compAct = new ArrayList();


    /** Get shared registry instance */
    public static Registry getRegistry() {
        if (registry == null) {
            registry = new Registry();
        }
        return registry;
    }

    /** Add document to registry. Doesn't search for repetitive
    * adding.
    * @return ID of the component
    */
    public static synchronized int addDocument(BaseDocument doc) {
        Registry r = getRegistry();
        int ind = getID(doc);
        if (ind != -1) {
            return ind;
        }
        ind = r.docRefs.size();
        r.docRefs.add(new WeakReference(doc));
        doc.putProperty(BaseDocument.ID_PROP, new Integer(ind));
        return ind;
    }

    /** Get documeent ID from the document by searching for ID property */
    public static synchronized int getID(BaseDocument doc) {
        if (doc == null) {
            return -1;
        }
        Integer ind = (Integer)doc.getProperty(BaseDocument.ID_PROP);
        if (ind == null) {
            return -1;
        } else {
            return ind.intValue();
        }
    }

    /** Get document when its ID is known.
    * It's rather cheap operation.
    * @return document instance or null when document no longer exists
    */
    public static synchronized BaseDocument getDocument(int docID) {
        Registry r = getRegistry();
        if (docID > r.docRefs.size()) {
            return null;
        }
        return (BaseDocument)((WeakReference)r.docRefs.get(docID)).get();
    }

    /** Put the document to the first position in the array of last accessed
    * documents.
    */
    public static synchronized void activate(BaseDocument doc) {
        int docID = getID(doc);
        if (docID == -1) { // doc not registered
            return;
        }
        Integer i = new Integer(docID);
        Registry r = getRegistry();
        int ind = r.docAct.indexOf(i);
        if (ind != -1) {
            r.docAct.add(0, r.docAct.remove(ind));
        } else { // not yet added
            r.docAct.add(0, i);
        }
    }

    private BaseDocument getValidDoc(int ind, boolean forward) {
        while (ind >= 0 && ind < docAct.size()) {
            int docID = ((Integer)docAct.get(ind)).intValue();
            BaseDocument doc = (BaseDocument)((WeakReference)docRefs.get(docID)).get();
            if (doc != null) {
                return doc;
            } else { // non-existent component, remove from activated list
                docAct.remove(ind);
                ind -= forward ? +1 : -1; // continue stmt gives unreachable code - bug?
            }
            ind += forward ? +1 : -1;
        }
        return null;
    }

    private BaseDocument getNextActiveDoc(int docID, boolean forward) {
        int actSize = docAct.size();
        int ind = forward ? 0 : (actSize - 1);
        while (ind >= 0 && ind < actSize) {
            if (((Integer)docAct.get(ind)).intValue() == docID) {
                ind += forward ? +1 : -1; // get next one
                return getValidDoc(ind, forward);
            }
            ind += forward ? +1 : -1;
        }
        return null;
    }

    public static synchronized BaseDocument getMostActiveDocument() {
        return getRegistry().getValidDoc(0, true);
    }

    public static synchronized BaseDocument getLeastActiveDocument() {
        int lastInd = getRegistry().docAct.size() - 1;
        return getRegistry().getValidDoc(lastInd, false);
    }

    public static BaseDocument getLessActiveDocument(BaseDocument doc) {
        return getLessActiveDocument(getID(doc));
    }

    public static synchronized BaseDocument getLessActiveDocument(int docID) {
        return getRegistry().getNextActiveDoc(docID, true);
    }

    public static BaseDocument getMoreActiveDocument(BaseDocument doc) {
        return getMoreActiveDocument(getID(doc));
    }

    public static synchronized BaseDocument getMoreActiveDocument(int docID) {
        return getRegistry().getNextActiveDoc(docID, false);
    }

    /** Add component to registry. If the component is already registered
    * it returns the existing ID.
    * @return ID of the component
    */
    public static synchronized int addComponent(JTextComponent c) {
        Registry r = getRegistry();
        int ind = getID(c);
        if (ind != -1) {
            return ind; // already registered
        }
        ind = r.compRefs.size();
        r.compRefs.add(new WeakReference(c));
        ((BaseTextUI)c.getUI()).getExtUI().componentID = ind;
        return ind;
    }

    /** Get documeent ID from the document by searching for ID property */
    public static synchronized int getID(JTextComponent c) {
        if (c == null) {
            return -1;
        }
        return ((BaseTextUI)c.getUI()).getExtUI().componentID;
    }

    /** Get component when its ID is known.
    * It's rather cheap operation.
    * @return component instance or null when component no longer exists
    */
    public static synchronized JTextComponent getComponent(int compID) {
        Registry r = getRegistry();
        if (compID > r.compRefs.size()) {
            return null;
        }
        return (JTextComponent)((WeakReference)r.compRefs.get(compID)).get();
    }

    /** Put the component to the first position in the array of last accessed
    * components. The activate of document is also called automatically.
    */
    public static synchronized void activate(JTextComponent c) {
        int compID = getID(c);
        if (compID == -1) { // c not registered
            return;
        }
        Integer i = new Integer(compID);
        Registry r = getRegistry();
        int ind = r.compAct.indexOf(i);
        if (ind != -1) {
            r.compAct.add(0, r.compAct.remove(ind));
        } else { // not yet added
            r.compAct.add(0, i);
        }
        activate((BaseDocument)c.getDocument()); // activate document too
    }

    private JTextComponent getValidComp(int ind, boolean forward) {
        while (ind < compAct.size() && ind >= 0) {
            int compID = ((Integer)compAct.get(ind)).intValue();
            JTextComponent c = (JTextComponent)((WeakReference)compRefs.get(compID)).get();
            if (c != null) {
                return c;
            } else { // non-existent component, remove from activated list
                compAct.remove(ind);
                ind -= forward ? +1 : -1; // continue stmt gives unreachable code - bug?
            }
            ind += forward ? +1 : -1;
        }
        return null;
    }

    private JTextComponent getNextActiveComp(int compID, boolean forward) {
        int actSize = compAct.size();
        int ind = forward ? 0 : (actSize - 1);
        while (ind >= 0 && ind < actSize) {
            if (((Integer)compAct.get(ind)).intValue() == compID) {
                ind += forward ? +1 : -1;
                return getValidComp(ind, forward);
            }
            ind += forward ? +1 : -1;
        }
        return null;
    }

    public static synchronized JTextComponent getMostActiveComponent() {
        return getRegistry().getValidComp(0, true);
    }

    public static synchronized JTextComponent getLeastActiveComponent() {
        int lastInd = getRegistry().compAct.size() - 1;
        return getRegistry().getValidComp(lastInd, false);
    }

    public static JTextComponent getLessActiveComponent(JTextComponent c) {
        return getLessActiveComponent(getID(c));
    }

    public static synchronized JTextComponent getLessActiveComponent(int compID) {
        return getRegistry().getNextActiveComp(compID, true);
    }

    public static JTextComponent getMoreActiveComponent(JTextComponent c) {
        return getMoreActiveComponent(getID(c));
    }

    public static synchronized JTextComponent getMoreActiveComponent(int compID) {
        return getRegistry().getNextActiveComp(compID, false);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Document References:\n"); // NOI18N
        for (int i = 0; i < docRefs.size(); i++) {
            WeakReference r = (WeakReference)docRefs.get(i);
            sb.append("docRefs[" + i + "]=" + ((r != null) ? r.get() : "null") + "\n\n"); // NOI18N
        }
        sb.append("Component References:\n"); // NOI18N
        for (int i = 0; i < compRefs.size(); i++) {
            WeakReference r = (WeakReference)compRefs.get(i);
            sb.append("compRefs[" + i + "]=" + ((r != null) ? r.get() : "null") + "\n\n"); // NOI18N
        }
        sb.append("\nActive Document Indexes:\n"); // NOI18N
        for (int i = 0; i < docAct.size(); i++) {
            sb.append(docAct.get(i));
            if (i != docAct.size() - 1) {
                sb.append(", "); // NOI18N
            }
        }
        sb.append("\nActive Component Indexes:\n"); // NOI18N
        for (int i = 0; i < compAct.size(); i++) {
            sb.append(compAct.get(i));
            if (i != compAct.size() - 1) {
                sb.append(", "); // NOI18N
            }
        }
        return sb.toString();
    }

}

/*
 * Log
 *  6    Gandalf   1.5         1/13/00  Miloslav Metelka 
 *  5    Gandalf   1.4         11/8/99  Miloslav Metelka 
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         5/7/99   Miloslav Metelka line numbering and fixes
 *  2    Gandalf   1.1         5/5/99   Miloslav Metelka 
 *  1    Gandalf   1.0         4/23/99  Miloslav Metelka 
 * $
 */

