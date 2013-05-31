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
package org.polymap.mosaic.server.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicRepository
        extends QiModule {

    private static Log log = LogFactory.getLog( MosaicRepository.class );

    public static final String              NAMESPACE = "http://polymap.org/mosaic";

    /**
     * The repository for the current user session.
     */
    public static final MosaicRepository instance() {
        return Qi4jPlugin.Session.instance().module( MosaicRepository.class );
    }
    
    
    //
    protected MosaicRepository( QiModuleAssembler assembler ) {
        super( assembler );
    }

}
