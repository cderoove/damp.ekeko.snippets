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

import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.text.JTextComponent;

/**
* Utility methods for managing settings
*
* @author Miloslav Metelka
* @version 1.00
*/

public class SettingsUtil {

    public static final PrintColoringSubstituter defaultPrintColoringSubstituter
    = new PrintColoringSubstituter();
    public static final PrintColoringSubstituter boldFontPrintColoringSubstituter
    = new BoldFontPrintColoringSubstituter();
    public static final PrintColoringSubstituter italicFontPrintColoringSubstituter
    = new ItalicFontPrintColoringSubstituter();

    private static final float defaultPrintFontHeight = 10;

    /** Get either the cloned list or new list if the old
    * one was null.
    * @param l list to check
    * @return the cloned list if it was non-null or the new list
    */
    public static List getClonedList(List l) {
        return Collections.synchronizedList( // to be sure to avoid sync problems
                   (l != null) ? new ArrayList(l) : new ArrayList()
               );
    }

    public static List getClonedList(Class kitClass, String settingName) {
        return getClonedList((List)Settings.getValue(kitClass, settingName));
    }

    /** Useful for initializers */
    public static List getClonedList(Map mapHoldingKitSettings, String settingName) {
        if (mapHoldingKitSettings != null) {
            return getClonedList((List)mapHoldingKitSettings.get(settingName));
        } else {
            return null;
        }
    }


    public static Map getClonedMap(Map m) {
        return Collections.synchronizedMap( // to be sure to avoid sync problems
                   (m != null) ? new HashMap(m) : new HashMap()
               );
    }

    public static Map getClonedMap(Class kitClass, String settingName) {
        return getClonedMap((Map)Settings.getValue(kitClass, settingName));
    }

    /** Useful for initializers */
    public static Map getClonedMap(Map mapHoldingKitSettings, String settingName) {
        if (mapHoldingKitSettings != null) {
            return getClonedMap((Map)mapHoldingKitSettings.get(settingName));
        } else {
            return null;
        }
    }


    public static Object getValue(Class kitClass, String settingName,
                                  Object defaultValue) {
        Object value = Settings.getValue(kitClass, settingName);
        return (value != null) ? value : defaultValue;
    }

    public static int getInteger(Class kitClass, String settingName,
                                 int defaultValue) {
        Integer i = (Integer)Settings.getValue(kitClass, settingName);
        return (i != null) ?  i.intValue() : defaultValue;
    }

    public static int getInteger(Class kitClass, String settingName,
                                 Integer defaultValue) {
        return getInteger(kitClass, settingName, defaultValue.intValue());
    }

    public static boolean getBoolean(Class kitClass, String settingName,
                                     boolean defaultValue) {
        Boolean b = (Boolean)Settings.getValue(kitClass, settingName);
        return (b != null) ? b.booleanValue() : defaultValue;
    }

    public static boolean getBoolean(Class kitClass, String settingName,
                                     Boolean defaultValue) {
        return getBoolean(kitClass, settingName, defaultValue.booleanValue());
    }

    public static String getString(Class kitClass, String settingName,
                                   String defaultValue) {
        String s = (String)Settings.getValue(kitClass, settingName);
        return (s != null) ? s : defaultValue;
    }

    public static Acceptor getAcceptor(Class kitClass, String settingName,
                                       Acceptor defaultValue) {
        Acceptor a = (Acceptor)Settings.getValue(kitClass, settingName);
        return (a != null) ? a : defaultValue;
    }

    public static List getList(Class kitClass, String settingName,
                               List defaultValue) {
        List l = (List)Settings.getValue(kitClass, settingName);
        return (l != null) ? l : defaultValue;
    }

    public static List getCumulativeList(Class kitClass, String settingName,
                                         List defaultValue) {
        Settings.KitAndValue[] kva = Settings.getKitAndValueArray(kitClass, settingName);
        if (kva != null && kva.length > 0) {
            List l = new ArrayList((List)kva[0].value);
            for (int i = 1; i < kva.length; i++) {
                l.addAll((List)kva[i].value);
            }
            return l;
        } else {
            return defaultValue;
        }
    }

    public static Map getMap(Class kitClass, String settingName,
                             Map defaultValue) {
        Map m = (Map)Settings.getValue(kitClass, settingName);
        return (m != null) ? m : defaultValue;
    }


    public static void updateListSetting(Class kitClass,
                                         String settingName, Object[] addToList) {
        if (addToList != null && addToList.length > 0) {
            List l = getClonedList(kitClass, settingName);
            l.addAll(Arrays.asList(addToList));
            Settings.setValue(kitClass, settingName, l);
        }
    }

