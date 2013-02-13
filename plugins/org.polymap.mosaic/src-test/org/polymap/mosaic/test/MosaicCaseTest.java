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

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.model2.store.recordstore.RecordStoreAdapter;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

import org.polymap.mosaic.model.MosaicCase;
import org.polymap.mosaic.model.MosaicRepositoryAssembler;
import org.polymap.mosaic.model.MosaicSession;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicCaseTest {

    private static Log log = LogFactory.getLog( MosaicCaseTest.class );

    static LuceneRecordStore            store;

    static MosaicRepositoryAssembler    assembler;

    MosaicSession                       session;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        store = new LuceneRecordStore();
        assembler = new MosaicRepositoryAssembler( new RecordStoreAdapter( store ) );
    }


    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        assembler.close();
        store.close();
        log.info( "closed." );
    }


    @Before
    public void setUp() throws Exception {
        session = assembler.newSession();
        log.info( "Session: " + session );
    }


    @After
    public void tearDown() throws Exception {
        session.close();
    }


    @Test
    public void testSimpleCase() {
        log.info( "simpleCase(): ..." );
        
        // create
        MosaicCase mcase = session.createCase( "Test1" );
        log.info( "mcase: " + mcase );
        log.info( "id: " + mcase.id() );
        assertEquals( "Test1", mcase.name.get() );
        assertNotNull( mcase.created.get() );
        assertNotNull( mcase.created.get().when.get() );

        mcase.description.set( "Description" );
        
        // commit
        session.commit();
        log.info( "id: " + mcase.id() );
        assertNotNull( mcase.id() );
        
        // retrieve and check
        MosaicSession session2 = assembler.newSession();
        assertNotSame( session, session2 );
        MosaicCase mcase2 = session2.findCase( mcase.id() );
        log.info( "mcase2: " + mcase2 );
        assertNotNull( mcase2 );
        assertNotSame( mcase, mcase2 );
        assertEquals( "Test1", mcase2.name.get() );
        assertNotNull( mcase2.created.get() );
        assertNotNull( mcase2.created.get().when.get() );
    }
    
}
