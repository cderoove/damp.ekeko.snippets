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

package org.openide.loaders;

import java.net.URL;
import java.io.*;
import java.util.*;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.text.MessageFormat;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.w3c.dom.Document;

import org.openide.*;
import org.openide.actions.*;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.text.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;


/** Object that provides main functionality for xml data loader.
 *
 * @author Libor Kramolis, Jaroslav Tulach
 */
public class XMLDataObject extends MultiDataObject {
    /** Public ID of xmlinfo dtd. */
    public static final String XMLINFO_DTD_PUBLIC_ID = "-//Forte for Java//DTD xmlinfo//EN"; // NOI18N
    public static final String XMLINFO_DTD_PUBLIC_ID_OLD = "-//NetBeans IDE//DTD xmlinfo//EN"; // NOI18N

    /** the static instance for users that do not want to have own processor */
    private static RequestProcessor XML_RP = new RequestProcessor ("XMLDataObject::Info"); // NOI18N

    /** Not parsed yet. Constant for getStatus method. */
    public static final int STATUS_NOT     = 0;
    /** Parsed ok. Constant for getStatus method. */
    public static final int STATUS_OK      = 1;
    /** Parsed with warnings. Constant for getStatus method. */
    public static final int STATUS_WARNING = 2;
    /** Parsed with errors. Constant for getStatus method. */
    public static final int STATUS_ERROR   = 3;

    /** property name of document property */
    public static final String PROP_DOCUMENT = "document"; // NOI18N

    /** property name of info property */
    public static final String PROP_INFO = "info"; // NOI18N

    /** generated Serialized Version UID */
    static final long serialVersionUID = 8757854986453256578L;

    /** Task for parse xmlinfo file */
    private Task infoTask = Task.EMPTY;

    /** xmlinfo */
    private Info info;

    /** XmlDocument created from 'xml' file
    * Weaker reference to org.w3c.dom.Document 
    */
    private Reference xmlDocument = new SoftReference (null);

    /** XML parse error handler */
    static private ErrorPrinter errorHandler = new ErrorPrinter();

    /** XML entity resolver */
    static private com.sun.xml.parser.Resolver entityResolver = new com.sun.xml.parser.Resolver();

    /** the result of parsing */
    private int status;

    /** editor support */
    private EditorCookie editor = createEditorCookie();

    /** parser of xmlinfo, watcher over changes of documents */
    private InfoParser infoParser = new InfoParser ();

    static {
        entityResolver.setIgnoringMIME (true);
        registerCatalogEntry (XMLINFO_DTD_PUBLIC_ID,
                              "org/openide/resources/xmlinfo.dtd", // NOI18N
                              ClassLoader.getSystemClassLoader());
        registerCatalogEntry (XMLINFO_DTD_PUBLIC_ID_OLD,              // [temporary] - back compability
                              "org/openide/resources/xmlinfo.dtd", // NOI18N
                              ClassLoader.getSystemClassLoader());
    }

    /** Create new XMLDataObject
     *
     * @param fo the primary file object
     * @param loader loader of this data object
     */
    public XMLDataObject (FileObject fo, MultiFileLoader loader)
    throws DataObjectExistsException {
        super (fo, loader);

        fo.getParent ().addFileChangeListener (
            WeakListener.fileChange (infoParser, fo.getParent ())
        );

        status = STATUS_NOT;
        info = null;

        fo = FileUtil.findBrother (fo, Loader.XMLINFO_EXT);
        registerEntry (fo);

        infoParser.setInfoFile (fo);
    }

    /** Provides node that should represent this data object. When a node for representation
    * in a parent is requested by a call to getNode (parent) it is the exact copy of this node
    * with only parent changed. This implementation creates instance
    * <CODE>DataNode</CODE>.
    * <P>
    * This method is called only once.
    *
    * @return the node representation for this data object
    * @see DataNode
    */
    protected Node createNodeDelegate () {
        DataNode node = new DataNode (this, Children.LEAF);
        node.setIconBase ("/org/openide/resources/xmlObject"); // NOI18N
        node.setDefaultAction (SystemAction.get (OpenAction.class));
        return node;
    }

