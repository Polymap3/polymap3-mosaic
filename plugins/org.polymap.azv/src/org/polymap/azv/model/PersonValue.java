/* 
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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
package org.polymap.azv.model;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.property.Property;
import org.qi4j.api.value.ValueComposite;

/**
 * Person value, used by {@link Nutzer}. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface PersonValue
        extends ValueComposite {
 
    /** Kundennummer */
    @Optional
    Property<String>        nummer();
    
    @Optional
    @UseDefaults
    Property<String>        name();
    
    @Optional
    @UseDefaults
    Property<String>        zusatz();
    
    /** Auch: akademischer Grad */
    @Optional
    @UseDefaults
    Property<String>        anrede();
    
    @Optional
    @UseDefaults
    Property<String>        vertreter();
    
    @Optional
    @UseDefaults
    Property<String>        strasse();
    
    @Optional
    @UseDefaults
    Property<String>        plz();
    
    @Optional
    @UseDefaults
    Property<String>        ort();
    
    @Optional
    @UseDefaults
    Property<String>        ortsteil();
    
    @Optional
    @UseDefaults
    Property<String>        land();
    
    @Optional
    @UseDefaults
    Property<String>        tel();

    @Optional
    @UseDefaults
    Property<String>        fax();
    
    @Optional
    @UseDefaults
    Property<String>        email();

}
