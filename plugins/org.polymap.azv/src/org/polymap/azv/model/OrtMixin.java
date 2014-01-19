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

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import org.polymap.core.model2.Computed;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.Property;

import org.polymap.mosaic.server.model2.MosaicCase2;

/**
 * Erweitert einen Vorgang {@link MosaicCase2} um Key-Felder für den
 * Ort der Maßnahme.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class OrtMixin
        extends KeyValuePropertyMixin {

    public static final String      KEY_POINT = "point";

    @Nullable
    @NameInStore(KEY_POINT)
    @Computed(MosaicCaseKeyProperty.class)
    public Property<String>         geomWkt;
    

    public Point getGeom() {
        try {
            String wkt = geomWkt.get();
            return wkt != null ? (Point)new WKTReader().read( wkt ) : null;
        }
        catch (ParseException e) {
            throw new RuntimeException( e );
        }
    }


    public void setGeom( Point point ) {
        String wkt = new WKTWriter().write( point );
        geomWkt.set( wkt );
    }
    
}