    public static void updateListSetting(Map mapHoldingKitSettings,
                                         String settingName, Object[] addToList) {
        if (mapHoldingKitSettings != null && addToList != null && addToList.length > 0) {
            List l = getClonedList(mapHoldingKitSettings, settingName);
            l.addAll(Arrays.asList(addToList));
            mapHoldingKitSettings.put(settingName, l);
        }
    }

    private static String getColoringSettingName(String coloringName, boolean printingSet) {
        return (coloringName
                + (printingSet ? Settings.COLORING_NAME_PRINT_SUFFIX : Settings.COLORING_NAME_SUFFIX)
               ).intern();
    }

    public static Coloring getColoring(Class kitClass, String coloringName, boolean printingSet) {
        return (Coloring)Settings.getValue(kitClass, getColoringSettingName(coloringName, printingSet));
    }

    public static void setColoring(Class kitClass, String coloringName,
                                   Object newValue, boolean printingSet) {
        Settings.setValue(kitClass, getColoringSettingName(coloringName, printingSet), newValue);
    }

    public static void setColoring(Class kitClass, String coloringName,
                                   Object componentColoringNewValue) {
        setColoring(kitClass, coloringName, componentColoringNewValue, false);
        setColoring(kitClass, coloringName, defaultPrintColoringSubstituter, true);
    }

    public static void setColoring(Class kitClass, String coloringName,
                                   Object componentColoringNewValue, Object printColoringNewValue) {
        setColoring(kitClass, coloringName, componentColoringNewValue, false);
        setColoring(kitClass, coloringName, printColoringNewValue, true);
    }

    /** Put the coloring into a map holding the settings for the particular kit.
    *
    */
    public static void setColoring(Map mapHoldingKitSettings, String coloringName,
                                   Object newValue, boolean printingSet) {
        mapHoldingKitSettings.put(getColoringSettingName(coloringName, printingSet), newValue);
    }

    /** Put the coloring into a map holding the settings for the particular kit and assign
    * a default print coloring substituter to the print coloring setting.
    */
    public static void setColoring(Map mapHoldingKitSettings, String coloringName,
                                   Object componentColoringNewValue) {
        setColoring(mapHoldingKitSettings, coloringName, componentColoringNewValue, false);
        setColoring(mapHoldingKitSettings, coloringName, defaultPrintColoringSubstituter, true);
    }

    public static void setColoring(Map mapHoldingKitSettings, String coloringName,
                                   Object componentColoringNewValue, Object printColoringNewValue) {
        setColoring(mapHoldingKitSettings, coloringName, componentColoringNewValue, false);
        setColoring(mapHoldingKitSettings, coloringName, printColoringNewValue, true);
    }

    public static void propagateColoring(Class kitClass, String coloringName,
                                         Object newValue, boolean printingSet) {
        Settings.propagateValue(kitClass, getColoringSettingName(coloringName, printingSet), newValue);
    }

    public static void propagateColoring(Class kitClass, String coloringName,
                                         Object componentColoringNewValue) {
        propagateColoring(kitClass, coloringName, componentColoringNewValue, false);
        propagateColoring(kitClass, coloringName, defaultPrintColoringSubstituter, true);
    }

    public static void propagateColoring(Class kitClass, String coloringName,
                                         Object componentColoringNewValue, Object printColoringNewValue) {
        propagateColoring(kitClass, coloringName, componentColoringNewValue, false);
        propagateColoring(kitClass, coloringName, printColoringNewValue, true);
    }


    /** Get the map holding [coloring-name, coloring] pairs for all the colorings
    * defined for the given kit. The <tt>Settings.COLORING_NAME_LIST</tt> setting
    * is used to the coloring names that will apear in the map.
    * @param kitClass kit class for which the colorings are retrieved from the settings.
    * @param printingSet retrieve the printing colorings instead of component colorings.
    */
    public static Map getColoringMap(Class kitClass, boolean printingSet) {
        HashMap coloringMap = new HashMap();
        List nameList = getCumulativeList(kitClass, Settings.COLORING_NAME_LIST, null);

        if (nameList != null) {
            for (int i = nameList.size() - 1; i >= 0; i--) {
                String name = (String)nameList.get(i);
                coloringMap.put(name, getColoring(kitClass, name, printingSet));
            }
        }
        return coloringMap;
    }

