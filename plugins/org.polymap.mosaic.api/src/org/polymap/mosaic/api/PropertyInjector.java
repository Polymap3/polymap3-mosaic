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

import java.util.ArrayDeque;
import java.util.Queue;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.mosaic.api.RemoteObject.Property;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class PropertyInjector {

    private static Log log = LogFactory.getLog( PropertyInjector.class );
    
    private RemoteObject        obj;
    
    
    public PropertyInjector( RemoteObject obj ) {
        this.obj = obj;    
    }
    
    
    protected abstract Property createProperty( Field field, Class propertyType );
    
    
    public RemoteObject run() {
        Queue<Class> types = new ArrayDeque( 16 );
        types.add( obj.getClass() );

        while (!types.isEmpty()) {
            Class type = types.remove();
            
            // recursion
            if (type.getSuperclass() != null) {
                types.add( type.getSuperclass() );
            }

            // fields
            for (Field f : type.getDeclaredFields()) {
                if (Property.class.isAssignableFrom( f.getType() )) {
                    f.setAccessible( true );
                    Type ftype = f.getGenericType();
                    
                    if (ftype instanceof ParameterizedType) {
                        Type ptype = ((ParameterizedType)ftype).getActualTypeArguments()[0];

                        // set
                        try {
                            f.set( obj, createProperty( f, (Class)ptype ) );
                            log.info( "injected: " + f.getName() + " (" + obj.getClass().getSimpleName() + ")" );
                            continue;
                        }
                        catch (Exception e) {
                            throw new RuntimeException( e );
                        }
                    }
                    else {
                        throw new IllegalStateException( "ContextProperty has no type param: " + f.getName() );
                    }
                }
            }
        }
        return obj;
    }
    
}
