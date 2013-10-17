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
package org.polymap.mosaic.server.project;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.project.ProjectRepository;
import org.polymap.core.qi4j.QiModuleAssembler;

import org.polymap.mosaic.server.model2.MosaicRepository2;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicProjectRepository
        extends ProjectRepository {

    private static Log log = LogFactory.getLog( MosaicProjectRepository.class );
    
    /**
     * Don't use!
     * @deprecated Call {@link MosaicRepository2#projectRepo()} instead.
     */
    public static final MosaicProjectRepository instance() {
        throw new RuntimeException( "Don't use! Call {@link MosaicRepository2#projectRepo()} instead." );
        //return Qi4jPlugin.Session.instance().module( ProjectRepository.class );
    }


    public MosaicProjectRepository( QiModuleAssembler assembler ) {
        super( assembler );
    }

}
