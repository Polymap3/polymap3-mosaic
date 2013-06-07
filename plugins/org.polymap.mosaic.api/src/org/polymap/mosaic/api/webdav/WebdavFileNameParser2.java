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
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.provider.URLFileName;
import org.apache.commons.vfs2.provider.UriParser;
import org.apache.commons.vfs2.provider.VfsComponentContext;
import org.apache.commons.vfs2.provider.webdav.WebdavFileNameParser;

/**
 * Fix issues regarding the default implementation of {@link URLFileName}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class WebdavFileNameParser2
        extends WebdavFileNameParser {

    private static Log log = LogFactory.getLog( WebdavFileNameParser2.class );
    
    private static final WebdavFileNameParser2  instance = new WebdavFileNameParser2();
    
    
    public static WebdavFileNameParser2 instance() {
        return instance;
    }

    
    @Override
    public FileName parseUri( final VfsComponentContext context, FileName base, final String filename )
            throws FileSystemException {
        // FTP URI are generic URI (as per RFC 2396)
        final StringBuilder name = new StringBuilder();

        // Extract the scheme and authority parts
        final Authority auth = extractToPath( filename, name );

        // Extract the queryString
        String queryString = UriParser.extractQueryString( name );

        // Decode and normalise the file name
        UriParser.canonicalizePath( name, 0, name.length(), this );
        UriParser.fixSeparators( name );
        FileType fileType = UriParser.normalisePath( name );
        final String path = name.toString();

        return new URLFileName2( auth.getScheme(), auth.getHostName(), auth.getPort(), getDefaultPort(),
                auth.getUserName(), auth.getPassword(), path, fileType, queryString );
    }

    
    /**
     * {@link URLFileName} does not include queryString into hashCode()/equals()
     * (called via getKey()). This causes FileNames with different queryString to
     * appear as equal in the cache.
     */
    class URLFileName2
            extends URLFileName {

        public URLFileName2( String scheme, String hostName, int port, int defaultPort, String userName,
                String password, String path, FileType type, String queryString ) {
            super( scheme, hostName, port, defaultPort, userName, password, path, type, queryString );
        }

        @Override
        public int hashCode() {
            return getURI().hashCode();
        }

        @Override
        public boolean equals( Object o ) {
            return getURI().equals( ((URLFileName2)o).getURI() );
        }

        @Override
        public int compareTo( FileName o ) {
            return getURI().compareTo( ((URLFileName2)o).getURI() );
        }
        
    }
    
}
