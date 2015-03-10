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

package org.netbeans.modules.antlr.nodes;

import org.openide.nodes.*;
import org.openide.loaders.DataNode;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.util.actions.*;
import org.openide.actions.OpenAction;

import org.netbeans.modules.java.JavaDataObject;
import org.netbeans.modules.java.JavaNode;

import org.netbeans.modules.antlr.*;

/**
 *
 * @author  jleppanen
 * @version 
 */
public class GNode extends DataNode {
    /** Icon base for the GNode node */
    private static final String G_ICON_BASE =
        "org/netbeans/modules/antlr/gObject";
    GDataObject obj;

    /** Creates new GNode */
    public GNode(GDataObject obj) {
        super(obj, new Children.Array());
        setIconBase(G_ICON_BASE);
        this.obj = obj;
        addNodesForSecondaryEntries();
    }

    Node createNode(MultiDataObject.Entry entry) {
        FileObject fo = entry.getFile();
        Node n = null;
        if (fo.hasExt("html")) {
            n = new GHtmlNode(entry);
        } else if (fo.hasExt("txt")) {
            n = new GTxtNode(entry);
        } else if (fo.hasExt("java")) {
            try {
                JavaDataObject o = new JavaDataObject(entry.getFile(),obj.getMultiFileLoader());
                n = new JavaNode(o);
            } catch (org.openide.loaders.DataObjectExistsException e) {
            }
            //n = new GJavaNode(entry);
        } else {
            n = new GHtmlNode(entry);
        }
        return n;
    }

    void addNodesForSecondaryEntries() {
        java.util.Set secondaryEntries = obj.secondaryEntries();
        java.util.Iterator i = secondaryEntries.iterator();
        while (i.hasNext()) {
            MultiDataObject.Entry entry = (MultiDataObject.Entry)i.next();
            Node n = createNode(entry);
            getChildren().add(new Node[]{ n });
        }
    }

    /** Overrides default action from DataNode.
    * Instantiate a template, if isTemplate() returns true.
    * Opens otherwise.
    */
    public SystemAction getDefaultAction () {
        SystemAction result = super.getDefaultAction();
        return result == null ? SystemAction.get(OpenAction.class) : result;
    }

    /*
    String PROP_SUPERGRAMMARS="";
    protected Sheet createSheet () {
    Sheet sheet = super.createSheet();

    Sheet.Set ps = sheet.get(Sheet.PROPERTIES);*/
    /*
        ps.put(new PropertySupport.ReadWrite (
            PROP_SYNCHRONIZATION_MODE,
            Byte.class,
            Util.getString("PROP_synchMode"),
            Util.getString("HINT_synchMode")
          ) {
          public Object getValue() {
            return new Byte(getJavaDataObject().getSynchronizationType());
          }
          public void setValue (Object val) throws InvocationTargetException {
            if (val instanceof Byte) {
              try {
                getJavaDataObject().setSynchronizationType(((Byte) val).byteValue());
                return;
              }
              catch(IllegalArgumentException e) {
              }
            }
            throw new IllegalArgumentException();
          }
          public PropertyEditor getPropertyEditor() {
            return new SyncModeEditor();
          }
        });

        ps = new Sheet.Set ();
        ps.setName(EXECUTION_SET_NAME);
        ps.setDisplayName(Util.getString("PROP_executionSetName"));
        ps.setShortDescription(Util.getString("HINT_executionSetName"));

        ExecSupport.addProperties(ps, ((MultiDataObject)getDataObject()).getPrimaryEntry());
        
        sheet.put(ps);

        ps = new Sheet.Set ();
        ps.setName(SOURCE_SET_NAME);
        ps.setDisplayName(Util.getString ("PROP_sourceSetName"));
        ps.setShortDescription(Util.getString ("HINT_sourceSetName"));
        ps.put(new PropertySupport.ReadWrite (
            ElementProperties.PROP_PACKAGE,
            String.class,
            Util.getString("PROP_package"),
            Util.getString("HINT_package")
          ) {
          public Object getValue() {
            SourceElement source = getJavaDataObject().getSource();
            switch (source.getStatus()) {
            case SourceElement.STATUS_NOT :
              return "<not parsed yet>";

            case SourceElement.STATUS_OK:
              Identifier id = source.getPackage();
              return (id == null) ? "<default package>" : id.getFullName();
              
            default:
              return "<parsing error>";
            }
          }
          public void setValue (Object val) throws InvocationTargetException {
            if (val instanceof String) {
              try {
                String id = (String) val;
                SourceElement source = getJavaDataObject().getSource();
                source.setPackage(id.equals("") ? null : Identifier.create(id));
              }
              catch(SourceException e) {
                throw new InvocationTargetException(e);
              }
            } else {
              throw new IllegalArgumentException();
            }
          }
        });
        sheet.put(ps);
    */  
    /*    ps = new Sheet.Set ();
        ps.setName("Props");
        ps.setDisplayName("Props");
        ps.setShortDescription("Peopleties");*/

    /*
    ps.put(new PropertySupport.ReadWrite (
        PROP_SUPERGRAMMARS,
        String.class,
        "Supergrammars",
        "Supergrammars finding path"
      )       public Object getValue() {
        SourceElement source = getJavaDataObject().getSource();
        switch (source.getStatus()) {
        case SourceElement.STATUS_NOT :
          return "<not parsed yet>";

        case SourceElement.STATUS_OK:
          Identifier id = source.getPackage();
          return (id == null) ? "<default package>" : id.getFullName();
          
        default:
          return "<parsing error>";
        }
      }
      public void setValue (Object val) throws InvocationTargetException {
        if (val instanceof String) {
          try {
            String id = (String) val;
            SourceElement source = getJavaDataObject().getSource();
            source.setPackage(id.equals("") ? null : Identifier.create(id));
          }
          catch(SourceException e) {
            throw new InvocationTargetException(e);
          }
        } else {
          throw new IllegalArgumentException();
        }
      }
});
    sheet.put(ps);

    return sheet;
}
    */

    protected GDataObject getGDataObject() {
        return (GDataObject) getDataObject();
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
    }

}