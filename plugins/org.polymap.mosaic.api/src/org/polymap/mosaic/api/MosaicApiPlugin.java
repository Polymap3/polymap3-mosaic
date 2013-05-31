/* 
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3.0 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.mosaic.api;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicApiPlugin 
        implements BundleActivator {

    public static final String          ID = "org.polymap.mosaic.api";
    
    private static BundleContext        context;
	
    private static MosaicApiPlugin      instance;

    
    public static BundleContext context() {
        return context;
    }

    public static MosaicApiPlugin instance() {
        return instance;
    }

	// instance *******************************************
	
    public void start( BundleContext bundleContext ) throws Exception {
        MosaicApiPlugin.context = bundleContext;
        MosaicApiPlugin.instance = this;
	}

	public void stop( BundleContext bundleContext ) throws Exception {
		MosaicApiPlugin.context = null;
        MosaicApiPlugin.instance = null;
	}

}
