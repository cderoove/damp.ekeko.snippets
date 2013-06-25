package damp.ekeko.snippets.gui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import clojure.lang.RT;
import clojure.lang.Symbol;
import ccw.util.osgi.ClojureOSGi;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "damp.ekeko.snippets.gui"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;		
		//startClojureCode(context);
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
	public static Activator getDefault() {
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

	public void startClojureCode(BundleContext bundleContext) throws Exception {
		Bundle b = bundleContext.getBundle();
		ClojureOSGi.require(b, "clojure.test");  
		//ClojureOSGi.require(damp.ekeko.Activator.getDefault().getBundle(), "damp.ekeko");
		ClojureOSGi.require(b, "damp.ekeko");  
		ClojureOSGi.require(b, "damp.ekeko.snippets");
		ClojureOSGi.require(b, "damp.ekeko.snippets.rewrite");
		ClojureOSGi.require(b, "damp.ekeko.snippets.representation");
		ClojureOSGi.require(b, "damp.ekeko.snippets.parsing");
		ClojureOSGi.require(b, "damp.ekeko.snippets.operators");
		ClojureOSGi.require(b, "damp.ekeko.snippets.operatorsrep");
		ClojureOSGi.require(b, "damp.ekeko.snippets.precondition");
		ClojureOSGi.require(b, "damp.ekeko.snippets.querying");
		ClojureOSGi.require(b, "damp.ekeko.snippets.gui");
		ClojureOSGi.require(b, "damp.ekeko.snippets.runtime");
		ClojureOSGi.require(b, "damp.ekeko.snippets.rewrite");
		ClojureOSGi.require(b, "damp.ekeko.snippets.searchspace");
		ClojureOSGi.require(b, "damp.ekeko.snippets.public");
		ClojureOSGi.require(b, "damp.ekeko.snippets.datastore");
	}
}
