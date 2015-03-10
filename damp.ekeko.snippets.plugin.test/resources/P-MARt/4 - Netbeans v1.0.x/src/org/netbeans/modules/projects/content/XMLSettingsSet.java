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

package org.netbeans.modules.projects.content;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;

import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.util.*;
import org.openidex.projects.*;

/**
 *
 * @author  mryzl
 */

public class XMLSettingsSet extends AbstractSettingsSet {

    /** SettingsSet element name. */
    public static final String SETTINGS_SET_ELEMENT = "SettingsSet"; // NOI18N

    FileObject fo;

    /** Creates new XMLSettingsSet.
    */
    public XMLSettingsSet(FileObject folder, String name, String ext, boolean create) throws IOException {
        fo = folder.getFileObject(name, ext);
        if (fo == null) {
            if (create) {
                fo = folder.createData(name, ext);
                write(OptionProcessor.EMPTY);
            } else throw new IOException("null file object"); // NOI18N
        }
    }

    /** Creates new XMLSettingsSet.
    */
    public XMLSettingsSet(FileObject fo) {
        this.fo = fo;
    }

    public void load() throws IOException {
        set.addAll(loadSettingsSet(new HashMap()).keySet());
    }

    /** Write options to storage device. Writing of some options
    * can be prohibited by option processor.
    *
    * If new object is added to the project, it will be written.
    * If an object is removed, it will be deleted.
    * If the object remains in the SettingsSet, processor.canProcess is checked
    * and if true, new value will be written. If false, old value will be used.
    *
    * @param processor - processor that controls writing of an option
    * @return an array of written objects
    */
    public void write(OptionProcessor processor) throws IOException {

        Map map, newMap = new HashMap();

        try {
            map = loadSettingsSet(new HashMap());
        } catch (IOException ex) {
            map = new HashMap();
        }

        // new values should be added
        Iterator it = set.iterator();
        while (it.hasNext()) {
            SharedClassObject sco = (SharedClassObject) it.next();
            if ((map.get(sco) == null) || processor.canProcess(sco)) {
                try {
                    newMap.put(sco, XMLSupport.encodeValue(sco));
                } catch (Exception ex) {
                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
                    IOException ex2 = new IOException(MessageFormat.format(
                                                       NbBundle.getBundle(XMLSettingsSet.class).getString("ERR_OptionSerialization"), // NOI18N
                                                       new Object[] { sco.getClass().getName() }
                                                     ));
                    TopManager.getDefault().notifyException(ex2);
                }
            } else {
                newMap.put(sco, map.get(sco));
            }
        }
        saveSettingsSet(newMap);
    }

    /** Read options from storage device. Reading of some options
    * can be prohibited by option processor.
    *
    * @param processor - processor that controls writing of an option
    * @return an array of read objects
    */
    public void read(OptionProcessor processor) throws IOException {
        Map map = loadSettingsSet(new HashMap());
        // read only intersection of actual set of options and set defined by the OptionProcessor
        Iterator it = set.iterator();
        while (it.hasNext()) {
            SharedClassObject sco = (SharedClassObject) it.next();
            if (processor.canProcess(sco)) {
                try {
                    XMLSupport.decodeValue((String)map.get(sco));
                } catch (Exception ex) {
                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
                    IOException ex2 = new IOException(MessageFormat.format(
                                                        NbBundle.getBundle(XMLSettingsSet.class).getString("ERR_OptionDeserialization"), // NOI18N
                                                        new Object[] { sco.getClass().getName() }
                                                      ));
                    TopManager.getDefault().notifyException(ex2);
                }
            }
        }
    }


    /** Save diff set to the given file object.
    * @param fo file object.
    * @param diffset diffset to be saved.
    */
    protected Map loadSettingsSet(Map map) throws IOException {
        Reader reader = null;
        try {
            reader = new InputStreamReader(fo.getInputStream());
            return XMLSupport.loadObjects(reader, map, SETTINGS_SET_ELEMENT);
        } finally {
            if (reader != null) reader.close();
        }
    }

    /** Load diff set from the given file object.
    * @param fo file object.
    * @param diffset diffset that will be filled up.
    */
    protected void saveSettingsSet(Map map) throws IOException {
        FileLock lock = null;
        Writer writer = null;
        try {
            lock = fo.lock();
            writer = new OutputStreamWriter(fo.getOutputStream(lock));
            XMLSupport.saveObjects(writer, map, SETTINGS_SET_ELEMENT);
        } finally {
            if (lock != null) lock.releaseLock();
            if (writer != null) writer.close();
        }
    }

}

/*
* Log
*  6    Gandalf   1.5         2/4/00   Martin Ryzl     correct handling of wrong
*       XML files
*  5    Gandalf   1.4         1/18/00  Martin Ryzl     
*  4    Gandalf   1.3         1/13/00  Martin Ryzl     
*  3    Gandalf   1.2         1/13/00  Martin Ryzl     heavy localization
*  2    Gandalf   1.1         1/7/00   Martin Ryzl     
*  1    Gandalf   1.0         12/22/99 Martin Ryzl     
* $ 
*/ 
