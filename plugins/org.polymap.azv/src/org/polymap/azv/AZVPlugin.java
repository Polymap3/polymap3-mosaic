/*
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.azv;

import org.osgi.framework.BundleContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AZVPlugin
        extends AbstractUIPlugin {

    private static Log log = LogFactory.getLog( AZVPlugin.class );

    public static final String      ID = "org.polymap.azv";

    private static AZVPlugin        instance;

    public static AZVPlugin instance() {
        assert instance != null;
        return instance;
    }

    // instance *******************************************

    @Override
    public void start( BundleContext context ) throws Exception {
        super.start( context );
        instance = this;
    }

    @Override
    public void stop( BundleContext context ) throws Exception {
        super.stop( context );
        instance = null;
    }

}
