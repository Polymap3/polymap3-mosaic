/*
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rigths reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.azv.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.query.grammar.BooleanExpression;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.unitofwork.ConcurrentEntityModificationException;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.model.CompletionException;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.qi4j.Qi4jPlugin;
import org.polymap.core.qi4j.Qi4jPlugin.Session;
import org.polymap.core.qi4j.QiModule;
import org.polymap.core.qi4j.QiModuleAssembler;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.entity.ConcurrentModificationException;

import org.polymap.rhei.data.entitystore.lucene.LuceneEntityStoreService;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AzvRepository
        extends QiModule {

    private static Log log = LogFactory.getLog( AzvRepository.class );

    public static final String              NAMESPACE = "http://polymap.org/azv";

    /**
     * Get or create the repository for the current user session.
     */
    public static final AzvRepository instance() {
        return Qi4jPlugin.Session.instance().module( AzvRepository.class );
    }


    // instance *******************************************

    private OperationSaveListener           operationListener = new OperationSaveListener();

    /** Allow direct access for operations. */
    protected AzvService                    azvService;

//    public ServiceReference<BiotopnummerGeneratorService> biotopnummern;


    public AzvRepository( final QiModuleAssembler assembler ) {
        super( assembler );
        log.debug( "Initializing AZV module..." );

        // for the global instance of the module (Qi4jPlugin.Session.globalInstance()) there
        // is no request context
        if (Polymap.getSessionDisplay() != null) {
            OperationSupport.instance().addOperationSaveListener( operationListener );
        }
//        biotopnummern = assembler.getModule().serviceFinder().findService( BiotopnummerGeneratorService.class );
    }


    public void init( final Session session ) {
        try {
            // build the queryProvider
            ServiceReference<LuceneEntityStoreService> storeService = assembler.getModule().serviceFinder().findService( LuceneEntityStoreService.class );
            LuceneEntityStoreService luceneStore = storeService.get();

//            azvService = new AzvService( new BiotopEntityProvider( this ) );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }

//        // register with catalog
//        CatalogRepository catalogRepo = session.module( CatalogRepository.class );
//        catalogRepo.getCatalog().addTransient( azvService );
    }


    protected void dispose() {
        if (operationListener != null) {
            OperationSupport.instance().removeOperationSaveListener( operationListener );
            operationListener = null;
        }
        if (azvService != null) {
            azvService.dispose( new NullProgressMonitor() );
        }
        super.dispose();

        log.info( "Running GC ..." );
        Runtime.getRuntime().gc();
    }


    public <T> Query<T> findEntities( Class<T> compositeType, BooleanExpression expression,
            int firstResult, int maxResults ) {
        // Lucene does not like Integer.MAX_VALUE!?
        maxResults = Math.min( maxResults, 1000000 );

        return super.findEntities( compositeType, expression, firstResult, maxResults );
    }


    public void applyChanges()
    throws ConcurrentModificationException, CompletionException {
        try {
            // save changes
            uow.apply();
        }
        catch (ConcurrentEntityModificationException e) {
            throw new ConcurrentModificationException( e );
        }
        catch (UnitOfWorkCompletionException e) {
            throw new CompletionException( e );
        }
    }


    public Schachtschein newSchachtschein() throws Exception {
        return newEntity( Schachtschein.class, null, new EntityCreator<Schachtschein>() {
            public void create( Schachtschein prototype ) throws Exception {
            }
        });
    }

}
