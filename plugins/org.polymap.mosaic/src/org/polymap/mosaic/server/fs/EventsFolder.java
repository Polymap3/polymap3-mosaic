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
package org.polymap.mosaic.server.fs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;

import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;

import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicCaseEvent;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EventsFolder
        extends DefaultContentFolder
        implements IContentFolder {

    private static Log log = LogFactory.getLog( EventsFolder.class );

    public static final String          NAME = "Events";
    
    
    public EventsFolder( IPath parentPath, IContentProvider provider, IMosaicCase source ) {
        super( NAME, parentPath, provider, source );
        assert source != null;
    }


    @Override
    public IMosaicCase getSource() {
        return (IMosaicCase)super.getSource();
    }


    public List<? extends IContentNode> getChildren( Map<String,String> params ) {
        List<IContentNode> result = new ArrayList();
        for (IMosaicCaseEvent event : getSource().getEvents()) {
            result.add( new EventFolder( getPath(), getProvider(), event ) );            
        }
        return result;
    }

}
