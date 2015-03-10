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
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.openide.TopManager;
import org.openide.text.Line;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.debugger.Watch;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;


/**
*
* @author   Jan Jancura
*/
public class PrintAction extends CoreBreakpoint.Action {

    static final long serialVersionUID =-5177383273138374863L;

    /** Default text to print on breakpoint. */
    public static final String BREAKPOINT_TEXT =
        NbBundle.getBundle (PrintAction.class).getString ("CTL_Default_print_text");

    /** Default text to print on breakpoint. */
    public static final String BREAKPOINT_METHOD_TEXT =
        NbBundle.getBundle (PrintAction.class).getString ("CTL_Default_method_print_text");

    /** Default text to print on breakpoint. */
    public static final String BREAKPOINT_EXCEPTION_TEXT =
        NbBundle.getBundle (PrintAction.class).getString ("CTL_Default_exception_print_text");

    /** Property name constant. */
    public static final String PROP_PRINT_TEXT = "printText"; // NOI18N

    /** Property text variable. */
    protected String text;


    // init ...............................................................................................

    /**
    * Creates the new Print action with given text.
    */
    public PrintAction (String s) {
        text = s;
    }


    // CoreBreakpoint.Action implementation ................................................................

    /**
    * Returns new initialized instance of Print action.
    */
    protected CoreBreakpoint.Action getNewInstance () {
        return new PrintAction (text);
    }

    /**
    * Returns specific properties of this event.
    */
    public Node.Property[] getProperties () {
        ResourceBundle bundle = NbBundle.getBundle (PrintAction.class);
        return new Node.Property[] {
                   new PropertySupport.ReadWrite (
                       PrintAction.PROP_PRINT_TEXT,
                       String.class,
                       bundle.getString ("PROP_print_text"),
                       bundle.getString ("HINT_print_text")
                   ) {
                       public Object getValue () {
                           return getPrintText ();
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           try {
                               setPrintText ((String) val);
                           } catch (ClassCastException e) {
                               throw new IllegalArgumentException ();
                           }
                       }
                   }
               };
    }

    /**
    * Prints text to debugger output.
    */
    protected void perform (CoreBreakpoint.Event event) {
        if ((text == null) || (text.trim ().length () < 1)) return;
        int i = 0, j = 0, k = 0;
        StringBuffer sb = new StringBuffer ();
        while ((j = text.indexOf ('{', i)) >= 0) {
            sb.append (text.substring (i, j));
            if (((j + 1) < text.length ()) && (text.charAt (j + 1) != '$')) {
                // spec. variable ({threadName})
                k = text.indexOf ('}', j + 1);
                String var = (k >= 0) ? text.substring (j + 1, k) : text.substring (j + 2);
                resolveTag (var, event, sb);
                if (k < 0) {
                    i = -1;
                    break;
                }
                i = k + 1;
            } else {
                // variable ({$i + j})
                k = text.indexOf ('}', j);
                String var = (k >= 0) ? text.substring (j + 2, k) : text.substring (j + 1);
                String val = getValue (var, event);
                sb.append (val).append (' ');
                if (k < 0) {
                    i = -1;
                    break;
                }
                i = k + 1;
            }
        } // while
        sb.append (text.substring (i, text.length ()));
        final String s = new String (sb);
        final AbstractDebugger d = event.getDebugger ();
        if (i >= 0) sb.append (text.substring (i));
        SwingUtilities.invokeLater (new Runnable () {
                                        public void run () {
                                            d.println (s, AbstractDebugger.ERR_OUT + AbstractDebugger.STL_OUT);
                                        }
                                    });
    }

    /**
    * Returns customizer visuall component.
    */
    public JComponent getCustomizer () {
        return new PrintActionPanel (this);
    }


    // properties ........................................................................................

    /**
    * Get text to print on breakpoint access.
    */
    public String getPrintText () {
        return text;
    }

    /**
    * Set name of method to stop on.
    */
    public void setPrintText (String s) {
        if (s == text) return;
        String old = text;
        text = s;
        firePropertyChange (PROP_PRINT_TEXT, old, s);
    }


    // other methods ...................................................................................


