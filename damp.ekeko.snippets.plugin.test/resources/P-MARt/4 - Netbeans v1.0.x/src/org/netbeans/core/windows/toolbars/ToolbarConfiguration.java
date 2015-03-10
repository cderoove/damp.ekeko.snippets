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

package org.netbeans.core.windows.toolbars;

import java.io.*;
import java.util.*;
import java.text.*;
import java.awt.Window;
import java.awt.Container;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import javax.swing.*;

import org.openide.util.NbBundle;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileLock;
import org.openide.awt.JPopupMenuPlus;
import org.openide.awt.Toolbar;
import org.openide.awt.ToolbarPool;
import org.openide.loaders.XMLDataObject;

import org.netbeans.core.windows.WorkspaceImpl;

import org.w3c.dom.*;

/** Toolbar configuration */
public class ToolbarConfiguration extends Object implements ToolbarPool.Configuration, Toolbar.DnDListener {
    protected static final String TOOLBAR_DTD               = "org/netbeans/core/windows/toolbars/toolbar.dtd"; // NOI18N
    protected static final String TOOLBAR_DTD_PUBLIC_ID     = "-//Forte for Java//DTD toolbar//EN"; // NOI18N
    protected static final String TOOLBAR_DTD_PUBLIC_ID_OLD = "-//NetBeans IDE//DTD toolbar//EN"; // NOI18N

    protected static final Class  TOOLBAR_PROCESSOR_CLASS   = org.netbeans.core.windows.toolbars.ToolbarProcessor.class;
    protected static final String TOOLBAR_ICON_BASE         = "/org/netbeans/core/windows/toolbars/xmlToolbars"; // NOI18N

    protected static final String EXT_XML                   = "xml"; // NOI18N
    protected static final String EXT_XMLINFO               = "xmlinfo"; // NOI18N

    protected static final int    BASIC_HEIGHT_2            = (Toolbar.BASIC_HEIGHT/2) + 2;
    protected static final int    BASIC_HEIGHT_4            = (Toolbar.BASIC_HEIGHT/4) + 1;

    static {
        XMLDataObject.registerCatalogEntry (TOOLBAR_DTD_PUBLIC_ID, TOOLBAR_DTD,
                                            ClassLoader.getSystemClassLoader());
        XMLDataObject.registerCatalogEntry (TOOLBAR_DTD_PUBLIC_ID_OLD, TOOLBAR_DTD,
                                            ClassLoader.getSystemClassLoader());
    }

    protected static final String TAG_CONFIG                = "Configuration"; // NOI18N
    protected static final String TAG_ROW                   = "Row"; // NOI18N
    protected static final String TAG_TOOLBAR               = "Toolbar"; // NOI18N
    protected static final String ATT_TOOLBAR_NAME          = "name"; // NOI18N
    protected static final String ATT_TOOLBAR_POSITION      = "position"; // NOI18N
    protected static final String ATT_TOOLBAR_VISIBLE       = "visible"; // NOI18N

    /** standard panel for all configurations */
    private static ToolbarPanel  toolbarPanel = new ToolbarPanel();
    private static ToolbarPool   toolbarPool;
    private        Window        toolbarPoolWindow;
    private        ToolbarLayout toolbarLayout = new ToolbarLayout (this);
    private static XMLDataObject.Info xmlinfo;
    private        ToolbarConstraints draggedToolbar;

    private WeakHashMap allToolbars;

    /**
     * @associates ToolbarRow 
     */
    private Vector      toolbarRows;
    private String      configName;
    private int         prefWidth;

    /**
     * @associates Integer 
     */
    private HashMap     invisibleToolbars;

    public static final ResourceBundle bundle = NbBundle.getBundle (ToolbarConfiguration.class);

    public ToolbarConfiguration (String name) {
        allToolbars = new WeakHashMap();
        toolbarRows = new Vector();
        configName = name;
        invisibleToolbars = new HashMap();
        draggedToolbar = null;
    }

