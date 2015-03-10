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

package org.netbeans.modules.javadoc.settings;

import java.beans.*;
import java.util.ResourceBundle;

import sun.tools.util.ModifierFilter;

import org.openide.util.NbBundle;

/** property editor for members property of JavadocSettings class
*
* @author Petr Hrebejk
*/
public class MembersPropertyEditor extends PropertyEditorSupport
    implements MemberConstants {

    private static ResourceBundle bundle = NbBundle.getBundle( MembersPropertyEditor.class );

    /** Array of tags
    */
    private static final String[] tags = {"public", "protected", "package", "private" }; // NOI18N
    private static final long [] values = {
        MemberConstants.PUBLIC,
        MemberConstants.PROTECTED,
        MemberConstants.PACKAGE,
        MemberConstants.PRIVATE };

    /** @return names of the supported member Acces types */
    public String[] getTags() {
        return tags;
    }

    /** @return text for the current value */
    public String getAsText () {
        long value = ((Long)getValue()).longValue();

        for (int i = 0; i < values.length ; i++)
            if (values[i] == value)
                return tags[i];

        return bundle.getString( "CTL_MembersEditor_Unsupported" );
    }

    /** @param text A text for the current value. */
    public void setAsText (String text) {
        for (int i = 0; i < tags.length ; i++)
            if (tags[i] == text) {
                setValue(new Long(values[i]));
                return;
            }

        setValue( new Long(0L) );
    }
}

/*
 * Log
 *  5    Gandalf   1.4         1/12/00  Petr Hrebejk    i18n
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/15/99  Petr Hrebejk    Localization
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         4/23/99  Petr Hrebejk    
 * $
 */
