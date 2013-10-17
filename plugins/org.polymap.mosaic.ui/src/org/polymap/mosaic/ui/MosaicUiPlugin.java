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

    public static final String          ROLE_SCHACHTSCHEIN = "Schachtschein beantragen";
    public static final String          ROLE_MA = "Interner Sachbearbeiter";
    
    public static final String          CSS_PREFIX = "mosaic-case-";
    public static final String          CSS_SUBMIT = CSS_PREFIX + "submit";
    public static final String          CSS_DISCARD = CSS_PREFIX + "discard";
    public static final String          CSS_STATUS_SECTION = CSS_PREFIX + "status";
    public static final String          CSS_ACTION_SECTION_ACTIVE = CSS_PREFIX + "action-active";
    public static final String          CSS_ACTION_SECTION_DEACTIVE = CSS_PREFIX + "action-deactive";
    public static final String          CSS_TOOLBAR_SECTION = CSS_PREFIX + "toolbar";
    public static final String          CSS_CONTENT_SECTION = CSS_PREFIX + "content";
    
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
