/* 
 * polymap.org
 * Copyright (C) 2013, Polymap GmbH. All rights reserved.
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

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IMosaicCaseEvent {

    public static final String      TYPE_NEW = "_created_";
    public static final String      TYPE_OPEN = "_open_";
    public static final String      TYPE_CLOSED = "_closed_";
    
    public String getId();
    
    /** 
     * Overview description of the event. 
     */
    public String getName();

    /** 
     * Full description of the event. 
     */
    public String getDescription();
    
    /**
     * The type of this event. This is mostly domain spezific. Exceptions are the
     * predefined types {@link #TYPE_NEW} and {@link #TYPE_CLOSED}, which are
     * automatically generated by the system.
     */
    public String getEventType();

    public String getUser();
    
    public Date getTimestamp();

}
