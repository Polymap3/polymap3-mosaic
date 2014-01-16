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

import javax.annotation.Nullable;

import static org.apache.commons.lang.StringUtils.*;

import org.polymap.core.model2.Computed;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.Property;

import org.polymap.rhei.um.Address;
import org.polymap.rhei.um.User;

import org.polymap.mosaic.server.model2.MosaicCase2;

/**
 * Erweitert einen Vorgang {@link MosaicCase2} um Key-Felder für die Adresse des
 * Vorgangs.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AdresseMixin
        extends KeyValuePropertyMixin {

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
    
    
    public AdresseMixin setAdresseVonNutzer( User nutzer ) {
        Address address = nutzer.address().get();
        strasse.set( defaultString( address.street().get() ) );
        nummer.set( defaultString( address.number().get() ) );
        stadt.set( defaultString( address.city().get() ) );
        plz.set( defaultString( address.postalCode().get() ) );
        return this;
    }
    
    
    public String adresse() {
        return new StringBuilder( 256 )
                .append( defaultString( strasse.get() ) ).append( " " )
                .append( defaultString( nummer.get() ) ).append( ", " )
                .append( defaultString( plz.get() ) ).append( " " )
                .append( defaultString( stadt.get() ) ).toString();
    }
    
}
