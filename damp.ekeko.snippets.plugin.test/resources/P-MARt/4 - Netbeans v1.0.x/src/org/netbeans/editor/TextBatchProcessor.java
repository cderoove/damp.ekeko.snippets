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

/** Process the batches of the text in the document. This interface
* can be passed to the BaseDocument.processText() and this method
* then calls the processTextBatch() to process the text batches.
*
* @author Miloslav Metelka
* @version 1.00
*/


public interface TextBatchProcessor {

    /** Process one batch of the text.
    * @doc document to work with
    * @startPos starting position of the batch
    * @endPos ending position of the batch
    * @lastBatch whether this batch is the last one in the text area that
    *   is searched.
    * @return non-negative number to stop the batch processing. The returned
    *   value is remembered and returned from BaseDocument.processText().
    *   Negative value means to continue with the next batch.
    */
    public int processTextBatch(BaseDocument doc, int startPos, int endPos,
                                boolean lastBatch);

}

/*
 * Log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/10/99  Miloslav Metelka 
 * $
 */

