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

package org.netbeans.modules.javadoc.search;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Enumeration;
import java.util.ArrayList;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.cookies.FilterCookie;
import org.openide.src.*;
import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataFilter;
import org.openide.util.WeakListener;

/** Implements children for basic source code patterns
 * 
 * @author Petr Hrebejk
 */
public class JavaDocChildren extends Children.Keys implements RepositoryListener {

    private PropertyChangeListener fsPCL = new fsChangeListener();
    //private WeakListener.PropertyChange wFsPCL = new WeakListener.PropertyChange( fsPCL );
    private PropertyChangeListener wFsPCL = WeakListener.propertyChange( fsPCL, null );

    private PropertyChangeListener capabilityPCL = new capChangeListener();
    //private WeakListener.PropertyChange wCapabilityPCL = new WeakListener.PropertyChange( capabilityPCL );
    private PropertyChangeListener wCapabilityPCL = WeakListener.propertyChange( capabilityPCL, null );

    // Constructors -----------------------------------------------------------------------

    /** Create pattern children. The children are initilay unfiltered.
     * @param elemrent the atteached class. For this class we recognize the patterns 
     */ 

    public JavaDocChildren () {
        super();
        Repository repository = TopManager.getDefault().getRepository();

        // Add repository listener
        repository.addRepositoryListener( WeakListener.repository( this, repository ) );

        // Add listeners to all existing file systems
        Enumeration fsEnum = repository.getFileSystems();
        while( fsEnum.hasMoreElements() ) {
            FileSystem fs = (FileSystem)fsEnum.nextElement();
            fs.addPropertyChangeListener( wFsPCL );
            fs.getCapability().addPropertyChangeListener( wCapabilityPCL );
        }
    }

    /** Called when the preparetion of nodes is needed
     */
    protected void addNotify() {
        setKeys ( getDocFileSystems() );
    }

    /** Called when all children are garbage collected */
    protected void removeNotify() {
        setKeys( java.util.Collections.EMPTY_SET );
    }

    // Children.keys implementation -------------------------------------------------------

    /** Creates nodes for given key.
    */
    protected Node[] createNodes( final Object key ) {

        //if ( key instanceof FileSystem )
        Node newNode;

        try {
            newNode = new JavaDocFSNode( key, getFSChildren( (FileSystem)key ) );
            return new Node[] { newNode };
        }
        catch ( java.beans.IntrospectionException e ) {
            // No node will be created
        }

        return new Node[0];
    }

    // Utility methods --------------------------------------------------------------------

    private Collection getDocFileSystems() {

        // System.out.println(" DOC FS " ); // NOI18N
        // Thread.dumpStack();

        Enumeration dfsEnum = FileSystemCapability.DOC.fileSystems();
        ArrayList docFileSystems = new ArrayList();

        while( dfsEnum.hasMoreElements() ) {
            FileSystem fs = (FileSystem) dfsEnum.nextElement();
            // System.out.println(" FS : " + fs ); // NOI18N
            docFileSystems.add( fs );
        }

        return docFileSystems;
    }

    void refreshFs( Object fs ) {
        // System.out.println(" Refrefh FS "  + fs ); // NOI18N
        refreshKey( fs );
    }

    private Children getFSChildren( FileSystem fs ) {
        DataFolder df = DataFolder.findFolder( fs.getRoot() );
        return df.createNodeChildren( DataFilter.ALL );
    }


    // Implementation of repository listener -------------------------------------------------------

    public void	fileSystemAdded(RepositoryEvent ev) {
        FileSystem fs = ev.getFileSystem();
        fs.addPropertyChangeListener( wFsPCL );
        fs.getCapability().addPropertyChangeListener( wCapabilityPCL );
        setKeys ( getDocFileSystems() );
    }

    public void	fileSystemPoolReordered(RepositoryReorderedEvent ev) {
        setKeys ( getDocFileSystems() );
    }

    public void	fileSystemRemoved(RepositoryEvent ev) {
        FileSystem fs = ev.getFileSystem();
        fs.removePropertyChangeListener( wFsPCL );
        fs.getCapability().removePropertyChangeListener( wCapabilityPCL );
        setKeys ( getDocFileSystems() );
    }


    void refreshAll( ) {
        setKeys ( getDocFileSystems() );
    }

    // Innerclass listener to file system's properties & capabilites of the file systems

    class fsChangeListener implements  PropertyChangeListener {
        public void propertyChange( PropertyChangeEvent evt ) {
            if ( evt.getPropertyName().equals( FileSystem.PROP_ROOT ) )
                refreshFs( evt.getSource() );
        }
    }

    class capChangeListener implements  PropertyChangeListener {
        public void propertyChange( PropertyChangeEvent evt ) {
            if ( evt.getPropertyName().equals( "doc" ) )
                refreshAll();
        }
    }
}

/*
 * Log
 *  4    Gandalf   1.3         1/15/00  Petr Hrebejk    New WeakListener 
 *       implementation
 *  3    Gandalf   1.2         1/13/00  Petr Hrebejk    i18n mk3  
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         8/13/99  Petr Hrebejk    
 * $ 
 */ 