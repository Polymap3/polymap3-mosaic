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

import org.qi4j.api.service.ServiceReference;

import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.Qi4jPlugin.Session;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreService;

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

    
    // instance *******************************************
    
    private LuceneRecordStore recordStore;

    
    protected MosaicRepository( QiModuleAssembler assembler ) {
        super( assembler );
    }

    
    @Override
    public void init( Session session ) {
        super.init( session );

        ServiceReference<LuceneEntityStoreService> ref = assembler.getModule().serviceFinder().findService( LuceneEntityStoreService.class );
        recordStore = ref.get().getStore();
    }


    public LuceneRecordStore recordStore() {
        return recordStore;
    }
    
}