    public ToolbarConfiguration (String name, Document doc) {
        this (name);
        parseDocument (doc);
    }


    // IO operations

    void parseDocument (Document doc) {
        Element rE = doc.getDocumentElement();
        if (!TAG_CONFIG.equals (rE.getTagName()))
            return;
        readConfiguration (rE);
        checkToolbarRows();
    }

    //// reading

    void readConfiguration (Element conf) {
        NodeList nL = conf.getElementsByTagName (TAG_ROW);
        toolbarRows = new Vector();
        for (int i = 0; i < nL.getLength(); i++) {
            Node node = nL.item (i);
            readRow ((Element)node);
        }
    }

    void readRow (Element row) {
        ToolbarRow tbRow = new ToolbarRow (this);
        addRow (tbRow);

        NodeList nL = row.getElementsByTagName (TAG_TOOLBAR);
        for (int i = 0; i < nL.getLength(); i++) {
            Node node = nL.item (i);
            addToolbar (tbRow, readToolbar ((Element)node));
        }
    }

    ToolbarConstraints readToolbar (Element tb) {
        String  name;
        String  posStr;
        String  visStr;
        Integer pos;
        Boolean vis;

        name = tb.getAttribute (ATT_TOOLBAR_NAME);
        if (name.length() == 0)
            return null;

        posStr = tb.getAttribute (ATT_TOOLBAR_POSITION);
        if (posStr.length() == 0)
            pos = null;
        else
            pos = new Integer (posStr);

        visStr = tb.getAttribute (ATT_TOOLBAR_VISIBLE);
        if (visStr.length() == 0)
            vis = Boolean.TRUE;
        else
            vis = new Boolean (visStr);

        return checkToolbarConstraints (name, pos, vis);
    }

    //// writting

    boolean tryWriteDocument (String cn) throws IOException {
        final FileObject tbFO = TopManager.getDefault().getPlaces().folders().toolbars().getPrimaryFile();
        final FileSystem tbFS = tbFO.getFileSystem();

        FileObject newFO = tbFS.find (tbFO.getName(), cn, EXT_XML);
        if (newFO != null) {
            NotifyDescriptor replaceD = new NotifyDescriptor.Confirmation
                                        (MessageFormat.format (bundle.getString ("MSG_replaceConfiguration"),
                                                               new String [] { cn }),
                                         NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.WARNING_MESSAGE);
            TopManager.getDefault().notify (replaceD);
            if (replaceD.getValue() != DialogDescriptor.OK_OPTION) {
                return false;
            }
        }
        writeDocument (cn);
        return true;
    }

    public void writeDocument () throws IOException {
        writeDocument (configName);
    }

    private void writeDocument (final String cn) throws IOException {
        WritableToolbarConfiguration wtc = new WritableToolbarConfiguration (toolbarRows, invisibleToolbars);
        final StringBuffer sb = new StringBuffer ("<?xml version=\"1.0\"?>\n\n"); // NOI18N
        sb.append ("<!DOCTYPE ").append (TAG_CONFIG).append (" PUBLIC \""). // NOI18N
        append (TOOLBAR_DTD_PUBLIC_ID).append ("\" \"\">\n\n").append (wtc.toString()); // NOI18N

        final FileObject tbFO = TopManager.getDefault().getPlaces().folders().toolbars().getPrimaryFile();
        final FileSystem tbFS = tbFO.getFileSystem();

        tbFS.runAtomicAction (new FileSystem.AtomicAction () {
                                  public void run () throws IOException {
                                      FileLock lock = null;
                                      OutputStream os = null;

                                      FileObject infoFO = tbFS.find (tbFO.getName(), cn, EXT_XMLINFO);
                                      if (infoFO == null)
                                          infoFO = tbFO.createData (cn, EXT_XMLINFO);
                                      try {
                                          lock = infoFO.lock ();
                                          os = infoFO.getOutputStream (lock);

                                          PrintWriter writer = new PrintWriter (os);

                                          getXMLInfo().write (writer);
                                          writer.close();
                                      } finally {
                                          if (os != null)
                                              os.close ();
                                          if (lock != null)
                                              lock.releaseLock ();
                                      }

                                      lock = null;
                                      os = null;

                                      FileObject xmlFO = tbFS.find (tbFO.getName(), cn, EXT_XML);
                                      if (xmlFO == null)
                                          xmlFO = tbFO.createData (cn, EXT_XML);
                                      try {
                                          lock = xmlFO.lock ();
                                          os = xmlFO.getOutputStream (lock);

                                          PrintWriter writer = new PrintWriter (os);
                                          writer.print (sb.toString());
                                          writer.close();
                                      } finally {
                                          if (os != null)
                                              os.close ();
                                          if (lock != null)
                                              lock.releaseLock ();
                                      }
                                  }
                              });
    }