    /** Called when the info file is parsed and the icon should change.
    * @param res resource for the icon
    */
    protected void updateIconBase (String res) {
        DataNode node = (DataNode)getNodeDelegate();
        node.setIconBase (res);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (XMLDataObject.class);
    }

    /** @return a cookie if it has been found in the xmlinfo file
    */
    public Node.Cookie getCookie (Class cls) {
        infoTask.waitFinished();
        return super.getCookie (cls);
    }

    /** Allows subclasses to provide own editor cookie.
     * @return editor cookie to use
     */
    protected EditorCookie createEditorCookie () {
        return new EditorSupport (getPrimaryEntry());
    }

    /** Creates w3c's document for the xml file. Either returns cached reference
    * or parses the file and creates new document.
    * 
    * @return the parsed document
    * @exception SAXException if there is a parsing error
    * @exception IOException if there is an I/O error
    */
    public final Document getDocument () throws IOException, SAXException {
        Document doc = (Document)xmlDocument.get ();
        if (doc != null)
            return doc;

        synchronized (this) {
            doc = (Document)xmlDocument.get ();
            if (doc != null)
                return doc;

            status = STATUS_OK;
            try {
                doc = parsePrimaryFile ();
            } catch (SAXException e) {
                status = STATUS_ERROR;
                throw e;
            } catch (IOException e) {
                status = STATUS_ERROR;
                throw e;
            }
            //        if (doc == null)      // never
            //  	status = STATUS_ERROR;

            // store it in the cache
            xmlDocument = new SoftReference (doc);

            return doc;
        }
    }

    /** Clears the document. Called when the document file is changed.
     */
    final void clearDocument () {
        xmlDocument.clear ();
        firePropertyChange (PROP_DOCUMENT, null, null);
    }

    /** @return one of STATUS_XXX constants  */
    public final int getStatus () {
        return status;
    }

    /* @return info. Returns null if file has no xmlinfo. */
    public final Info getInfo () {
        infoTask.waitFinished();
        if (info == null)
            return info;
        return (Info)info.clone();
    }

    /* Sets info */
    public final synchronized void setInfo (Info ii) throws IOException {
        setInfoImpl (ii);
        writeInfo();
    }

    private final void setInfoImpl (Info ii) throws IOException {
        if (info == ii)
            return;
        if ((info != null) && info.equals (ii))
            return;
        Info prevInfo = info;
        info = ii;

        if (info != null) {
            for (Iterator it = info.processorClasses(); it.hasNext(); ) {
                try {
                    Class c = (Class)it.next();
                    Object o = c.newInstance ();
                    Processor proc = (Processor)o;
                    proc.attachTo (this);
                    getCookieSet().add (proc);
                } catch (InstantiationException e) {
                    throw new IOException (e.getClass().getName() + ": " + e.getMessage()); // NOI18N
                } catch (IllegalAccessException e) {
                    throw new IOException (e.getClass().getName() + ": " + e.getMessage()); // NOI18N
                }
            }
            String iconBase = info.getIconBase();
            if (iconBase != null) {
                infoTask.waitFinished();
                updateIconBase (iconBase);
            }
        }
        firePropertyChange (PROP_INFO, prevInfo, info);
    }

