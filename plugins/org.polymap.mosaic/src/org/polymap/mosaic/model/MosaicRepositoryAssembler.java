/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.mosaic.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.store.StoreSPI;

/**
 * Creates and maintains an {@link EntityRepository} for the Mosaic entities.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicRepositoryAssembler {

    private static Log log = LogFactory.getLog( MosaicRepositoryAssembler.class );

    private StoreSPI            store;

    private EntityRepository    repo;

    
    public MosaicRepositoryAssembler( StoreSPI store ) {
        this.store = store;
        this.repo = EntityRepository.newConfiguration()
                .setEntities( MosaicCase.class, CaseEvent.class )
                .setStore( this.store )
                .create();
    }


    public MosaicSession newSession() {
        return new MosaicSession( this );
    }

    
    public EntityRepository getRepo() {
        return repo;
    }


    public StoreSPI getStore() {
        assert repo != null;
        return repo.getStore();
    }


    public void close() {
        assert repo != null;
        repo.close();
        store.close();
    }
    
}
