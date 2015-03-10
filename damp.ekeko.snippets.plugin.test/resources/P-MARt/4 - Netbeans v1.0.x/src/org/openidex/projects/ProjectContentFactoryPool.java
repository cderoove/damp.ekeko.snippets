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

package org.openidex.projects;

import java.util.*;

/**
 *
 * @author  mryzl
 * @version 
 */
public class ProjectContentFactoryPool extends Object {
    /**
     * @associates ProjectContentFactory 
     */
    static List factories = new LinkedList();

    /** Creates new ProjectContentFactoryPool
    */
    protected ProjectContentFactoryPool() {
    }

    /** Add the factory.
    * @param pcf - factory to be added
    */
    public static void addProjectContentFactory(ProjectContentFactory pcf) {
        factories.add(pcf);
    }

    /** Remove the factory.
    * @param pcf - factory to be removed
    */
    public static void removeProjectContentFactory(ProjectContentFactory pcf) {
        factories.remove(pcf);
    }

    /** Get default project factory.
    * @return default factory
    */
    public static ProjectContentFactory getProjectContentFactory() {
        return (ProjectContentFactory) factories.get(0);
    }

    /** Get a factory by class.
    * @return the factory or null if there is no factory of given class
    */
    public static ProjectContentFactory getProjectContentFactory(Class clazz) {
        for(int i = 0; i < factories.size(); i++) {
            Object obj = factories.get(i);
            if (obj.getClass().equals(clazz)) return (ProjectContentFactory)obj;
        }
        return null;
    }

    /** Get all factories.
    * @return array of all factories
    */
    public static ProjectContentFactory[] getFactories() {
        int len = factories.size();
        return (ProjectContentFactory[]) factories.toArray(new ProjectContentFactory[len]);
    }

    /** Get a factory by class.
    * @return collection of all factories
    */
    public static Collection getFactoriesCollection() {
        return factories;
    }
}