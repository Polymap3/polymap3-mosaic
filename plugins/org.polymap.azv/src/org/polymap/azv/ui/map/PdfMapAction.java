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

import java.io.ByteArrayInputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextpdf.text.Rectangle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.jface.action.ContributionItem;

import org.polymap.core.runtime.IMessages;

import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.app.BatikApplication;

import org.polymap.azv.Messages;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicDocument;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.openlayers.rap.widget.base_types.OpenLayersMap;

/**
 * Erzeugt ein PDF der aktuellen Karte und hängt es als Dokument an den Vorgang. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PdfMapAction
        extends ContributionItem {

    private static Log log = LogFactory.getLog( PdfMapAction.class );
    
    public static final IMessages   i18n = Messages.forPrefix( "KartePDF" ); //$NON-NLS-1$

    private IMosaicCase         mcase;

    private MosaicRepository2   repo;

    private IPanelSite          site;
    
    private OpenLayersMap       map;

    private MapViewer           viewer;
    
    private String              title;

    private Rectangle           pageSize;

    
    public PdfMapAction( MapViewer viewer, String title, Rectangle pageSize, IMosaicCase mcase, MosaicRepository2 repo ) {
        this.viewer = viewer;
        this.site = viewer.getPanelSite();
        this.map = viewer.getMap();
        this.mcase = mcase;
        this.repo = repo;
        this.title = title;
        this.pageSize = pageSize;
    }


    public void fill( Composite parent ) {
        Button btn = site.toolkit().createButton( parent, title, SWT.PUSH );
        btn.setToolTipText( i18n.get( "buttonTip" ) );
        btn.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_SUBMIT );
        btn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                createDocument();
            }
        });
    }


    protected void createDocument() {
        try {            
            byte[] bytes = viewer.createPdf( pageSize, mcase.getName() );

            // find next name
            String name = mcase.getName() + "-" + title + ".pdf";
            int c = 1;
            IMosaicDocument doc = null;
            do { try {
                    doc = repo.newDocument( mcase, name );
                } catch (Exception e) {
                    name = mcase.getName() + "-" + title + "-" + c++ + ".pdf"; 
                }
            } while (doc == null);
            
            // write contents
            OutputStream out = doc.getOutputStream();
            try {
                IOUtils.copy( new ByteArrayInputStream( bytes ), out );
            }
            finally {
                IOUtils.closeQuietly( out );
            }
            repo.commitChanges();
        }
        catch (Exception e) {
            repo.rollbackChanges();
            BatikApplication.handleError( i18n.get( "fehler" ), e );
        }
    }

    
//    protected class NameEquals
//            implements Predicate<IMosaicDocument> {
//
//        private String name
//        @Override
//        public boolean apply( IMosaicDocument input ) {
//        }
//    }
    
}
