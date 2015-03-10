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
import java.util.*;
import java.io.*;
import java.text.MessageFormat;

import org.openide.filesystems.*;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.enum.RemoveDuplicatesEnumeration;
import org.openide.util.NbBundle;

import org.netbeans.modules.jarpackager.util.VersionSerializator;
import org.netbeans.modules.jarpackager.options.JarPackagerOption;

/** This class is responsible for holding all the information needed
* to create an archive.
* Archive is fully specified and described by a set of properties
* which this class defines.
* Class is serializable to allow clients to store the information
* about archive for later refreshing of the content of archive.<p>
* Jar package is created from a set of file objects.
* Clients will use putFile method to create set of root file objects
* which will represent the content of resulting jar. If the root file
* object is folder, then all its content will be added into jar.
* Clients can also specify FileObjectFilter implementation to control
* which types of files to put in the resulting jar.<p>
*
* Manifest handling is divided into two approaches:
* 1. It's possible to specify main manifest attributes in the contructor.<br>
* 2. By specifying AttributesProducer implementation, client can 
* add per-entry manifest attributes for specific file objects.<br>
*
* Clients can also specify extra information through callback interface
* ExtraInfoProducer.
*
* @author Dafe Simonek
*/
public class JarContent extends Object {

    /** Accepts class files only */
    public static final FileObjectFilter CLASSES_ONLY = new ClassesOnlyFilter();
    /** Accepts all but java files */
    public static final FileObjectFilter DEFAULT = new AllButJavaFilter();
    /** Accepts all files */
    public static final FileObjectFilter ALL = new AcceptAllFilter();

    /** Set of file objects to represent the content of
    * the jar file */
    ArrayList content;
    /** The filter for file objects */
    FileObjectFilter filter;
    /** Manifest instance */
    Manifest manifest;
    /** Mapping between names and contents of extra info entries */
    ExtraInfoProducer extraProducer;
    /** Flag for automatic file list creation in manifest */
    boolean manifestFileList;
    /** Flag for automatic main manifest attributes generation */
    boolean mainAttributes;
    /** true if jar should be compressed, false otherwise */
    boolean compressed;
    /** level of the compression */
    int compressionLevel;
    /** target file containing jar */
    File targetFile;

    /** manager for versioned serialization */
    VersionSerializator serializationManager;

    /** Creates new JarContent with default file object filter which
    * accepts all but java files and with no manifest.
    */
    public JarContent () {
        this(DEFAULT);
    }

    /** Creates new JarContent with given main manifest attributes and
    * with given file object filter.
    * @param filter File object filter
    */
    public JarContent (FileObjectFilter filter) {
        this.filter = filter;
        manifestFileList = false;
        compressed = true;
        compressionLevel = 6;
        mainAttributes = true;
    }

    /** Adds specified file object to the jar content.
    * When a folder is specified, all its subentries will
    * be inserted into the archive too during the process of
    * creating archive.<br>
    * Duplicates are not allowed (method checks it automatically)
    */
    public synchronized void putFile (FileObject file) {
        List content = getContent();
        if (!content.contains(file)) {
            content.add(file);
        }
    }

    /** Inserts specified array of file objects into the
    * jar content. */  
    public synchronized void putFiles (FileObject[] files) {
        for (int i = 0; i < files.length; i++) {
            putFile(files[i]);
        }
    }

    /** Inserts specified collection of file objects into the
    * jar content. */  
    public synchronized void putFiles (Collection files) {
        for (Iterator iter = files.iterator(); iter.hasNext(); ) {
            putFile((FileObject)iter.next());
        }
    }

    /** Removes specified file object from the archive content */
    public synchronized void removeFile (FileObject file) {
        getContent().remove(file);
    }

    /** Removes specified array of file objects from the archive content */
    public synchronized void removeFiles (FileObject[] files) {
        for (int i = 0; i < files.length; i++) {
            removeFile(files[i]);
        }
    }

    /** @return list of root file objects which was added by putFiles(...)
    * methods */
    public List getRoots () {
        return getContent();
    }

    /** Removes all file objects that were previously added by
    * calling putFile(..) and putFiles(..) methods */ 
    public synchronized void clear () {
        getContent().clear();
    }

    /** Returns currently asociated file object filter.
    */
    public FileObjectFilter getFilter () {
        return filter;
    }

    /** Sets new filter for filtering file objects in archive.
    * @filter New filter
    */
    public synchronized void setFilter (FileObjectFilter filter) {
        this.filter = filter;
    }

    /** Returns currently asociated extra info producer.
    * (or null if no asociated)
    */
    public ExtraInfoProducer getExtraProducer () {
        return extraProducer;
    }

