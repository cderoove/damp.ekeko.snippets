package damp.ekeko.snippets;

import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import ccw.util.osgi.ClojureOSGi;

/**
 * The activator class controls the plug-in life cycle
 */
public class EkekoSnippetsPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "damp.ekeko.snippets.plugin"; //$NON-NLS-1$


	// The shared instance
	private static EkekoSnippetsPlugin plugin;
	
	/**
	 * The constructor
	 */
	public EkekoSnippetsPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;		
		startClojureCode(context);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static EkekoSnippetsPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public static Image IMG_ADD;   
	public static Image IMG_DELETE; 
	public static Image IMG_DELETE_DISABLED;
	
	
	public static Image IMG_TEMPLATE; 
	public static Image IMG_TEMPLATE_ADD;   
	public static Image IMG_TEMPLATE_EDIT;
	public static Image IMG_TEMPLATE_DELETE; 
	public static Image IMG_TEMPLATE_COPY_FROM_LHS; 


	
	
	public static Image IMG_TEMPLATE_MATCH; 
	public static Image IMG_TEMPLATE_INSPECT; 


	public static Image IMG_TRANSFORMATION; 
	public static Image IMG_TRANSFORM; 

	public static Image IMG_OPERATOR_APPLY; 
	
	public static Image IMG_INTENDED_RESULTS;
	public static Image IMG_RECOMMENDATION;

	public static Image IMG_NEGATIVE_EXAMPLE;
	public static Image IMG_POSITIVE_EXAMPLE;
	public static Image IMG_COLUMN_ADD;
	public static Image IMG_COLUMN_DELETE;
	
	public static Image IMG_SEARCH;
	public static Image IMG_RESULTS_IMPORT;


	public static Image IMG_RESULTS_REFRESH;

	public static Image IMG_ANCHOR;
	public static Image IMG_BACK;
	public static Image IMG_HISTORY;
	public static Image IMG_PROPERTIES;


	static {
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		
		IMG_ADD = sharedImages.getImage(ISharedImages.IMG_OBJ_ADD);
		IMG_DELETE = sharedImages.getImage(ISharedImages.IMG_ETOOL_DELETE);
		IMG_DELETE_DISABLED = sharedImages.getImage(ISharedImages.IMG_ETOOL_DELETE_DISABLED);
		IMG_BACK = sharedImages.getImage(ISharedImages.IMG_TOOL_BACK);
		
		IMG_TEMPLATE = getImageDescriptor("icons/notebooks.png").createImage();
		IMG_TEMPLATE_ADD = getImageDescriptor("icons/notebook--plus.png").createImage();
		IMG_TEMPLATE_EDIT = getImageDescriptor("icons/notebook--pencil.png").createImage();

		IMG_TEMPLATE_DELETE = getImageDescriptor("icons/notebook--minus.png").createImage();
		IMG_TEMPLATE_COPY_FROM_LHS = getImageDescriptor("icons/notebook--arrow.png").createImage();
				
		IMG_TEMPLATE_MATCH = getImageDescriptor("icons/occluder.png").createImage();
		IMG_TEMPLATE_INSPECT = getImageDescriptor("icons/magnifier.png").createImage();
		
		IMG_TRANSFORMATION = getImageDescriptor("icons/cog.png").createImage();
		IMG_TRANSFORM = getImageDescriptor("icons/cog_go.png").createImage();
		
		IMG_OPERATOR_APPLY = getImageDescriptor("icons/tick.png").createImage();
		
		IMG_INTENDED_RESULTS = getImageDescriptor("icons/spectacle.png").createImage();
		IMG_RECOMMENDATION = getImageDescriptor("icons/dna.png").createImage();
		IMG_HISTORY = getImageDescriptor("icons/history.png").createImage();

		IMG_POSITIVE_EXAMPLE = getImageDescriptor("icons/plus-white.png").createImage();
		IMG_NEGATIVE_EXAMPLE = getImageDescriptor("icons/minus-white.png").createImage();
		
		IMG_COLUMN_ADD = getImageDescriptor("icons/table-insert-column.png").createImage();
		IMG_COLUMN_DELETE = getImageDescriptor("icons/table-delete-column.png").createImage();

		IMG_SEARCH = getImageDescriptor("icons/brain.png").createImage();
		IMG_RESULTS_IMPORT = getImageDescriptor("icons/table-import.png").createImage();
		IMG_RESULTS_REFRESH = getImageDescriptor("icons/arrow_refresh.png").createImage();
		
		
		IMG_ANCHOR = getImageDescriptor("icons/anchor.png").createImage();
		IMG_PROPERTIES = getImageDescriptor("icons/property.png").createImage();
		
				
	}
	
	public static Font getEditorFont() {
		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		ITheme currentTheme = themeManager.getCurrentTheme();
		FontRegistry fontRegistry = currentTheme.getFontRegistry();
		return fontRegistry.get(PreferenceConstants.EDITOR_TEXT_FONT);
	}
	
	public void startClojureCode(BundleContext bundleContext) throws Exception {
		Bundle b = bundleContext.getBundle();
		String[] filenames= { "damp.ekeko", "damp.ekeko.snippets", "damp.ekeko.snippets.geneticsearch.search" };	
		for(String filename : filenames) {
			try {
				ClojureOSGi.require(b, filename);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
