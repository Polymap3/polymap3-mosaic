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
package org.polymap.mosaic.test;

import org.geotools.data.FeatureSource;
import org.geotools.feature.NameImpl;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;

import org.polymap.core.runtime.DefaultSessionContextProvider;
import org.polymap.core.runtime.SessionContext;

import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicCaseEvent;
import org.polymap.mosaic.server.model2.MosaicRepository2;

/**
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Model2Test {

    private static Log log = LogFactory.getLog( Model2Test.class );

    private static MosaicRepository2    repo;

    static {
        System.setProperty( "org.apache.commons.logging.simplelog.defaultlog", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.runtime.recordstore", "trace" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.model2", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.model2.store.feature", "debug" );
        System.setProperty( "org.apache.commons.logging.simplelog.log.org.polymap.core.data.feature.recordstore", "debug" );
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // register session context provider
        DefaultSessionContextProvider sessionContextProvider = new DefaultSessionContextProvider();
        SessionContext.addProvider( sessionContextProvider );

        // create session
        sessionContextProvider.mapContext( "test", true );
        
        // init RAM dir
        MosaicRepository2.init( null, false );
        repo = MosaicRepository2.newInstance();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void createCase() {
        IMosaicCase case1 = repo.newCase( "Case1", null );
        case1.addNature( "TestNature" );
        case1.put( "key1", "value1" );
        repo.commitChanges();
        log.info( "Case1.id = " + case1.getId() );
        
        assertEquals( "Case1", case1.getName() );
        int count = 0;
        for (IMosaicCaseEvent event : case1.getEvents()) {
            log.info( "    Event: " + event );
            count ++;
        }
        assertEquals( 1, count );
        
        assertEquals( "TestNature", Iterables.getOnlyElement( case1.getNatures() ) );
        assertEquals( "value1", case1.get( "key1" ) );
    }

    
    @Test
    public void featureSchema() {
        FeatureSource fs = repo.featureSource( IMosaicCase.class );    
        assertNotNull( fs );
        FeatureType schema = fs.getSchema();
        
        for (PropertyDescriptor descriptor : schema.getDescriptors()) {
            log.info( "IMosaicCase: property: " + descriptor.getName() );
        }
        // FIXME namespace "" is wrong
        PropertyDescriptor name = schema.getDescriptor( new NameImpl( null, "name" ) );
        assertNotNull( name );
    }
    
}
