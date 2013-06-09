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
package org.polymap.mosaic.api.test;

import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.commons.vfs2.FileSystemException;
import org.polymap.mosaic.api.MosaicCase;
import org.polymap.mosaic.api.MosaicCaseEvent;
import org.polymap.mosaic.api.MosaicQuery;
import org.polymap.mosaic.api.MosaicRemoteServer;

/**
 * Mosaic API specicific tests.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BasicTests {

    private static MosaicRemoteServer   server;
    
    private static String               rootURI = "webdav://admin:login@localhost:10080/webdav/Mosaic/";
    

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        server = MosaicRemoteServer.instance();
    }

    
    @Test
    public void createCaseTest() throws FileSystemException {
        // create
        String name = String.valueOf( System.currentTimeMillis() );
        MosaicCase created = server.createCase( name, "der erste :)" );
        
        Assert.assertEquals( name, created.name.get() );
        Assert.assertEquals( "der erste :)", created.description.get() );
        created.store();
        
        // load case
        MosaicCase loaded = server.findCase( name );
        Assert.assertNotNull( loaded );
        Assert.assertEquals( name, loaded.name.get() );
        Assert.assertEquals( "der erste :)", loaded.description.get() );
        
        // check events
        List<MosaicCaseEvent> events = loaded.events();
        Assert.assertEquals( 1, events.size() );
        
        MosaicCaseEvent event = events.get( 0 );
        Assert.assertEquals( "Angelegt", event.name.get() );
    }

    
    @Test
    public void queryCaseTest() throws FileSystemException {
        // create
        String name = String.valueOf( System.currentTimeMillis() );
        MosaicCase created = server.createCase( name, "der erste :)" );
        
        // query1
        MosaicQuery<MosaicCase> query = MosaicQuery.forType( MosaicCase.class ).setMaxResults( 100 );
        query.eq().name.set( name );
        //query.match().description.set( "der*" );
        
        List<MosaicCase> results = server.queryCases( query );
        Assert.assertEquals( 1, results.size() );

        // query2
        query = MosaicQuery.forType( MosaicCase.class ).setMaxResults( 100 );
        query.eq().name.set( name );
        query.match().description.set( "das*" );
        
        results = server.queryCases( query );
        Assert.assertEquals( 0, results.size() );
    }
    

    @Test
    public void modifyCaseTest() throws Exception {
        // create
        String name = String.valueOf( System.currentTimeMillis() );
        MosaicCase created = server.createCase( name, "ein Vorgang" );
        created.store();
        
        // load and manipulate
        MosaicCase manipulated = server.findCase( name );
        Assert.assertNotNull( manipulated );
        manipulated.description.set( "geändert" );
        manipulated.store();
        
        // load and check
        MosaicCase checked = server.findCase( name );
        Assert.assertEquals( "geändert", checked.description.get() );
    }
    
    
    @Test(expected=UnsupportedOperationException.class)
    public void immutablePropertyTest() throws Exception {
        // create
        String name = String.valueOf( System.currentTimeMillis() );
        MosaicCase created = server.createCase( name, "ein Vorgang" );
        created.id.set( "immutable" );
    }
    
}
