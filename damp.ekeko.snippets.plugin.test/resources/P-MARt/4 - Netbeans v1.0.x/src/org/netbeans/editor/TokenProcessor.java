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

/** Process the tokens
*
* @author Miloslav Metelka
* @version 1.00
*/


public interface TokenProcessor {

    /** Notify that the token was found.
    * @param tokenID ID of the token found
    * @param helperID helper ID for the token found. For some tokens
    *   in some scanners there can be additional information like
    *   which keyword or operator was found
    * @param offset offset of the token found
    * @param tokenLen length of the token found
    * @return true if the next token should be searched or false if the scan should
    *   be stopped completely.
    */
    public boolean token(int tokenID, int helperID, int offset, int tokenLen);

    /** Notify that end of scanned buffer was found.
    * The method decides whether to continue the scan or stop. The rest
    * of characters that were not scanned, because the is not completed
    * is also provided.
    * @param offset offset of the rest of the characters
    * @return 0 to stop token processing,
    *         &gt 0 process additional characters in the document
    */
    public int eot(int offset);

    /** Notify that the following buffer will be scanned. This method
    * is called before the buffer is being scanned. (startPos - offset)
    * gives the starting position of the buffer in the text.
    * @param buffer buffer that will be scanned
    * @param offset offset in the buffer with the first character to be scanned.
    *   If doesn't reflect the preScan.
    * @param len count of the characters that will be scanned. It doesn't reflect
    *   the preScan.
    * @param startPos starting position of the scanning in the document.
    *   It doesn't reflect the preScan.
    * @param preScan preScan needed for the scanning.
    * @param lastBuffer whether this is the last buffer to scan in the document
    *   so there are no more characters in the document after this buffer.
    * @*/
    public void nextBuffer(char[] buffer, int offset, int len,
                           int startPos, int preScan, boolean lastBuffer);

}

/*
 * Log
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/30/99  Miloslav Metelka 
 *  3    Gandalf   1.2         9/15/99  Miloslav Metelka 
 *  2    Gandalf   1.1         8/27/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/29/99  Miloslav Metelka 
 * $
 */

