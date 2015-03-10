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

package  org.netbeans.modules.web.wizards.beanjsp.ide.netbeans;

import org.netbeans.modules.web.util.*;
import org.netbeans.modules.web.wizards.beanjsp.ui.*;
import org.netbeans.modules.web.wizards.wizardfw.MultiLineLabel;

import java.awt.event.*;

import java.util.HashSet;
import java.io.*;

import javax.swing.text.PlainDocument;

import org.openide.awt.*;
import org.openide.*;
import org.openide.loaders.*;
import org.openide.windows.*;
import org.openide.explorer.*;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.*;

import org.openide.cookies.*;

import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.CallableSystemAction;
import org.openide.nodes.Node;

import org.openide.src.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileLock;
import org.openide.loaders.DataObject;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.ExecSupport;
import org.openide.execution.Executor;
import org.openide.debugger.DebuggerType;
import org.openide.loaders.DataFolder;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

import java.util.*;


public class IDEHelper  {

    public static void openJSPFile(FileObject jspFileObject) {
        try {
            openJSPFile(TopManager.getDefault().getLoaderPool().findDataObject(jspFileObject));
        }catch(Exception ex) { Debug.print(ex);}
    }

    public static void openJSPFile(DataObject jspDataObject) {

        // set default executor to JSP Executor

        if(jspDataObject instanceof MultiDataObject) {
            try {
                MultiDataObject jspMultiDataObject = (MultiDataObject) jspDataObject;
                //Executor defExec = findExecutor(JspExecutor.class); <- this makes the module dependent on jswdk module
                String jspExecutorClassName = "org.netbeans.modules.web.core.jswdk.JspExecutor"; //NOI18N
                Class jspExecutorClass = Class.forName(jspExecutorClassName);
                Executor defExec = findExecutor(jspExecutorClass);
                if (defExec != null)
                    ExecSupport.setExecutor(jspMultiDataObject.getPrimaryEntry(), defExec);
            }catch(Exception ex) {
                Debug.print(ex);
                // I don't care for any exceptions here. so I silently ignore ( this is sometimes called ugly in style guide )
            }
        }


        Node node = jspDataObject.getNodeDelegate ();
        // run default action (hopefully should be here) and it would be Open action
        SystemAction sa = node.getDefaultAction ();
        if (sa != null) {
            sa.actionPerformed (new ActionEvent (node, ActionEvent.ACTION_PERFORMED, ""));			 // NOI18N
        }
    }

    /** Finds an instance of an executor of the given class or null if no such
     *  executor exists. 
     */
    protected static Executor findExecutor(Class executorClass) {
        for (Enumeration execs = Executor.getDefault().executors();
                execs.hasMoreElements();) {
            Executor exec = (Executor)execs.nextElement();
            if (executorClass.isInstance(exec))
                return exec;
        }
        return null;
    }


    public static FileObject saveJSPFileAs(String name, String extension, StringBuffer fileDataBuffer, boolean overwrite) throws IOException {
        FileObject jspFileObj = saveJSPFileAs(JSPPageWizard.jspFolder.getPrimaryFile(),name,extension,fileDataBuffer.toString(),overwrite);
        openJSPFile(jspFileObj);
        return jspFileObj;
    }

    public static FileObject saveJSPFileAs(FileObject folderFile, String name, String extension, StringBuffer fileDataBuffer, boolean overwrite) throws IOException {
        return saveJSPFileAs(folderFile,name,extension,fileDataBuffer.toString(),overwrite);
    }

    //todo: Original NB code has some indentation mechanism. ask petr jerikka and add that here to indent JSP file
    public static FileObject saveJSPFileAs(FileObject folderFile, String name, String extension, String fileData, boolean overwrite) throws IOException {

        FileObject fo = null;

        try {
            fo = folderFile.createData(name, extension);
        }catch(IOException ex) {
            if(!overwrite)
                throw ex;
            fo  = folderFile.getFileObject(name,extension);
        }

        FileLock lock = null;
        Writer wrFile = null;
        Writer beautyWrFile = null;
        try {
            wrFile = new OutputStreamWriter(fo.getOutputStream(lock = fo.lock()));
            try {
                beautyWrFile = org.openide.text.IndentEngine.find("text/x-jsp").createWriter(new PlainDocument(), 0, wrFile);    // NOI18N
            }catch(Exception ex) {} // ignored ex safely
            if(beautyWrFile != null) {
                beautyWrFile.write(fileData);
            } else {
                wrFile.write(fileData);
            }

            return fo;
        } finally {
            try {
                if (wrFile != null)
                    wrFile.close ();
                if(beautyWrFile != null)
                    beautyWrFile.close();
            }
            catch (IOException e) {
                if (Boolean.getBoolean("netbeans.debug.exceptions"))			 // NOI18N
                    e.printStackTrace();
            }
            if (lock != null) lock.releaseLock ();
        }
    }

