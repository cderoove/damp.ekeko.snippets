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

package org.netbeans.modules.form.editors;

import java.beans.*;

public class CharacterEditor extends PropertyEditorSupport
{
    public String getJavaInitializationString() {
        String s = getValue().toString();
        if (s.length() == 0) {
            return "'\\0'";
        }
        else {
            char c = s.charAt(0);
            StringBuffer buf = new StringBuffer(6);

            switch (c) {
            case '\b': buf.append("\\b"); break; // NOI18N
            case '\t': buf.append("\\t"); break; // NOI18N
            case '\n': buf.append("\\n"); break; // NOI18N
            case '\f': buf.append("\\f"); break; // NOI18N
            case '\r': buf.append("\\r"); break; // NOI18N
                //        case '\"': buf.append("\\\""); break; // NOI18N
            case '\'': buf.append("\\'"); break; // NOI18N
            case '\\': buf.append("\\\\"); break; // NOI18N
            default:
                if (c >= 0x0020 && c <= 0x007f)
                    buf.append(c);
                else {
                    buf.append("\\u"); // NOI18N
                    String hex = Integer.toHexString(c);
                    for (int j = 0; j < 4 - hex.length(); j++)
                        buf.append('0');
                    buf.append(hex);
                }
            }

            return "'" +  buf.toString() + "'";
        }
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (text.length() >= 1) {
            text = text.substring(0,1);
            setValue(text);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
