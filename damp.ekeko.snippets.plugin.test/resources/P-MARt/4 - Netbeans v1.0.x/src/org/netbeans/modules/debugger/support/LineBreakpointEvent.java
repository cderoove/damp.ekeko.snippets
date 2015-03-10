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

package org.netbeans.modules.debugger.support;

import java.util.ResourceBundle;
import java.text.MessageFormat;
import java.io.ObjectOutputStream;
import java.io.IOException;
import javax.swing.JComponent;

import org.openide.text.Line;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.cookies.SourceCookie;
import org.openide.cookies.EditorCookie;
import org.openide.text.NbDocument;

import org.openide.src.*;

import org.netbeans.modules.debugger.support.util.Utils;
import org.netbeans.modules.debugger.support.actions.AddBreakpointAction;

/**
* Abstract implementation of breakpoint event on line.
*
* @author   Jan Jancura
*/
public abstract class LineBreakpointEvent extends ClassBreakpointEvent {

    static final long serialVersionUID = 5611559342537392476L;


    // variables .....................................................................................................

    private int                   lineNumber;
    protected Line                line = null;

    {
        lineNumber = ((AddBreakpointAction) SystemAction.get
                      (AddBreakpointAction.class)).getCurrentLineNumber ();
        String className = ((AddBreakpointAction) SystemAction.get
                            (AddBreakpointAction.class)).getCurrentClassName ();
        line = Utils.getLine (className, lineNumber);
    }


    // init .....................................................................................................

    private void writeObject (ObjectOutputStream oos) throws IOException {
        if (line != null) lineNumber = line.getLineNumber () + 1;
        oos.defaultWriteObject ();
    }

    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject ();
        //if (lineNumber >= 0)
        //  line = Utils.getLine (className, lineNumber);
    }


    // Event implementation ....................................................................................

    /**
    * Returns specific properties of this event.
    */
    public Node.Property[] getProperties () {
        ResourceBundle bundle = NbBundle.getBundle (CoreBreakpoint.class);
        return new Node.Property[] {
                   new PropertySupport.ReadWrite (
                       CoreBreakpoint.PROP_CLASS_NAME,
                       String.class,
                       bundle.getString ("PROP_breakpoint_class_name"),
                       bundle.getString ("HINT_breakpoint_class_name")
                   ) {
                       public Object getValue () {
                           return getClassName ();
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           try {
                               setClassName (((String)val).trim ());
                           } catch (ClassCastException e) {
                               throw new IllegalArgumentException ();
                           }
                       }
                   },
                   new PropertySupport.ReadWrite (
                       CoreBreakpoint.PROP_LINE_NUMBER,
                       Integer.TYPE,
                       bundle.getString ("PROP_breakpoint_line_number"),
                       bundle.getString ("HINT_breakpoint_line_number")
                   ) {
                       public Object getValue () {
                           return new Integer (getLineNumber ());
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           try {
                               setLineNumber (((Integer)val).intValue ());
                           } catch (ClassCastException e) {
                               throw new IllegalArgumentException ();
                           }
                       }
                   }
               };
    }

    /**
    * Returns name of type of this event.
    */
    public String getTypeName () {
        return "Line"; // NOI18N
    }

    /**
    * Returns display name of this event.
    */
    public String getTypeDisplayName () {
        return NbBundle.getBundle (LineBreakpointEvent.class).getString ("CTL_Line_event_type_name");
    }

    /**
    * Returns display name of this instance of event. It will be used
    * as the name of the breakpoint.
    */
    public String getDisplayName () {
        return new MessageFormat (
                   NbBundle.getBundle (LineBreakpointEvent.class).getString ("CTL_Line_event_name")
               ).format (new Object[] {getClassName (), new Integer (getLineNumber ())});
    }

    /**
    * Returns lines to highlite in the editor.
    */
    public Line[] getLines () {
        if (line == null) return null;
        return new Line [] {line};
    }

    /**
    * Returns customizer visual component.
    */
    public JComponent getCustomizer () {
        return new LineBreakpointPanel (this);
    }


    // properties ........................................................................................

    /**
    * Set name of class to stop on.
    */
    public void setClassName (String cn) {
        Object old = className;
        className = cn;
        int ln = getLineNumber ();
        line = null;
        if ( (className != null) &&
                (className.length () > 0) &&
                (ln > 0)
           ) if ((line = Utils.getLine (className, ln)) != null
                    ) lineNumber = -1;
            else lineNumber = ln;
        firePropertyChange (CoreBreakpoint.PROP_CLASS_NAME, old, className);
    }

    /**
    * Get number of line to stop on.
    */
    public int getLineNumber() {
        if (line != null) {
            int num = line.getLineNumber () + 1;
            return num;
        }
        return lineNumber;
    }

    /**
    * Set number of line to stop on.
    */
    public void setLineNumber (int ln) {
        int old = lineNumber;
        if (ln > 0) {
            line = Utils.getLine (className, ln);
            lineNumber = ln;
        }
        else {
            line = null;
            lineNumber = -1;
        }
        firePropertyChange (
            CoreBreakpoint.PROP_LINE_NUMBER,
            new Integer (old),
            new Integer (getLineNumber ())
        );
    }

    /**
    * Get line to stop on.
    */
    public Line getLine () {
        return line;
    }

    /**
    * Set line to stop on.
    */
    public void setLine (Line l) {
        Object old = line;
        line = l;
        if (line != null) {
            SourceCookie.Editor sourceCookie = (SourceCookie.Editor) line.getDataObject ().getCookie (SourceCookie.Editor.class);
            EditorCookie editorCookie = (EditorCookie) line.getDataObject ().getCookie (EditorCookie.class);
            if ((sourceCookie != null) && (editorCookie != null)) {
                int offset = NbDocument.findLineOffset (editorCookie.getDocument (), l.getLineNumber());
                className = Utils.getClassNameForElement (sourceCookie.findElement (offset));
            }
        }
        firePropertyChange (CoreBreakpoint.PROP_LINE_NUMBER, old, line);
    }

}