    public static DataObject classToDataObject(String className) {
        className = className.replace('.', '/') + '.' + "class";				 // NOI18N
        FileObject classFile = TopManager.getDefault().getRepository().findResource(className);
        if (classFile == null)
            return null;
        try {
            return TopManager.getDefault().getLoaderPool().findDataObject(classFile);
        }
        catch (IOException e) {
            return null;
        }
    }

    public static DataObject packageToDataObject(String packageName) {
        packageName = packageName.replace('.', '/');
        // Debug.println("Finding package "+packageName);
        FileObject packageFile = TopManager.getDefault().getRepository().findResource(packageName);
        if (packageFile == null) {
            // Debug.println("Could not find package :"+packageName);
            return null;
        }
        try {
            return TopManager.getDefault().getLoaderPool().findDataObject(packageFile);
        }
        catch (IOException ex) {
            // Debug.print(ex);
            return null;
        }
    }

    // return Vector of classes

    public static Vector findBeansInFolder(String folderName) {
        DataFolder pakFolder = (DataFolder) packageToDataObject(folderName);
        return findBeansInFolder(pakFolder);
    }

    /* ***
    public static Vector findBeansInFolder(DataFolder pakFolder) {
    		
    	Vector beans = new Vector();
    	if(pakFolder == null) {
    		return beans;
    	}
    	try {
    		FileObject pakFileObj = pakFolder.getPrimaryFile();
    		Enumeration enum = pakFileObj.getData(false);
    		for(; enum.hasMoreElements();) {
    			FileObject classFileObj = (FileObject)enum.nextElement();
    			if(classFileObj.hasExt("class")) {
    				Class beanClass = TopManager.getDefault().currentClassLoader().loadClass(classFileObj.getPackageName('.'));
    				if(beanClass != null)
    					beans.add(beanClass);
    			}
    		}
    		
    	}catch(Exception ex) { Debug.print(ex);}
    	return beans;		
      }
    *** */

    public static Vector findBeansInFolder(DataFolder pakFolder) {
        Vector beans = new Vector();
        if(pakFolder == null) {
            return beans;
        }
        try {
            pakFolder.getPrimaryFile().refresh();
            DataObject[] dataObjects = pakFolder.getChildren();
            for(int i=0; i < dataObjects.length; ++i) {
                try {
                    InstanceCookie instanceCookie = (InstanceCookie)dataObjects[i].getCookie(InstanceCookie.class);
                    if(instanceCookie != null) {
                        Class beanClass = instanceCookie.instanceClass();
                        if(beanClass != null)
                            beans.add(beanClass);
                    }
                }catch(Exception ex) {
                    // ignore the class cast exception for InstanceCookie
                    // Debug.print(ex);
                }
            }

        }catch(Exception ex) {} // ignored exceptions safely
        return beans;
    }


    public static boolean acceptDataObjectForJSPPages(DataObject dataObj) {
        // return (oj instanceof DataFolder);
        if(dataObj instanceof DataFolder) {
            return true;
        } else {
            //// here look for .jsp file in this and return true
            FileObject jspFileObj = dataObj.getPrimaryFile();
            if(jspFileObj == null)
                return false;
            if(jspFileObj.getExt().equalsIgnoreCase("jsp"))					 // NOI18N
                return true;
            if(jspFileObj.getExt().equalsIgnoreCase("html"))							// NOI18N
                return true;
            if(jspFileObj.getExt().equalsIgnoreCase("htm"))								// NOI18N
                return true;

            return false;
        }
    }


    public static boolean acceptDataObjectForBean(DataObject dataObj) {
        // return (oj instanceof DataFolder);
        if(dataObj instanceof DataFolder) {
            return true;
        } else {
            if(dataObj.getCookie(SourceCookie.class) == null) {
                return false;
            } else {
                //// here look for .class file in this and return true
                FileObject javaFileObj = dataObj.getPrimaryFile();
                if(javaFileObj == null)
                    return false;
                if(!javaFileObj.getExt().equalsIgnoreCase("java"))				 //NOI18N
                    return false;
                if(!javaFileObj.existsExt("class"))								 //NOI18N
                    return false;
                return true;
            }
        }
    }

