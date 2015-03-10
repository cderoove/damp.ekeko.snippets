/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.ide.elixir;

/*<Imports>*/
import org.acm.seguin.ide.common.SingleDirClassDiagramReloader;
import org.acm.seguin.ide.common.SourceBrowser;
import org.acm.seguin.tools.install.RefactoryInstaller;
import org.acm.seguin.uml.loader.ReloaderSingleton;

import com.sun.corba.se.internal.iiop.messages.Message;
/*</Imports>*/

/**
 *  Refactory extension loads the refactory tool into memory
 *
 *@author     Chris Seguin
 *@created    April 4, 2000
 */
public class RefactoryExtension extends PrettyPrinterExtension
		 implements ApplicationBus.ICard {
	private UMLDocManager manager;
	/**
	 *  Stores the base directory for the source code
	 */
	static String base;


	/**
	 *  Gets the Name attribute of the Refactory extension
	 *
	 *@return    The Name value
	 */
	public String getName() {
		return "Refactory";
	}


	/**
	 *  Gets the CardName attribute of the Refactory
	 *
	 *@return    The CardName value
	 */
	public String getCardName() {
		return "Refactory";
	}


	/**
	 *  Initializes the extension
	 *
	 *@param  args  the arguments
	 *@return       true if installed
	 */
	public boolean init(String[] args) {
		FrameManager fm = FrameManager.current();
		if (fm == null) {
			return false;
		}

		(new RefactoryInstaller(true)).run();

		manager = new UMLDocManager();
		fm.addDocManager(manager);

		Folder.addOpenFileFilter(".uml", "Class Diagrams (*.uml)");

		ApplicationBus.addCard(this);

		boolean result = super.init(args);

		ZoomDiagram.tenPercent();
		new UndoMenuItem();
		new ElixirClassDiagramLoader();
		try {
			new ElixirExtractMethod();
		}
		catch (Exception exc) {
		}

		SourceBrowser.set(new ElixirSourceBrowser());

		FrameManager.current().addMenuItem("Script|JRefactory|Extract Method=((method \"extractMethod\" \"org.acm.seguin.ide.elixir.ElixirExtractMethod\"))");
		FrameManager.current().addMenuItem("Script|JRefactory|Reload Diagrams=((method \"reload\" \"org.acm.seguin.ide.elixir.ElixirClassDiagramLoader\"))");
		FrameManager.current().addMenuItem("Script|JRefactory|Undo Refactoring=((method \"undo\" \"org.acm.seguin.ide.elixir.UndoMenuItem\"))");
		FrameManager.current().addMenuItem("Script|JRefactory|Zoom|10%=((method \"tenPercent\" \"org.acm.seguin.ide.elixir.ZoomDiagram\"))");
		FrameManager.current().addMenuItem("Script|JRefactory|Zoom|25%=((method \"twentyfivePercent\" \"org.acm.seguin.ide.elixir.ZoomDiagram\"))");
		FrameManager.current().addMenuItem("Script|JRefactory|Zoom|50%=((method \"fiftyPercent\" \"org.acm.seguin.ide.elixir.ZoomDiagram\"))");
		FrameManager.current().addMenuItem("Script|JRefactory|Zoom|100%=((method \"fullSize\" \"org.acm.seguin.ide.elixir.ZoomDiagram\"))");
		FrameManager.current().addMenuItem("Script|JRefactory|About JRefactory=((method \"run\" \"org.acm.seguin.awt.AboutBox\"))");

		return result;
	}


	/**
	 *  Removes the extension mechanism
	 *
	 *@return    Always returns true
	 */
	public boolean destroy() {
		ApplicationBus.removeCard(this);
		return super.destroy();
	}


	/**
	 *  Listener for GUI change events
	 *
	 *@param  msg  the message
	 */
	public void update(Message msg) {
		SingleDirClassDiagramReloader reloader = manager.getReloader();
		MsgType type = msg.getType();
		if (type == MsgType.PROJECT_OPENED) {
			RefactoryExtension.base = SettingManager.getSetting("WorkRoot");
			reloader.setRootDirectory(RefactoryExtension.base);
			Thread anonymous =
				new Thread() {
					/**
					 *  Main processing method for the RefactoryExtension object
					 */
					public void run() {
						ReloaderSingleton.reload();
					}
				};
			anonymous.start();
		}
		else if (type == MsgType.PROJECT_CLOSED) {
			reloader.clear();
		}
		else if (type == MsgType.DOCUMENT_OPENED) {
			if (msg.getData() instanceof UMLViewManager) {
				UMLViewManager view = (UMLViewManager) msg.getData();
				reloader.add(view.getDiagram());
			}
		}
		else if (type == MsgType.DOCUMENT_CLOSED) {
			if (msg.getData() instanceof UMLViewManager) {
				UMLViewManager view = (UMLViewManager) msg.getData();
				reloader.remove(view.getDiagram());
			}
		}
	}


	/**
	 *  Opportunity to veto a message
	 *
	 *@param  msg  the message
	 */
	public void veto(Message msg) {
		// no veto
	}
}
