/* 
 * polymap.org
 * Copyright (C) 2013, Polymap GmbH. All rights reserved.
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
package org.polymap.mosaic.server.fs;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bradmcevoy.http.Request;

import org.eclipse.core.runtime.IPath;

import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.DefaultContentProvider;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentNode;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.webdav.WebDavServer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicContentProvider
        extends DefaultContentProvider
        implements IContentProvider {

    private static Log log = LogFactory.getLog( MosaicContentProvider.class );

    /** The content encoding of the JSON files. */
    public static final String              ENCODING = "utf-8";

    /** The name of the query request parameter. */
    public static final String              QUERY_PARAM = "query";
    /** The name of the firstResult request parameter. */
    public static final String              FIRSTRESULT_PARAM = "firstResult";
    /** The name of the maxResults request parameter. */
    public static final String              MAXRESULTS_PARAM = "maxResults";


    @Override
    public List<? extends IContentNode> getChildren( IPath path ) {
        IContentFolder parent = getSite().getFolder( path );
        Request request = WebDavServer.request();
        Map<String,String> requestParams = request.getParams() != null ? request.getParams() : MapUtils.EMPTY_MAP;
        
        // root
        if (path.segmentCount() == 0) {
            return Collections.singletonList( new RootFolder( path, this ) );
        }
        // MosaicCasesFolder
        else if (parent instanceof RootFolder) {
            return Collections.singletonList( new MosaicCasesFolder( "Cases", path, this ) );
        }
        // MosaicCaseFolder
        else if (parent instanceof MosaicCasesFolder) {
            return ((MosaicCasesFolder)parent).getChildren( request.getAbsolutePath(), requestParams );
        }
        // MosaicCaseFile
        else if (parent instanceof MosaicCaseFolder) {
            return ((MosaicCaseFolder)parent).getChildren( requestParams );
        }
        // events
        else if (parent instanceof EventsFolder) {
            return ((EventsFolder)parent).getChildren( requestParams );
        }
        // event
        else if (parent instanceof EventFolder) {
            return ((EventFolder)parent).getChildren( requestParams );
        }
        return null;
    }
    
    
    /**
     * 
     */
    public static class RootFolder
            extends DefaultContentFolder {

        public RootFolder( IPath parentPath, IContentProvider provider ) {
            super( "Mosaic", parentPath, provider, null );
        }

        @Override
        public String getDescription( String contentType ) {
            return "Dieses Verzeichnis bietet die WebDAV/JSON-Schnittstelle des <b>Mosaic-Servers</b>. "
                    + "Nähere Informationen zu Mosaic: <b><a href=\"http://polymap.org/mosaic\" target=\"_blank\">polymap.org/mosaic</a></b>.";
        }
    }

}
