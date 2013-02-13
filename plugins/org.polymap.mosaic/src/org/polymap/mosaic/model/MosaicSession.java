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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.runtime.ValueInitializer;

/**
 * Provides convenience methods to create and access Mosaic entities. This is a
 * facade of an instance of {@link UnitOfWork}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicSession {

    private static Log log = LogFactory.getLog( MosaicSession.class );

    private MosaicRepositoryAssembler   assembler;

    private UnitOfWork                  uow;

    
    public MosaicSession( MosaicRepositoryAssembler assembler ) {
        this.assembler = assembler;
        this.uow = assembler.getRepo().newUnitOfWork();
    }


    public MosaicCase createCase( String name ) {
        assert uow != null;
        MosaicCase entity = uow.createEntity( MosaicCase.class, null, null );

        entity.name.set( name );
        
        entity.created.getOrCreate( new ValueInitializer<CaseEvent>() {
            public CaseEvent initialize( CaseEvent value ) throws Exception {
                value.when.set( new Date() );
                value.why.set( "Case created." );
                return value;
            }
        });
        return entity;
    }
    

    /**
     * Find the Mosaic case for the given id.
     * 
     * @see UnitOfWork#entity(Class, Object)
     * @param id
     * @return A newly created entity or a previously created instance. Returns null
     *         if no Entity exists for the given id.
     */
    public MosaicCase findCase( Object id ) {
        return uow.entity( MosaicCase.class, id );
    }


//    public <T extends Entity> Collection<T> find( Class<T> entityClass ) {
//        return uow.find( entityClass );
//    }


    /**
     * Persistently stores all modifications that were made within this UnitOfWork.
     * <p/>
     * This does not invalidate this {@link UnitOfWork} but may flush internal
     * caches.
     *
     * @see UnitOfWork#commit()
     */
    public void commit() {
        uow.commit();
    }


    public void close() {
        assert uow != null;
        uow.close();
    }


    @Override
    public String toString() {
        return "MosaicSession[uow=" + uow + "]";
    }
    
}
