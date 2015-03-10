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

package org.netbeans.modules.emacs;

public interface Protocol {
    // create ()
    String CMD_create = "create";
    // close ()
    String CMD_close = "close";
    // getLength () -> (length)
    String FUN_getLength = "getLength";
    // startDocumentListen ()
    String CMD_startDocumentListen = "startDocumentListen";
    // stopDocumentListen ()
    String CMD_stopDocumentListen = "stopDocumentListen";
    // ignore (what)
    String CMD_ignore = "ignore";
    // setTitle (title)
    String CMD_setTitle = "setTitle";
    // remove (off, length) -> () or (reason, off)
    String FUN_remove = "remove";
    // insert (off, text) -> () or (reason, off)
    String FUN_insert = "insert";
    // getText (off, length) -> (text) or (reason, off)
    String FUN_getText = "getText";
    // createPosition (which, off, forwardBias) -> () or (reason, off)
    String FUN_createPosition = "createPosition";
    // lookupPosition (which) -> (off)
    String FUN_lookupPosition = "lookupPosition";
    // destroyPosition (which)
    String CMD_destroyPosition = "destroyPosition";
    // countLines () -> (count)
    String FUN_countLines = "countLines";
    // findLineFromOffset (off) -> (line)
    String FUN_findLineFromOffset = "findLineFromOffset";
    // getLineStartOffset (line) -> (off)
    String FUN_getLineStartOffset = "getLineStartOffset";
    // insert (off, text)
    String EVT_insert = "insert";
    // remove (off, len)
    String EVT_remove = "remove";
    // setContentType (type)
    String CMD_setContentType = "setContentType";
    // setDot (off)
    String CMD_setDot = "setDot";
    // setMark (off)
    String CMD_setMark = "setMark";
    // getDot () -> (off)
    String FUN_getDot = "getDot";
    // getMark () -> (off)
    String FUN_getMark = "getMark";
    // setLocAndSize (x, y, w, h)
    String CMD_setLocAndSize = "setLocAndSize";
    // startCaretListen ()
    String CMD_startCaretListen = "startCaretListen";
    // stopCaretListen ()
    String CMD_stopCaretListen = "stopCaretListen";
    // newDotAndMark (dot, mark)
    String EVT_newDotAndMark = "newDotAndMark";
    // guard (off, len)
    String CMD_guard = "guard";
    // unguard (off, len)
    String CMD_unguard = "unguard";
    // setModified (isMod)
    String CMD_setModified = "setModified";
    // startAtomic ()
    String CMD_startAtomic = "startAtomic";
    // endAtomic ()
    String CMD_endAtomic = "endAtomic";
    // setAsUser (asuser)
    String CMD_setAsUser = "setAsUser";
    // setStyle (pos, name)
    String CMD_setStyle = "setStyle";
    // well-known styles
    String STYLE_NORMAL = "normal";
    String STYLE_BREAKPOINT = "breakpoint";
    String STYLE_CURRENT = "current";
    String STYLE_ERROR = "error";
    // keyPressed (name)
    // see Utilities.stringToKey, e.g. CS-F9 for control-shift-function9
    String EVT_keyCommand = "keyCommand";
    // killed ()
    String EVT_killed = "killed";

    // AUTH <password>
    String META_AUTH = "AUTH";
    // ACCEPT
    String META_ACCEPT = "ACCEPT";
    // REJECT
    String META_REJECT = "REJECT";
    // DISCONNECT
    String META_DISCONNECT = "DISCONNECT";

    // value instanceof java.awt.Rectangle
    String PROP_locAndSize = "locAndSize";

    String[] SERIAL_FUNCTIONS = { FUN_remove, FUN_insert, FUN_getText };
    String[] SERIAL_COMMANDS = { CMD_guard, CMD_unguard, CMD_startAtomic, CMD_endAtomic, CMD_setAsUser, CMD_setStyle };
    String[] SERIAL_EVENTS = { EVT_insert, EVT_remove, EVT_keyCommand, EVT_killed };
}
