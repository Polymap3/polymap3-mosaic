/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.mosaic.server.model;

import java.util.Date;

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
@Mixins({
        MosaicCaseEvent.Mixin.class,
        QiEntity.Mixin.class,
        ModelChangeSupport.Mixin.class
})
interface MosaicCaseEvent
        extends IMosaicCaseEvent, ModelChangeSupport, QiEntity, EntityComposite {

    public Property<String> user();
    
    public Property<Date> time();

    public Property<String> description();
    
    
    /**
     * Transient fields and method implementations.
     */
    public abstract class Mixin
            implements MosaicCaseEvent {

        @Override
        public String getUser() {
            return user().get();
        }

        @Override
        public Date getTime() {
            return time().get();
        }

        @Override
        public String getDescription() {
            return description().get();
        }
    }
}
