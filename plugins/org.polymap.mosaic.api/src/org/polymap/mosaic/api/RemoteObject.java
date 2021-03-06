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

import java.util.ConcurrentModificationException;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;

import org.json.JSONObject;
import org.json.JSONTokener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

/**
 * Represents a remote object with JSON content. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class RemoteObject {

    private static Log log = LogFactory.getLog( RemoteObject.class );

    public static final String  OBJECT_FILENAME = "object.json";
    
    /** The folder of the case. */
    private FileObject          folder;
    
    /** Lazily initialized by {@link #json()}. */
    private JSONObject          json;

    
    /** 
     * No args ctor for the internal query interface. 
     */
    protected RemoteObject() {
        create();
    }
    
    
    protected RemoteObject( FileObject folder ) {
        assert folder != null;
        this.folder = folder;
    }


    /**
     * The URL representing this object in the Mosaic server. 
     *
     * @throws MosaicRemoteException
     */
    public URL getURL() {
        try {
            return folder.getURL();
        }
        catch (FileSystemException e) {
            throw new MosaicRemoteException( e );
        }
    }
    
    
    /**
     * Send all changes to the underlying Mosaic server. 
     *
     * @throws MosaicRemoteException
     */
    public void store() throws ConcurrentModificationException {
        Writer out = null;
        try {
            FileObject f = folder.getChild( OBJECT_FILENAME );
            if (f == null) {
                f = MosaicRemoteServer.instance().fsManager().resolveFile( folder, OBJECT_FILENAME );
                f.createFile();
            }
            
            log.info( "JSON:" + json.toString( 4 ) );
            out = new OutputStreamWriter( f.getContent().getOutputStream(), MosaicRemoteServer.ENCODING );
            json.write( out );
        }
        // FIXME check for concurrent modifications
        catch (Exception e) {
            throw new MosaicRemoteException( e );
        }
        finally {
            closeQuietly( out );
        }
    }
    
    
    /**
     * Call this on a newly created case which does not have a server file yet. It
     * creates an empty JSON object.
     */
    protected void create() {
        json = new JSONObject();
    }


    /**
     * 
     *
     * @return Newly requested or cached {@link JSONObject}.
     * @throws MosaicRemoteException
     */
    protected JSONObject json() {
        if (json == null) {
            InputStream in = null;
            try {
                FileObject f = folder.getChild( OBJECT_FILENAME );
                in = f.getContent().getInputStream();
                json = new JSONObject( new JSONTokener( new InputStreamReader( in, MosaicRemoteServer.ENCODING ) ) );
            }
            catch (Exception e) {
                throw new MosaicRemoteException( e );
            }
            finally {
                closeQuietly( in );
            }
        }
        return json;
    }

    
    protected void closeQuietly( Closeable stream ) {
        if (stream != null) {
            try { stream.close(); } catch (IOException e) { }
        }
    }
    
    
    /**
     * A property of a {@link RemoteObject}.
     */
    public static interface Property<T> {
        
        public T get();
        
        public void set( T value );
    }

    
    /**
     * 
     */
    public static class JsonProperty<T>
            implements Property<T> {
        
        private RemoteObject    obj;
        
        private String          key;
        
        private Class<T>        type;

        protected JsonProperty( RemoteObject obj, String key, Class<T> type ) {
            assert obj!= null && key != null && type != null;
            this.obj = obj;
            this.key = key;
            this.type = type;
        }
        
        @Override
        public T get() {
            if (String.class.equals( type )) {
                return (T)obj.json().optString( key );                
            }
            throw new RuntimeException( "Property type not supported: " + type );
        }

        @Override
        public void set( T value ) {
            if (value == null 
                    || value instanceof String
                    || value instanceof Integer
                    || value instanceof Long
                    || value instanceof Float
                    || value instanceof Double
                    || value instanceof Boolean) {
                obj.json().remove( key );
                obj.json().put( key, value );
            }
            else {
                throw new RuntimeException( "Property type not supported: " + type );
            }
        }
    }


    /**
     * 
     */
    public static class ImmutableProperty<T>
            implements Property<T> {
        
        private Property    delegate;

        protected ImmutableProperty( Property delegate ) {
            assert delegate != null;
            this.delegate = delegate;
        }
        
        @Override
        public T get() {
            return (T)delegate.get();
        }

        @Override
        public void set( T value ) {
            throw new UnsupportedOperationException( "Property is immutable." );
        }

        /**
         * Allows to init this immutable property without getting an exception.
         */
        public void init( T value ) {
            delegate.set( value );
        }
    }

}