    XMLDataObject.Info getXMLInfo () {
        if (xmlinfo == null) {
            xmlinfo = new XMLDataObject.Info();
            xmlinfo.addProcessorClass (TOOLBAR_PROCESSOR_CLASS);
            xmlinfo.setIconBase (TOOLBAR_ICON_BASE);
        }
        return xmlinfo;
    }


    //

    void reflectChanges () {
        try {
            writeDocument();
        } catch (IOException e) { /* ??? */ }
    }

    void addToolbar (ToolbarRow row, ToolbarConstraints tc) {
        if (tc == null)
            return;

        if (tc.isVisible())
            row.addToolbar (tc);
        else {
            int rI;
            if (row == null)
                rI = toolbarRows.size();
            else
                rI = toolbarRows.indexOf (row);
            invisibleToolbars.put (tc, new Integer (rI));
        }
        allToolbars.put (tc.getName(), tc);
    }

    void removeToolbar (String name) {
        ToolbarConstraints tc = (ToolbarConstraints)allToolbars.remove (name);
        if (tc.destroy())
            checkToolbarRows();
    }

    void addRow (ToolbarRow row) {
        addRow (row, toolbarRows.size());
    }

    void addRow (ToolbarRow row, int index) {
        ToolbarRow prev = null;
        ToolbarRow next = null;
        try {
            prev = (ToolbarRow)toolbarRows.elementAt (index - 1);
        } catch (ArrayIndexOutOfBoundsException e) { }
        try {
            next = (ToolbarRow)toolbarRows.elementAt (index);
        } catch (ArrayIndexOutOfBoundsException e) { }

        if (prev != null)
            prev.setNextRow (row);
        row.setPrevRow (prev);
        row.setNextRow (next);
        if (next != null)
            next.setPrevRow (row);

        toolbarRows.insertElementAt (row, index);
        updateBounds (row);
    }

    void removeRow (ToolbarRow row) {
        ToolbarRow prev = row.getPrevRow();
        ToolbarRow next = row.getNextRow();
        if (prev != null) {
            prev.setNextRow (next);
        }
        if (next != null) {
            next.setPrevRow (prev);
        }
        toolbarRows.removeElement (row);
        updateBounds (next);
        revalidateWindow();
    }

    void updateBounds (ToolbarRow row) {
        while (row != null) {
            row.updateBounds();
            row = row.getNextRow();
        }
    }

    void revalidateWindow () {
        if (toolbarPool == null)
            updateToolbarPool();

        toolbarPanel.revalidate();

        java.awt.Window w = javax.swing.SwingUtilities.windowForComponent (toolbarPool);
        if (w != null) {
            w.validate ();
        }
    }

    int rowIndex (ToolbarRow row) {
        return toolbarRows.indexOf (row);
    }

    void updatePrefWidth () {
        Iterator it = toolbarRows.iterator();
        prefWidth = 0;
        int tryPrefWidth;
        while (it.hasNext()) {
            prefWidth = Math.max (prefWidth, ((ToolbarRow)it.next()).getPrefWidth());
        }
    }