/*
* Log
*  20   Gandalf-post-FCS1.17.3.1    4/17/00  Daniel Prusa    bugfix for getLine, 
*       setLine
*  19   Gandalf-post-FCS1.17.3.0    3/28/00  Daniel Prusa    
*  18   Gandalf   1.17        1/20/00  Daniel Prusa    getLineNumber changed
*  17   Gandalf   1.16        1/15/00  Daniel Prusa    Line serialization
*  16   Gandalf   1.15        1/13/00  Daniel Prusa    NOI18N
*  15   Gandalf   1.14        1/4/00   Jan Jancura     Use trim () on user 
*       input.
*  14   Gandalf   1.13        11/9/99  Jan Jancura     Bug 3082 + Add breakpoint
*       on line from Add breakpoint dialog
*  13   Gandalf   1.12        11/8/99  Jan Jancura     Somma classes renamed
*  12   Gandalf   1.11        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  11   Gandalf   1.10        10/5/99  Jan Jancura     Serialization of 
*       debugger.
*  10   Gandalf   1.9         9/28/99  Jan Jancura     
*  9    Gandalf   1.8         8/18/99  Jan Jancura     Localization & Current 
*       thread & Current session
*  8    Gandalf   1.7         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  7    Gandalf   1.6         8/3/99   Jan Jancura     
*  6    Gandalf   1.5         7/14/99  Jan Jancura     
*  5    Gandalf   1.4         7/13/99  Jan Jancura     
*  4    Gandalf   1.3         7/2/99   Jan Jancura     Session debugging support
*  3    Gandalf   1.2         6/10/99  Jan Jancura     
*  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         6/1/99   Jan Jancura     
* $
*/
