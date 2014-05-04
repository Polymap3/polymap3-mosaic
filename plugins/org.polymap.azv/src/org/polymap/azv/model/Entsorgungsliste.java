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

import java.util.Date;

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Mixins( {
    ModelChangeSupport.Mixin.class,
    QiEntity.Mixin.class
} )
public interface Entsorgungsliste
    extends QiEntity, ModelChangeSupport, EntityComposite {

    Property<String>            name();
    
    @Optional
    Property<Date>              angelegtAm();
    
    @Optional
    Property<String>            angelegtVon();
    
    @Optional
    @UseDefaults
    Property<Boolean>           geschlossen();
    
//    @Optional
//    @UseDefaults
//    Property<Collection<String>> mcaseIds();

}
