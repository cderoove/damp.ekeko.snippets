/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder;

import com.borland.primetime.PrimeTime;
import com.borland.primetime.actions.ActionGroup;
import com.borland.primetime.actions.UpdateAction;
import com.borland.primetime.editor.EditorManager;
import com.borland.primetime.ide.Browser;
import com.borland.primetime.ide.ProjectView;
import com.borland.primetime.node.DuplicateNodeException;
import com.borland.primetime.node.FileNode;
import com.borland.primetime.node.FileType;
import com.borland.primetime.node.Node;
import com.borland.primetime.node.Project;
import com.borland.primetime.vfs.InvalidUrlException;
import com.borland.primetime.vfs.Url;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;
import org.acm.seguin.ide.common.action.GenericAction;
import org.acm.seguin.ide.common.EditorOperations;
import org.acm.seguin.ide.common.MultipleDirClassDiagramReloader;
import org.acm.seguin.ide.common.PackageNameLoader;
import org.acm.seguin.ide.common.SourceBrowser;
import org.acm.seguin.ide.common.UMLIcon;
import org.acm.seguin.ide.common.action.CurrentSummary;
import org.acm.seguin.ide.common.action.ExtractMethodAction;
import org.acm.seguin.ide.common.action.PrettyPrinterAction;
import org.acm.seguin.ide.jbuilder.refactor.JBuilderCurrentSummary;
import org.acm.seguin.ide.jbuilder.refactor.JBuilderRefactoringFactory;
import org.acm.seguin.ide.jbuilder.refactor.MenuBuilder;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.tools.install.RefactoryInstaller;
import org.acm.seguin.uml.UMLPackage;
import org.acm.seguin.util.FileSettings;

/**
 *  File node representing a UML class diagram
 *
 *@author    Chris Seguin
 */
public class UMLNode extends FileNode {
	private UMLPackage packageDiagram;
	private String packageName;


	/**
	 *  Constructor for the UMLNode object
	 *
	 *@param  project                     Description of Parameter
	 *@param  parent                      Description of Parameter
	 *@param  url                         Description of Parameter
	 *@exception  DuplicateNodeException  Description of Exception
	 */
	public UMLNode(Project project,
			Node parent,
			Url url)
			 throws DuplicateNodeException
	{
		super(project, parent, url);

		MultipleDirClassDiagramReloader reloader = UMLNodeViewerFactory.getFactory().getReloader();
		if (!reloader.isNecessary()) {
			reloader.setNecessary(true);
			reloader.reload();
		}

		PackageNameLoader loader = new PackageNameLoader();
		packageName = loader.load(url.getFile());
	}


	/**
	 *  Sets the Diagram attribute of the UMLNode object
	 *
	 *@param  diagram  The new Diagram value
	 */
	public void setDiagram(UMLPackage diagram)
	{
		packageDiagram = diagram;
	}


	/**
	 *  Gets the Diagram attribute of the UMLNode object
	 *
	 *@return    The Diagram value
	 */
	public UMLPackage getDiagram()
	{
		return packageDiagram;
	}


	/**
	 *  Gets the Persistant attribute of the UMLNode object
	 *
	 *@return    The Persistant value
	 */
	public boolean isPersistant()
	{
		return false;
	}


	/**
	 *  Gets the DisplayIcon attribute of the UMLNode object
	 *
	 *@return    The DisplayIcon value
	 */
	public Icon getDisplayIcon()
	{
		return new UMLIcon();
	}


	/**
	 *  Gets the DisplayName attribute of the UMLNode object
	 *
	 *@return    The DisplayName value
	 */
	public String getDisplayName()
	{
		if ((PrimeTime.CURRENT_MAJOR_VERSION >= 4) &&
				(PrimeTime.CURRENT_MINOR_VERSION >= 1)) {
			return packageName + ".uml";
		}
		return packageName;
	}


