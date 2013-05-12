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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.event.PropertyChangeSupport;

/**
 * A 'Person'. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Concerns( {
    PropertyChangeSupport.Concern.class
})
@Mixins( {
    Nutzer.Mixin.class, 
    PropertyChangeSupport.Mixin.class,
    ModelChangeSupport.Mixin.class,
    QiEntity.Mixin.class
})
public interface Nutzer
        extends EntityComposite, QiEntity, PropertyChangeSupport {
    
    Property<PersonValue>       person();
    
    @Optional
    Property<String>            passwordHash();
    
    /**
     * Wurde die Identität des Nutzers überprüft?
     */
    @UseDefaults
    Property<Boolean>           authentifiziert();
    
    /**
     * Creates a string representation of this person by appending all
     * fields with the given separator.
     * 
     * @param separator
     * @return Newly created String.
     */
    public String getLabelString( String separator );

    
    /**
     * Methods and transient fields.
     */
    public static abstract class Mixin
            implements Nutzer {
        
        private static Log log = LogFactory.getLog( Mixin.class );

        
        public String getLabelString( String separator ) {
            PersonValue value = person().get();
            return new StringBuffer( 256 )
                    .append( value.name().get() ).append( separator )
                    .append( value.strasse().get() ).append( separator )
                    .append( value.plz().get() ).append( " " )
                    .append( value.ort().get() ).append( separator )
                    .append( value.tel().get() ).append( separator )
                    .append( value.email().get() ).append( separator )
                    .append( value.fax().get() ).append( separator ).toString();
        }
        
    }
    
}
