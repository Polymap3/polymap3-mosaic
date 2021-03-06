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
package org.polymap.mosaic.api.webdav;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.provider.URLFileName;
import org.apache.commons.vfs2.provider.webdav.WebdavFileProvider;

/**
 * Fix issues regarding the default implementation of {@link URLFileName}.
 *
 * @see WebdavFileNameParser2
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WebdavFileProvider2
        extends WebdavFileProvider {

    private static Log log = LogFactory.getLog( WebdavFileProvider2.class );

    public WebdavFileProvider2() {
        super();
        setFileNameParser( WebdavFileNameParser2.instance() );
    }
    
}