	/**
	 *  Determines if the diagram has been modified
	 *
	 *@return    true if it has
	 */
	public boolean isModified()
	{
		if (packageDiagram == null) {
			return false;
		}
		return packageDiagram.isDirty();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  url                         Description of Parameter
	 *@exception  IOException             Description of Exception
	 *@exception  InvalidUrlException     Description of Exception
	 *@exception  DuplicateNodeException  Description of Exception
	 */
	public void saveAs(Url url) throws IOException,
			InvalidUrlException, DuplicateNodeException
	{
		save();
	}


	/**
	 *  Saves the diagram to the disk
	 *
	 *@exception  IOException          Description of Exception
	 *@exception  InvalidUrlException  Description of Exception
	 */
	public void save() throws IOException, InvalidUrlException
	{
		if (packageDiagram != null) {
			packageDiagram.save();
		}
	}


	/**
	 *  Initialize the open tools
	 *
	 *@param  majorVersion  the version number
	 *@param  minorVersion  the version number
	 */
	public static void initOpenTool(byte majorVersion, byte minorVersion)
	{
		if (majorVersion != 4) {
			return;
		}

		System.out.println("Version:  " + majorVersion + "." + minorVersion +
				"     (Primetime:  " + PrimeTime.CURRENT_MAJOR_VERSION + "." +
				PrimeTime.CURRENT_MINOR_VERSION + ")");

		//  Create the property files
		(new RefactoryInstaller(true)).run();
		cleanJBuilderSetting();

		//  Register the source browser
		SourceBrowser.set(new JBuilderBrowser());
		EditorOperations.register(new JBuilderEditorOperations());
		CurrentSummary.register(new JBuilderCurrentSummary());

		// Initialize OpenTool here...
		FileType.registerFileType("uml", new FileType("Class Diagram",
				UMLNode.class,
				new TestObject(),
				new UMLIcon()));

		FileNode.registerFileNodeClass("uml",
				"Class Diagram",
				UMLNode.class,
				new UMLIcon());

		Browser.registerNodeViewerFactory(UMLNodeViewerFactory.getFactory(), true);
		Browser.addStaticBrowserListener(new NewProjectAdapter());
		//Browser.addStaticBrowserListener(new RefactoringAdapter());

		//  Adds a menu item
		ActionGroup group = new ActionGroup("JRefactory");
		Action prettyPrintAction = new PrettyPrinterAction();
		prettyPrintAction.putValue(UpdateAction.ACCELERATOR, prettyPrintAction.getValue(GenericAction.ACCELERATOR));
		group.add(prettyPrintAction);
		Action extractMethodAction = new ExtractMethodAction();
		extractMethodAction.putValue(UpdateAction.ACCELERATOR, extractMethodAction.getValue(GenericAction.ACCELERATOR));
		group.add(extractMethodAction);
		group.add(new ReloadAction());
		group.add(new NewClassDiagramAction());
		group.add(MenuBuilder.build());
		group.add(new UndoAction());
		group.add(new PrintAction());
		group.add(new JPGFileAction());
		ActionGroup zoomGroup = new ActionGroup("Zoom");
		zoomGroup.setPopup(true);
		zoomGroup.add(new ZoomAction(0.1));
		zoomGroup.add(new ZoomAction(0.25));
		zoomGroup.add(new ZoomAction(0.5));
		zoomGroup.add(new ZoomAction(1.0));
		group.add(zoomGroup);
		group.add(new AboutAction());
		Browser.addMenuGroup(8, group);

		ProjectView.registerContextActionProvider(new ProjectViewRefactorings());
		JBuilderRefactoringFactory.register();

		setupKeys(prettyPrintAction, extractMethodAction);
	}


	/**
	 *  Setup the key maps
	 *
	 *@param  prettyPrint    the pretty print action
	 *@param  extractMethod  the extract method action
	 */
	private static void setupKeys(Action prettyPrint, Action extractMethod)
	{
		ModifyKeyBinding m = new ModifyKeyBinding(prettyPrint, extractMethod);
		EditorManager.addPropertyChangeListener(m);
	}


	/**
	 *  Description of the Method
	 */
	private static void cleanJBuilderSetting()
	{
		String dir = FileSettings.getSettingsRoot() + File.separator + ".Refactory";
		String filename = dir + File.separator + "jbuilder.settings";
		File file = new File(filename);
		if (file.exists()) {
			file.delete();
		}
	}
}
