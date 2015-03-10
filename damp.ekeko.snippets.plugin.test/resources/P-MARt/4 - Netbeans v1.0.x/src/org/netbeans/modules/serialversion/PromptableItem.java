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

package org.netbeans.modules.serialversion;

import java.io.ObjectStreamField;

import org.openide.src.ClassElement;

final class PromptableItem {

    final ClassElement clazz;
    final String className;
    final ObjectStreamField[] fields;
    final long currSvuid;
    final long idealSvuid;

    PromptableItem (ClassElement clazz, String className, ObjectStreamField[] fields, long currSvuid, long idealSvuid) {
        this.clazz = clazz;
        this.className = className;
        this.fields = fields;
        this.currSvuid = currSvuid;
        this.idealSvuid = idealSvuid;
    }

    public int hashCode () {
        return className.hashCode ();
    }

    public boolean equals (Object o) {
        if (! (o instanceof PromptableItem)) return false;
        return className.equals (((PromptableItem) o).className);
    }

}
