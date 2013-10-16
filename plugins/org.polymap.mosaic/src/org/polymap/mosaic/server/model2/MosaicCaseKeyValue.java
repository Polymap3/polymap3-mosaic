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

import com.vividsolutions.jts.geom.Geometry;

import org.polymap.core.model2.Description;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.store.feature.SRS;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
@Description( "" )
@SRS( "EPSG:4326" )
@NameInStore( MosaicCaseKeyValue.FEATURETYPE_NAME )
public class MosaicCaseKeyValue
        extends Entity {

    public static final String      FEATURETYPE_NAME = "KeyValues";

    /** JsonSchemaCoder needs a ggeom prop. */
    public Property<Geometry>       geom;
    
    public Property<String>         key;
    
    public Property<String>         value;
    
    /** To keep types simple (WFS, etc.) this is the id of the associated {@link MosaicCase2}. */
    public Property<String>         caseId;
    
}
