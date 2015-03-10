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

package org.netbeans.modules.editor.java;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.editor.ext.JCompletion;
import org.netbeans.editor.ext.JCUtilities;
import org.netbeans.editor.ext.JCClass;
import org.netbeans.editor.ext.JCClassProvider;
import org.netbeans.editor.ext.JCFileProvider;
import org.netbeans.editor.ext.JCQuery;
import org.netbeans.editor.ext.JCFinder;
import org.netbeans.editor.ext.JCBaseFinder;
import org.openide.execution.NbClassPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
* Management of Java Completion storage
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JCStorage {

    private static final String DB_DIR = "ParserDB"; // NOI18N
    private static final String JCS_EXT = "jcs"; // NOI18N
    private static final String STORAGE = "storage"; // NOI18N
    private static final String STORAGE_EXT = "ser"; // NOI18N

    private static final String PROVIDER_FILE_EXT
    = JCFileProvider.SKEL_FILE_EXT;

    private static boolean inited;

    private static JCStorage storage;

    private FileObject dbFolder; // parser db folder


    /**
     * @associates JCStorageElement 
     */
    private ArrayList elementList = new ArrayList();


    public static JCStorage getStorage() {
        return storage;
    }

    public static void init(FileObject rootFolder) {
        JCFinder finder = new JCBaseFinder();
        JCompletion.setFinder(finder);
        storage = new JCStorage(rootFolder);
    }

    JCStorage(FileObject rootFolder) {
        checkDBFolder(rootFolder);
        if (dbFolder != null) {
            initElements();
            initProviders();
            initFinder();
        }
    }

    private void checkDBFolder(FileObject rootFolder) {
        if (rootFolder != null) {
            dbFolder = rootFolder.getFileObject(DB_DIR);
            if (dbFolder == null) {
                try {
                    dbFolder = rootFolder.createFolder(DB_DIR);
                } catch (IOException e) {
                    // probably exists or cannot be created
                }
            }
        }
    }

    private File getStorageFile() {
        File storage = null;
        if (dbFolder != null) {
            FileObject storageFO = dbFolder.getFileObject(STORAGE, STORAGE_EXT);
            try {
                if (storageFO == null) {
                    storageFO = dbFolder.createData(STORAGE, STORAGE_EXT);
                }
            } catch (IOException e) {
            }

            if (storageFO != null) {
                storage = NbClassPath.toFile(storageFO);
            }
        }
        return storage;
    }

    private void initElements() {
        try {
            File storage = getStorageFile();
            if (storage != null) {
                if (storage.exists()) {
                    ObjectInputStream is = new ObjectInputStream(new FileInputStream(storage));
                    elementList = (ArrayList)is.readObject();
                    is.close();
                }
            }
        } catch (ClassNotFoundException e) {
        } catch (IOException e) {
        }
    }

    private void initFinder() {
        JCFinder finder = JCompletion.getFinder();
        Iterator i = elementList.iterator();
        while (i.hasNext()) {
            JCStorageElement e = (JCStorageElement)i.next();
            if (e.getProvider() != null) {
                finder.append(e.getProvider());
            }
        }
    }

    public void saveElements() {
        try {
            File storage = getStorageFile();
            if (storage != null) {
                ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(storage));
                os.writeObject(elementList);
                os.close();
            }
        } catch (IOException e) {
        }
    }

    public JCStorageElement getElement(String name) {
        int i = elementList.indexOf(new JCStorageElement(name));
        if (i >= 0) {
            return (JCStorageElement)elementList.get(i);
        } else {
            return null;
        }
    }

    private void initProviders() {
        if (dbFolder != null) {
            File dbDir = NbClassPath.toFile(dbFolder);
            if (dbDir != null) {
                String[] names = dbDir.list(
                                     new FilenameFilter() {
                                         public boolean accept(File dir, String name) {
                                             return name.endsWith(PROVIDER_FILE_EXT);
                                         }
                                     }
                                 );

                for (int i = 0; i < names.length; i++) {
                    String name = names[i].substring(0,
                                                     names[i].length() - PROVIDER_FILE_EXT.length());
                    String prefix = dbDir.getAbsolutePath() + File.separator + name;
                    JCClassProvider provider = new JCFileProvider(prefix);
                    JCStorageElement e = getElement(name);
                    if (e != null) {
                        e.setProvider(provider);
                    } else {
                        e = new JCStorageElement(name);
                        e.setProvider(provider);
                        addElement(e);
                    }
                }
            }
        }
    }

    JCStorageElement findFileSystemElement(String fsName) {
        Iterator i = elementList.iterator();
        while (i.hasNext()) {
            JCStorageElement e = (JCStorageElement)i.next();
            if (fsName.equals(e.getFileSystemName())) {
                return e;
            }
        }
        return null;
    }

    void removeFileSystemElement(String fsName) {
        int cnt = elementList.size();
        for (int i = 0; i < cnt; i++) {
            if (fsName.equals(((JCStorageElement)elementList.get(i)).getFileSystemName())) {
                elementList.remove(i);
                cnt--;
            }
        }
    }

    private void addElement(JCStorageElement e) {
        int i = elementList.indexOf(e);
        if (i >= 0) {
            elementList.set(i, e);
        } else {
            elementList.add(e);
        }
    }

    public JCStorageElement addElement(
        String name, String fsName,
        int classLevel, int fieldLevel, int methodLevel) {
        JCStorageElement e = new JCStorageElement(name, fsName,
                             classLevel, fieldLevel, methodLevel);

        removeFileSystemElement(fsName);
        addElement(e);

        saveElements();

        return e;
    }

    public void checkProvider(JCStorageElement e) {
        if (e.getProvider() == null && dbFolder != null) {
            File dbDir = NbClassPath.toFile(dbFolder);
            if (dbDir != null) {
                JCClassProvider cp = new JCFileProvider(
                                         new File(dbDir, e.getName()).getAbsolutePath());
                cp.reset();
                e.setProvider(cp);
            }
        }
    }


    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("arg1=base-source-dir arg2=target-file-prefix arg3=class-level" // NOI18N
                               + " arg4=field-level arg5=method-level\n" // NOI18N
                               + "Storage level: 0=all, 1=not private 2=public and protected, 3=public only\n" // NOI18N
                               + "Example of args: e:\\java\\jdk12\\src e:\\NB\\system\\ParserDB\\jdk12 2 2 2"); // NOI18N
            return;
        }

        int classLevel = Integer.parseInt(args[2]);
        int fieldLevel = Integer.parseInt(args[3]);
        int methodLevel = Integer.parseInt(args[4]);

        System.out.println("Inspecting classes ..."); // NOI18N
        List classNameList = JCUtilities.getClassNameList(args[0]);
        List classList = JCUtilities.getClassList(classNameList, true,
                         classLevel, fieldLevel, methodLevel);

        final int cnt[] = new int[1];
        JCFileProvider fp = new JCFileProvider(args[1]);
        fp.reset();

        JCompletion.ListProvider classes = new JCompletion.ListProvider(classList) {
                                               public boolean notifyAppend(JCClass c, boolean appendFinished) {
                                                   if (appendFinished) {
                                                       System.out.print("Building " + c.getFullName() + " ...             \r"); // NOI18N
                                                       cnt[0]++;
                                                   }
                                                   return true;
                                               }
                                           };

        long tm = System.currentTimeMillis();

        fp.append(classes);

        System.out.println(cnt[0] + " classes rebuilt in " + (System.currentTimeMillis() - tm) + "ms."); // NOI18N
        File skels = new File(args[1] + JCFileProvider.SKEL_FILE_EXT);
        File bodies = new File(args[1] + JCFileProvider.BODY_FILE_EXT);
        System.out.println("Files created:\nSkeleton file: " // NOI18N
                           + skels.getAbsolutePath() + ", length=" + skels.length() // NOI18N
                           + "\nBody file: " + bodies.getAbsolutePath() // NOI18N
                           + ", length=" + bodies.length()); // NOI18N

        System.exit(0);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        Iterator i = elementList.iterator();
        while (i.hasNext()) {
            sb.append(((JCStorageElement)i.next()).toString());
            sb.append('\n');
        }
        return sb.toString();
    }

}

/*
 * Log
 *  11   Gandalf   1.10        1/26/00  Miloslav Metelka removed jdk12.jar
 *  10   Gandalf   1.9         1/13/00  Miloslav Metelka Localization
 *  9    Gandalf   1.8         1/10/00  Miloslav Metelka 
 *  8    Gandalf   1.7         11/14/99 Miloslav Metelka 
 *  7    Gandalf   1.6         11/8/99  Miloslav Metelka 
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         9/15/99  Miloslav Metelka 
 *  4    Gandalf   1.3         7/22/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/21/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/21/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */

