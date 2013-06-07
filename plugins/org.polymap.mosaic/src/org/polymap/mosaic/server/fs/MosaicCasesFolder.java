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
package org.polymap.mosaic.server.fs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.polymap.core.runtime.recordstore.IRecordState;
import org.polymap.core.runtime.recordstore.RecordQuery;
import org.polymap.core.runtime.recordstore.ResultSet;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordQuery;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentFolder;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentFolder;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentPutable;
import org.polymap.service.fs.spi.IMakeFolder;
import org.polymap.service.fs.spi.NotAuthorizedException;

import org.polymap.mosaic.server.model.MosaicCase;
import org.polymap.mosaic.server.model.MosaicRepository;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicCasesFolder
        extends DefaultContentFolder
        implements IContentPutable, IMakeFolder {

    private static Log log = LogFactory.getLog( MosaicCasesFolder.class );
    
    private Query           query = new TermQuery( new Term( "type", MosaicCase.class.getName() ) );
    
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
            MosaicRepository repo = MosaicRepository.instance();
            LuceneRecordStore recordStore = repo.recordStore();

            // query in request params
            String queryParam = requestParams.get( MosaicContentProvider.QUERY_PARAM );
            Query luceneQuery = queryParam != null ? expandQuery( query, queryParam ) : query;
            String firstResultParam = requestParams.get( MosaicContentProvider.FIRSTRESULT_PARAM );
            String maxResultsParam = requestParams.get( MosaicContentProvider.MAXRESULTS_PARAM );

            log.info( "LUCENE: " + luceneQuery );
            RecordQuery recordQuery = new LuceneRecordQuery( recordStore, luceneQuery );
            
            recordQuery.setFirstResult( firstResultParam != null
                    ? Integer.parseInt( firstResultParam ) : firstResult );
            
            recordQuery.setMaxResults( maxResultsParam != null 
                    ? Integer.parseInt( maxResultsParam ) : maxResults );
            
            ResultSet rs = recordStore.find( recordQuery );
            
            // find entities
            List<IContentFolder> result = new ArrayList();
            for (IRecordState state : rs) {
                MosaicCase entity = repo.findEntity( MosaicCase.class, (String)state.id() );
                result.add( new MosaicCaseFolder( entity.getName(), getPath(), getProvider(), entity ) ); 
            }
            
            // query via child node
            IPath path = Path.fromOSString( requestPath );
            if (path.segmentCount() > getPath().segmentCount() + 1) {
                // XXX +1 for the leading /webdav segment
                String nextSegment = path.segment( getPath().segmentCount() + 1 );
                if (nextSegment.startsWith( "search" )) {
                    MosaicCasesFolder searchFolder = new MosaicCasesFolder( nextSegment, getPath(), getProvider() );
                    String base64 = nextSegment.substring( 7 );
                    String searchString = new String( Base64.decodeBase64( base64.getBytes( MosaicContentProvider.ENCODING ) ) );
                    searchFolder.query = searchFolder.expandQuery( query, searchString );
                    result.add( searchFolder );
                }
            }
            
            return result;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
    @SuppressWarnings("hiding")
    protected Query expandQuery( Query query, String luceneQueryString ) throws Exception {
        Query filterQuery = new QueryParser( 
                LuceneRecordStore.VERSION, "name", new WhitespaceAnalyzer( LuceneRecordStore.VERSION ) )
                .parse( luceneQueryString );

        BooleanQuery result = new BooleanQuery();
        result.add( query, BooleanClause.Occur.MUST );
        result.add( filterQuery, BooleanClause.Occur.MUST );
        return result;
    }
    
    
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

}
