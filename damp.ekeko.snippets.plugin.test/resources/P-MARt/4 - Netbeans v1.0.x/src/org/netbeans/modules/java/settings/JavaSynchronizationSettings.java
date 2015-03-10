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

package org.netbeans.modules.java.settings;

import java.beans.*;
import java.util.HashMap;

import org.openide.options.SystemOption;
import org.openide.src.Type;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** A settings for synchronization (connections) of java sources.
*
* @author Petr Hamernik
*/
public class JavaSynchronizationSettings extends SystemOption {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 24341252342342345L;

    public static final String PROP_GENERATE_RETURN = "generateReturn"; // NOI18N
    public static final String PROP_ENABLED = "enabled"; // NOI18N

    public static final int RETURN_GEN_NOTHING = 0;
    public static final int RETURN_GEN_EXCEPTION = 1;
    public static final int RETURN_GEN_NULL = 2;

    private static final String[] RETURN_STRINGS = {
        "\n", "\nthrow new UnsupportedOperationException();\n", "\nreturn null;\n" // NOI18N
    };

    /**
     * @associates String 
     */
    private static final HashMap RETURN_STRINGS_PRIMITIVE;

    static {
        String number = "\nreturn 0;\n"; // NOI18N
        RETURN_STRINGS_PRIMITIVE = new HashMap();
        RETURN_STRINGS_PRIMITIVE.put(Type.VOID, RETURN_STRINGS[RETURN_GEN_NOTHING]);
        RETURN_STRINGS_PRIMITIVE.put(Type.BOOLEAN, "\nreturn false;\n"); // NOI18N
        RETURN_STRINGS_PRIMITIVE.put(Type.INT, number);
        RETURN_STRINGS_PRIMITIVE.put(Type.CHAR, "\nreturn ' ';\n"); // NOI18N
        RETURN_STRINGS_PRIMITIVE.put(Type.BYTE, number);
        RETURN_STRINGS_PRIMITIVE.put(Type.SHORT, number);
        RETURN_STRINGS_PRIMITIVE.put(Type.LONG, number);
        RETURN_STRINGS_PRIMITIVE.put(Type.FLOAT, number);
        RETURN_STRINGS_PRIMITIVE.put(Type.DOUBLE, number);
    }

    private static int generateReturn = RETURN_GEN_NOTHING;
    private static boolean enabled = true;

    /** human presentable name */
    public String displayName() {
        return NbBundle.getBundle(JavaSynchronizationSettings.class).getString("CTL_JavaSynchronization_Settings");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (JavaSynchronizationSettings.class);
    }

    public int getGenerateReturn() {
        return generateReturn;
    }

    public boolean isGlobal() {
        return false;
    }

    public void setGenerateReturn(int val) {
        if (generateReturn != val) {
            int old = generateReturn;
            generateReturn = val;
            firePropertyChange(PROP_GENERATE_RETURN, new Integer(old), new Integer(val));
        }
    }

    public String getGenerateReturnAsString(Type type) {
        if (generateReturn == RETURN_GEN_NULL) {
            if (type.isPrimitive())
                return (String) RETURN_STRINGS_PRIMITIVE.get(type);
            else
                return RETURN_STRINGS[RETURN_GEN_NULL];
        }
        else
            return RETURN_STRINGS[generateReturn];
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean val) {
        if (val != enabled) {
            enabled = val;
            if (val)
                firePropertyChange(PROP_ENABLED, Boolean.FALSE, Boolean.TRUE);
            else
                firePropertyChange(PROP_ENABLED, Boolean.TRUE, Boolean.FALSE);
        }
    }

}

/*
 * Log
 *  9    Gandalf   1.8         1/14/00  Petr Hamernik   made it project settings
 *  8    Gandalf   1.7         1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         9/10/99  Petr Hamernik   redundant properties 
 *       removed.
 *  5    Gandalf   1.4         7/2/99   Jesse Glick     More help IDs.
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         6/4/99   Petr Hamernik   synchronization update
 *  2    Gandalf   1.1         6/2/99   Petr Hamernik   connections of java 
 *       sources
 *  1    Gandalf   1.0         6/1/99   Petr Hamernik   
 * $
 */


