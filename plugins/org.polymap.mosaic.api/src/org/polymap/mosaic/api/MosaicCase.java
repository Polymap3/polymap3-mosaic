/* 
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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
package org.polymap.mosaic.api;

import java.util.List;

import org.apache.commons.vfs2.FileObject;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicCase
        extends RemoteObject {
    
    @Immutable
    @JsonName("identity")
    public Property<String>     id; 

    /** The name of the case. */
    @JsonName("name")
    public Property<String>     name; 

    /** The entire desciption text of the case. */
    @JsonName("description")
    public Property<String>     description;

    
    /** 
     * No args ctor for the internal query interface. 
     */
    MosaicCase() {        
    }
    
    
    MosaicCase( FileObject f ) {
        super( f );
    }
    
    
    public List<MosaicCaseEvent> events() {
        return MosaicRemoteServer.instance().queryEvents( this );
    }
    
}
