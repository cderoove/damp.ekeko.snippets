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

package org.netbeans.editor;

import java.util.Arrays;
import java.util.ArrayList;

/**
* All the strings that should be localized will go through this place.
* Multiple custom localizers can be registered.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class LocaleSupport {

    private static Localizer[] localizers = new Localizer[0];

    /** Add a new localizer to the localizer array. The array of localizers
    * is tracked from the lastly added localizer to the firstly added one
    * until the translation for the given key is found.
    * @param localizer localizer to add to the localizer array.
    */
    public static void addLocalizer(Localizer localizer) {
        ArrayList ll = new ArrayList(Arrays.asList(localizers));
        ll.add(localizer);
        Localizer[] la = new Localizer[ll.size()];
        ll.toArray(la);
        localizers = la;
    }

    /** Get the localized string for the given key using the registered
    * localizers.
    * @param key key to translate to localized string.
    * @return localized string or null if there's no localization.
    */
    public static String getString(String key) {
        String ret = null;
        for (int i = localizers.length - 1; i >= 0; i--) {
            ret = localizers[i].getString(key);
            if (ret != null) {
                break;
            }
        }
        return ret;
    }

    /** Get the localized string or the default value if no translation
    * for the given key exists.
    * @param key key to translate to localized string.
    * @param defaultValue default value to be returned in case no localized
    *   string is found for the given key.
    */
    public static String getString(String key, String defaultValue) {
        String ret = getString(key);
        return (ret != null) ? ret : defaultValue;
    }

    /** Translates the keys to the localized strings. There can be multiple localizers
    * registered in the locale support.
    */
    public interface Localizer {

        /** Translate the key to the localized string.
        * @param key key to translate to the localized string.
        */
        public String getString(String key);

    }

}

/*
 * Log
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/4/99  Miloslav Metelka 
 *  3    Gandalf   1.2         9/30/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/2/99   Miloslav Metelka 
 * $
 */
