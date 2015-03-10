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

package org.netbeans.modules.icebrowser;

import org.openide.explorer.propertysheet.editors.EnhancedPropertyEditor;

public class EncodingEditor extends ChoicePropertyEditor
    implements EnhancedPropertyEditor {
    public EncodingEditor () {
        super (
            new Object[] {
                "",
                "ISO8859_1",

                "ISO8859_2",
                "Cp1250",

                "JISAutoDetect",
                "SJIS",
                "EUC_JP",

                "Big5",
                "EUC_TW",

                "EUC_CN",

                "EUC_KR",

                "ISO8859_5",
                "Cp1251",
                "Cp866",

                "ISO8859_4",
                "Cp1257",

                "ISO8859_7",
                "Cp1253",

                "ISO8859_9",

                "UTF8"
            },
            new String[] {
                "Default",
                "Western (ISO-8859-1)",

                "Central European (ISO-8859-2)",
                "Central European (Windows-1250)",

                "Japanese (Auto-Detect)",
                "Japanese (Shift_JIS)",
                "Japanese (EUC-JP)",

                "Traditional Chinese (BIG5)",
                "Traditional Chinese (EUC-TW)",

                "Simplified Chinese (GB2312)",

                "Korean (EUC_KR)",

                "Cyrillic (ISO-8859-5)",
                "Cyrillic (Windows-1251)",
                "Cyrillic (CP866)",

                "Baltic (ISO-8859-4)",
                "Baltic (Windows-1257)",

                "Greek (ISO-8859-7)",
                "Greek (Windows-1253)",

                "Turkish (ISO-8859-9)",

                "Unicode (UTF-8)"
            },
            true
        );
    }// constructor
}

/*
 * Log
 *  2    Gandalf-post-FCS1.1         4/5/00   Jan Jancura     Default values for Fonts
 *       & encoding are in bundles now
 *  1    Gandalf-post-FCS1.0         4/5/00   Jan Jancura     
 * $
 */
