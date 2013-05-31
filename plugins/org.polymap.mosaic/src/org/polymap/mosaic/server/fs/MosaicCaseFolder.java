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

import org.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.qi4j.QiModule.EntityCreator;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentPutable;
import org.polymap.service.fs.spi.NotAuthorizedException;

import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.MosaicCase;
import org.polymap.mosaic.server.model.MosaicRepository;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicCaseFolder
        extends DefaultContentFolder
        implements IContentPutable {

    private static Log log = LogFactory.getLog( MosaicCaseFolder.class );
    
    private IMosaicCase                 entity;
    
    
    public MosaicCaseFolder( final String name, IPath parentPath, IContentProvider provider ) {
        super( name, parentPath, provider, null );
    }
    
    
    @Override
    public IMosaicCase getSource() {
        return entity;
    }


    public List<? extends IContentNode> getChildren( Map<String, String> params ) {
        List<? extends IContentNode> result = new ArrayList();
        return result;
    }


    protected void create() {
        try {
            log.info( "New MosaicCase: " + getName() );
            
            final MosaicRepository repo = MosaicRepository.instance();
            entity = repo.newEntity( MosaicCase.class, getName(), new EntityCreator<MosaicCase>() {
                public void create( MosaicCase prototype ) throws Exception {
                    prototype.name().set( getName() );
                }
            });
            repo.commitChanges();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    @Override
    public IContentFile createNew( String newName, InputStream in, Long length, String contentType )
            throws IOException, NotAuthorizedException, BadRequestException {
        assert newName.equals( MosaicCaseFile.NAME );
        try {            
            String json = IOUtils.toString( in );
            log.info( "newName: " + newName + ", JSON: " + json );
            
            final MosaicRepository repo = MosaicRepository.instance();
            entity = repo.findEntity( MosaicCase.class, getName() );
            // client API may decide to create and write in 2 separated requests
            if (json != null && json.length() > 0) {
                entity.decodeJsonState( new JSONObject( json ), repo, false );
            }
            return new MosaicCaseFile( newName, getPath(), getProvider(), entity );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
        finally {
            getSite().invalidateFolder( getSite().getFolder( getParentPath() ) );
        }
    }

}
