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

import java.lang.reflect.Field;
import java.net.URL;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;

import com.google.common.base.Joiner;

import org.polymap.mosaic.api.RemoteObject.ImmutableProperty;
import org.polymap.mosaic.api.RemoteObject.JsonProperty;
import org.polymap.mosaic.api.RemoteObject.Property;

/**
 * Represents a connection to a remote Mosaic server.
 * <p/>
 * There is no session or user transaction concept in the Mosaic API. Multiple
 * threads can access the repository concurrently. Every {@link RemoteObject} type
 * exposes a <code>store()</code> method.
 * <p/>
 * XXX There is no cache of the remote files and/or folders. Do we need one?
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicRemoteServer {

    public static final String          PROP_ROOT_URI = "org.polymap.mosaic.api.rootUri";

    public static final String          ENCODING = "utf-8";

    public static final String          FOLDER_CASES = "Cases";
    public static final String          FOLDER_EVENTS = "Events";
    public static final String          FOLDER_SEARCH = "Search";

    public static final String          QUERY_PARAM = "query";
    public static final String          FIRSTRESULT_PARAM = "firstResult";
    public static final String          MAXRESULTS_PARAM = "maxResults";

    private static MosaicRemoteServer   instance = new MosaicRemoteServer(); 
    
    public static final MosaicRemoteServer instance() {
        return instance;
    }
    
    
    // instance *******************************************
    
    private StandardFileSystemManager   fsManager;
    
    private String                      rootUri;

    private FileObject                  allCasesFolder;
    
    
    protected MosaicRemoteServer() {
        try {
            fsManager = new StandardFileSystemManager();
            ClassLoader cl = MosaicRemoteServer.class.getClassLoader();
            URL config = cl.getResource( "vfs_config.xml" );
            //URL config = MosaicApiPlugin.context().getBundle().getResource( "vfs_config.xml" );
            fsManager.setConfiguration( config );
            fsManager.init();

            rootUri = System.getProperty( PROP_ROOT_URI );
            assert rootUri != null;

            allCasesFolder = fsManager.resolveFile( Joiner.on( "/" ).join( rootUri, FOLDER_CASES ) );
        }
        catch (FileSystemException e) {
            throw new RuntimeException( e );
        }
    }
    

    protected StandardFileSystemManager fsManager() {
        return fsManager;
    }

    
    private <T extends RemoteObject> T injectProperties( final T obj ) {
        return (T)new PropertyInjector( obj ) {
            protected Property createProperty( Field field, Class propertyType ) {
                JsonName a = field.getAnnotation( JsonName.class );
                String name = a != null ? a.value() : field.getName();
                Property result = new JsonProperty( obj, name, propertyType );
                
                if (field.getAnnotation( Immutable.class ) != null ) {
                    result = new ImmutableProperty( result );
                }
                return result;
            }
        }.run();    
    }
    
    
    /**
    *
    * @param query
    * @return The queried cases, or an empty list if result set is empty.
    * @throws MosaicRemoteException
    */
   public List<MosaicCase> queryCases( MosaicQuery query ) {
       assert query != null;
       try {
           List<MosaicCase> result = new ArrayList( 256 );

           String queryString = new String( Base64.encodeBase64( query.getQueryString().getBytes( ENCODING ) ) );
           StringBuilder uri = new StringBuilder( 256 )
                   .append( rootUri ).append( "/" ).append( FOLDER_CASES )
                   .append( "/search-" ).append( queryString );

//           if (query.getFirstResult() > 0) {
//               uri.append( "&firstResult=" ).append( query.getFirstResult() );
//           }
//           if (query.getMaxResults() < Integer.MAX_VALUE) {
//               uri.append( "&maxResults=" ).append( query.getMaxResults() );
//           }

           FileObject folder = fsManager.resolveFile( uri.toString() );
           for (FileObject f : folder.getChildren()) {
               // FIXME Milton delivers a 'shadow' folder with that name; or something wrong with the provider?
               if (!f.getName().getBaseName().endsWith( queryString )) {
                   result.add( injectProperties( new MosaicCase( f ) ) );
               }
           }
           return result;
       }
       catch (FileSystemException e) {
           throw new MosaicRemoteException( e );
       }
       catch (Exception e) {
           throw new RuntimeException( e );
       }
   }
   
   
    /**
     * 
     * @param query
     * @return A {@link MosaicCase} instance representing the case for the given id,
     *         or null if the id does not exist.
     * @throws MosaicRemoteException
     */
   public MosaicCase findCase( String id ) {
       try {
           String url = Joiner.on( "/" ).join( rootUri, FOLDER_CASES, id );
           FileObject f = fsManager.resolveFile( url );
           return f.exists() ? injectProperties( new MosaicCase( f ) ) : null;
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
            FileObject f = fsManager.resolveFile( allCasesFolder, name );
//            if (f.exists() || f.getType() != FileType.FOLDER) {
//                throw new MosaicRemoteException( "Folder already exists: " + f.getName() );
//            }
            f.createFolder();
            
            MosaicCase result = injectProperties( new MosaicCase( f ) );
            result.create();
            ((ImmutableProperty)result.id).init( name );
            result.name.set( name );
            result.description.set( description );
            return result;
        }
        catch (FileSystemException e) {
            throw new MosaicRemoteException( e );
        }
    }
    
    
    /**
     *
     * @param query
     * @return 
     * @throws MosaicRemoteException
     */
    protected List<MosaicCaseEvent> queryEvents( MosaicCase mosaicCase ) {
        try {
            String url = Joiner.on( "/" ).join( rootUri, FOLDER_CASES, mosaicCase.name.get(), FOLDER_EVENTS );

            FileObject rs = fsManager.resolveFile( url );
            List<MosaicCaseEvent> result = new ArrayList( 256 );
            for (FileObject f : rs.getChildren()) {
                // FIXME Milton delivers a 'shadow' folder with that name; or something wrong with the provider?
                if (!f.getName().getBaseName().equals( FOLDER_EVENTS )) {
                    result.add( injectProperties( new MosaicCaseEvent( f ) ) );
                }
            }
            return result;
        }
        catch (FileSystemException e) {
            throw new MosaicRemoteException( e );
        }
    }

}
