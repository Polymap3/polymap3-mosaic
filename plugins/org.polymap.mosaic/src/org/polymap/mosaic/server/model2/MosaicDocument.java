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
package org.polymap.mosaic.server.model2;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;

import com.google.common.base.Function;

import org.polymap.mosaic.server.model.IMosaicDocument;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicDocument
        implements IMosaicDocument {

    private static Log log = LogFactory.getLog( MosaicDocument.class );

    public static final Function<FileObject,IMosaicDocument> toDocument = new Function<FileObject,IMosaicDocument>() {
        @Override
        public IMosaicDocument apply( FileObject input ) {
            return new MosaicDocument( input );
        }
    };

    
    // instance *******************************************
    
    private FileObject         file;
    
    
    protected MosaicDocument( FileObject file ) {
        this.file = file;
    }

    @Override
    public String getName() {
        return file.getName().getBaseName();
    }

    @Override
    public long getSize() {
        try {
            return file.getContent().getSize();
        }
        catch (FileSystemException e) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public String getContentType() {
        try {
            return file.getContent().getContentInfo().getContentType();
        }
        catch (FileSystemException e) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public long getLastModified() {
        try {
            return file.getContent().getLastModifiedTime();
        }
        catch (FileSystemException e) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public InputStream getInputStream() {
        try {
            return file.getContent().getInputStream();
        }
        catch (FileSystemException e) {
            throw new RuntimeException( e );
        }
    }

    @Override
    public OutputStream getOutputStream() {
        try {
            return file.getContent().getOutputStream();
        }
        catch (FileSystemException e) {
            throw new RuntimeException( e );
        }
    }
    
}