    public static void changeColorings(Map coloringMap, ColoringChanger coloringChanger) {
        Iterator i = coloringMap.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            String coloringName = (String)me.getKey();
            Coloring c = (Coloring)me.getValue();
            c = coloringChanger.changeColoring(coloringName, c);
            me.setValue(c); // update the coloring in the map
        }
    }

    /** This method decodes the possibly updated coloring map back into the settings.
    * @param kitClass kit class for which the colorings should be updated.
    * @param coloringMap possibly updated coloring-map
    * @param printingSet update the printing colorings instead of component colorings.
    */
    public static void updateColoringSettings(final Class kitClass,
            final Map coloringMap, final boolean printingSet) {
        Settings.update(
            new Runnable() {
                public void run() {
                    Iterator i = coloringMap.entrySet().iterator();
                    while (i.hasNext()) {
                        Map.Entry me = (Map.Entry)i.next();
                        String coloringName = (String)me.getKey();
                        Coloring c = (Coloring)me.getValue();
                        setColoring(kitClass, coloringName, c, printingSet);
                    }
                }
            }
        );
    }

    /** Substituter that translates the regular to the print coloring */
    public static class PrintColoringSubstituter implements Settings.Substituter {

        /** Translates the regular coloring to the print coloring
        * @param kitClass kit class for which the coloring is being retrieved
        * @param coloringName name of the coloring without the suffix
        * @param componentColoring component coloring retrieved from the settings. It's provided
        *   for convenience because the majority of substituters will derive
        *   the particular print coloring from the given component coloring.
        */
        protected Coloring getPrintColoring(Class kitClass,
                                            String coloringName, Coloring componentColoring) {
            Coloring printColoring = componentColoring;
            // Make the background color white
            if (printColoring.getBackColor() != null) {
                printColoring = Coloring.changeBackColor(printColoring, Color.white);
            }
            // Make the foreground color black
            if (printColoring.getForeColor() != null) {
                printColoring = Coloring.changeForeColor(printColoring, Color.black);
            }
            // Update the font height
            float pfh = getPrintFontHeight();
            if (pfh >= 0) {
                printColoring = Coloring.changeFontSize(printColoring, pfh);
            }
            return printColoring;
        }

        /** Return the font size to which the coloring font should be updated.
        * Negative value means not to update the coloring font.
        */
        protected float getPrintFontHeight() {
            return defaultPrintFontHeight;
        }

        public Object getValue(Class kitClass, String settingName) {
            if (settingName.endsWith(Settings.COLORING_NAME_PRINT_SUFFIX)) {
                String coloringName = settingName.substring(0,
                                      settingName.length() - Settings.COLORING_NAME_PRINT_SUFFIX.length());
                Coloring c = getColoring(kitClass, coloringName, false);
                return getPrintColoring(kitClass, coloringName, c);
            }
            return null;
        }

    }

    /** Print coloring substituter that changes the foreground color to the color given
    * in the constructor.
    */
    public static class ForeColorPrintColoringSubstituter extends PrintColoringSubstituter {

        private Color foreColor;

        public ForeColorPrintColoringSubstituter(Color foreColor) {
            this.foreColor = foreColor;
        }

        protected Coloring getPrintColoring(Class kitClass, String coloringName,
                                            Coloring componentColoring) {
            return Coloring.changeForeColor(
                       super.getPrintColoring(kitClass, coloringName, componentColoring),
                       foreColor
                   );
        }

    }

    /** Print coloring substituter that changes the font style to bold.
    */
    static class BoldFontPrintColoringSubstituter extends PrintColoringSubstituter {

        protected Coloring getPrintColoring(Class kitClass, String coloringName,
                                            Coloring componentColoring) {
            return Coloring.changeFontStyle(
                       super.getPrintColoring(kitClass, coloringName, componentColoring),
                       Font.BOLD
                   );
        }

    }

    /** Print coloring substituter that changes the font style to italic.
    */
    static class ItalicFontPrintColoringSubstituter extends PrintColoringSubstituter {

        protected Coloring getPrintColoring(Class kitClass, String coloringName,
                                            Coloring componentColoring) {
            return Coloring.changeFontStyle(
                       super.getPrintColoring(kitClass, coloringName, componentColoring),
                       Font.ITALIC
                   );
        }

    }

    /** Interface intended to change the given coloring.
    */
    public interface ColoringChanger {

        /** Change the coloring
        * @param coloringName name assigned to the coloring <tt>c</tt>. It can be used
        *   to filter just some coloring depending on the name.
        * @param c coloring to change.
        * @return the changed coloring or the same coloring as <tt>c</tt>
        *    if no change was performed.
        */
        public Coloring changeColoring(String coloringName, Coloring c);

    }

}

/*
 * Log
 *  6    Gandalf   1.5         2/15/00  Miloslav Metelka getColoringSettingName()
 *       fixed
 *  5    Gandalf   1.4         1/4/00   Miloslav Metelka 
 *  4    Gandalf   1.3         1/3/00   Miloslav Metelka 
 *  3    Gandalf   1.2         12/28/99 Miloslav Metelka 
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */

