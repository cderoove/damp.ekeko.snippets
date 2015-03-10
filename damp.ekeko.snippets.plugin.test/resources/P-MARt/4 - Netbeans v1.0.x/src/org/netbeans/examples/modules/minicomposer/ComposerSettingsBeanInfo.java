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

package org.netbeans.examples.modules.minicomposer;
import java.awt.Image;
import java.beans.*;
import java.text.*;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;
public class ComposerSettingsBeanInfo extends SimpleBeanInfo {
    public PropertyDescriptor[] getPropertyDescriptors () {
        ResourceBundle bundle = NbBundle.getBundle (ComposerSettingsBeanInfo.class);
        try {
            PropertyDescriptor player = new PropertyDescriptor ("player", ComposerSettings.class);
            player.setDisplayName (bundle.getString ("PROP_player"));
            player.setShortDescription (bundle.getString ("HINT_player"));
            PropertyDescriptor sampleRate = new PropertyDescriptor ("sampleRate", ComposerSettings.class);
            sampleRate.setDisplayName (bundle.getString ("PROP_sampleRate"));
            sampleRate.setShortDescription (bundle.getString ("HINT_sampleRate"));
            sampleRate.setExpert (true);
            sampleRate.setPropertyEditorClass (SampleRateEd.class);
            return new PropertyDescriptor[] { player, sampleRate };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }
    private static Image icon;
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("ScoreDataIcon.gif");
            return icon;
        } else {
            return null;
        }
    }
    public static class SampleRateEd extends PropertyEditorSupport {
        private static final float[] rates = new float [] { 12000.0f, 24000.0f, 48000.0f };
        private static final String[] tags = new String[rates.length];
        static {
            NumberFormat format = new DecimalFormat ();
            for (int i = 0; i < rates.length; i++)
                tags[i] = format.format (rates[i]);
        }
        public String[] getTags () {
            return tags;
        }
        public String getAsText () {
            float value = ((Float) getValue ()).floatValue ();
            for (int i = 0; i < rates.length; i++)
                if (rates[i] == value)
                    return tags[i];
            return "???";
        }
        public void setAsText (String text) throws IllegalArgumentException {
            for (int i = 0; i < tags.length; i++) {
                if (tags[i].equals (text)) {
                    setValue (new Float (rates[i]));
                    return;
                }
            }
            throw new IllegalArgumentException ();
        }
    }
}