    int getPrefWidth () {
        return prefWidth;
    }

    int getPrefHeight () {
        double rowCount = getRowCount();
        if (rowCount == 0)
            rowCount = 0.25;
        return (ToolbarLayout.VGAP + (int)((ToolbarLayout.VGAP + Toolbar.BASIC_HEIGHT) * rowCount));
    }

    void checkToolbarRows () {
        Object[] rows = toolbarRows.toArray();
        ToolbarRow row;

        for (int i = rows.length - 1; i >= 0; i--) {
            row = (ToolbarRow)rows[i];
            if (row.isEmpty())
                removeRow (row);
        }
    }

    int getRowCount () {
        return toolbarRows.size();
    }

    ToolbarConstraints checkToolbarConstraints (String name, Integer position, Boolean visible) {
        ToolbarConstraints tc = (ToolbarConstraints)allToolbars.get (name);
        if (tc == null)
            tc = new ToolbarConstraints (this, name, position, visible);
        else
            tc.checkNextPosition (position, visible);
        return tc;
    }

    void checkConfigurationOver () {
        Object[] names = allToolbars.keySet().toArray();
        String name;
        for (int i = 0; i < names.length; i++) {
            name = (String)names[i];
            if (toolbarPool.findToolbar (name) == null)
                removeToolbar (name);
        }
    }

    void removeVisible (ToolbarConstraints tc) {
        invisibleToolbars.put (tc, new Integer (tc.rowIndex()));
        if (tc.destroy())
            checkToolbarRows();
        tc.setVisible (false);

        reflectChanges();
    }

    void addInvisible (ToolbarConstraints tc) {
        int rC = toolbarRows.size();
        int pos = ((Integer)invisibleToolbars.remove (tc)).intValue();
        tc.setVisible (true);
        for (int i = pos; i < pos + tc.getRowCount(); i++) {
            getRow (i).addToolbar (tc, tc.getPosition());
        }

        if (rC != toolbarRows.size())
            revalidateWindow();
        reflectChanges();
    }

    ToolbarRow getRow (int rI) {
        ToolbarRow row;
        int s = toolbarRows.size();
        if (rI < 0) {
            row = new ToolbarRow (this);
            addRow (row, 0);
        } else if (rI >= s) {
            row = new ToolbarRow (this);
            addRow (row);
        } else {
            row = (ToolbarRow)toolbarRows.elementAt (rI);
        }
        return row;
    }

    ToolbarRow createLastRow () {
        return getRow (toolbarRows.size());
    }

    void reactivatePanel () {
        toolbarPanel.removeAll();
        prefWidth = 0;

        Toolbar tbs[] = toolbarPool.getToolbars();
        Toolbar tb;
        ToolbarConstraints tc;
        String name;

        for (int i = 0; i < tbs.length; i++) {
            tb = tbs[i];
            name = tb.getName();
            tc = (ToolbarConstraints)allToolbars.get (name);
            if (tc == null) {
                tc = new ToolbarConstraints (this, name, null, Boolean.FALSE);
                addToolbar (null, tc);
            }
            toolbarPanel.add (tb, tc);
        }
    }

    void updateToolbarPool () {
        if (toolbarPoolWindow == null) {
            toolbarPool = ToolbarPool.getDefault();
            toolbarPoolWindow = javax.swing.SwingUtilities.windowForComponent (toolbarPool);
        }
    }

    // from ToolbarPool.Configuration

    /** Activates the configuration and returns right
     * component that can display the configuration.
     * @return representation component
     */
    public Component activate () {
        if (toolbarPool == null)
            updateToolbarPool();
        toolbarPool.setToolbarsListener (this);

        checkConfigurationOver();
        toolbarPanel.setLayout (toolbarLayout);
        reactivatePanel();

        return toolbarPanel;
    }

