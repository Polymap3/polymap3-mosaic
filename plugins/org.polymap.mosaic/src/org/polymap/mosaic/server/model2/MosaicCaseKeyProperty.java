/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.mosaic.server.model2;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Computed;
import org.polymap.core.model2.ComputedProperty;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.runtime.PropertyInfo;

/**
 * Use this {@link Computed} property in mixins for {@link MosaicCase2} to access
 * key/value pair. The {@link NameInStore} of the property is used as key.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicCaseKeyProperty
        extends ComputedProperty<String> {

    public MosaicCaseKeyProperty( PropertyInfo info, Composite composite ) {
        super( info, composite );
    }

    @Override
    public String get() {
        throw new RuntimeException( "not yet..." );
//        MosaicCase2 mcase = composite.context.getCompositePart( MosaicCase2.class );
//        return mcase.get( info.getNameInStore() );
    }

    @Override
    public void set( String value ) {
        throw new RuntimeException( "not yet..." );
//        MosaicCase2 mcase = composite.context.getCompositePart( MosaicCase2.class );
//        mcase.put( info.getNameInStore(), value );
    }

}
