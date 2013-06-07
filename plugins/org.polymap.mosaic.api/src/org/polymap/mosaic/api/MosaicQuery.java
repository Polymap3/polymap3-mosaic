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

import java.util.Date;

import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.mosaic.api.RemoteObject.Property;

/**
 * Query remote objects on the server.
 * <p/><b>Example:</b>
 * <pre>
 *    MosaicQuery<MosaicCase> query = MosaicQuery.forType( MosaicCase.class );
 *    query.eq().name.set( name );
 *    query.match().description.set( "der*" );
 * </pre>
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicQuery<T extends RemoteObject> {

    private static Log log = LogFactory.getLog( MosaicQuery.class );
    
    public static <T extends RemoteObject> MosaicQuery<T> forType( Class<T> type ) {
        return new MosaicQuery( type );    
    }
    
    // instance *******************************************
    
    private int             firstResult = 0;
    
    private int             maxResults = Integer.MAX_VALUE;
    
    private Class<T>        type;
    
    private String          junction = "AND";
    
    private StringBuilder   queryString = new StringBuilder( 256 );
    

    /**
     * 
     * @param type The type of the remote object to query.
     */
    protected MosaicQuery( Class<T> type ) {
        this.type = type;
    }
    
    @Override
    public String toString() {
        return queryString.toString();
    }
    
    public int getFirstResult() {
        return firstResult;
    }
    
    public MosaicQuery<T> setFirstResult( int firstResult ) {
        this.firstResult = firstResult;
        return this;
    }
    
    public int getMaxResults() {
        return maxResults;
    }

    public MosaicQuery<T> setMaxResults( int maxResults ) {
        this.maxResults = maxResults;
        return this;
    }

    public String getQueryString() {
        return queryString.toString();
    }


    /**
     * Query property equals.
     * 
     * @return Newly created prototype of the queried class. Use the setters on one
     *         of the {@link Property} members to specify the value to query.
     */
    public T eq() {
        return newPrototype( new PropertySupplier() {
            public Property get( final String name, Class propertyType ) {
                return new QueryProperty() {
                    
                    public void set( Object value ) {
                        if (Date.class.isAssignableFrom( type )) {
                            append( name, String.valueOf( ((Date)value).getTime() ) );                
                        }
                        else {
                            append( name, value.toString() );
                        }
                    }

                    protected void append( String fieldName, String value ) {
                        if (queryString.length() > 0) {
                            queryString.append( " " ).append( junction ).append( " " );
                        }
                        queryString.append( fieldName ).append( ":" ).append( value );
                    }
                };
            };
        });
    }
    
    
    /**
     * Query property matches a given wildcard string.
     * 
     * @return Newly created prototype of the queried class. Use the setters on one
     *         of the {@link Property} members to specify the value to query.
     */
    public T match() {
        return eq();
    }
    

    /**
     * 
     */
    interface PropertySupplier {
        Property get( String name, Class propertyType );
    }
    
    
    protected T newPrototype( final PropertySupplier supplier ) {
        try {
            T prototype = type.newInstance();
            
            return (T)new PropertyInjector( prototype ) {
                protected Property createProperty( Field field, Class propertyType ) {
                    JsonName a = field.getAnnotation( JsonName.class );
                    String name = a != null ? a.value() : field.getName();
                    return supplier.get( name, propertyType );
                }
            }.run();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
    /**
     * 
     */
    protected abstract class QueryProperty<P>
            implements Property<P> {

        @Override
        public P get() {
            throw new RuntimeException( "Calling getter of an query prototype property is not allowed." );
        }
        
    }
    
}
