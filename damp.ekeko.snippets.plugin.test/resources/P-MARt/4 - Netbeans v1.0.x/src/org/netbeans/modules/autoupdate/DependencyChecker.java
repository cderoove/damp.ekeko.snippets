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

package org.netbeans.modules.autoupdate;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.openide.modules.ModuleDescription;
import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;

/** Support class for checking dependencies between modules
 *
 * @author  phrebejk
 * @version 
 */
class DependencyChecker extends Object {

    /** The ResourceBundle */
    private static final ResourceBundle bundle = NbBundle.getBundle( DependencyChecker.class );

    /** Holds modules available for update */
    Updates updates;


    /** DependencyChecker is a singleton */
    DependencyChecker( Updates updates ) {
        this.updates = updates;
    }

    /** Gets collection of modules which have to be added to download list
     * if we have to add the toAddModule.
     */
    Collection modulesToAdd( ModuleUpdate toAdd ) {
        Collection result = new ArrayList();
        checkDependencies( toAdd.getRemoteModule(), result );
        return result;
    }

    /** Gets collection of modules which we have to remove from download list
     * if we have to remove the toRemove module.
     */
    Collection modulesToRemove( ModuleUpdate toRemove ) {
        Collection result = new ArrayList();
        checkReverseDependencies( toRemove.getRemoteModule(), result );
        return result;
    }

    /** Builds Collection with modules which should be added
     * into download list to satisfy module dependencies.
     */
    boolean checkDependencies( ModuleDescription md, Collection result ) {

        // Get all module dependencies
        ModuleDescription.Dependency[] deps = md.getDependencies();
        // Array values say if the dependency is satisfied or not
        boolean[] satisfied = new boolean [ deps.length ];

        // All installed modules
        ModuleDescription[] installedModules = Updates.getInstalledModules();
        ModuleDescription[] installedPatches = Updates.getInstalledPatches();

        // For all dependencies
        for ( int j = 0; j < deps.length; j++ ) {

            String message = null;

            // The module depends on other module
            if ( deps[j].getType() == ModuleDescription.Dependency.TYPE_MODULE ) {

                boolean ok = false;

                // Try to figure out if the dependency is satisfied by installed modules
                for (int i = 0; i < installedModules.length; i++) {
                    ok = checkModuleDependency ( deps[j], installedModules[i] );
                    if ( ok )
                        break;
                }

                if ( !ok ) {
                    // Try to figure out if the dependency is satisfied by installed patches
                    for (int i = 0; i < installedPatches.length; i++) {
                        ok = checkModuleDependency ( deps[j], installedPatches[i] );
                        if ( ok )
                            break;
                    }
                }
                
                // Dependency was not satisfied by other module let's try modules
                // available for download
                if ( !ok ) {

                    Collection availableModules = updates.getModules();
                    Iterator it = updates.getModules().iterator();
                    while ( it.hasNext() ) {
                        ModuleUpdate mu = (ModuleUpdate)it.next();

                        ok = checkModuleDependency ( deps[j], mu.getRemoteModule() );

                        if ( ok ) {
                            if ( !result.contains( mu ) ) {
                                result.add( mu );
                                //checkDependencies( mu.getRemoteModule(), result );
                            }
                            break;
                        }
                    }
                }

                if ( !ok )
                    satisfied[j] = false;
                else
                    satisfied[j] = true;
            }
            // Module depends on specific version of IDE
            else if ( deps[j].getType() == ModuleDescription.Dependency.TYPE_IDE ) {
                // Try to figure out if the dependency is satisfied by installed ide
                if (  checkIdeDependency ( deps[j], IdeDescription.getIdeDescription() ) ) {
                    satisfied[j] = true;
                }
                else {
                    // Try to find suitable IDE between the modules
                    Collection availableModules = updates.getModules();
                    Iterator it = updates.getModules().iterator();
                    boolean ok = false;
                    while ( it.hasNext() ) {
                        ModuleUpdate mu = (ModuleUpdate)it.next();

                        ok = checkModuleDependency ( deps[j], mu.getRemoteModule() );

                        if ( ok ) {
                            if ( !result.contains( mu ) ) {
                                result.add( mu );
                                //checkDependencies( mu.getRemoteModule(), result );
                            }
                            break;
                        }
                    }
                    satisfied[j] = ok;
                }
            }
        }


        StringBuffer sb = new StringBuffer( 200 );
        sb.append( bundle.getString( "MSG_NotSatisfied" ) + "MODULE :" + md.getCodeName() );

        int notSatisfied = 0;

        // For all dependencies
        for ( int j = 0; j < deps.length; j++ ) {
            if ( !satisfied[j] ) {
                sb.append( deps[j] );
                notSatisfied++;
            }
        }

        if ( notSatisfied == 0 )
            return true;

        NotifyDescriptor.Message nd = new NotifyDescriptor.Message(
                                          sb.toString(),
                                          NotifyDescriptor.ERROR_MESSAGE );

        TopManager.getDefault().notify( nd );

        return false;
    }


