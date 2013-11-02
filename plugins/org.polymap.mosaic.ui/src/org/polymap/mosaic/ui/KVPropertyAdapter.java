/*
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rigths reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.mosaic.ui;

import java.util.Map;

import org.geotools.feature.NameImpl;
import org.geotools.feature.type.AttributeTypeImpl;
import org.opengis.feature.Property;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;

import org.polymap.mosaic.server.model.IMosaicCase;

/**
 * Provides a key/value of an IMosaicCase as OGC property. Used by
 * {@link IFormPageProvider} instances.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class KVPropertyAdapter
        implements Property {

    private IMosaicCase     mcase;
    
    private String          name;
    

    public KVPropertyAdapter( IMosaicCase mcase, String name ) {
        this.mcase = mcase;
        this.name = name;
    }

    @Override
    public Name getName() {
        return new NameImpl( name );
    }

    @Override
    public PropertyType getType() {
        return new AttributeTypeImpl( getName(), String.class, false, false, null, null, null );
    }

    @Override
    public PropertyDescriptor getDescriptor() {
        // signal that we are a 'complex' property
        // see FormEditor#doSave() for implementation detail
        return null;
    }

    @Override
    public Object getValue() {
        return mcase.get( name );
    }

    @Override
    public void setValue( Object value ) {
        mcase.put( name, (String)value );
    }

    @Override
    public Map<Object, Object> getUserData() {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public boolean isNillable() {
        throw new RuntimeException( "not yet implemented." );
    }

}
