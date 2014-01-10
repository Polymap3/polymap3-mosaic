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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Static methods that help to work with {@link IMosaicCaseEvent}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicCaseEvents {

    private static Log log = LogFactory.getLog( MosaicCaseEvents.class );
    
    
    public static Predicate<IMosaicCaseEvent> contains( final String eventType ) {
        return new Predicate<IMosaicCaseEvent>() {
            public boolean apply( IMosaicCaseEvent input ) {
                return input.getEventType().equals( eventType );
            }
        };
    }
    
    
    public static boolean contains( Iterable<IMosaicCaseEvent> events, String eventType ) {
        return Iterables.find( events, contains( eventType ), null ) != null;
    }
    
    
    public static boolean contains( IMosaicCase mcase, String eventType ) {
        return contains( mcase.getEvents(), eventType );
    }
    
    
    /**
     * The general status of the given case:
     * 
     * @param mcase
     * @return {@link IMosaicCaseEvent#TYPE_CREATED},
     *         {@link IMosaicCaseEvent#TYPE_OPEN} or
     *         {@link IMosaicCaseEvent#TYPE_CLOSED}
     */
    public static String caseStatus( IMosaicCase mcase ) {
        List<IMosaicCaseEvent> events = ImmutableList.copyOf( mcase.getEvents() );
        if (events.size() == 1) {
            return IMosaicCaseEvent.TYPE_NEW;
        }
        else {
            IMosaicCaseEvent last = events.get( events.size()-1 );
            if (last.getEventType() == IMosaicCaseEvent.TYPE_CLOSED) {
                return IMosaicCaseEvent.TYPE_CLOSED;
            }
            else {
                return IMosaicCaseEvent.TYPE_OPEN;                
            }
        }
    
    }
    
}


