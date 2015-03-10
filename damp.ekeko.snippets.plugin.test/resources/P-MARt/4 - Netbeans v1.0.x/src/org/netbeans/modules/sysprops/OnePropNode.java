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

package org.netbeans.modules.sysprops;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import javax.swing.event.*;
import org.openide.actions.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
public class OnePropNode extends AbstractNode {
    private static ResourceBundle bundle = NbBundle.getBundle (OnePropNode.class);
    private String key;
    private ChangeListener listener;
    public OnePropNode (String key) {
        super (Children.LEAF);
        this.key = key;
        setIconBase ("/org/netbeans/modules/sysprops/onePropIcon");
        setDefaultAction (SystemAction.get (PropertiesAction.class));
        super.setName (key);
        setShortDescription (bundle.getString ("HINT_OnePropNode"));
    }
    protected SystemAction[] createActions () {
        return new SystemAction[] {
                   SystemAction.get (DeleteAction.class),
                   SystemAction.get (RenameAction.class),
                   null,
                   SystemAction.get (ToolsAction.class),
                   SystemAction.get (PropertiesAction.class),
               };
    }
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.sysprops");
    }
    public Node cloneNode () {
        return new OnePropNode (key);
    }
    protected Sheet createSheet () {
        Sheet sheet = super.createSheet ();
        Sheet.Set props = sheet.get (Sheet.PROPERTIES);
        if (props == null) {
            props = Sheet.createPropertiesSet ();
            sheet.put (props);
        }
        props.put (new PropertySupport.Name (this));
        class ValueProp extends PropertySupport.ReadWrite {
            public ValueProp () {
                super ("value", String.class,
                       bundle.getString ("PROP_value"), bundle.getString ("HINT_value"));
            }
            public Object getValue () {
                return System.getProperty (key);
            }
            public void setValue (Object nue) {
                System.setProperty (key, (String) nue);
                PropertiesNotifier.changed ();
            }
        }
        props.put (new ValueProp ());
        PropertiesNotifier.addChangeListener (listener = new ChangeListener () {
                                                  public void stateChanged (ChangeEvent ev) {
                                                      firePropertyChange ("value", null, null);
                                                  }
                                              });
        return sheet;
    }
    protected void finalize () throws Throwable {
        super.finalize ();
        if (listener != null)
            PropertiesNotifier.removeChangeListener (listener);
    }
    public boolean canRename () {
        return true;
    }
    public void setName (String nue) {
        Properties p = System.getProperties ();
        String value = p.getProperty (key);
        p.remove (key);
        if (value != null) p.setProperty (nue, value);
        System.setProperties (p);
        PropertiesNotifier.changed ();
    }
    public boolean canDestroy () {
        return true;
    }
    public void destroy () throws IOException {
        Properties p = System.getProperties ();
        p.remove (key);
        System.setProperties (p);
        PropertiesNotifier.changed ();
    }
}
