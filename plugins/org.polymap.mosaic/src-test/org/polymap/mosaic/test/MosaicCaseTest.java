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
package org.polymap.mosaic.test;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.store.recordstore.RecordStoreAdapter;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

import org.polymap.mosaic.model.CaseEvent;
import org.polymap.mosaic.model.MosaicCase;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicCaseTest {

    private static Log log = LogFactory.getLog( MosaicCaseTest.class );

    static EntityRepository         repo;

    static LuceneRecordStore        store;

    UnitOfWork                      uow;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        store = new LuceneRecordStore();
        repo = EntityRepository.newConfiguration()
                .setEntities( MosaicCase.class, CaseEvent.class )
                .setStore( new RecordStoreAdapter( store ) )
                .create();
    }


    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        repo.close();
        store.close();
    }


    @Before
    public void setUp() throws Exception {
        uow = repo.newUnitOfWork();
        log.info( "UoW: " + uow );
    }


    @After
    public void tearDown() throws Exception {
        uow.close();
    }


    @Test
    public void test1() {
        log.info( "test1(): ..." );
        //fail( "Not yet implemented" );
    }
    
    
    @Test
    public void test2() {
        log.info( "test2(): ..." );
    }
    
}
