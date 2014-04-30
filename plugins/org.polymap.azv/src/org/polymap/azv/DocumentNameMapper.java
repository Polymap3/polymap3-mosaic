/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.azv;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.azv.model.AzvVorgang;
import org.polymap.mosaic.server.document.SimpleFilesystemMapper;
import org.polymap.mosaic.server.model.IMosaicCase;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DocumentNameMapper
        extends SimpleFilesystemMapper {

    private static Log log = LogFactory.getLog( DocumentNameMapper.class );

    public static final FastDateFormat df = FastDateFormat.getInstance( "yyyy/MM/" );

    
    @Override
    public String documentPath( IMosaicCase mcase, String name ) {
        String laufendeNr = mcase.as( AzvVorgang.class ).laufendeNr.get();
        if (laufendeNr != null && laufendeNr.length() > 0) {
            log.warn( "!!! Vorgang hat keine laufendeNr. !!!" );
        }
        else {
            laufendeNr = normalize( mcase.getId() );
        }
        
        return df.format( mcase.getCreated() ) + laufendeNr + (name != null ? "/"+name : "");
    }
    
}
