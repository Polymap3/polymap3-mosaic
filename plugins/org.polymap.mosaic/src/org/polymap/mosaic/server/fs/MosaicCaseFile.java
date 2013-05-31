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

import java.util.Date;
import java.util.Map;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bradmcevoy.http.FileItem;

import org.eclipse.core.runtime.IPath;

import org.polymap.service.fs.spi.BadRequestException;
import org.polymap.service.fs.spi.DefaultContentNode;
import org.polymap.service.fs.spi.IContentFile;
import org.polymap.service.fs.spi.IContentProvider;
import org.polymap.service.fs.spi.IContentWriteable;
import org.polymap.service.fs.spi.NotAuthorizedException;
import org.polymap.service.fs.spi.Range;

import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.MosaicRepository;

/**
 * The 'object.json' file of a Mosaic case.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicCaseFile
        extends DefaultContentNode
        implements IContentFile, IContentWriteable {

    private static Log log = LogFactory.getLog( MosaicCaseFile.class );
    
    public static final String          NAME = "object.json";
    

    public MosaicCaseFile( IPath parentPath, IContentProvider provider, IMosaicCase source ) {
        super( NAME, parentPath, provider, source );
        assert source != null;
    }


    @Override
    public IMosaicCase getSource() {
        return (IMosaicCase)super.getSource();
    }


    @Override
    public void replaceContent( InputStream in, Long length ) 
            throws IOException, BadRequestException, NotAuthorizedException {
        try {
            String json = IOUtils.toString( in, MosaicContentProvider.ENCODING );
            log.info( "JSON: " + json );

            final MosaicRepository repo = MosaicRepository.instance();
            getSource().decodeJsonState( new JSONObject( json ), repo, false );
            repo.commitChanges();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }


    @Override
    public String processForm( Map<String,String> params, Map<String,FileItem> files )
            throws IOException, BadRequestException, NotAuthorizedException {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType )
            throws IOException, BadRequestException {
        OutputStreamWriter writer = new OutputStreamWriter( out, MosaicContentProvider.ENCODING );
        try {
            JSONObject json = getSource().encodeJsonState( false );
            log.info( "JSON: " + json.toString( 4 ) );
            json.write( writer );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
        finally {
            IOUtils.closeQuietly( writer );
        }
    }


    @Override
    public String getContentType( String accepts ) {
        return "application/json";
    }


    @Override
    public Long getContentLength() {
        return null;
    }


    @Override
    public Date getModifiedDate() {
        return new Date( getSource().lastModified() );
    }


    @Override
    public Long getMaxAgeSeconds() {
        return null;
    }
    
}
