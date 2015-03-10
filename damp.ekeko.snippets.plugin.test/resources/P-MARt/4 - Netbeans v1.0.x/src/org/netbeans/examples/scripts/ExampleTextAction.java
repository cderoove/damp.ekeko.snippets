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

package org.netbeans.examples.scripts;
import java.awt.Toolkit;
import javax.swing.JEditorPane;
import javax.swing.text.*;
import org.openide.TopManager;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.CookieAction;

/** An example action for the F4J IDE that works on open text editor windows.
* To customize, change performAction(Document,Caret); getName; and if you wish,
* iconResource() too.
* To install, you might:
* - copy this class and paste the source into Global Options | Menu (Toolbars)
*   [compile it there]
* - paste as default instance (you must repeat this if you change the source,
*   but at least the source can be kept in your Filesystems; PREFERRED)
*   [compile in Filesystems first!]
* - paste into the Actions pool; then you can add it as a keyboard shortcut
*   (or to the context menu for an object type, probably not useful)
* - get the API Support module, make a module manifest (see bottom of file), and
*   hit Execute on manifest to install as "Tools" action; now available e.g. in
*   Editor context menu; keep sources compiled in Filesystems
* @author Jesse Glick
* @version 0.1; just an example for the public domain
*/
public class ExampleTextAction extends CookieAction {

    /** Do something with an open document.
    * This is the part you should write according to your needs.
    * Please use javax.swing.text.* API.
    * @param d a document open in the editor pane
    * @param c the caret giving cursor position, selection, etc.
    */
    protected void performAction (Document d, Caret c) throws BadLocationException {
        int dot = c.getDot ();
        int mark = c.getMark ();
        if (dot == mark) {
            // No selection: insert [[]] at point and move cursor to center
            d.insertString (dot, "[[]]", null);
            c.setDot (dot + 2);
        } else {
            // Selection: wrap it in [[...]] and clear selection
            int start = Math.min (dot, mark);
            int end = Math.max (dot, mark);
            d.insertString (end, "]]", null);
            d.insertString (start, "[[", null);
            c.setDot (end + 4);
        }
    }

    public String getName () {
        return "Example Action";
    }

    /* Implement if you want an icon (e.g. for non-context menu item/toolbar button):
    protected String iconResource () {
      return "/resource/path/to/icon.gif";
}
    */

    // --- BOILERPLATE BELOW THIS POINT ---
    protected Class[] cookieClasses () {
        return new Class[] { EditorCookie.class };
    }
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }
    protected boolean enable (Node[] nodes) {
        if (! super.enable (nodes)) return false;
        EditorCookie ed = (EditorCookie) nodes[0].getCookie (EditorCookie.class);
        return ed.getOpenedPanes () != null;
    }
    protected void performAction (Node[] nodes) {
        EditorCookie ed = (EditorCookie) nodes[0].getCookie (EditorCookie.class);
        JEditorPane pane = ed.getOpenedPanes ()[0];
        try {
            performAction (pane.getDocument (), pane.getCaret ());
        } catch (BadLocationException ble) {
            Toolkit.getDefaultToolkit ().beep ();
            // TopManager.getDefault ().notifyException (ble);
        }
    }
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }
}

/*
Example manifest to install this as a module for inclusion in Tools... menus:
--------------------%<---------------------- example-action.mf
Manifest-Version: 1.0
OpenIDE-Module: org.netbeans.examples.scripts.example_action
OpenIDE-Module-Name: Example Action Module

Name: org/netbeans/examples/scripts/ExampleTestAction.class
OpenIDE-Module-Class: Action
--------------------%<----------------------
*/