    /**
    * Resolving special tags:
    *   threadName      name of thread where breakpoint ocurres 
    *   className       name of class where breakpoint ocurres 
    *   lineNumber      number of line where breakpoint ocurres 
    *   methodName      name of method where breakpoint ocurres 
    *   variableValue   valueof given variable 
    *   variableType    type ofgiven variable
    */
    protected void resolveTag (String tag, CoreBreakpoint.Event event, StringBuffer sb) {
        if (tag.equals ("threadName")) { // NOI18N
            AbstractThread tt = event.getThread ();
            if (tt == null)
                sb.append ('?');
            else
                try {
                    sb.append (tt.getName ());
                } catch (Exception e) {
                    sb.append ('?');
                }
        } else
            if (tag.equals ("className")) { // NOI18N
                CallStackFrame[] stack = event.getCallStack ();
                if ((stack == null) || (stack.length < 1))
                    sb.append ('?');
                else
                    try {
                        sb.append (stack [0].getClassName ());
                    } catch (Exception e) {
                        sb.append ('?');
                    }
            } else
                if (tag.equals ("lineNumber")) { // NOI18N
                    CallStackFrame[] stack = event.getCallStack ();
                    if ((stack == null) || (stack.length < 1))
                        sb.append ('?');
                    else
                        try {
                            sb.append (stack [0].getLineNumber ());
                        } catch (Exception e) {
                            sb.append ('?');
                        }
                } else
                    if (tag.equals ("methodName")) { // NOI18N
                        CallStackFrame[] stack = event.getCallStack ();
                        if ((stack == null) || (stack.length < 1))
                            sb.append ('?');
                        else
                            try {
                                sb.append (stack [0].getMethodName ());
                            } catch (Exception e) {
                                sb.append ('?');
                            }
                    } else
                        if (tag.equals ("variableValue")) { // NOI18N
                            AbstractVariable variable = event.getVariable ();
                            if (variable == null)
                                sb.append ('?');
                            else {
                                String s = variable.getAsText ();
                                if (s == null)
                                    sb.append ('?');
                                else
                                    sb.append (s);
                            }
                        } else
                            if (tag.equals ("variableType")) { // NOI18N
                                AbstractVariable variable = event.getVariable ();
                                if (variable == null)
                                    sb.append ('?');
                                else {
                                    String s = variable.getInnerType ();
                                    if (s == null)
                                        sb.append ('?');
                                    else
                                        sb.append (s);
                                }
                            } else
                                sb.append ('?');
    }

    /**
    * Returns value of given variable as text. Can be changed by debugger
    * implementations. Default implementation uses hidden watch for getting this
    * value.
    */
    protected String getValue (String variable, CoreBreakpoint.Event event) {
        AbstractWatch w = (AbstractWatch) event.getDebugger ().createWatch (variable, true);
        w.refresh (event.getThread ());
        if (w == null) return NbBundle.getBundle (PrintAction.class).getString ("CTL_Not_in_scope");
        String res = w.getAsText ();
        w.remove ();
        return res;
    }
}

/*
* Log
*  18   Gandalf-post-FCS1.16.4.0    3/28/00  Daniel Prusa    
*  17   Gandalf   1.16        1/13/00  Daniel Prusa    NOI18N
*  16   Gandalf   1.15        11/29/99 Jan Jancura     Bug 3341 - bad \n in 
*       output of debugger  
*  15   Gandalf   1.14        11/8/99  Jan Jancura     Somma classes renamed
*  14   Gandalf   1.13        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  13   Gandalf   1.12        9/28/99  Jan Jancura     
*  12   Gandalf   1.11        9/15/99  Jan Jancura     
*  11   Gandalf   1.10        9/2/99   Jan Jancura     
*  10   Gandalf   1.9         8/18/99  Jan Jancura     Localization & Current 
*       thread & Current session
*  9    Gandalf   1.8         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  8    Gandalf   1.7         8/3/99   Jan Jancura     Localized
*  7    Gandalf   1.6         8/2/99   Jan Jancura     
*  6    Gandalf   1.5         7/30/99  Jan Jancura     
*  5    Gandalf   1.4         7/13/99  Jan Jancura     
*  4    Gandalf   1.3         7/2/99   Jan Jancura     Session debugging support
*  3    Gandalf   1.2         6/10/99  Jan Jancura     
*  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         6/1/99   Jan Jancura     
* $
*/
