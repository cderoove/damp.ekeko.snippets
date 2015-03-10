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

package org.netbeans.modules.apisupport;

import java.awt.Image;
import java.beans.*;

import org.openide.cookies.CompilerCookie;
import org.openide.compiler.Compiler;

public class CompilerTypeTesterBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor bd = new BeanDescriptor (CompilerTypeTester.class);
        bd.setDisplayName ("Test CompilerType Objects");
        return bd;
    }

    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] { Introspector.getBeanInfo (Tester.class) };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor cookie = new PropertyDescriptor ("cookie", CompilerTypeTester.class);
            cookie.setDisplayName ("Compilation Type");
            cookie.setShortDescription ("Whether to compile, build, or clean with the compiler type.");
            cookie.setPropertyEditorClass (CookieEd.class);
            PropertyDescriptor depth = new PropertyDescriptor ("depth", CompilerTypeTester.class);
            depth.setDisplayName ("Depth");
            depth.setShortDescription ("Depth of recursion of compilation--files only; folders also; recursive subfolders also.");
            depth.setPropertyEditorClass (DepthEd.class);
            return new PropertyDescriptor[] { cookie, depth };
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
                icon = loadImage ("resources/CompilerTypeTesterIcon.gif");
            return icon;
        } else {
            return null;
        }
    }

    public static class CookieEd extends PropertyEditorSupport {

        private static String TAG_COMPILE = "Compile";
        private static String TAG_BUILD = "Build";
        private static String TAG_CLEAN = "Clean";
        private static String[] tags = new String[] { TAG_COMPILE, TAG_BUILD, TAG_CLEAN };

        public String[] getTags () {
            return tags;
        }

        public String getAsText () {
            Class clazz = (Class) getValue ();
            if (clazz == CompilerCookie.Compile.class)
                return TAG_COMPILE;
            else if (clazz == CompilerCookie.Build.class)
                return TAG_BUILD;
            else if (clazz == CompilerCookie.Clean.class)
                return TAG_CLEAN;
            else
                throw new InternalError ();
        }

        public void setAsText (String nue) throws IllegalArgumentException {
            if (nue.equals (TAG_COMPILE))
                setValue (CompilerCookie.Compile.class);
            else if (nue.equals (TAG_BUILD))
                setValue (CompilerCookie.Build.class);
            else if (nue.equals (TAG_CLEAN))
                setValue (CompilerCookie.Clean.class);
            else
                throw new IllegalArgumentException ();
        }

    }

    public static class DepthEd extends PropertyEditorSupport {

        private static String TAG_ZERO = "Zero";
        private static String TAG_ONE = "One";
        private static String TAG_INFINITE = "Infinite";
        private static String[] tags = new String[] { TAG_ZERO, TAG_ONE, TAG_INFINITE };

        public String[] getTags () {
            return tags;
        }

        public String getAsText () {
            int d = ((Integer) getValue ()).intValue ();
            return tags[d];
        }

        public void setAsText (String nue) throws IllegalArgumentException {
            if (nue.equals (TAG_ZERO))
                setValue (new Integer (0));
            else if (nue.equals (TAG_ONE))
                setValue (new Integer (1));
            else if (nue.equals (TAG_INFINITE))
                setValue (new Integer (2));
            else
                throw new IllegalArgumentException ();
        }

    }

}

/*
 * Log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         10/13/99 Jesse Glick     
 * $
 */
