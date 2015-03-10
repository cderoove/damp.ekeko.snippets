/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jarpackager;

import java.util.jar.*;
import java.util.zip.CRC32;
import java.util.*;
import java.io.*;
import java.text.MessageFormat;

import org.openide.filesystems.*;
import org.openide.util.enum.RemoveDuplicatesEnumeration;
import org.openide.execution.NbClassPath;
import org.openide.util.NbBundle;
import org.openide.TopManager;

import org.netbeans.modules.jarpackager.util.ProgressListener;

/** Utility class for creating jar packages from a set of file objects.
* Common usage is create JarContent instance which describes the jar at first
* and then create instance of this class, asociated with previously
* created JarContent instance. Then it's possible to create the jar
* by calling one of the createJar methods.
* This class also supports progress notification. To be notified about
* a progress, implement ProgressListener interface and asociate the
* listener with this instance.
*
* @author Dafe Simonek
*/
public class JarCreater extends Object {

    // Attributes

    /** The block size for buffered reading and writing */
    private static int blockSize = 65536;

    /** Message format for progress info */
    private static MessageFormat progressInfo;

    /** holds the content of the jar */
    JarContent jc;

    /** progress listeners 
     * @associates ProgressListener*/
    HashSet listeners;

    /** file object currently being processed */
    FileObject curFo;

    /** Only utitlity class, no need to instantiate. */
    public JarCreater (JarContent jc) {
        this.jc = jc;
        // message format for progress message
        if (progressInfo == null) {
            progressInfo = new MessageFormat(
                               NbBundle.getBundle(PackagingPanel.class).getString("FMT_ProgressInfo")
                           );
        }
    }

    /** Writes an archive described by asociated JarContent instance to the
    * specified file object.
    * The content of jar is collected from the root file objects
    * inserted by JarContent.putFile(..) and JarContent.putFiles(...) calls.
    * The manifest file is created automatically from main attributes
    * and per-entry attributes.
    */  
    public void createJar (FileObject file)
    throws IOException {
        FileLock lock = file.lock();
        OutputStream os = file.getOutputStream(lock);
        try {
            createJar(os, NbClassPath.toFile(file));
        } finally {
            os.close();
            lock.releaseLock();
        }
    }

    /** Writes an archive described by asociated JarContent instance to the
    * specified output stream. Called from createJar(FileObject)
    */
    public void createJar (OutputStream os, File targetFile)
    throws IOException {
        long now = System.currentTimeMillis();
        Manifest manifest = jc.getManifest();
        JarOutputStream jos = new JarOutputStream(os, manifest);
        try {
            // set write method
            boolean compressed = jc.isCompressed();
            jos.setMethod(
                compressed ? JarOutputStream.DEFLATED : JarOutputStream.STORED
            );
            if (compressed) {
                jos.setLevel(jc.getCompressionLevel());
            }
            BufferedInputStream bufIs = null;
            curFo = null;
            String curName = null;
            JarEntry curJarEntry = null;
            long curSize = 0;
            // get the enumeration of the content
            Enumeration enum = jc.fullContent();
            double count = jc.filteredContent().size();
            long curIndex = 1;
            FileObjectFilter filter = jc.getFilter();
            InputStream in = null;
            while (enum.hasMoreElements()) {
                curFo = (FileObject)enum.nextElement();
                // add file object to the jar if filter accepts it
                if (filter.accept(curFo)) {
                    // try to open input stream for current file object
                    try {
                        in = curFo.getInputStream();
                    } catch (FileNotFoundException exc) {
                        // warn user that this file object cannot be added
                        // and skip to next file object
                        cannotAddFo(curFo);
                        continue;
                    }
                    // create named entry
                    curJarEntry = createEntry(curFo, compressed, targetFile);
                    jos.putNextEntry(curJarEntry);
                    bufIs = new BufferedInputStream(in);
                    try {
                        writeEntry(jos, bufIs,
                                   compressed ? curFo.getSize() : curJarEntry.getSize());
                    } finally {
                        bufIs.close();
                    }
                    jos.closeEntry();
                    // notify progress listeners
                    fireProgressEvent(
                        (int)(Math.round(curIndex++ / count * 100)),
                        progressInfo.format(new Object[] { curFo.getName() } )
                    );
                }
            }
            // write extra info entries, if present
            ExtraInfoProducer extraProducer = jc.getExtraProducer();
            List extraInfoList =
                (extraProducer == null) ? null : extraProducer.extraInfo();
            if (extraInfoList != null) {
                BufferedOutputStream bufOs = new BufferedOutputStream(jos);
                for (Iterator iter = extraInfoList.iterator(); iter.hasNext(); ) {
                    ExtraInfoProducer.ExtraEntry curEntry =
                        (ExtraInfoProducer.ExtraEntry)iter.next();
                    bufIs = compressed ? null :
                            new BufferedInputStream(curEntry.createInputStream());
                    try {
                        jos.putNextEntry(createEntry(
                                             curEntry.getName(),
                                             compressed, curEntry.getSize(), bufIs, targetFile
                                         ));
                    } finally {
                        if (bufIs != null) {
                            bufIs.close();
                        }
                    }
                    bufIs = new BufferedInputStream(curEntry.createInputStream());
                    try {
                        int ch;
                        while((ch = bufIs.read()) != -1) {
                            bufOs.write(ch);
                        }
                        bufOs.flush();
                    } finally {
                        bufIs.close();
                    }
                    jos.closeEntry();
                }
            }
        } finally {
            jos.finish();
            curFo = null;
        }
        //System.out.println(System.currentTimeMillis() - now);
    }

