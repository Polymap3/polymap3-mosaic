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


import java.util.Set;

import org.polymap.core.project.IMap;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IMosaicCase {

    public String getId();

    public String getName();

    public String getDescription();

    public Set<String> getNatures();
    
    public void addNature( String nature );
    
    public String put( String key, String value );
    
    public String get( String key );
    
    /** First event is the creation event. */
    public Iterable<? extends IMosaicCaseEvent> getEvents();

    public void addEvent( IMosaicCaseEvent event );

    public IMap getMetaDataMap();

    public IMap getDataMap();

}
