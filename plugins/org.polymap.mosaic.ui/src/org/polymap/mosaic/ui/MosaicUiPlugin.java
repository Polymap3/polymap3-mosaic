package org.polymap.mosaic.ui;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;
import org.osgi.framework.BundleContext;

import com.google.common.base.Supplier;

import org.eclipse.swt.graphics.Color;

import org.eclipse.rwt.graphics.Graphics;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.ImageRegistryHelper;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.PlainLazyInit;

/**
 * The activator class controls the plug-in life cycle.
 */
public class MosaicUiPlugin 
        extends AbstractUIPlugin {

    public static final String          ID = "org.polymap.mosaic.ui"; //$NON-NLS-1$

	/* The scope of context properties used in this bundle. */
    public static final String          CONTEXT_PROPERTY_SCOPE = ID;

    public static final String          CSS_PREFIX = "mosaic-case-";
    public static final String          CSS_SUBMIT = CSS_PREFIX + "submit";
    public static final String          CSS_DISCARD = CSS_PREFIX + "discard";
    public static final String          CSS_STATUS_SECTION = CSS_PREFIX + "status";
    public static final String          CSS_ACTION_SECTION_ACTIVE = CSS_PREFIX + "action-active";
    public static final String          CSS_ACTION_SECTION_DEACTIVE = CSS_PREFIX + "action-deactive";
    public static final String          CSS_TOOLBAR_SECTION = CSS_PREFIX + "toolbar";
    public static final String          CSS_CONTENT_SECTION = CSS_PREFIX + "content";

    /** Mosaic case state: NEW */
    public static final Lazy<Color>     COLOR_NEW = new PlainLazyInit( new ColorSupplier( 188, 166, 0 ) );
    public static final Lazy<Color>     COLOR_CLOSED = new PlainLazyInit( new ColorSupplier( 110, 176, 46 ) );
    public static final Lazy<Color>     COLOR_OPEN = new PlainLazyInit( new ColorSupplier( 255, 204, 0 ) );
    public static final Lazy<Color>     COLOR_RED = new PlainLazyInit( new ColorSupplier( 211, 37, 22 ) );
    public static final Lazy<Color>     COLOR_STATUS_FOREGROUND = new PlainLazyInit( new ColorSupplier( 255, 255, 255 ) );

    public static final FilterFactory2  ff = CommonFactoryFinder.getFilterFactory2( null );

    private static MosaicUiPlugin       plugin;
	
    public static MosaicUiPlugin getDefault() {
        return plugin;
    }

    // instance *******************************************
    
    private ImageRegistryHelper         images = new ImageRegistryHelper( this );
	

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

    
    /**
     *  
     */
    static class ColorSupplier
            implements Supplier<Color> {
        private int r, g, b;
    
        public ColorSupplier( int r, int g, int b ) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    
        @Override
        public Color get() {
            return Graphics.getColor( r, g, b );
        }
    }
    
}
