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

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.polymap.core.model2.Composite;

import static org.polymap.azv.AzvPlugin.*;

import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicCaseEvent;
import org.polymap.mosaic.server.model.MosaicCaseEvents;
import org.polymap.mosaic.server.model2.MosaicCase2;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AzvStatusMixin
        extends Composite {
    
    public static final List<String>        AZV_STATUS = Arrays.asList( 
            EVENT_TYPE_BEANTRAGT, EVENT_TYPE_ANFREIGABE, EVENT_TYPE_ANBEARBEITUNG, 
            EVENT_TYPE_FREIGABE, EVENT_TYPE_STORNIERT, EVENT_TYPE_ABGEBROCHEN );

    public static String ofCase( IMosaicCase mcase ) {
        for (IMosaicCaseEvent event : Lists.reverse( ImmutableList.copyOf( mcase.getEvents() ))) {
            if (AZV_STATUS.contains( event.getEventType() )) {
                return event.getEventType();
            }
        }
        return null;
    }
    
    /**
     * 
     * @return
     */
    public String azvStatus() {
        MosaicCase2 mcase = context.getCompositePart( MosaicCase2.class );
        return ofCase( mcase );
    }
    
    public boolean contains( String eventType ) {
        MosaicCase2 mcase = context.getCompositePart( MosaicCase2.class );
        return MosaicCaseEvents.contains( mcase, eventType );        
    }
    
}
