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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IPath;
import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentPutable;
import org.polymap.service.fs.spi.IMakeFolder;
import org.polymap.service.fs.spi.NotAuthorizedException;
import org.polymap.service.fs.spi.Range;

import org.polymap.mosaic.server.model2.MosaicCase2;
import org.polymap.mosaic.server.model2.MosaicRepository2;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicCasesFolder
        extends DefaultContentFolder
        implements IContentPutable, IMakeFolder {

    private static Log log = LogFactory.getLog( MosaicCasesFolder.class );
    
//    private Query           query = new TermQuery( new Term( "type", MosaicCase.class.getName() ) );
    
    private int             firstResult = 0;
    
    private int             maxResults = 1000;
    
    
    public MosaicCasesFolder( String name, IPath parentPath, IContentProvider provider ) {
        super( name, parentPath, provider, null );
    }


    @Override
    public boolean isValid() {
        // don't cache, always process query in request params
        return false;
    }


    public List<IContentFolder> getChildren( String requestPath, Map<String,String> requestParams ) {
        assert requestParams != null;
        assert requestPath != null;
        try {
            MosaicRepository2 repo = MosaicRepository2.instance();
//            LuceneRecordStore recordStore = repo.recordStore();

            // query in request params
            String queryParam = requestParams.get( MosaicContentProvider.QUERY_PARAM );
            if (queryParam != null) {
                throw new RuntimeException( "Query on MosaicCases folder is not implemented." );
            }
//            Query luceneQuery = queryParam != null ? expandQuery( query, queryParam ) : query;
//            String firstResultParam = requestParams.get( MosaicContentProvider.FIRSTRESULT_PARAM );
//            String maxResultsParam = requestParams.get( MosaicContentProvider.MAXRESULTS_PARAM );
//
//            log.info( "LUCENE: " + luceneQuery );
//            RecordQuery recordQuery = new LuceneRecordQuery( recordStore, luceneQuery );
//            
//            recordQuery.setFirstResult( firstResultParam != null
//                    ? Integer.parseInt( firstResultParam ) : firstResult );
//            
//            recordQuery.setMaxResults( maxResultsParam != null 
//                    ? Integer.parseInt( maxResultsParam ) : maxResults );
//            
//            ResultSet rs = recordStore.find( recordQuery );
            
            // find entities
            List<IContentFolder> result = new ArrayList();
            for (MosaicCase2 entity : repo.query( MosaicCase2.class, null ).execute()) {
                result.add( new MosaicCaseFolder( entity.getName(), getPath(), getProvider(), entity ) ); 
            }
            
//            // query via child node
//            IPath path = Path.fromOSString( requestPath );
//            if (path.segmentCount() > getPath().segmentCount() + 1) {
//                // XXX +1 for the leading /webdav segment
//                String nextSegment = path.segment( getPath().segmentCount() + 1 );
//                if (nextSegment.startsWith( "search" )) {
//                    MosaicCasesFolder searchFolder = new MosaicCasesFolder( nextSegment, getPath(), getProvider() );
//                    String base64 = nextSegment.substring( 7 );
//                    // XXX URLEncode?
//                    String searchString = new String( Base64.decodeBase64( base64.getBytes( MosaicContentProvider.ENCODING ) ) );
//                    searchFolder.query = searchFolder.expandQuery( query, searchString );
//                    result.add( searchFolder );
//                }
//            }
            
            return result;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
//    protected Query expandQuery( Query query, String luceneQueryString ) throws Exception {
//        Query filterQuery = new QueryParser( 
//                LuceneRecordStore.VERSION, "name", new WhitespaceAnalyzer( LuceneRecordStore.VERSION ) )
//                .parse( luceneQueryString );
//
//        BooleanQuery result = new BooleanQuery();
//        result.add( query, BooleanClause.Occur.MUST );
//        result.add( filterQuery, BooleanClause.Occur.MUST );
//        return result;
//    }
    
    
    @Override
    public IContentFile createNew( String newName, InputStream inputStream, Long length, String contentType )
            throws IOException, NotAuthorizedException, BadRequestException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    
    @Override
    public IContentFolder createFolder( String newName ) {
        getSite().invalidateFolder( this );
        MosaicCaseFolder result = new MosaicCaseFolder( newName, getParentPath(), getProvider() );
        result.create();
        return result;
    }


    @Override
    public void sendDescription( OutputStream out, Range range, Map<String, String> params, String contentType )
            throws IOException {
        if (contentType.contains( "json" )) {
            // TODO encode MosaicCases as JSON
            throw new RuntimeException( "Encoding MosaicCases as JSON is not yet implemented." );
        }
        else {
            super.sendDescription( out, range, params, contentType );
        }
    }

}