    /** Builds Collection with modules which should be removed
     * from download list to satisfy module dependencies.
     */
    boolean checkReverseDependencies( ModuleDescription module, Collection result ) {

        //ArrayList dependentModules = new ArrayList();
        ModuleDescription[] installedModules = Updates.getInstalledModules();
        ModuleDescription[] installedPatches = Updates.getInstalledPatches();

        // All listed modules
        Collection availableModules = updates.getModules();
        Iterator it = updates.getModules().iterator();
        while ( it.hasNext() ) {
            ModuleUpdate mu = (ModuleUpdate)it.next();


            //if ( !info.update() ) We have to check all modules
            //  continue;

            ModuleDescription md = mu.getRemoteModule();
            ModuleDescription.Dependency[] deps = md.getDependencies();

            // All dependencies of module
            boolean moduleOk = true;
            for ( int j = 0; j < deps.length; j++ ) {



                if ( deps[j].getType() == ModuleDescription.Dependency.TYPE_MODULE &&
                        deps[j].getName().equals( module.getCodeName() ) ) {

                    boolean ok = false;

                    // Check if not satisfied by installed modules
                    for (int k = 0; k < installedModules.length; k++) {
                        ok = checkModuleDependency ( deps[j], installedModules[k] );
                        if ( ok )
                            break;
                    }

                    // Check if it is not stisfied by installed patches
                    for (int k = 0; k < installedPatches.length; k++) {
                        ok = checkModuleDependency ( deps[j], installedPatches[k] );
                        if ( ok )
                            break;
                    }


                    // The module has unsatisfied dependency we have to remove it
                    if ( !ok ) {
                        moduleOk = false;
                        break;
                    }
                }
                else if ( deps[j].getType() == ModuleDescription.Dependency.TYPE_IDE &&
                          deps[j].getName().equals( module.getCodeName() ) ) {

                    //Check if not satisfied by installed IDE
                    if ( !checkModuleDependency ( deps[j], IdeDescription.getIdeDescription() ) ) {
                        moduleOk = false;
                        break;
                    }
                }
            }

            if ( !moduleOk ) {
                if ( ! result.contains( mu ) ) {
                    result.add( mu );
                    //checkReverseDependencies( mu.getRemoteModule(), result );
                }
            }
        }

        if ( result.size() == 0 )
            return true;
        else {

            /*
            StringBuffer sb = new StringBuffer( dependentModules.size() * 60 + 100);
            sb.append( bundle.getString( "MSG_DependentModules" ) );
            for( int i = 0; i < dependentModules.size(); i++ )
              sb.append( ((ModuleAutoUpdater.ModuleUpdateInformation)dependentModules.get( i )).getName() ).append( "\n" );

            NotifyDescriptor.Confirmation nd = new NotifyDescriptor.Confirmation(
              sb.toString(),
              bundle.getString( "CTL_DependentModules" ),
              NotifyDescriptor.OK_OPTION );

            TopManager.getDefault().notify( nd );

            if( nd.getValue().equals( NotifyDescriptor.YES_OPTION ) ) {
             for( int i = 0; i < dependentModules.size(); i++ )
              ((ModuleAutoUpdater.ModuleUpdateInformation)dependentModules.get( i )).setUpdate( false ); 
             return true; 
        }
            else
            */
            return false;

        }
    }


    /** Tests if the dependency on module is satisfied by the otherModule
     */
    boolean checkModuleDependency ( ModuleDescription.Dependency dep,
                                    ModuleDescription otherModule ) {

        boolean satisfied = false;

        if ( dep.getName().equals (otherModule.getCodeName ())) {
            if ( dep.getComparison() ==  ModuleDescription.Dependency.COMPARE_ANY) {
                return true;
            }
            else if (dep.getComparison() ==  ModuleDescription.Dependency.COMPARE_SPEC) {
                try {
                    if (otherModule.getSpecVersion () == null)
                        return false;
                    else if (! ModuleDescription.compatibleWith ( dep.getVersion(), otherModule.getSpecVersion ()))
                        return false;
                    else
                        return true;
                }
                catch ( org.openide.modules.IllegalModuleException e ) {
                    return false;
                }
            }
            else {
                // COMPARE_IMPL
                if (otherModule.getImplVersion () == null)
                    return false;
                else if (! otherModule.getImplVersion ().equals (dep.getVersion()))
                    return false;
                else
                    return true;
            }
        }
        return false;
    }


    /** Tests dependency on the IDE */
    boolean checkIdeDependency( ModuleDescription.Dependency dep,
                                ModuleDescription ide ) {

        String IDEName = ide.getCodeName();
        String IDESpecVersion = ide.getSpecVersion();
        String IDEImplVersion = ide.getImplVersion();

        // Not equal names
        if ( !IDEName.equals ( dep.getName() ) )
            return false;
        //return ModuleDescription.getStringFormatted ("MSG_IDE_Name", name, IDEName); // NOI18N

        try {
            if ( dep.getComparison() == ModuleDescription.Dependency.COMPARE_SPEC ) {
                return ModuleDescription.compatibleWith (dep.getVersion(), IDESpecVersion);
                // ? null : ModuleDescription.getStringFormatted ("MSG_IDE_Spec", version, IDESpecVersion); // NOI18N
            }
            else if ( dep.getComparison() == ModuleDescription.Dependency.COMPARE_IMPL ) {
                return dep.getVersion().equals (IDEImplVersion);
                // ? null : ModuleDescription.getStringFormatted ("MSG_IDE_Impl", version, IDEImplVersion); // NOI18N
            }
            else {
                // COMPARE_ANY
                return true;
            }
        }
        catch ( org.openide.modules.IllegalModuleException e ) {
            return false;
        }
    }


}
/*
 * Log
 *  7    Gandalf   1.6         1/13/00  Petr Hrebejk    i18 mk3
 *  6    Gandalf   1.5         12/20/99 Petr Hrebejk    Autocheck & security 
 *       finished
 *  5    Gandalf   1.4         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/8/99  Petr Hrebejk    Next development version
 *  3    Gandalf   1.2         10/8/99  Petr Hrebejk    Next Develop version
 *  2    Gandalf   1.1         10/7/99  Petr Hrebejk    Next development version
 *  1    Gandalf   1.0         10/7/99  Petr Hrebejk    
 * $
 */
