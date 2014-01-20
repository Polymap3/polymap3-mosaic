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
package org.polymap.azv.ui.map;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PdfCreator {

    private static Log log = LogFactory.getLog( PdfCreator.class );
    
    private Document            doc;

    private PdfWriter           writer;
    
    
    public PdfCreator( Rectangle pageSize, OutputStream out ) throws DocumentException {
        doc = new Document( pageSize );
        writer = PdfWriter.getInstance( doc, out );
        doc.open();

//            String imageUrl = "http://jenkov.com/images/" + "20081123-20081123-3E1W7902-small-portrait.jpg";
//            Image image2 = Image.getInstance( new URL( imageUrl ) );
//            document.add( image2 );
    }
    
    
    public void close() {
        doc.close();
    }


    @Override
    protected void finalize() throws Throwable {
        if (doc != null) {
            close();
        }
    }


    public Document document() {
        return doc;
    }

    public PdfCreator addHtml( String html ) throws IOException {
        XMLWorkerHelper.getInstance().parseXHtml( writer, doc, new StringReader( html ) );
        return this;
    }
}
