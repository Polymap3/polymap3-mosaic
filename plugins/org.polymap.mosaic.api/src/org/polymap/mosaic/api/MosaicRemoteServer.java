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

import java.util.ArrayList;
import java.util.List;

import java.net.URL;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;

/**
 * Represents a connection to a remote Mosaic server.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicRemoteServer {

    public static final String          PROP_ROOT_URI = "org.polymap.mosaic.api.rootUri";

    public static final String          ENCODING = "utf-8";

    private static MosaicRemoteServer   instance = new MosaicRemoteServer(); 
    
    public static final MosaicRemoteServer instance() {
        return instance;
    }
    
    
    // instance *******************************************
    
    private StandardFileSystemManager   fsManager;
    
    private String                      rootUri;

    private FileObject                  casesFolder;
    
    
    protected MosaicRemoteServer() {
        try {
            fsManager = new StandardFileSystemManager();
            URL config = MosaicRemoteServer.class.getClassLoader().getResource( "vfs_config.xml" );
            //URL config = MosaicApiPlugin.context().getBundle().getResource( "vfs_config.xml" );
            fsManager.setConfiguration( config );
            fsManager.init();

            rootUri = System.getProperty( PROP_ROOT_URI );
            assert rootUri != null;

            casesFolder = fsManager.resolveFile( rootUri + "/Cases" );
        }
        catch (FileSystemException e) {
            throw new RuntimeException( e );
        }
    }
    

    protected StandardFileSystemManager fsManager() {
        return fsManager;
    }

    
    /**
     *
     * @param query
     * @return 
     * @throws MosaicRemoteException
     */
    public List<MosaicCase> queryCases( MosaicQuery query ) {
        try {
            String url = new StringBuilder( 256 ).append( rootUri ).append( "/Cases" ).toString();
            FileObject rs = fsManager.resolveFile( url );
            List<MosaicCase> result = new ArrayList( 256 );
            for (FileObject f : rs.getChildren()) {
                result.add( new MosaicCase( f ) );
            }
            return result;
        }
        catch (FileSystemException e) {
            throw new MosaicRemoteException( e );
        }
    }
    
    
    /**
     * Creates a new case.
     * 
     * @param name
     * @param description
     * @return The newly created case. Must be {@link MosaicCase#store()}d after all
     *         fields are setup properly.
     */
    public MosaicCase createCase( String name, String description ) {
        assert name != null;
        try {
            FileObject f = fsManager.resolveFile( casesFolder, name );
//            if (f.exists() || f.getType() != FileType.FOLDER) {
//                throw new MosaicRemoteException( "Folder already exists: " + f.getName() );
//            }
            f.createFolder();
            
            MosaicCase result = new MosaicCase( f );
            result.create();
            result.name.set( name );
            result.description.set( description );
            return result;
        }
        catch (FileSystemException e) {
            throw new MosaicRemoteException( e );
        }
    }
}