    /** Sets new producer for attaching extra information to the archive.
    * @filter New extra info producer (can be null)
    */
    public synchronized void setExtraProducer (ExtraInfoProducer extraProducer) {
        this.extraProducer = extraProducer;
    }

    /** Sets the manifest of the archive. */
    public void setManifest (Manifest manifest) {
        this.manifest = manifest;
    }

    /** @return the manifest of archive described by this jar content.
    * Completion of the manifest is performed first if needed.
    *
    * Note that when you modify the manifest returned from this method,
    * you will also update manifest of this jar content. It is not really
    * needed to call setManifest again to re-store modified manifest.
    */
    public Manifest getManifest () {
        return manifest;
    }

    /** Enables or disables automatic generation of archive file
    * list to the manifest */
    public void setManifestFileList (boolean manifestFileList) {
        this.manifestFileList = manifestFileList;
    }

    /** @return true if automatic generation of archive file list
    * in manifest is enabled, false otherwise. Default is false. */
    public boolean isManifestFileList () {
        return manifestFileList;
    }

    /** Enables or disables automatic generation of various
    * main attributes of the manifest */
    public void setMainAttributes (boolean mainAttributes) {
        this.mainAttributes = mainAttributes;
    }

    /** @return true if automatic generation of main attributes in
    * enabled, false otherwise. Enabled by default. */
    public boolean isMainAttributes () {
        return mainAttributes;
    }

    /** @return target file which contains jar
    * (or will contain jar if it is not created yet) */
    public File getTargetFile () {
        return targetFile;
    }

    /** Sets new target file.
    * @param targetFile new target file
    */
    public void setTargetFile (File targetFile) {
        this.targetFile = targetFile;
    }

    /** Getter for property compress.
    *@return Value of property compress.
    */
    public boolean isCompressed () {
        return compressed;
    }

    /** Setter for property compress.
    *@param compress New value of property compress.
    */
    public void setCompressed (boolean compressed) {
        this.compressed = compressed;
    }

    /** @return current compression level (0-9). Property is
    * ignored when compression is disabled */
    public int getCompressionLevel () {
        return compressionLevel;
    }

    /** Sets new compression level (0-9). Ignored when
    * compression is disabled. Throws IllegalArgumentException
    * if new compression level is invalid. */
    public void setCompressionLevel (int compressionLevel) {
        if ((compressionLevel < 0) || (compressionLevel > 9))
            throw new IllegalArgumentException(
                MessageFormat.format(
                    NbBundle.getBundle(JarContent.class).getString("FMT_InvalidLevel"),
                    new Object[] { new Integer(compressionLevel) }
                )
            );
        this.compressionLevel = compressionLevel;
    }

    /** Returns the list with all file objects (not only that were added by
    * putFile(...) methods, but also with all their subentries - recursively
    * went through) that are currently selected to be in resulting
    * jar archive and satisfies file object filter conditions.
    * @result list which holds all filtered entries of current content
    */
    public synchronized List filteredContent () {
        // get the enumeration of the content
        Enumeration enum =
            new RemoveDuplicatesEnumeration(new AllFiles(getContent()));
        // filter the enumeration and convert into resulting list
        ArrayList result = new ArrayList();
        FileObject curFo = null;
        while (enum.hasMoreElements()) {
            curFo = (FileObject)enum.nextElement();
            if (filter.accept(curFo)) {
                // create named entry
                result.add(curFo);
            }
        }
        return result;
    }

    /** Returns full content (recursively went through) of the archive
    * as enumeration. Performs no filtering.
    * @return Enumeration of all entries of current content.
    */
    public synchronized Enumeration fullContent () {
        // get the enumeration of the content
        return new RemoveDuplicatesEnumeration(new AllFiles(getContent()));
    }

    /** Safe accessor for content */
    public List getContent () {
        if (content == null) {
            content = new ArrayList();
        }
        return content;
    }

    // only for testing
    /*public String toString () {
      return "Content: " + getContent().toString() + "\n" + 
              "Filter: " + filter + "\n" +
              "Extra producer: " + extraProducer;    
}*/

    /** Versioned deserialization */
    public void readContent (ObjectInput in)
    throws IOException, ClassNotFoundException {
        VersionSerializator vs = serializationManager();
        vs.readVersion(in);
    }

    /** Versioned serialization */
    public void writeContent (ObjectOutput out)
    throws IOException {
        VersionSerializator vs = serializationManager();
        vs.writeLastVersion(out);
    }

    /** Safe getter for serialization manager which
    * manages versioned serisalization */
    private VersionSerializator serializationManager () {
        if (serializationManager == null) {
            serializationManager = new VersionSerializator();
            serializationManager.putVersion(new Version1Serializator(this));
        }
        return serializationManager;
    }