    /** Name of the configuration.
     * @return the name
     */
    public String getName () {
        return configName;
    }

    /** Popup menu that should be displayed when the users presses
     * right mouse button on the panel. This menu can contain
     * contains list of possible configurations, additional actions, etc.
     *
     * @return popup menu to be displayed
     */
    public JPopupMenu getContextMenu () {
        JPopupMenu menu = new JPopupMenuPlus ();

        // generate list of available toolbars
        Iterator it = Arrays.asList (ToolbarPool.getDefault ().getToolbars ()).iterator ();
        while (it.hasNext()) {
            final Toolbar tb = (Toolbar)it.next();
            String name = tb.getName();
            final ToolbarConstraints tc = (ToolbarConstraints)allToolbars.get (name);

            JCheckBoxMenuItem mi = new JCheckBoxMenuItem (name, tc.isVisible());
            mi.addActionListener (new ActionListener () {
                                      public void actionPerformed (ActionEvent ae) {
                                          boolean wasVisible = tc.isVisible();
                                          if (wasVisible) {
                                              removeVisible (tc);
                                              tb.setVisible (false);
                                          } else {
                                              addInvisible (tc);
                                              tb.setVisible (true);
                                          }
                                      }
                                  });
            menu.add (mi);
        }
        menu.add (new JPopupMenu.Separator());
        // generate list of available toolbar panels
        it = Arrays.asList (ToolbarPool.getDefault ().getConfigurations ()).iterator ();
        ButtonGroup bg = new ButtonGroup ();
        String current = ToolbarPool.getDefault ().getConfiguration ();
        while (it.hasNext()) {
            final String name = (String)it.next ();
            JRadioButtonMenuItem mi = new JRadioButtonMenuItem (name, (name.compareTo (current) == 0));
            mi.addActionListener (new ActionListener () {
                                      public void actionPerformed (ActionEvent e) {
                                          WorkspaceImpl curWs = (WorkspaceImpl)TopManager.getDefault().
                                                                getWindowManager().getCurrentWorkspace();
                                          curWs.setToolbarConfigName (name);
                                      }
                                  });
            bg.add (mi);
            menu.add (mi);
        }
        menu.add (new JPopupMenu.Separator());
        JMenuItem mi = new JMenuItem (bundle.getString ("PROP_saveAs"));
        mi.addActionListener (new ActionListener () {
                                  public void actionPerformed (ActionEvent e) {
                                      NotifyDescriptor.InputLine il = new NotifyDescriptor.InputLine
                                                                      (bundle.getString ("PROP_saveLabel"),
                                                                       bundle.getString ("PROP_saveDialog"));
                                      il.setInputText (bundle.getString ("PROP_saveName"));

                                      Object ok = TopManager.getDefault ().notify (il);
                                      if (ok == NotifyDescriptor.OK_OPTION) {
                                          String s = il.getInputText();
                                          if (s.length() != 0) {
                                              try {
                                                  String newName = il.getInputText();
                                                  if (tryWriteDocument (newName)) {
                                                      WorkspaceImpl curWs = (WorkspaceImpl)TopManager.getDefault().
                                                                            getWindowManager().getCurrentWorkspace();
                                                      curWs.setToolbarConfigName (newName);
                                                  }
                                              } catch (IOException ioe) {
                                                  TopManager.getDefault ().notifyException (ioe);
                                              }
                                          }
                                      }
                                  }
                              });
        menu.add (mi);

        //      menu.add (new JPopupMenu.Separator());
        //      mi = new JMenuItem ("Test"); // NOI18N
        //      mi.addActionListener (new ActionListener () {
        //        public void actionPerformed (ActionEvent e) {
        //  	testPrinting();
        //        }
        //      });
        //      menu.add (mi);

        return menu;
    } // getContextMenu


