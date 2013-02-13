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
package org.polymap.mosaic.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.CollectionProperty;
import org.polymap.core.model2.Description;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.Property;

/**
 * Provides the base Mosaic business case. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Description("Beschreibung dieses Datentyps")
public class MosaicCase
        extends Entity {

    private static Log log = LogFactory.getLog( MosaicCase.class );

    public Property<String>                 name;

    public Property<String>                 description;

    public Property<CaseEvent>              created;
    
    /**
     * First event is the creation event.
     */
    protected CollectionProperty<CaseEvent> events;
    
}