    public static String browseForJSPPage() {

        DataFilter dataFilter = new DataFilter () {
                                    public boolean acceptDataObject (DataObject dataObj) {
                                        return acceptDataObjectForJSPPages(dataObj);
                                    }
                                };

        java.util.ResourceBundle resBundle = NbBundle.getBundle(JSPPageWizard.i18nBundle);
        String title = resBundle.getString("JBW_WebPageBrowseDialogTitle");						 //NOI18N
        String rootTitle = resBundle.getString("JBW_WebPageBrowseDialogRootTitle");				 //NOI18N

        Node place = TopManager.getDefault().getPlaces().nodes().repository(dataFilter);
        try {
            Node[] selected = TopManager.getDefault().getNodeOperation().select(title, rootTitle, place, new NodeAcceptor() {
                                  public final boolean acceptNodes(Node[] nodes) {
                                      if (nodes == null || nodes.length != 1) return false;
                                      return nodes[0].getCookie(DataFolder.class) == null;
                                  }
                              });

            DataObject jspDataObject = (DataObject)selected[0].getCookie(DataObject.class);
            FileObject jspFileObject = jspDataObject.getPrimaryFile();
            return jspFileObject.getPackageNameExt('/','.');

        } catch (org.openide.util.UserCancelException ex) {
            return "";			 //NOI18N
        }
    }


    public static DataFolder browseForBeanPackage() {

        java.util.ResourceBundle resBundle = NbBundle.getBundle(JSPPageWizard.i18nBundle);
        String title = resBundle.getString("JBW_BeanPackageBrowseDialogTitle");					 //NOI18N
        String rootTitle = resBundle.getString("JBW_BeanPackageBrowseDialogRootTitle");			 //NOI18N

        DataFilter dataFilter = new DataFilter () {
                                    public boolean acceptDataObject (DataObject dataObj) {
                                        return acceptDataObjectForBean(dataObj);
                                    }
                                };
        return browseForFolder(dataFilter,title,rootTitle);

    }

    public static DataFolder browseForJSPFolder() {

        java.util.ResourceBundle resBundle = NbBundle.getBundle(JSPPageWizard.i18nBundle);
        String title = resBundle.getString("JBW_JSPFolderBrowseDialogTitle");    //NOI18N
        String rootTitle = resBundle.getString("JBW_JSPFolderBrowseDialogRootTitle");  //NOI18N

        DataFilter dataFilter = new DataFilter () {
                                    public boolean acceptDataObject (DataObject dataObj) {
                                        return (dataObj instanceof DataFolder);
                                    }
                                };
        return browseForFolder(dataFilter,title,rootTitle);

    }

    public static DataFolder browseForFolder(DataFilter dataFilter,String title, String rootTitle) {

        Node place = TopManager.getDefault().getPlaces().nodes().repository(dataFilter);
        try {
            Node[] selected = TopManager.getDefault().getNodeOperation().select(title, rootTitle, place, new NodeAcceptor() {
                                  public final boolean acceptNodes(Node[] nodes) {
                                      if (nodes == null || nodes.length != 1) return false;
                                      return nodes[0].getCookie(DataFolder.class) != null;
                                  }
                              });

            DataFolder targetFolder = (DataFolder)selected[0].getCookie(DataFolder.class);
            return targetFolder;


        } catch (org.openide.util.UserCancelException ex) {
            return null;
        }
    }

    public static void showWarningMessageI18N(String i18nWarningMsg) {
        java.util.ResourceBundle resBundle = NbBundle.getBundle(JSPPageWizard.i18nBundle);

        MultiLineLabel msgLabel = new MultiLineLabel(resBundle.getString(i18nWarningMsg));
        msgLabel.setMinimumSize (new java.awt.Dimension(500, 75));
        // msgLabel.setMaximumSize (new java.awt.Dimension(400, 75));
        msgLabel.setPreferredSize (new java.awt.Dimension(600, 75));

        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(msgLabel,NotifyDescriptor.WARNING_MESSAGE);
        msg.setTitle(resBundle.getString("JBW_WarningMsgTitle"));			// NOI18N
        TopManager.getDefault().notify(msg);
    }

    public static void showErrorMessageI18N(String i18nErrorMsg) {
        java.util.ResourceBundle resBundle = NbBundle.getBundle(JSPPageWizard.i18nBundle);
        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(resBundle.getString(i18nErrorMsg),
                                       NotifyDescriptor.ERROR_MESSAGE);
        msg.setTitle(resBundle.getString("JBW_ErrorMsgTitle"));			// NOI18N
        TopManager.getDefault().notify(msg);

    }

    public static boolean askConfirmationI18N(String i18nQuestionMsg) {
        java.util.ResourceBundle resBundle = NbBundle.getBundle(JSPPageWizard.i18nBundle);
        NotifyDescriptor.Confirmation confirm = new NotifyDescriptor.Confirmation(resBundle.getString(i18nQuestionMsg),
                                                resBundle.getString("JBW_ConfirmMsgTitle"),				 //NOI18N
                                                NotifyDescriptor.YES_NO_OPTION);
        Object confirmOption = TopManager.getDefault().notify(confirm);
        return confirmOption.equals(NotifyDescriptor.YES_OPTION);
    }

    //todo if folder is null , check for the file in repository root.
    public static boolean fileExists(DataFolder folder, String name, String ext) {
        if(folder != null) {
            if(folder.getPrimaryFile().getFileObject(name,ext) != null)
                return true;
        }
        return false;
    }

}