    private void writeInfo () throws IOException {
        if (info == null)
            return;

        final FileObject primary = getPrimaryFile();
        final FileObject parent = primary.getParent();
        final org.openide.filesystems.FileSystem FS = parent.getFileSystem();

        FS.runAtomicAction (new org.openide.filesystems.FileSystem.AtomicAction () {
                                public void run () throws IOException {
                                    FileLock lock = null;
                                    OutputStream os = null;

                                    FileObject infoFO = FS.find (parent.getName(), primary.getName(), Loader.XMLINFO_EXT);
                                    if (infoFO == null)
                                        infoFO = parent.createData (primary.getName(), Loader.XMLINFO_EXT);
                                    try {
                                        lock = infoFO.lock ();
                                        os = infoFO.getOutputStream (lock);
                                        PrintWriter writer = new PrintWriter (os);
                                        info.write (writer);
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

    /** Parses the primary file of this data object.
    * and provide different implementation.
    *
    * @return the document in the primary file
    * @exception IOException if error during parsing occures
    */
    final Document parsePrimaryFile () throws IOException, SAXException {
        URL url = getPrimaryFile ().getURL ();

        return parse (url, errorHandler, false);
    }

    // Start of Utilities
    /** Provides access to internal XML parser.
    * This method takes URL. After successful finish the
    * document tree is returned. Used non validating parser.
    *
    * @param url the url to read the file from
    */
    public static Document parse (URL url) throws IOException, SAXException {
        return parse (url, errorHandler, false);
    }

    /** Provides access to internal XML parser.
    * This method takes URL. After successful finish the
    * document tree is returned. Used non validating parser.
    *
    * @param url the url to read the file from
    * @param validate if true validating parser is used
    */
    public static Document parse (URL url, boolean validate) throws IOException, SAXException {
        return parse (url, errorHandler, validate);
    }

    /** Provides access to internal XML parser.
    * This method takes URL. After successful finish the
    * document tree is returned.
    *
    * @param url the url to read the file from
    * @param eh error handler to notify about exception
    */
    public static Document parse (URL url, ErrorHandler eh) throws IOException, SAXException {
        return parse (url, eh, false);
    }

    /** Provides access to internal XML parser.
    * This method takes URL. After successful finish the
    * document tree is returned.
    *
    * @param url the url to read the file from
    * @param eh error handler to notify about exception
    * @param validate if true validating parser is used
    */
    public static Document parse (URL url, ErrorHandler eh, boolean validate) throws IOException, SAXException {
        Parser parser;
        com.sun.xml.tree.XmlDocumentBuilder builder;

        parser = createParser (validate);
        parser.setErrorHandler (eh);
        builder = new com.sun.xml.tree.XmlDocumentBuilder();
        builder.setDisableNamespaces (true);
        builder.setParser (parser);
        parser.parse (createInputSource (url));

        return builder.getDocument ();
    }

    /** Creates SAX parse that can be used to parse XML files.
     * @return sax parser
     */
    public static Parser createParser () {
        return createParser (false);
    }

    /** Creates SAX parse that can be used to parse XML files.
     * @param validate if true validating parser is returned
     * @return sax parser
     */
    public static Parser createParser (boolean validate) {
        Parser parser;

        if (validate)
            parser = new com.sun.xml.parser.ValidatingParser (true);
        else
            parser = new com.sun.xml.parser.Parser ();
        parser.setEntityResolver (entityResolver);

        return parser;
    }

    /** Creates empty DOM Document. */
    public static Document createDocument () {
        return new com.sun.xml.tree.XmlDocument ();
    }

    /** Writes DOM Document to writer. */
    public static void write (Document doc, Writer writer) throws IOException {
        if (doc instanceof com.sun.xml.tree.XmlDocument) {
            ((com.sun.xml.tree.XmlDocument)doc).write (writer);
        } else {
            throw new InternalError ("Unsupported DOM Document!"); // NOI18N
        }
    }

    /** Creates SAX InputSource for specified URL */
    public static org.xml.sax.InputSource createInputSource (URL url) throws IOException {
        InputStream stream = url.openStream();
        if (stream instanceof PushbackInputStream)
            stream = new DataInputStream (stream); // see com.sun.xml.parser.XmlReader line 192
        org.xml.sax.InputSource retval =
            entityResolver.createInputSource (null, stream, false, url.getProtocol());
        retval.setSystemId (url.toString ());
        return retval;
        //      return entityResolver.createInputSource (url, true); // previous version
        // not correct for mime type "text/xml" with url protocol "http" (!"file") // NOI18N
        // -> every time created ASCII reader
        // see com.sun.xml.parser.Resolver line 221 and com.sun.xml.parser.XmlReade line 109
    }

    /**
     * Registers the given public ID as corresponding to a particular 
     * URI, typically a local copy.  This URI will be used in preference
     * to ones provided as system IDs in XML entity declarations.  This
     * mechanism would most typically be used for Document Type Definitions
     * (DTDs), where the public IDs are formally managed and versioned.
     *
     * <P> Any created parser use global entity resolver and you can
     * register its catalog entry.
     *
     * @param publicId The managed public ID being mapped
     * @param uri The URI of the preferred copy of that entity
     */
    public static void registerCatalogEntry (String publicId, String uri) {
        entityResolver.registerCatalogEntry (publicId, uri);
    }

    /**
     * Registers a given public ID as corresponding to a particular Java
     * resource in a given class loader, typically distributed with a
     * software package.  This resource will be preferred over system IDs
     * included in XML documents.  This mechanism should most typically be
     * used for Document Type Definitions (DTDs), where the public IDs are
     * formally managed and versioned.
     *
     * <P> If a mapping to a URI has been provided, that mapping takes
     * precedence over this one.
     *
     * <P> Any created parser use global entity resolver and you can
     * register its catalog entry.
     *
     * @param publicId The managed public ID being mapped
     * @param resourceName The name of the Java resource
     * @param loader The class loader holding the resource, or null if
     *	it is a system resource.
     */
    public static void registerCatalogEntry (String publicId, String resourceName, ClassLoader loader) {
        entityResolver.registerCatalogEntry (publicId, resourceName, loader);
    }
    // end of utilities


    // class ErrorPrinter
    static class ErrorPrinter implements org.xml.sax.ErrorHandler {
        private void message (final String level, final org.xml.sax.SAXParseException e) {
            TopManager.getDefault ().notifyException (new Throwable () {
                        public String getMessage () {
                            return new String (MessageFormat.format
                                               (NbBundle.getBundle (XMLDataObject.class).getString ("PROP_XmlMessage"),
                                                new Object [] { level,
                                                                e.getMessage(),
                                                                (e.getSystemId() == null ? "" : e.getSystemId()), // NOI18N
                                                                e.getLineNumber()+"", // NOI18N
                                                                e.getColumnNumber()+"" // NOI18N
                                                              }));
                        }
                    });
        }

        public void error (org.xml.sax.SAXParseException e) {
            message (NbBundle.getBundle (XMLDataObject.class).getString ("PROP_XmlError"), e);
        }

        public void warning (org.xml.sax.SAXParseException e) {
            message (NbBundle.getBundle (XMLDataObject.class).getString ("PROP_XmlWarning"), e);
        }

        public void fatalError (org.xml.sax.SAXParseException e) {
            message (NbBundle.getBundle (XMLDataObject.class).getString ("PROP_XmlFatalError"), e);
        }
    } // end of inner class ErrorPrinter


    /** This class has to be implemented by all processors in the
    * xmlinfo file. It is cookie, so after parsing such class is instantiated
    * and put into data objects cookie set.
    */
    public static interface Processor extends Node.Cookie {
        /** When the XMLDataObject creates new instance of the processor,
        * it uses this method to attach the processor to the data object.
        *
        * @param xmlDO XMLDataObject
        */
        public void attachTo (XMLDataObject xmlDO);
    }


    /** Parser for XML info.
    */
    private final class InfoParser extends HandlerBase
        implements Runnable, FileChangeListener {
        /** the name of info tag */
        private static final String TAG_INFO = "info"; // NOI18N
        /** the name of processor tag */
        private static final String TAG_PROCESSOR = "processor"; // NOI18N
        /** the class attribute */
        private static final String ATT_PROCESSOR_CLASS = "class"; // NOI18N
        /** icon tag */
        private static final String TAG_ICON = "icon"; // NOI18N
        /** base attribute */
        private static final String ATT_ICON_BASE = "base"; // NOI18N

        /** file object to parse */
        private FileObject fileObject;

        private Info tempInfo;

        /** Changes the info file that starts new parsing.
        */
        public synchronized void setInfoFile (FileObject fo) {
            // synchronized to allow only one parsing to be running at one time.
            // wait till previous parsing finishes
            infoTask.waitFinished ();

            fileObject = fo;

            CookieSet cs = new CookieSet ();
            cs.add (editor);
            setCookieSet (cs);

            if (fileObject == null)
                return;
            // start new parsing
            infoTask = XML_RP.post (
                           this, 0, Thread.NORM_PRIORITY - 1
                       );
        }

        public void startDocument () {
            tempInfo = new Info();
        }

        public void endDocument () {
            try {
                XMLDataObject.this.setInfoImpl (tempInfo);
            } catch (Exception e) {
                TopManager.getDefault().notifyException (e);
            }
        }

        /** Accepts module item */
        public void startElement (String name, AttributeList attr) {
            if (name.equals (TAG_PROCESSOR)) {
                String className = attr.getValue (ATT_PROCESSOR_CLASS);
                if (className != null) {
                    try {
                        className = org.openide.util.Utilities.translate(className);
                        Class c = Class.forName (className, true, TopManager.getDefault().systemClassLoader ());
                        tempInfo.addProcessorClass (c);
                    } catch (Exception e) {
                        TopManager.getDefault ().notifyException (e);
                    }
                }
                return;
            }
            if (name.equals (TAG_ICON)) {
                String file = attr.getValue (ATT_ICON_BASE);
                tempInfo.setIconBase (file);
                return;
            }
        }

        /** Starts the parsing.
         */
        public void run () {
            try {
                Parser p = createParser ();
                p.setDocumentHandler (this);
                p.setErrorHandler (errorHandler);
                p.parse (createInputSource (fileObject.getURL()));
            } catch (SAXException ex) {
                TopManager.getDefault ().notifyException (ex);
            } catch (IOException ex) {
                TopManager.getDefault ().notifyException (ex);
            }
        }

        public void fileFolderCreated (FileEvent fe) {
            // not interesting
        }

        public void fileDataCreated (FileEvent fe) {
            FileObject fo = fe.getFile ();
            if (
                fo.hasExt (Loader.XMLINFO_EXT) &&
                fo.getName ().equals (getPrimaryFile ().getName ())
            ) {
                // new info file created => force it to be reparsed
                setInfoFile (fo);
            }
        }

        /** Fired when a file is changed.
        * @param fe the event describing context where action has taken place
        */
        public void fileChanged (FileEvent fe) {
            if (fe.getFile().equals (fileObject)) {
                // repase the file
                setInfoFile (fe.getFile ());
            } else {
                if (getPrimaryFile ().equals (fe.getFile ())) {
                    // the main file changed
                    clearDocument ();
                }
            }
        }

        public void fileDeleted (FileEvent fe) {
            if (fe.getFile().equals (fileObject)) {
                // repase the file
                setInfoFile (null);
            }
        }

        public void fileRenamed (FileRenameEvent fe) {
            // the same behaviour as when the file is deleted
            fileDeleted (fe);
        }

        public void fileAttributeChanged (FileAttributeEvent fe) {
        }

    } // end of InfoParser

    /** The DataLoader for XmlDataObjects.
     * This class is final only for performance reasons,
     * can be happily unfinaled if desired.
     */
    static class Loader extends MultiFileLoader {
        /** Extension constants */
        static final String XML_EXT = "xml"; // NOI18N
        static final String XMLINFO_EXT = "xmlinfo"; // NOI18N

        static final long serialVersionUID =3917883920409453930L;
        /** Creates a new XMLDataLoader */
        public Loader () {
            super (XMLDataObject.class);
        }

        /** Initialize XMLDataLoader: name, actions, ...
        */
        protected void initialize () {
            setDisplayName(
                NbBundle.getBundle (XMLDataObject.class).getString ("PROP_XmlLoader_Name")
            );

            setActions(new SystemAction[] {
                           SystemAction.get(OpenAction.class),
                           null,
                           SystemAction.get(CutAction.class),
                           SystemAction.get(CopyAction.class),
                           SystemAction.get(PasteAction.class),
                           null,
                           SystemAction.get(DeleteAction.class),
                           SystemAction.get(RenameAction.class),
                           null,
                           SystemAction.get(SaveAsTemplateAction.class),
                           null,
                           SystemAction.get(ToolsAction.class),
                           SystemAction.get(PropertiesAction.class)
                       });
        }

        /** For a given file finds a primary file.
        * @param fo the file to find primary file for
        *
        * @return the primary file for the file or null if the file is not
        *   recognized by this loader
        */
        protected FileObject findPrimaryFile (FileObject fo) {
            if (XML_EXT.equals(fo.getExt())) {
                return fo;
            }
            if ("tld".equals(fo.getExt())) { // NOI18N
                return fo; // JSP Tag Library Descriptor
            }
            if (XMLINFO_EXT.equals(fo.getExt())) {
                return FileUtil.findBrother (fo, XML_EXT);
            }
            // not recognized
            return null;
        }

        /** Creates the right data object for given primary file.
        * It is guaranteed that the provided file is realy primary file
        * returned from the method findPrimaryFile.
        *
        * @param primaryFile the primary file
        * @return the data object for this file
        * @exception DataObjectExistsException if the primary file already has data object
        */
        protected MultiDataObject createMultiObject (FileObject primaryFile)
        throws DataObjectExistsException {
            return new XMLDataObject (primaryFile, this);
        }

        /** Creates the right primary entry for given primary file.
        *
        * @param primaryFile primary file recognized by this loader
        * @return primary entry for that file
        */
        protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry (obj, primaryFile);
        }

        /** Creates right secondary entry for given file. The file is said to
        * belong to an object created by this loader.
        *
        * @param secondaryFile secondary file for which we want to create entry
        * @return the entry
        */
        protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry (obj, secondaryFile);
        }
    }

    /**
     * Representation of xmlinfo file
     */
    public static final class Info implements Cloneable {
        /**
         * @associates Class 
         */
        Set processors;
        String iconBase;

        /** Create info */
        public Info () {
            processors = new HashSet (7);
            iconBase = null;
        }

        public Object clone () {
            Info ii = new Info();
            for (Iterator it = processors.iterator(); it.hasNext();) {
                Class proc = (Class)it.next();
                ii.processors.add (proc);
            }
            ii.iconBase = iconBase;
            return ii;
        }

        /** Add processor class to info. */
        public void addProcessorClass (Class proc) {
            if (!Processor.class.isAssignableFrom (proc))
                throw new IllegalArgumentException();
            processors.add (proc);
        }

        /** Remove processor class from info.
         * @return true if removed
         */
        public boolean removeProcessorClass (Class proc) {
            return processors.remove (proc);
        }

        public Iterator processorClasses () {
            return processors.iterator();
        }

        /** Set icon base */
        public void setIconBase (String base) {
            iconBase = base;
        }

        /** @return icon base */
        public String getIconBase () {
            return iconBase;
        }

        /** Write specified info to writer */
        public void write (Writer writer) throws IOException {
            writer.write ("<?xml version=\"1.0\"?>\n\n"); // NOI18N
            writer.write (MessageFormat.format ("<!DOCTYPE {0} PUBLIC \"{1}\" \"\">\n\n", // NOI18N
                                                new Object [] { InfoParser.TAG_INFO, XMLINFO_DTD_PUBLIC_ID }));
            writer.write (MessageFormat.format ("<{0}>\n", // NOI18N
                                                new Object [] { InfoParser.TAG_INFO }));
            for (Iterator it = processors.iterator(); it.hasNext();)
                writer.write (MessageFormat.format ("  <{0} {1}=\"{2}\" />\n", // NOI18N
                                                    new Object [] { InfoParser.TAG_PROCESSOR,
                                                                    InfoParser.ATT_PROCESSOR_CLASS,
                                                                    ((Class)it.next()).getName() }));
            if (iconBase != null)
                writer.write (MessageFormat.format ("  <{0} {1}=\"{2}\" />\n", // NOI18N
                                                    new Object [] { InfoParser.TAG_ICON,
                                                                    InfoParser.ATT_ICON_BASE,
                                                                    iconBase }));
            writer.write (MessageFormat.format ("</{0}>\n", // NOI18N
                                                new Object [] { InfoParser.TAG_INFO }));
        }
    } // end of inner class XMLInfoCreator
}


/*
 * Log
 *  37   Gandalf   1.36        1/16/00  Libor Kramolis  
 *  36   Gandalf   1.35        1/13/00  Ian Formanek    NOI18N
 *  35   Gandalf   1.34        1/13/00  Jesse Glick     All data loaders now 
 *       public for serialization.
 *  34   Gandalf   1.33        1/12/00  Ian Formanek    NOI18N
 *  33   Gandalf   1.32        12/3/99  Petr Jiricka    Added extension for JSP 
 *       tag library descriptor
 *  32   Gandalf   1.31        11/26/99 Patrik Knakal   
 *  31   Gandalf   1.30        11/25/99 Libor Kramolis  
 *  30   Gandalf   1.29        11/11/99 Libor Kramolis  
 *  29   Gandalf   1.28        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  28   Gandalf   1.27        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  27   Gandalf   1.26        10/1/99  Libor Kramolis  
 *  26   Gandalf   1.25        9/30/99  Libor Kramolis  
 *  25   Gandalf   1.24        9/30/99  Libor Kramolis  Info updated
 *  24   Gandalf   1.23        9/29/99  Libor Kramolis  xml parser changed & new
 *       utils
 *  23   Gandalf   1.22        9/2/99   Libor Kramolis  
 *  22   Gandalf   1.21        8/9/99   Libor Kramolis  set
 *  21   Gandalf   1.20        8/6/99   Jaroslav Tulach parse (URL, 
 *       ErrorHandler)
 *  20   Gandalf   1.19        7/30/99  Libor Kramolis  
 *  19   Gandalf   1.18        7/22/99  Libor Kramolis  
 *  18   Gandalf   1.17        7/21/99  Jaroslav Tulach MultiDataObject can mark
 *       easily mark secondary entries in constructor as belonging to the 
 *       object.
 *  17   Gandalf   1.16        7/20/99  Libor Kramolis  
 *  16   Gandalf   1.15        7/19/99  Ian Formanek    Info parsing in own 
 *       request processor
 *  15   Gandalf   1.14        7/19/99  Libor Kramolis  
 *  14   Gandalf   1.13        7/16/99  Jaroslav Tulach Allows subclasses to 
 *       have own parser for the primary file.
 *  13   Gandalf   1.12        7/9/99   Libor Kramolis  
 *  12   Gandalf   1.11        7/8/99   Jesse Glick     Context help.
 *  11   Gandalf   1.10        7/3/99   Libor Kramolis  
 *  10   Gandalf   1.9         7/2/99   Jaroslav Tulach Added PROP_DOCUMENT that
 *       is fired when the document is changed.
 *  9    Gandalf   1.8         6/24/99  Ian Formanek    Updated to better handle
 *       existing code for changes introduced in last checkin
 *  8    Gandalf   1.7         6/22/99  Libor Kramolis  
 *  7    Gandalf   1.6         6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  6    Gandalf   1.5         6/9/99   Ian Formanek    ToolsAction
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    Minor changes
 *  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         6/4/99   Libor Kramolis  
 *  2    Gandalf   1.1         5/26/99  Ian Formanek    changed incorrect usage 
 *       of getBundle
 *  1    Gandalf   1.0         5/24/99  Jaroslav Tulach 
 * $
 */
