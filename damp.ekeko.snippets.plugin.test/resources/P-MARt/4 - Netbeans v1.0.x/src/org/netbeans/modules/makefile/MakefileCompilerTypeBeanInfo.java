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

package org.netbeans.modules.makefile;

import java.awt.Image;
import java.beans.*;

import org.openide.compiler.ExternalCompilerType;
import org.openide.util.NbBundle;

/** Description of the compiler type.
 * @author Jesse Glick
 */
public class MakefileCompilerTypeBeanInfo extends SimpleBeanInfo {

    /** Get default info.
     * @return infos from super
     */
    public BeanInfo[] getAdditionalBeanInfo () {
        try {
            return new BeanInfo[] {
                       Introspector.getBeanInfo (ExternalCompilerType.class)
                   };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    /** Get the bean descriptor.
     * @return a localized descriptor
     */
    public BeanDescriptor getBeanDescriptor () {
        BeanDescriptor desc = new BeanDescriptor (MakefileCompilerType.class);
        desc.setDisplayName (NbBundle.getBundle (MakefileCompilerTypeBeanInfo.class).getString ("LABEL_MakefileCompilerType"));
        desc.setShortDescription (NbBundle.getBundle (MakefileCompilerTypeBeanInfo.class).getString ("HINT_MakefileCompilerType"));
        return desc;
    }

    /** Get the bean properties.
     * @return properties for the targets
     */
    public PropertyDescriptor[] getPropertyDescriptors () {
        try {
            PropertyDescriptor target = new PropertyDescriptor ("target", MakefileCompilerType.class);
            target.setDisplayName (NbBundle.getBundle (MakefileCompilerTypeBeanInfo.class).getString ("PROP_target"));
            target.setShortDescription (NbBundle.getBundle (MakefileCompilerTypeBeanInfo.class).getString ("HINT_target"));
            PropertyDescriptor cleanTarget = new PropertyDescriptor ("cleanTarget", MakefileCompilerType.class);
            cleanTarget.setDisplayName (NbBundle.getBundle (MakefileCompilerTypeBeanInfo.class).getString ("PROP_cleanTarget"));
            cleanTarget.setShortDescription (NbBundle.getBundle (MakefileCompilerTypeBeanInfo.class).getString ("HINT_cleanTarget"));
            PropertyDescriptor forceTarget = new PropertyDescriptor ("forceTarget", MakefileCompilerType.class);
            forceTarget.setDisplayName (NbBundle.getBundle (MakefileCompilerTypeBeanInfo.class).getString ("PROP_forceTarget"));
            forceTarget.setShortDescription (NbBundle.getBundle (MakefileCompilerTypeBeanInfo.class).getString ("HINT_forceTarget"));
            /*
            PropertyDescriptor errorExpression = new PropertyDescriptor ("errorExpression", MakefileCompilerType.class);
            errorExpression.setDisplayName (NbBundle.getBundle (MakefileCompilerTypeBeanInfo.class).getString ("PROP_errorExpression"));
            errorExpression.setShortDescription (NbBundle.getBundle (MakefileCompilerTypeBeanInfo.class).getString ("HINT_errorExpression"));
            errorExpression.setPropertyEditorClass (ErrExprEd.class);
            */
            return new PropertyDescriptor[] { target, cleanTarget, forceTarget /* , errorExpression */ };
        } catch (IntrospectionException ie) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                ie.printStackTrace ();
            return null;
        }
    }

    /** Cached icon.
     */
    private static Image icon;
    /** Get a bean icon.
     * @param type the style
     * @return the icon
     */
    public Image getIcon (int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            if (icon == null)
                icon = loadImage ("makefileObject.gif");
            return icon;
        } else {
            return null;
        }
    }

    /*
    public static class ErrExprEd extends org.openide.explorer.propertysheet.editors.ExternalCompiler.ErrorExpressionEditor {

      public ErrExprEd () {
        super (makeCollection ());
      }

      private static Collection makeCollection () {
        java.util.List list = new java.util.ArrayList ();
        list.addAll (java.util.Arrays.asList (MakefileCompilerType.ERROR_EXPRS));
        return list;
      }

}
    */

}
