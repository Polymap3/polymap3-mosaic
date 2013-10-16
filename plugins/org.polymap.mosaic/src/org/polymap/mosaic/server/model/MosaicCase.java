/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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

import org.qi4j.api.common.Optional;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.concern.Concerns;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.entity.association.ManyAssociation;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import org.polymap.core.qi4j.QiEntity;
import org.polymap.core.qi4j.event.ModelChangeSupport;
import org.polymap.core.qi4j.security.ACL;
import org.polymap.core.qi4j.security.ACLCheckConcern;
import org.polymap.core.qi4j.security.ACLFilterConcern;

import org.polymap.rhei.data.model.JsonState;

/**
 * Provides the base Mosaic business case. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Concerns( {
    ACLCheckConcern.class, 
    ACLFilterConcern.class 
})
@Mixins({
    MosaicCase.Mixin.class,
    QiEntity.Mixin.class,
    ModelChangeSupport.Mixin.class,
    JsonState.Mixin.class,
    ACL.Mixin.class
})
public interface MosaicCase
        extends IMosaicCase, JsonState, ACL, ModelChangeSupport, QiEntity, EntityComposite {

    public Property<String> name();

    @Optional
    public Property<String> description();

    @Optional
    public Property<MosaicCaseEvent> created();
    
    /** First event is the creation event. */
    @UseDefaults
    public ManyAssociation<MosaicCaseEvent> events();
    
    
    /**
     * Transient fields and method implementations.
     */
    public abstract class Mixin
            implements MosaicCase {

        @Override
        public String getName() {
            return name().get();
        }

        @Override
        public String getDescription() {
            return description().get();
        }

        @Override
        public Iterable<IMosaicCaseEvent> getEvents() {
            return Iterables.transform( events(), new Function<MosaicCaseEvent,IMosaicCaseEvent>() {
                public IMosaicCaseEvent apply( MosaicCaseEvent input ) {
                    return input;
                }
            });
        }
    }
    
}
