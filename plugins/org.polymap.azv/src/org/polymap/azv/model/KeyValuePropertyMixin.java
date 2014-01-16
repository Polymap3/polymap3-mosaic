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
package org.polymap.azv.model;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.ComputedProperty;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.mosaic.server.model2.MosaicCase2;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class KeyValuePropertyMixin
        extends Composite {

    /**
     * 
     */
    public static class MosaicCaseKeyProperty
            extends ComputedProperty<String> {

        public MosaicCaseKeyProperty( PropertyInfo info, Composite composite ) {
            super( info, composite );
        }

        @Override
        public String get() {
            MosaicCase2 mcase = ((KeyValuePropertyMixin)composite).context.getCompositePart( MosaicCase2.class );
            return mcase.get( info.getNameInStore() );
        }

        @Override
        public void set( String value ) {
            MosaicCase2 mcase = ((KeyValuePropertyMixin)composite).context.getCompositePart( MosaicCase2.class );
            mcase.put( info.getNameInStore(), value );
        }
        
    }
    
}