    /** Enumeration that enumerates all file objects from the
    * given set of root file objects. */
    static final class AllFiles implements Enumeration {
        /** Array of :
        * 1) file objects (for primary entries of data objects)
        * 2) iterators (for seconmdary entries of data objects)
        * 3) enumerations (for folders) */
        ArrayList roots;
        /** Enumeration of children of currently processed
        * root file object or iterator of secondary entries of processed
        * item */
        Object curEnum;
        /** Currently processed item in array */
        int curIndex = 0;

        AllFiles (List content) {
            FileObject[] fos =
                (FileObject[])content.toArray(new FileObject[0]);
            roots = new ArrayList(fos.length*2);
            DataObject curDo = null;
            for (int i = 0; i < fos.length; i++) {
                try {
                    curDo = DataObject.find(fos[i]);
                } catch (DataObjectNotFoundException exc) {
                    // ignore and continue to next file object
                    // PENDING - warning to the output window ?
                    exc.printStackTrace();
                    continue;
                }
                //        System.out.println(fos[i].getName());
                if (fos[i].isFolder()) {
                    // folder (add all its children)
                    roots.add(fos[i].getData(true));
                } else {
                    // not folder - add all its files
                    // PENDING - ensure that children are initialized approprietly
                    roots.add(curDo.files().iterator());
                }
            }
            curIndex = 0;
        }

        /** Tests if this enumeration contains more elements. */
        public boolean hasMoreElements () {
            // ask current enumeration of iterator
            if (doHasNext(curEnum)) {
                return true;
            } else {
                curEnum = null;
            }
            // try to inspect following items to find out if we are finished
            Object curItem = null;
            for (int i = curIndex; i < roots.size(); i++) {
                curItem = roots.get(i);
                if ((curItem instanceof FileObject) || doHasNext(curItem)) {
                    return true;
                }
            }
            // yep, we are on the end, no next elements exist
            return false;
        }

        /** Returns the next element of this enumeration if this
        * enumeration object has at least one more element to provide. */
        public Object nextElement () throws NoSuchElementException {
            // return next element from current enumeration or iterator
            // if this is possible
            if (doHasNext(curEnum)) {
                return doNext(curEnum);
            } else {
                curEnum = null;
            }
            // continue further and search next item
            Object curItem = null;
            for (int i = curIndex; i < roots.size(); i++) {
                curItem = roots.get(i);
                if (curItem instanceof FileObject) {
                    curIndex = i + 1;
                    return curItem;
                }
                if (doHasNext(curItem)) {
                    curIndex = i + 1;
                    curEnum = curItem;
                    return doNext(curItem);
                }
            }
            // no next element exists
            throw new NoSuchElementException();
        }

        /** @return true if given iterator or enumeration has
        * next elements, false otherwise */
        private boolean doHasNext (Object enumOrIter) {
            return ((enumOrIter instanceof Enumeration) &&
                    ((Enumeration)enumOrIter).hasMoreElements()) ||
                   ((enumOrIter instanceof Iterator) &&
                    ((Iterator)enumOrIter).hasNext());
        }

        /** @return next element of given enumeration or iterator.
        * Throws NoSuchElementException if no next element exist. */
        private Object doNext (Object enumOrIter)
        throws NoSuchElementException {
            if (enumOrIter instanceof Enumeration) {
                return ((Enumeration)enumOrIter).nextElement();
            }
            if (enumOrIter instanceof Iterator) {
                return ((Iterator)enumOrIter).next();
            }
            throw new NoSuchElementException();
        }

    } // end of AllFiles inner class


    /** Implementation of the filter that accepts only *.class files */
    private static final class ClassesOnlyFilter implements FileObjectFilter {
        static final long serialVersionUID = 7475557013758392767L;
        public boolean accept (FileObject fo) {
            return "class".equals(fo.getExt()); // NOI18N
        }
        /** Keep the uniquennes of this filter after deserialization */
        private Object readResolve () throws ObjectStreamException {
            return CLASSES_ONLY;
        }
    };

    /** Implementation of the filter that accepts all but *.java, *.form files */
    private static final class AllButJavaFilter implements FileObjectFilter {
        static final long serialVersionUID = -6474655716324211768L;
        public boolean accept (FileObject fo) {
            String extension = fo.getExt();
            return !("java".equals(extension)) && !("jar".equals(extension)) && // NOI18N
                   !("form".equals(extension)) && // NOI18N
                   (JarPackagerOption.singleton().getContentExt() != extension);
        }
        /** Keep the uniquennes of this filter after deserialization */
        private Object readResolve () throws ObjectStreamException {
            return DEFAULT;
        }
    };

