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

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;

import org.eclipse.core.runtime.IPath;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentPutable;
import org.polymap.service.fs.spi.IMakeFolder;
import org.polymap.service.fs.spi.NotAuthorizedException;

import org.polymap.mosaic.server.model.MosaicCase;
import org.polymap.mosaic.server.model.MosaicRepository;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicCasesFolder
        extends DefaultContentFolder
        implements IContentPutable, IMakeFolder {

    private static Log log = LogFactory.getLog( MosaicCasesFolder.class );
    
    public MosaicCasesFolder( String name, IPath parentPath, IContentProvider provider ) {
        super( name, parentPath, provider, null );
    }


    public List<MosaicCaseFolder> getChildren( Map<String,String> requestParams ) {
        Query<MosaicCase> query = MosaicRepository.instance().findEntities( MosaicCase.class, null, 0, -1 );
        List<MosaicCaseFolder> result = new ArrayList();
        for (MosaicCase entity : query) {
            result.add( new MosaicCaseFolder( entity.getName(), getPath(), getProvider(), entity ) ); 
        }
        return result;
    }

    
    @Override
    public IContentFile createNew( String newName, InputStream inputStream, Long length, String contentType )
            throws IOException, NotAuthorizedException, BadRequestException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    
    @Override
    public IContentFolder createFolder( String newName ) {
        getSite().invalidateFolder( this );
        MosaicCaseFolder result = new MosaicCaseFolder( newName, getParentPath(), getProvider() );
        result.create();
        return result;
    }

}
