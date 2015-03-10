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

package org.netbeans.examples.modules.multifs;

import org.openide.TopManager;
import org.openide.filesystems.*;

import java.awt.Component;
import java.beans.PropertyEditorSupport;

public class ConfigEditor extends PropertyEditorSupport {
    Config val;
    public ConfigEditor () {
        val = new Config (new FileSystem[] { }, null, new Config.Sieve[] { });
    }
    public String getAsText () {
        return null;
    }
    public void setAsText (String ignore) {
        throw new IllegalArgumentException ();
    }
    public Object getValue () {
        return val;
    }
    public void setValue (Object o) {
        if (o instanceof Config) {
            val = (Config) o;
            firePropertyChange ();
        } else {
            throw new IllegalArgumentException ();
        }
    }
    public boolean supportsCustomEditor () {
        return true;
    }
    public Component getCustomEditor () {
        return new ConfigCustomEditor (this);
    }
}
