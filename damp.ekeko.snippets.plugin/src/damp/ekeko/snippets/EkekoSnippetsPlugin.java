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
	public static Image IMG_TEMPLATE_DELETE; 

	
	
	public static Image IMG_TEMPLATE_MATCH; 
	public static Image IMG_TEMPLATE_INSPECT; 


	public static Image IMG_TRANSFORMATION; 
	public static Image IMG_TRANSFORM; 

	public static Image IMG_OPERATOR_APPLY; 

	
	static {
		ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
		
		IMG_ADD = sharedImages.getImage(ISharedImages.IMG_OBJ_ADD);
		IMG_DELETE = sharedImages.getImage(ISharedImages.IMG_ETOOL_DELETE);
		IMG_DELETE_DISABLED = sharedImages.getImage(ISharedImages.IMG_ETOOL_DELETE_DISABLED);
		
		IMG_TEMPLATE = getImageDescriptor("icons/notebooks.png").createImage();
		IMG_TEMPLATE_ADD= getImageDescriptor("icons/notebook--plus.png").createImage();
		IMG_TEMPLATE_DELETE = getImageDescriptor("icons/notebook--minus.png").createImage();
		
		IMG_TEMPLATE_MATCH = getImageDescriptor("icons/occluder.png").createImage();
		IMG_TEMPLATE_INSPECT = getImageDescriptor("icons/magnifier.png").createImage();
		
		IMG_TRANSFORMATION = getImageDescriptor("icons/cog.png").createImage();
		IMG_TRANSFORM = getImageDescriptor("icons/cog_go.png").createImage();
		
		IMG_OPERATOR_APPLY = getImageDescriptor("icons/tick.png").createImage();

				
	}
	
	public static Font getEditorFont() {
		IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
		ITheme currentTheme = themeManager.getCurrentTheme();
		FontRegistry fontRegistry = currentTheme.getFontRegistry();
		return fontRegistry.get(PreferenceConstants.EDITOR_TEXT_FONT);
	}
	
	public void startClojureCode(BundleContext bundleContext) throws Exception {
		Bundle b = bundleContext.getBundle();
		String[] filenames= { "damp.ekeko", "damp.ekeko.snippets" };	
		for(String filename : filenames) {
			try {
				ClojureOSGi.require(b, filename);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}