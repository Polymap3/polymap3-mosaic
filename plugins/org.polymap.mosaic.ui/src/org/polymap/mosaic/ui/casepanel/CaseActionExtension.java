/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.mosaic.ui.casepanel;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

import org.polymap.mosaic.ui.MosaicUiPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class CaseActionExtension {

    private static Log log = LogFactory.getLog( CaseActionExtension.class );
    
    public static final String  POINT_ID = "caseActions";

    
    public static List<CaseActionExtension> all() {
        IConfigurationElement[] elms = Platform.getExtensionRegistry()
                .getConfigurationElementsFor( MosaicUiPlugin.ID, POINT_ID );
        
        return Lists.transform( Arrays.asList( elms ), new Function<IConfigurationElement,CaseActionExtension>() {
            public CaseActionExtension apply( IConfigurationElement elm ) {
                return new CaseActionExtension( elm );
            }
        });
    }
    
    
    // instance *******************************************
    
    private IConfigurationElement   elm;
    
    protected CaseActionExtension( IConfigurationElement elm ) {
        this.elm = elm;
    }

    public ICaseAction newInstance() {
        try {
            return (ICaseAction)elm.createExecutableExtension( "class" );
        }
        catch (CoreException e) {
            throw new RuntimeException( e );
        }
    }

    public int getPriority() {
        String result = elm.getAttribute( "priority" );
        return result != null ? Integer.parseInt( result ) : 0; 
    }

    public String getName() {
        return elm.getAttribute( "name" );
    }
    
    public String getDescription() {
        return elm.getAttribute( "description" );
    }
    
    public ImageDescriptor getIcon() {
        String path = elm.getAttribute( "icon" );
        if (path == null) {
            return null;
        }
        
        String contributor = elm.getDeclaringExtension().getContributor().getName();
//        Bundle bundle = Platform.getBundle( contributor );
//        String pluginId = bundle.getSymbolicName();
        
        ImageRegistry images = MosaicUiPlugin.getDefault().getImageRegistry();
        String key = contributor + "." + path;
        ImageDescriptor result = images.getDescriptor( key );
        if (result == null) {
            result = MosaicUiPlugin.imageDescriptorFromPlugin( contributor, path );
            images.put( key, result );
        }
        return result;
    }
    
    public boolean isCaseChangeAction() {
        String result = elm.getAttribute( "icon" );
        return result != null ? Boolean.parseBoolean( result ) : false;
        
    }
}
