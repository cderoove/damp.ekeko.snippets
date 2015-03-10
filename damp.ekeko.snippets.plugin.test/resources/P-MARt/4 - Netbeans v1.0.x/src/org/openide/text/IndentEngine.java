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

package org.openide.text;

import java.io.Writer;
import java.util.HashMap;
import javax.swing.text.*;

/** Indentation engine for formating text in documents.
* Provides mapping between MIME types and engines, so anybody
* can find appropriate type of engine for type of document.
*
* @author Jaroslav Tulach
*/
public abstract class IndentEngine extends Object {
    /** hashtable mapping MIME type to engine (String, IndentEngine) 
     * @associates IndentEngine*/
    private static HashMap map = new HashMap (7);

    /** Indents the current line. Should not effect any other
    * lines.
    * @param doc the document to work on
    * @param offset the offset of a character on the line
    * @return new offset of the original character
    */
    public abstract int indentLine (Document doc, int offset);

    /** Inserts new line at given position and indents the new line with
    * spaces.
    *
    * @param doc the document to work on
    * @param offset the offset of a character on the line
    * @return new offset to place cursor to
    */
    public abstract int indentNewLine (Document doc, int offset);

    /** Creates writer that formats text that is inserted into it.
    * The writer should not modify the document but use the 
    * provided writer to write to. Usually the underlaying writer
    * will modify the document itself and optionally it can remember
    * the current position in document. That is why the newly created
    * writer should do no buffering.
    * <P>
    * The provided document and offset are only informational,
    * should not be modified but only used to find correct indentation
    * strategy.
    *
    * @param doc document 
    * @param offset position to begin inserts at
    * @param writer writer to write to
    * @return new writer that will format written text and pass it
    *   into the writer
    */
    public abstract Writer createWriter (Document doc, int offset, Writer writer);


    /** Registers an engine for the MIME type.
    * @param mime mime type
    * @param eng indentation engine
    */
    public synchronized static void register (String mime, IndentEngine eng) {
        map.put (mime, eng);
    }

    /** Finds engine associated with given mime type.
    * If no engine is associated returns default one.
    */
    public synchronized static IndentEngine find (String mime) {
        IndentEngine eng = (IndentEngine)map.get (mime);
        if (eng == null) {
            eng = new Default ();
        }
        return eng;
    }

    /** Default indentation engine.
    */
    private static final class Default extends IndentEngine {
        public int indentLine (Document doc, int offset) {
            return offset;
        }

        public int indentNewLine (Document doc, int offset) {
            try {
                doc.insertString (offset, "\n", null); // NOI18N
            } catch (BadLocationException ex) {
            }
            return offset + 1;
        }

        public Writer createWriter (Document doc, int offset, Writer writer) {
            return writer;
        }
    }
}

/*
* Log
*  4    Gandalf   1.3         1/13/00  Ian Formanek    NOI18N
*  3    Gandalf   1.2         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         4/21/99  Jaroslav Tulach 
* $
*/
