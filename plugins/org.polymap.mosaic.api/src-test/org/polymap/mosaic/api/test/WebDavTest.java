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

import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WebDavTest {

    private static StandardFileSystemManager    fsManager;
    
    private static String                       rootURI = "webdav://admin:login@localhost:10080/webdav/Mosaic/";
    

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        fsManager = new StandardFileSystemManager();
        fsManager.setConfiguration( WebDavTest.class.getClassLoader().getResource( "vfs_config.xml" ) );
        fsManager.init();
    }

    @Test
    public void ping() throws FileSystemException {
        FileObject folder = fsManager.resolveFile( rootURI + "Cases" );
        for (FileObject f : folder.getChildren()) {
            System.out.println( "    " + f.getName() );
        }
    }
    
}
