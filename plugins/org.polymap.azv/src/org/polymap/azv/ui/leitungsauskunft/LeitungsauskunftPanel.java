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
package org.polymap.azv.ui.leitungsauskunft;

import java.util.Date;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.rwt.lifecycle.WidgetUtil;
import org.eclipse.rwt.widgets.ExternalBrowser;

import org.eclipse.jface.action.ContributionItem;

import org.polymap.core.data.operation.DownloadServiceHandler;
import org.polymap.core.data.operation.DownloadServiceHandler.ContentProvider;
import org.polymap.core.runtime.IMessages;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.app.BatikApplication;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.ui.map.AddressSearchMapAction;
import org.polymap.azv.ui.map.HomeMapAction;
import org.polymap.azv.ui.map.MapViewer;
import org.polymap.azv.ui.map.ScaleMapAction;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.CasePanel;
import org.polymap.openlayers.rap.widget.layers.WMSLayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LeitungsauskunftPanel
        extends DefaultPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( LeitungsauskunftPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "Leitungsauskunft" );

    public static final IMessages       i18n = Messages.forPrefix( "LeitungsauskunftPanel" );

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase> mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2> repo;

    private MapViewer                   mapViewer;


    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        site.setTitle( "Automatische Leitungsauskunft" );
        return false;
    }

    
    @Override
    public void dispose() {
        mapViewer.dispose();
    }


    @Override
    public PanelIdentifier id() {
        return ID;
    }


    @Override
    public void createContents( Composite panelBody ) {
//        IPanelSection contents = getSite().toolkit().createPanelSection( panelBody, null );  //"Wasserhärten und -Qualitäten" );
//        contents.addConstraint( new MinWidthConstraint( 500, 1 ) )
//                .addConstraint( new MaxWidthConstraint( 800, 1 ) );
//        Composite body = contents.getBody();
        //body.setLayout( ColumnLayoutFactory.defaults().margins( 0 ).columns( 1, 1 ).spacing( 2 ).create() );
        //body.setLayout( FormLayoutFactory.defaults().spacing( 5 ).margins( 20 ).create() );

        ((FillLayout)panelBody.getLayout()).marginHeight = 10;
        ((FillLayout)panelBody.getLayout()).marginWidth = 10;
        
        mapViewer = new MapViewer();
        mapViewer.createContents( panelBody, getSite() );
        
        // layers
        WMSLayer kanal = new WMSLayer( "Kanal", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-Kanal" );
        kanal.setVisibility( true );
        mapViewer.addLayer( kanal );
        
        WMSLayer wasser = new WMSLayer( "Wasser", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-Wasser" );
        wasser.setVisibility( true );
        mapViewer.addLayer( wasser );
        
        // toolbar
        mapViewer.addToolbarItem( new HomeMapAction( mapViewer ) );
        mapViewer.addToolbarItem( new ScaleMapAction( mapViewer, 250 ) );
        mapViewer.addToolbarItem( new ScaleMapAction( mapViewer, 500 ) );
        mapViewer.addToolbarItem( new ScaleMapAction( mapViewer, 1000 ) );
        mapViewer.addToolbarItem( new AddressSearchMapAction( mapViewer ) );
        mapViewer.addToolbarItem( new ContributionItem() {
            public void fill( Composite parent ) {
                Button btn = getSite().toolkit().createButton( parent, "A4", SWT.PUSH );
                btn.setToolTipText( "PDF/A4 erzeugen" );
                btn.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_SUBMIT );
                btn.addSelectionListener( new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent ev ) {
                        createPDF();
                    }
                });
            }
        });
        mapViewer.addToolbarItem( new ContributionItem() {
            public void fill( Composite parent ) {
                Button btn = getSite().toolkit().createButton( parent, "A3", SWT.PUSH );
                btn.setToolTipText( "PDF/A3 erzeugen" );
                btn.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_SUBMIT );
                btn.setEnabled( false );
            }
        });
        
        mapViewer.addToolbarItem( new ContributionItem() {
            public void fill( Composite parent ) {
                Button btn = getSite().toolkit().createButton( parent, "Antrag auf Einzelauskunft" );
                btn.setToolTipText( "Einen Antrag auf Einzelauskunft durch den AZV" );
                btn.addSelectionListener( new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent ev ) {
                        try {
                            // create new case; commit/rollback inside CaseAction
                            IMosaicCase newCase = repo.get().newCase( "", "" );
                            newCase.addNature( AzvPlugin.CASE_LEITUNGSAUSKUNFT );
                            //newCase.put( "user", user.get().username().get() );
                            mcase.set( newCase );
                            getContext().openPanel( CasePanel.ID );
                        }
                        catch (Exception e) {
                            BatikApplication.handleError( "Schachtschein konnte nicht angelegt werden.", e );
                        }
                    }
                });
            }
        });
    }
    
    
    protected void createPDF() {
        String url = DownloadServiceHandler.registerContent( new ContentProvider() {
            @Override
            public String getFilename() {
                return "Leistungsauskunft-" + AzvPlugin.df.format( new Date() ) + ".pdf";
            }
            @Override
            public String getContentType() {
                return "application/pdf";
            }
            @Override
            public InputStream getInputStream() throws Exception {
                return getClass().getClassLoader().getResourceAsStream( "resources/Lageplan.pdf" );
            }
            @Override
            public boolean done( boolean success ) {
                return true;
            }
        });  
        ExternalBrowser.open( "download_window", url,
                ExternalBrowser.NAVIGATION_BAR | ExternalBrowser.STATUS );
    }
    
}