    //    void testPrinting () {
    //      System.out.println ("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv"); // NOI18N
    //      System.out.println ("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv"); // NOI18N
    //      System.out.println ("vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv"); // NOI18N
    //      System.out.println (">>> ToolbarConfiguration [" + super.toString() + "]: " + configName); // NOI18N
    //      System.out.println ("  Toolbar Rows, size = " + toolbarRows.size()); // NOI18N
    //      for (int i = 0; i < toolbarRows.size(); i++) {
    //        System.out.print ("  # row [" + i + "] = "); // NOI18N
    //        ((ToolbarRow)toolbarRows.elementAt (i)).testPrinting();
    //      }
    //      System.out.println ("  Invisible Toolbars, size = " + invisibleToolbars.size()); // NOI18N
    //      Iterator it = invisibleToolbars.keySet().iterator();
    //      int i = 0;
    //      while (it.hasNext()) {
    //        System.out.print ("  # toolbar [" + (i++) + "] = "); // NOI18N
    //        ((ToolbarConstraints)it.next()).testPrinting();
    //      }
    //      System.out.println ("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"); // NOI18N
    //      System.out.println ("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"); // NOI18N
    //      System.out.println ("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^"); // NOI18N
    //    }


    void moveToolbar2EndHorizontally (ToolbarConstraints tc, int dx) {
        if (dx == 0) // no move
            return;
        if (dx < 0)
            tc.moveLeft2End (-dx);
        if (dx > 0)
            tc.moveRight2End (dx);
    }

    void moveToolbarHorizontally (ToolbarConstraints tc, int dx) {
        if (dx == 0) // no move
            return;
        if (dx < 0)
            tc.moveLeft (-dx);
        if (dx > 0)
            tc.moveRight (dx);
    }

    void moveToolbarVertically (ToolbarConstraints tc, int dy) {
        if (dy == 0) // no move
            return;

        if (dy < 0)
            moveUp (tc, -dy);
        if (dy > 0)
            moveDown (tc, dy);
    }

    void moveUp (ToolbarConstraints tc, int dy) {
        if (dy < BASIC_HEIGHT_2)
            return;

        int rI = tc.rowIndex();
        if (draggedToolbar.isAlone()) { // is alone on row(s) -> no new rows
            if (rI == 0) // in first row
                return;
        }

        int pos = rI - 1;
        tc.destroy();

        int plus = 0;
        int rowCount = getRowCount();
        for (int i = pos; i < pos + tc.getRowCount(); i++) {
            getRow (i + plus).addToolbar (tc, tc.getPosition());
            if (rowCount != getRowCount()) {
                rowCount = getRowCount();
                plus++;
            }
        }
        checkToolbarRows();
    }

    void moveDown (ToolbarConstraints tc, int dy) {
        int rI = tc.rowIndex();

        int step = BASIC_HEIGHT_2;

        if (draggedToolbar.isAlone()) { // is alone on row(s) -> no new rows
            if (rI == (toolbarRows.size() - tc.getRowCount())) // in last rows
                return;
            step = BASIC_HEIGHT_4;
        }

        if (dy < step)
            return;

        int pos = rI + 1;
        tc.destroy();

        for (int i = pos; i < pos + tc.getRowCount(); i++)
            getRow (i).addToolbar (tc, tc.getPosition());

        checkToolbarRows();
    }

    // from Toolbar.DnDListener

    public void dragToolbar (Toolbar.DnDEvent e) {
        if (draggedToolbar == null) {
            draggedToolbar = (ToolbarConstraints)allToolbars.get (e.name);
        }

        switch (e.type) {
        case Toolbar.DnDEvent.DND_LINE:
            // not implemented yet - it's bug [1]
            // not implemented int this version
            return; // only Toolbar.DnDEvent.DND_LINE
        case Toolbar.DnDEvent.DND_END:
            moveToolbar2EndHorizontally (draggedToolbar, e.dx);
            break;
        case Toolbar.DnDEvent.DND_ONE:
            moveToolbarVertically (draggedToolbar, e.dy);
            break;
        }
        if (e.type == Toolbar.DnDEvent.DND_ONE)
            moveToolbarHorizontally (draggedToolbar, e.dx);

        draggedToolbar.updatePosition();

        revalidateWindow();
    }

