package org.polymap.mosaic.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.ImageRegistryHelper;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 */
public class MosaicUiPlugin 
        extends AbstractUIPlugin {

    public static final String          ID = "org.polymap.mosaic.ui"; //$NON-NLS-1$

	/* The scope of context properties used in this bundle. */
    public static final String          CONTEXT_PROPERTY_SCOPE = ID;
    
    public static final FilterFactory2  ff = CommonFactoryFinder.getFilterFactory2( null );

    private static MosaicUiPlugin       plugin;
	
    private ImageRegistryHelper         images = new ImageRegistryHelper( this );
	

    public static MosaicUiPlugin getDefault() {
        return plugin;
    }

    public void start( BundleContext context ) throws Exception {
        super.start( context );
        plugin = this;
    }

    public void stop( BundleContext context ) throws Exception {
        plugin = null;
        super.stop( context );
    }

    public ImageRegistryHelper images() {
        return images;
    }
    
}
