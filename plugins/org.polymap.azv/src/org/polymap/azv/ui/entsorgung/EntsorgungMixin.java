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
package org.polymap.azv.ui.entsorgung;

import javax.annotation.Nullable;

import org.polymap.core.model2.Composite;
import org.polymap.core.model2.Computed;
import org.polymap.core.model2.ComputedProperty;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.runtime.PropertyInfo;
import org.polymap.mosaic.server.model2.MosaicCase2;

/**
 * Erweitert einen Vorgang {@link MosaicCase2} um Key-Felder für Adresse der
 * Entsorgung.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EntsorgungMixin
        extends Composite {

    public static final String      KEY_LISTE = "liste";
    public static final String      KEY_NAME = "name";
    public static final String      KEY_KUNDENNUMMER = "kundennummer";
    public static final String      KEY_BEMERKUNG = "bemerkung";

    public static final String      KEY_STREET = "street";
    public static final String      KEY_NUMBER = "number";
    public static final String      KEY_CITY = "city";
    public static final String      KEY_POSTALCODE = "postalcode";

    @Nullable
    @NameInStore(KEY_STREET)
    @Computed(MosaicCaseKeyProperty.class)
    public Property<String>         strasse;
    
    @Nullable
    @NameInStore(KEY_NUMBER)
    @Computed(MosaicCaseKeyProperty.class)
    public Property<String>         nummer;
    
    @Nullable
    @NameInStore(KEY_CITY)
    @Computed(MosaicCaseKeyProperty.class)
    public Property<String>         stadt;
    
    @Nullable
    @NameInStore(KEY_POSTALCODE)
    @Computed(MosaicCaseKeyProperty.class)
    public Property<String>         plz;
    
    @Nullable
    @NameInStore(KEY_LISTE)
    @Computed(MosaicCaseKeyProperty.class)
    public Property<String>         liste;
    
    @Nullable
    @NameInStore(KEY_NAME)
    @Computed(MosaicCaseKeyProperty.class)
    public Property<String>         name;
    
    @Nullable
    @NameInStore(KEY_KUNDENNUMMER)
    @Computed(MosaicCaseKeyProperty.class)
    public Property<String>         kundennummer;
    
    @Nullable
    @NameInStore(KEY_BEMERKUNG)
    @Computed(MosaicCaseKeyProperty.class)
    public Property<String>         bemerkung;
    

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
            MosaicCase2 mcase = ((EntsorgungMixin)composite).context.getCompositePart( MosaicCase2.class );
            return mcase.get( info.getNameInStore() );
        }

        @Override
        public void set( String value ) {
            MosaicCase2 mcase = ((EntsorgungMixin)composite).context.getCompositePart( MosaicCase2.class );
            mcase.put( info.getNameInStore(), value );
        }
        
    }
    
}
