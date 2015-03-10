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

package org.netbeans.modules.editor.java;

import javax.swing.ImageIcon;
import org.netbeans.editor.ext.JCompletion;
import org.netbeans.editor.ext.JCCellRenderer;

/**
* Extended Java Completion support
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbJCCellRenderer extends JCCellRenderer {

    static final String PACKAGE = "/org/openide/resources/defaultFolder"; // NOI18N
    static final String CLASS = "/org/openide/resources/src/class"; // NOI18N
    static final String INTERFACE = "/org/openide/resources/src/interface"; // NOI18N

    static final String FIELD_PUBLIC = "/org/openide/resources/src/variablePublic"; // NOI18N
    static final String FIELD_PROTECTED = "/org/openide/resources/src/variableProtected"; // NOI18N
    static final String FIELD_PACKAGE = "/org/openide/resources/src/variablePackage"; // NOI18N
    static final String FIELD_PRIVATE = "/org/openide/resources/src/variablePrivate"; // NOI18N

    static final String FIELD_ST_PUBLIC = "/org/openide/resources/src/variableStPublic"; // NOI18N
    static final String FIELD_ST_PROTECTED = "/org/openide/resources/src/variableStProtected"; // NOI18N
    static final String FIELD_ST_PACKAGE = "/org/openide/resources/src/variableStPackage"; // NOI18N
    static final String FIELD_ST_PRIVATE = "/org/openide/resources/src/variableStPrivate"; // NOI18N

    static final String CONSTRUCTOR_PUBLIC = "/org/openide/resources/src/constructorPublic"; // NOI18N
    static final String CONSTRUCTOR_PROTECTED = "/org/openide/resources/src/constructorProtected"; // NOI18N
    static final String CONSTRUCTOR_PACKAGE = "/org/openide/resources/src/constructorPackage"; // NOI18N
    static final String CONSTRUCTOR_PRIVATE = "/org/openide/resources/src/constructorPrivate"; // NOI18N

    static final String METHOD_PUBLIC = "/org/openide/resources/src/methodPublic"; // NOI18N
    static final String METHOD_PROTECTED = "/org/openide/resources/src/methodProtected"; // NOI18N
    static final String METHOD_PACKAGE = "/org/openide/resources/src/methodPackage"; // NOI18N
    static final String METHOD_PRIVATE = "/org/openide/resources/src/methodPrivate"; // NOI18N

    static final String METHOD_ST_PUBLIC = "/org/openide/resources/src/methodStPublic"; // NOI18N
    static final String METHOD_ST_PROTECTED = "/org/openide/resources/src/methodStProtected"; // NOI18N
    static final String METHOD_ST_PRIVATE = "/org/openide/resources/src/methodStPrivate"; // NOI18N
    static final String METHOD_ST_PACKAGE = "/org/openide/resources/src/methodStPackage"; // NOI18N

    static final String ICON_SUFFIX = ".gif"; // NOI18N

    static final long serialVersionUID =4145932612758106982L;
    public NbJCCellRenderer() {
        updateIcons();
    }

    protected void updateIcons() {
        String[] names = new String[] {
                             PACKAGE,
                             CLASS,
                             INTERFACE
                         };

        int[] offsets = new int[] {
                            JCCellRenderer.PACKAGE_ICON,
                            JCCellRenderer.CLASS_ICON,
                            JCCellRenderer.INTERFACE_ICON
                        };

        for (int i = 0; i < names.length; i++) {
            setIcon(new ImageIcon(getClass().getResource(names[i] + ICON_SUFFIX)),
                    offsets[i]);
        }

        names = new String[] {
                    FIELD_PUBLIC,
                    FIELD_PROTECTED,
                    FIELD_PACKAGE,
                    FIELD_PRIVATE,

                    FIELD_ST_PUBLIC,
                    FIELD_ST_PROTECTED,
                    FIELD_ST_PACKAGE,
                    FIELD_ST_PRIVATE,

                    CONSTRUCTOR_PUBLIC,
                    CONSTRUCTOR_PROTECTED,
                    CONSTRUCTOR_PACKAGE,
                    CONSTRUCTOR_PRIVATE,

                    METHOD_PUBLIC,
                    METHOD_PROTECTED,
                    METHOD_PACKAGE,
                    METHOD_PRIVATE,

                    METHOD_ST_PUBLIC,
                    METHOD_ST_PROTECTED,
                    METHOD_ST_PACKAGE,
                    METHOD_ST_PRIVATE
                };

        offsets = new int[] {
                      JCCellRenderer.FIELD_ICON,
                      JCompletion.PUBLIC_LEVEL,
                      JCCellRenderer.FIELD_ICON,
                      JCompletion.PROTECTED_LEVEL,
                      JCCellRenderer.FIELD_ICON,
                      JCompletion.PACKAGE_LEVEL,
                      JCCellRenderer.FIELD_ICON,
                      JCompletion.PRIVATE_LEVEL,

                      JCCellRenderer.FIELD_STATIC_ICON,
                      JCompletion.PUBLIC_LEVEL,
                      JCCellRenderer.FIELD_STATIC_ICON,
                      JCompletion.PROTECTED_LEVEL,
                      JCCellRenderer.FIELD_STATIC_ICON,
                      JCompletion.PACKAGE_LEVEL,
                      JCCellRenderer.FIELD_STATIC_ICON,
                      JCompletion.PRIVATE_LEVEL,

                      JCCellRenderer.CONSTRUCTOR_ICON,
                      JCompletion.PUBLIC_LEVEL,
                      JCCellRenderer.CONSTRUCTOR_ICON,
                      JCompletion.PROTECTED_LEVEL,
                      JCCellRenderer.CONSTRUCTOR_ICON,
                      JCompletion.PACKAGE_LEVEL,
                      JCCellRenderer.CONSTRUCTOR_ICON,
                      JCompletion.PRIVATE_LEVEL,

                      JCCellRenderer.METHOD_ICON,
                      JCompletion.PUBLIC_LEVEL,
                      JCCellRenderer.METHOD_ICON,
                      JCompletion.PROTECTED_LEVEL,
                      JCCellRenderer.METHOD_ICON,
                      JCompletion.PACKAGE_LEVEL,
                      JCCellRenderer.METHOD_ICON,
                      JCompletion.PRIVATE_LEVEL,

                      JCCellRenderer.METHOD_STATIC_ICON,
                      JCompletion.PUBLIC_LEVEL,
                      JCCellRenderer.METHOD_STATIC_ICON,
                      JCompletion.PROTECTED_LEVEL,
                      JCCellRenderer.METHOD_STATIC_ICON,
                      JCompletion.PACKAGE_LEVEL,
                      JCCellRenderer.METHOD_STATIC_ICON,
                      JCompletion.PRIVATE_LEVEL
                  };

        for (int i = 0; i < names.length; i++) {
            setIcon(new ImageIcon(getClass().getResource(names[i] + ICON_SUFFIX)),
                    offsets[(i<<1)], offsets[(i<<1) + 1]);
        }

    }

}

/*
 * Log
 *  5    Gandalf   1.4         1/13/00  Miloslav Metelka Localization
 *  4    Gandalf   1.3         11/14/99 Miloslav Metelka 
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  1    Gandalf   1.0         7/21/99  Miloslav Metelka 
 * $
 */

