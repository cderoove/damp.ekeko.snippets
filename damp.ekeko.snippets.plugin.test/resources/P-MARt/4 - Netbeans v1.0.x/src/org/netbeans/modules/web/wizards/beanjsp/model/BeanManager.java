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

package  org.netbeans.modules.web.wizards.beanjsp.model;

import org.netbeans.modules.web.wizards.beanjsp.util.*;
import org.netbeans.modules.web.util.*;

import java.lang.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import java.beans.*;


public class BeanManager extends Object {

    public static final int GETTER_PROPS = 0;
    public static final int SETTER_PROPS = 1;
    public static final int ALL_PROPS = 2;

    public BeanManager() { }

    // I am not using this method. But, in case...
    public Collection getValidJSPBeans(String basePath, String packageName) {

        String beansPath = basePath+File.separator+packageName.replace('.',File.separatorChar);   // NOI18N

        JSPVector jspBeans = new JSPVector();

        File[] beanFiles = getAllClassFilesInPackage(beansPath);
        // Debug.println("Looking in basepath : "+basePath);
        // Debug.println("Looking in package : "+packageName);
        try {
            for(int i=0; i < beanFiles.length; ++i) {

                String beanName = beanFiles[i].getName();
                beanName = beanName.substring(0,beanName.indexOf("."));			 // NOI18N
                // Debug.println("Reflecting "+beanName);
                try {
                    Class cls = Class.forName(packageName+"."+beanName);				 // NOI18N
                    int m = cls.getModifiers();
                    if(Modifier.isInterface(m) || Modifier.isAbstract(m) ||
                            !Modifier.isPublic(m) ) {
                        continue;
                    }

                    BeanInfo jspBeanInfo = Introspector.getBeanInfo(cls,Object.class);
                    if(jspBeanInfo == null)
                        continue;
                    jspBeans.add(new JSPBean(jspBeanInfo));
                }catch(Exception ex) {}

            }
        }catch(Exception ex) {}
        return jspBeans;
    }


    public JSPVector createValidJSPBeans(Vector jspBeanClasses) {
        JSPVector jspBeans = new JSPVector();
        Iterator beanIterator = jspBeanClasses.iterator();
        for(;beanIterator.hasNext();) {
            Class jspBeanClass = (Class)beanIterator.next();
            JSPBean jspBean = createJSBBean(jspBeanClass);
            if(jspBean != null)
                jspBeans.add(jspBean);
        }

        try {
            Collections.sort(jspBeans, BeanManager.getJSPItemComparator());
        } catch(Exception ex) { } // ignore safely as we need not sort it

        return jspBeans;
    }

    public JSPBean createJSBBean(Class jspBeanClass) {
        JSPBean jspBean = null;
        try {
            int m = jspBeanClass.getModifiers();
            if(Modifier.isInterface(m) || Modifier.isAbstract(m) ||
                    !Modifier.isPublic(m) ) {
                return jspBean;
            }

            BeanInfo jspBeanInfo = Introspector.getBeanInfo(jspBeanClass,Object.class);
            if(jspBeanInfo == null) {
                return jspBean;
            }
            jspBean = new JSPBean(jspBeanInfo);
        }catch(Exception ex) { } // Debug.print(ex);  // don't care
        return jspBean;
    }

    public boolean isProperty(MethodDescriptor methodDesc) {
        String name = methodDesc.getMethod().getName();
        if(name.startsWith("get") || name.startsWith("set") || name.startsWith("is"))			 // NOI18N
            return true;
        else
            return false;
    }

    public Collection getValidJSPBeanMethods(JSPBean jspBean) {

        JSPVector methodDescVec = new JSPVector();
        try {
            MethodDescriptor[] methodDesc = jspBean.getBeanInfo().getMethodDescriptors();
            // Debug.println("Methods Found on "+jspBean.getBeanName()+" : "+methodDesc.length);
            for(int i=0; i < methodDesc.length; ++i) {
                // if(!isProperty(methodDesc[i])) {
                // Debug.println("Adding : "+methodDesc[i].getName()+" Method to "+jspBean.getBeanName());
                methodDescVec.add(new JSPBeanMethod(jspBean,methodDesc[i]));
                //  }
            }
        }catch(Exception ex) {Debug.print(ex);}

        try {
            Collections.sort(methodDescVec, BeanManager.getJSPItemComparator());
        } catch(Exception ex) { } // ignore safely as we need not sort it

        return methodDescVec;
    }