    /** @return file object currently being added to the archive.
    * Returns null if creation is not in progress. */
    public FileObject getProcessedFileObject () {
        return curFo;
    }

    /* Adds new listener which will be notified about creating progress.
    * @param pl new listener
    */
    public synchronized void addProgressListener (ProgressListener pl) {
        if (listeners == null)
            listeners = new HashSet();
        listeners.add(pl);
    }

    /* Removes specified listener from the listener list.
    * @param pl listener to remove
    */
    public synchronized void removeProgressListener (ProgressListener pl) {
        if (listeners == null)
            return;
        listeners.remove(pl);
    }

    /** Fires notification about creating progress.
    * @param pe progress event to fire off
    */
    protected void fireProgressEvent (int percent, String description) {
        if (listeners == null)
            return;
        HashSet cloned;
        // clone listener list
        synchronized (this) {
            cloned = (HashSet)listeners.clone();
        }
        // fire on cloned list to prevent from modifications when firing
        for (Iterator iter = cloned.iterator(); iter.hasNext(); ) {
            ((ProgressListener)iter.next()).progress(percent, description);
        }
    }

    /** Creates jar entry for given file object */
    JarEntry createEntry (FileObject fo, boolean compressed, File targetFile)
    throws IOException {
        BufferedInputStream bufIs = null;
        if (!compressed) {
            bufIs = new BufferedInputStream(fo.getInputStream());
        }
        try {
            // we must check if we are not archiving itself
            // and deal with this fact, but only in STORED mode
            if ((!compressed) && (targetFile != null) &&
                    targetFile.equals(NbClassPath.toFile(fo))) {
                // trying to archive itself, so create dumb empty entry
                JarEntry result = new JarEntry(fo.getPackageNameExt('/', '.'));
                result.setSize(0);
                result.setCompressedSize(0);
                result.setCrc(new CRC32().getValue());
                return result;
            } else {
                return createEntry(fo.getPackageNameExt('/', '.'), compressed,
                                   fo.getSize(), bufIs, targetFile);
            }
        } finally {
            if (bufIs != null) {
                bufIs.close();
            }
        }
    }

    /** Create jar entry from given information. If compressed is true,
    * size and in parameters are ignored and needn't be set */
    JarEntry createEntry (String name, boolean compressed, long size,
                          InputStream in, File targetFile)
    throws IOException {
        JarEntry result = new JarEntry(name);
        // we must provide header information for the entry
        // if STORED method is used
        if (!compressed) {
            result.setSize(size);
            result.setCompressedSize(size);
            CRC32 crc = new CRC32();
            int ch;
            while((ch = in.read()) != -1) {
                crc.update(ch);
            }
            result.setCrc(crc.getValue());
        }
        return result;
    }

    /** Writes the content of given input stream to the specified
    * jar output stream */
    static void writeEntry (JarOutputStream jos, InputStream is, long size)
    throws IOException {
        long left = size;
        byte[] content = new byte[blockSize];
        int readCount = 0;
        int curPos = 0;
        while (left > 0) {
            int leftInBlock =
                (left >= blockSize) ? blockSize : (int)(size % blockSize);
            curPos = 0;
            // copy a block
            while (leftInBlock > 0) {
                readCount = is.read(content, curPos, leftInBlock);
                jos.write(content, curPos, readCount);
                curPos += readCount;
                leftInBlock -= readCount;
            }
            left -= blockSize;
        }
    }

    /** Utiltity method, notifies user that given file object
    * cannot be added to the archive */
    private static void cannotAddFo (FileObject fo) {
        TopManager.getDefault().getStdOut().println(
            MessageFormat.format(
                NbBundle.getBundle(JarCreater.class).getString("FMT_CannotAddFo"),
                new Object[] {fo}
            )
        );
    }


}

/*
* <<Log>>
*  19   Gandalf   1.18        12/7/99  David Simonek   
*  18   Gandalf   1.17        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems copyright in file comment
*  17   Gandalf   1.16        10/13/99 David Simonek   various bugfixes 
*       concerning primarily manifest
*  16   Gandalf   1.15        10/13/99 David Simonek   jar content now primary 
*       file, other small changes
*  15   Gandalf   1.14        10/4/99  David Simonek   
*  14   Gandalf   1.13        9/13/99  David Simonek   bugfixes, compressed 
*       on/off support fixed
*  13   Gandalf   1.12        9/8/99   David Simonek   new version of jar 
*       packager
*  12   Gandalf   1.11        8/18/99  Jesse Glick     Trivial.
*  11   Gandalf   1.10        8/1/99   David Simonek   automatic file list 
*       generation to the manifest added
*  10   Gandalf   1.9         6/10/99  David Simonek   progress indocator + 
*       minor bugfixes....
*  9    Gandalf   1.8         6/10/99  David Simonek   progress dialog now 
*       functional
*  8    Gandalf   1.7         6/9/99   David Simonek   bugfixes, progress 
*       dialog, compiling progress..
*  7    Gandalf   1.6         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  6    Gandalf   1.5         6/5/99   David Simonek   
*  5    Gandalf   1.4         6/4/99   David Simonek   
*  4    Gandalf   1.3         6/4/99   Petr Hamernik   temporary version
*  3    Gandalf   1.2         6/4/99   David Simonek   manifest ceration now 
*       correctly supported
*  2    Gandalf   1.1         6/3/99   David Simonek   
*  1    Gandalf   1.0         5/26/99  David Simonek   
* $
*/