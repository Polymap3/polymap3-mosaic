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
package org.polymap.mosaic.server.document;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;

import org.polymap.mosaic.server.model.IMosaicCase;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SimpleFilesystemMapper
        implements IDocumentNameMapper {

    private static Log log = LogFactory.getLog( SimpleFilesystemMapper.class );

    public static final Pattern     notAllowed = Pattern.compile( "[^a-zA-Z0-9.-_]" ); 

    @Override
    public String documentPath( IMosaicCase mcase, String name ) {
        return Joiner.on( "/").skipNulls().join( normalize( mcase.getId() ), normalize( name ) );
    }
    
    
    protected String normalize( String name ) {
        return name != null ? notAllowed.matcher( name ).replaceAll( "_" ) : null;
    }
    
}