    public boolean isSupportedProperty(PropertyDescriptor propDesc,int readWriteType) {

        // Don't show hidden or expert properties.
        if (propDesc.isHidden() || propDesc.isExpert()) {
            return false;
        }

        String name = propDesc.getDisplayName();
        Class type = propDesc.getPropertyType();
        Method getter = propDesc.getReadMethod();
        Method setter = propDesc.getWriteMethod();

        if( SETTER_PROPS == readWriteType && setter == null)
            return false;

        if( GETTER_PROPS == readWriteType && getter == null)
            return false;

        // Only show read/write properties.
        // if (getter == null || setter == null) {
        //	return false;
        //}

        // for time being, don't deal with indexed properties
        if( type.isArray() ) {
            // Debug.println("Indexed properties are not supported yet.");
            // Debug.println("Indexed property ignored : "+name);
            return false;
        }

        return true;
    }

    public Collection getAllValidJSPBeanFields(JSPBean jspBean) {
        return getValidJSPBeanFields(jspBean,this.ALL_PROPS);
    }

    public Collection getValidJSPBeanSetterFields(JSPBean jspBean) {
        return getValidJSPBeanFields(jspBean,this.SETTER_PROPS);
    }

    public Collection getValidJSPBeanGetterFields(JSPBean jspBean) {
        return getValidJSPBeanFields(jspBean,this.GETTER_PROPS);
    }

    public Collection getValidJSPBeanFields(JSPBean jspBean, int readWriteType) {
        JSPVector propDescVec = new JSPVector();
        try {
            PropertyDescriptor[] propDesc = jspBean.getBeanInfo().getPropertyDescriptors();
            // Debug.println("Properties Found on "+jspBean.getBeanName()+" : "+propDesc.length);
            for(int i=0; i < propDesc.length; ++i) {
                if(isSupportedProperty(propDesc[i],readWriteType)) {
                    // Debug.println("Adding : "+propDesc[i].getName()+" Property to "+jspBean.getBeanName());
                    propDescVec.add(new JSPBeanField(jspBean,propDesc[i]));
                }
            }
        }catch(Exception ex) {Debug.print(ex);}

        try {
            Collections.sort(propDescVec, BeanManager.getJSPItemComparator());
        } catch(Exception ex) { } // ignore safely as we need not sort it

        return propDescVec;
    }

    public File[] getAllClassFilesInPackage(String packagePath) {
        File packageDir = new File(packagePath);
        if(!packageDir.isDirectory()) {
            // Debug.print(new Exception(packageDir+" Not a valid package"));
            return new File[0];
        }

        return packageDir.listFiles(new FileFilter() {
                                        public boolean accept(File pathname) {
                                            if(pathname.isDirectory())
                                                return false;
                                            if(pathname.getName().endsWith(".class"))				 // NOI18N
                                                return true;
                                            else
                                                return false;
                                        }
                                    });
    }

    public void printJSPCode(Collection collection) {
        Iterator iterator = collection.iterator();
        for(;iterator.hasNext();) {
            Object obj = iterator.next();
            Debug.println(obj.toString());
        }
    }

    public static Comparator getJSPItemComparator() {
        class JSPItemComparator implements Comparator  {
            public int compare(Object o1,
                               Object o2) {
                return o1.toString().compareTo(o2.toString());
            }
            public boolean equals(Object obj) {
                return obj.equals(JSPItemComparator.this);
            }
        }

        return new JSPItemComparator();
    }

    public static void main(String[] args) {

        if(Debug.TEST) {
            BeanManager beanManager = new BeanManager();
            Collection beans = beanManager.getValidJSPBeans(args[0],args[0]);
            beanManager.printJSPCode(beans);

            try {
                Iterator conIterator = beans.iterator();
                for(;conIterator.hasNext();){

                    JSPBean jspBean = (JSPBean)conIterator.next();

                    Collection properties = beanManager.getAllValidJSPBeanFields(jspBean);
                    beanManager.printJSPCode(properties);

                    Collection methods = beanManager.getValidJSPBeanMethods(jspBean);
                    beanManager.printJSPCode(methods);

                }
            }catch(Exception ex) {Debug.print(ex);}
        }
    }

}