    public void dropToolbar (Toolbar.DnDEvent e) {
        dragToolbar (e);

        reflectChanges();
        draggedToolbar = null;
    }


    // writable classes

    // class WritableToolbarConfiguration
    class WritableToolbarConfiguration {
        /**
         * @associates WritableToolbarRow 
         */
        Vector rows;

        public WritableToolbarConfiguration (Vector rs, HashMap iv) {
            initRows (rs);
            initInvisible (iv);
            removeEmptyRows();
        }

        void initRows (Vector rs) {
            rows = new Vector();

            Iterator it = rs.iterator();
            while (it.hasNext()) {
                rows.addElement (new ToolbarRow.WritableToolbarRow ((ToolbarRow)it.next()));
            }
        }

        void initInvisible (HashMap iv) {
            Iterator it = iv.keySet().iterator();
            ToolbarConstraints tc;
            int row;
            while (it.hasNext()) {
                tc = (ToolbarConstraints)it.next();
                row = ((Integer)iv.get (tc)).intValue();
                for (int i = row; i < row + tc.getRowCount(); i++) {
                    getRow (i).addToolbar (tc);
                }
            }
        }

        void removeEmptyRows () {
            ToolbarRow.WritableToolbarRow row;
            for (int i = rows.size() - 1; i >= 0; i--) {
                row = (ToolbarRow.WritableToolbarRow)rows.elementAt (i);
                if (row.isEmpty())
                    rows.removeElement (row);
            }
        }

        ToolbarRow.WritableToolbarRow getRow (int r) {
            try {
                return (ToolbarRow.WritableToolbarRow)rows.elementAt (r);
            } catch (ArrayIndexOutOfBoundsException e) {
                rows.addElement (new ToolbarRow.WritableToolbarRow ());
                return getRow (r);
            }
        }

        public String toString () {
            StringBuffer sb = new StringBuffer();

            sb.append ("<").append (TAG_CONFIG).append (">\n"); // NOI18N
            Iterator it = rows.iterator();
            while (it.hasNext()) {
                sb.append (it.next().toString());
            }
            sb.append ("</").append (TAG_CONFIG).append (">\n"); // NOI18N

            return sb.toString();
        }
    } // end of class WritableToolbarConfiguration
} // end of class Configuration

/*
 * Log
 *  16   Gandalf   1.15        3/11/00  Martin Ryzl     menufix [by E.Adams, 
 *       I.Formanek]
 *  15   Gandalf   1.14        1/20/00  Libor Kramolis  
 *  14   Gandalf   1.13        1/20/00  Libor Kramolis  
 *  13   Gandalf   1.12        1/19/00  Libor Kramolis  
 *  12   Gandalf   1.11        1/16/00  Libor Kramolis  
 *  11   Gandalf   1.10        1/16/00  Libor Kramolis  
 *  10   Gandalf   1.9         1/13/00  David Simonek   localization
 *  9    Gandalf   1.8         12/13/99 Libor Kramolis  
 *  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         9/30/99  Libor Kramolis  
 *  6    Gandalf   1.5         9/29/99  Libor Kramolis  
 *  5    Gandalf   1.4         8/16/99  Ian Formanek    Fixed 100% CPU problem 
 *       (with each toolbar switch some Vector doubled in size), hopefully 
 *       correctly
 *  4    Gandalf   1.3         8/3/99   Libor Kramolis  
 *  3    Gandalf   1.2         8/3/99   Libor Kramolis  
 *  2    Gandalf   1.1         7/30/99  Libor Kramolis  
 *  1    Gandalf   1.0         7/11/99  David Simonek   
 * $
 */
