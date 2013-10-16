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
package org.polymap.mosaic.server.model2;

import java.util.Date;

import javax.annotation.Nullable;

import com.vividsolutions.jts.geom.Point;

import org.polymap.core.model2.Description;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.store.feature.SRS;

import org.polymap.mosaic.server.model.IMosaicCaseEvent;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Description( "Meta data of all Mosaic case events in the store" )
@SRS( "EPSG:4326" )
@NameInStore( MosaicCaseEvent2.FEATURETYPE_NAME )
public class MosaicCaseEvent2
        extends Entity
        implements IMosaicCaseEvent {

    public static final String      FEATURETYPE_NAME = "Events";

    @Nullable
    public Property<Point>          geom;

    public Property<String>         name;
    
    @Nullable
    public Property<String>         description;
    
    public Property<String>         type;
    
    public Property<String>         user;
    
    public Property<Date>           timestamp;
    
    /** To keep types simple (WFS, etc.) this is the id of the associated {@link MosaicCase2}. */
    public Property<String>         caseId;
    
    
    @Override
    public String getId() {
        return (String)id();
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public String getDescription() {
        return description.get();
    }

    @Override
    public String getEventType() {
        return type.get();
    }

    @Override
    public String getUser() {
        return user.get();
    }

    @Override
    public Date getTimestamp() {
        return timestamp.get();
    }
    
}
