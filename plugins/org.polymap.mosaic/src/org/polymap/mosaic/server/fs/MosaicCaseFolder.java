/* 
 * polymap.org
 * Copyright (C) 2013, Polymap GmbH. All rights reserved.
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

import org.polymap.core.project.IMap;
import org.polymap.service.fs.providers.ProjectContentProvider;
import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model2.MosaicRepository2;

/**
 * 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicCaseFolder
        extends DefaultContentFolder {

    private static Log log = LogFactory.getLog( MosaicCaseFolder.class );
    
    private IMosaicCase                 mosaicCase;
    
    
    public MosaicCaseFolder( String name, IPath parentPath, IContentProvider provider ) {
        super( name, parentPath, provider, null );
    }
    
    
    public MosaicCaseFolder( String name, IPath parentPath, IContentProvider provider, IMosaicCase entity ) {
        super( name, parentPath, provider, entity );
        this.mosaicCase = entity;
    }


    @Override
    public IMosaicCase getSource() {
        return mosaicCase;
    }


    public List<? extends IContentNode> getChildren( Map<String, String> params ) {
        List<IContentNode> result = new ArrayList();
        
        IMap metaDataMap = mosaicCase.getMetaDataMap();
        result.add( new ProjectContentProvider.MapFolder( getPath(), getProvider(), metaDataMap ) );
        
        IMap dataMap = mosaicCase.getDataMap();
        result.add( new ProjectContentProvider.MapFolder( getPath(), getProvider(), dataMap ) );
        
//        result.add( new MosaicCaseFile( getPath(), getProvider(), getSource() ) );
//        result.add( new MosaicCaseFile( getPath(), getProvider(), getSource() ) );
//        result.add( new EventsFolder( getPath(), getProvider(), getSource() ) );
        return result;
    }


    protected void create() {
        try {
            log.info( "New MosaicCase2: " + getName() );
            MosaicRepository2 repo = MosaicRepository2.instance();
            mosaicCase = repo.newCase( getName() );
            repo.commitChanges();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


//    @Override
//    public IContentFile createNew( String newName, InputStream in, Long length, String contentType )
//            throws IOException, NotAuthorizedException, BadRequestException {
//        assert newName.equals( MosaicCaseFile.NAME );
//        try {            
//            String json = IOUtils.toString( in );
//            log.info( "newName: " + newName + ", JSON: " + json );
//            
//            final MosaicRepository2 repo = MosaicRepository2.instance();
//            mosaicCase = repo.entity( IMosaicCase.class, getName() );
//            // client API may decide to create and write in 2 separated requests
//            if (json != null && json.length() > 0) {
//                mosaicCase.decodeJsonState( new JSONObject( json ), repo, false );
//            }
//            return new MosaicCaseFile( getPath(), getProvider(), mosaicCase );
//        }
//        catch (Exception e) {
//            throw new RuntimeException( e );
//        }
//        finally {
//            getSite().invalidateFolder( getSite().getFolder( getParentPath() ) );
//        }
//    }
    
}
