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
package org.polymap.atlas.internal;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.core.runtime.SessionSingleton;

import org.polymap.atlas.AtlasPlugin;
import org.polymap.atlas.IAtlasApplicationLayouter;
import org.polymap.atlas.IAtlasPanel;
import org.polymap.atlas.IAtlasPanelContext;
import org.polymap.atlas.IAtlasPanelSite;

/**
 * Factory of components of the extensible Atlas UI. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AtlasComponentFactory
        extends SessionSingleton {

    private static Log log = LogFactory.getLog( AtlasComponentFactory.class );
    
    public static final String          APPLICATION_LAYOUTER_EXTENSION_POINT = "applicationLayouters";
    public static final String          PANEL_EXTENSION_POINT = "panels";
    
    public static AtlasComponentFactory instance() {
        return instance( AtlasComponentFactory.class );
    }
    
    
    // instance *******************************************
    
    protected AtlasComponentFactory() {
    }
    
    
    public IAtlasApplicationLayouter createApplicationLayouter() {
        try {
            IConfigurationElement[] elms = Platform.getExtensionRegistry()
                    .getConfigurationElementsFor( AtlasPlugin.PLUGIN_ID, APPLICATION_LAYOUTER_EXTENSION_POINT );
            assert elms.length == 1;
            
            return (IAtlasApplicationLayouter)elms[0].createExecutableExtension( "class" );
        }
        catch (CoreException e) {
            throw new RuntimeException( e );
        }
    }
    
    
    public List<IAtlasPanel> createPanels( IAtlasPanelSite site, IAtlasPanelContext context ) {
        IConfigurationElement[] elms = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( AtlasPlugin.PLUGIN_ID, PANEL_EXTENSION_POINT );

        List<IAtlasPanel> result = new ArrayList();
        for (IConfigurationElement elm : elms) {
            try {
                IAtlasPanel panel = (IAtlasPanel)elms[0].createExecutableExtension( "class" );
                if (panel.init( site, context )) {
                    result.add( panel );
                }
            }
            catch (Exception e) {
                log.error( "Error while initializing panel: " + elm.getName(), e );
            }
        }
        return result;
    }
    
}
