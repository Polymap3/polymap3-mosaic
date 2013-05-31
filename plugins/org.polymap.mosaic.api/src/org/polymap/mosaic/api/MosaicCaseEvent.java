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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicCaseEvent
        extends RemoteObject {

    private static Log log = LogFactory.getLog( MosaicCaseEvent.class );
    
    public Property<String>     id = new Immutable( new JsonProperty( "identity", String.class ) ); 

    public Property<String>     name = new Immutable( new JsonProperty( "name", String.class ) ); 

    public Property<String>     description = new Immutable( new JsonProperty( "description", String.class ) );

    public Property<String>     user = new Immutable( new JsonProperty( "user", String.class ) ); 

    public Property<String>     time = new JsonProperty( "time", String.class );

    
    protected MosaicCaseEvent( FileObject folder ) {
        super( folder );
    }

}
