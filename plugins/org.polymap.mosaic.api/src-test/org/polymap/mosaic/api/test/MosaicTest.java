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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.commons.vfs2.FileSystemException;
import org.polymap.mosaic.api.MosaicCase;
import org.polymap.mosaic.api.MosaicRemoteServer;

/**
 * Mosaic API specicific tests.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicTest {

    private static MosaicRemoteServer   server;
    
    private static String               rootURI = "webdav://admin:login@localhost:10080/webdav/Mosaic/";
    

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        server = MosaicRemoteServer.instance();
    }

    @Test
    public void createCaseTest() throws FileSystemException {
        String name = String.valueOf( System.currentTimeMillis() );
        MosaicCase mosaicCase = server.createCase( name, "der erste :)" );
        
        Assert.assertEquals( name, mosaicCase.name.get() );
        Assert.assertEquals( "der erste :)", mosaicCase.description.get() );

        mosaicCase.store();
    }
    
}