    /** Implementation of the filter that accepts all files */
    private static final class AcceptAllFilter implements FileObjectFilter {
        static final long serialVersionUID = 8921981094756492767L;
        public boolean accept (FileObject fo) {
            return true;
        }
        /** Keep the uniquennes of this filter after deserialization */
        private Object readResolve () throws ObjectStreamException {
            return ALL;
        }
    };


    /** This class takes care of serializaton of JarContent
    * in first version of jar packager module.
    */
    static final class Version1Serializator implements VersionSerializator.Versionable {
        /** Asociated jar content to manage serialization for */
        JarContent jc;

        public Version1Serializator (JarContent jc) {
            this.jc = jc;
        }

        public String getName () {
            return "Version_1.0"; // NOI18N
        }

        public void readData (ObjectInput in)
        throws IOException, ClassNotFoundException {
            jc.content = (ArrayList)in.readObject();
            jc.filter = (FileObjectFilter)in.readObject();
            jc.extraProducer = (ExtraInfoProducer)in.readObject();
            jc.targetFile = (File)in.readObject();
            jc.manifestFileList = ((Boolean)in.readObject()).booleanValue();
            jc.mainAttributes = ((Boolean)in.readObject()).booleanValue();
            jc.compressed = ((Boolean)in.readObject()).booleanValue();
            jc.compressionLevel = ((Integer)in.readObject()).intValue();
            if (((Boolean)in.readObject()).booleanValue()) {
                jc.manifest = new Manifest();
                jc.manifest.read((InputStream)in);
            }
        }

        public void writeData (ObjectOutput out)
        throws IOException {
            out.writeObject(jc.content);
            out.writeObject(jc.filter);
            out.writeObject(jc.extraProducer);
            out.writeObject(jc.targetFile);
            out.writeObject(new Boolean(jc.manifestFileList));
            out.writeObject(new Boolean(jc.mainAttributes));
            out.writeObject(new Boolean(jc.compressed));
            out.writeObject(new Integer(jc.compressionLevel));
            out.writeObject(new Boolean(jc.manifest != null));
            if (jc.manifest != null)
                jc.manifest.write((OutputStream)out);
        }

    } // end of Version1Serializator inner class


}

/*
* <<Log>>
*  24   Gandalf-post-FCS1.22.1.0    3/15/00  David Simonek   serialization fix 
*       (serialization UIDs added)
*  23   Gandalf   1.22        1/25/00  David Simonek   Various bugfixes and i18n
*  22   Gandalf   1.21        1/16/00  David Simonek   i18n
*  21   Gandalf   1.20        11/10/99 David Simonek   Workaround for bug - 
*       MultiDataObject secondary entries incorrectly empty (caused missing 
*       files in archive)
*  20   Gandalf   1.19        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  19   Gandalf   1.18        10/13/99 David Simonek   various bugfixes 
*       concerning primarily manifest
*  18   Gandalf   1.17        10/10/99 Petr Hamernik   console debug messages 
*       removed.
*  17   Gandalf   1.16        10/5/99  David Simonek   various fixes, only 
*       primary entries now resides in 'chosen content'
*  16   Gandalf   1.15        10/4/99  David Simonek   
*  15   Gandalf   1.14        9/16/99  David Simonek   a lot of bugfixes (RE 
*       filters, empty jar content etc)  added templates
*  14   Gandalf   1.13        9/13/99  David Simonek   bugfixes, compressed 
*       on/off support fixed
*  13   Gandalf   1.12        9/8/99   David Simonek   new version of jar 
*       packager
*  12   Gandalf   1.11        8/1/99   David Simonek   another bugfixes...
*  11   Gandalf   1.10        8/1/99   David Simonek   automatic file list 
*       generation to the manifest added
*  10   Gandalf   1.9         6/10/99  David Simonek   progress indocator + 
*       minor bugfixes....
*  9    Gandalf   1.8         6/9/99   David Simonek   bugfixes, progress 
*       dialog, compiling progress..
*  8    Gandalf   1.7         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  7    Gandalf   1.6         6/8/99   David Simonek   
*  6    Gandalf   1.5         6/8/99   David Simonek   bugfixes....
*  5    Gandalf   1.4         6/4/99   Petr Hamernik   defe's bugfix
*  4    Gandalf   1.3         6/4/99   David Simonek   
*  3    Gandalf   1.2         6/4/99   Petr Hamernik   temporary version
*  2    Gandalf   1.1         6/4/99   David Simonek   manifest ceration now 
*       correctly supported
*  1    Gandalf   1.0         6/3/99   David Simonek   
* $
*/