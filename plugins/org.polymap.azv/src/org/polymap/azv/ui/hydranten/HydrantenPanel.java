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
package org.polymap.azv.ui.hydranten;

import java.util.Date;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Principal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;

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
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;

import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.ui.map.AddressSearchMapAction;
import org.polymap.azv.ui.map.HomeMapAction;
import org.polymap.azv.ui.map.MapViewer;
import org.polymap.azv.ui.map.ScaleMapAction;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.openlayers.rap.widget.layers.WMSLayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class HydrantenPanel
        extends DefaultPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( HydrantenPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "Hydranten" ); //$NON-NLS-1$

    public static final IMessages       i18n = Messages.forPrefix( "HydrantenPanel" ); //$NON-NLS-1$

//    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
//    private ContextProperty<IMosaicCase> mcase;
//
//    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
//    private ContextProperty<MosaicRepository2> repo;

    private MapViewer                   mapViewer;


    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        site.setTitle( i18n.get( "title" ) );
        //site.setIcon( AzvPlugin.instance().imageForName( "resources/icons/fire-filter.png" ) );
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
        panelBody.setLayout( new FillLayout() );
        ((FillLayout)panelBody.getLayout()).marginHeight = 10;
        ((FillLayout)panelBody.getLayout()).marginWidth = 10;
        
        mapViewer = new MapViewer();
        mapViewer.createContents( panelBody, getSite() );
        
        // layers
        WMSLayer hydranten = new WMSLayer( i18n.get( "layerName" ), i18n.get( "layerWmsUrl" ), i18n.get( "layerWmsLayer" ) );
        hydranten.setIsBaseLayer( false );
        hydranten.setVisibility( true );
        mapViewer.addLayer( hydranten, false, true );
        
        // toolbar
        mapViewer.addToolbarItem( new HomeMapAction( mapViewer ) );
        mapViewer.addToolbarItem( new ScaleMapAction( mapViewer, 250 ) );
        mapViewer.addToolbarItem( new ScaleMapAction( mapViewer, 500 ) );
        mapViewer.addToolbarItem( new ScaleMapAction( mapViewer, 1000 ) );
        
        AddressSearchMapAction addressSearch = new AddressSearchMapAction( mapViewer );
        mapViewer.addToolbarItem( addressSearch );
        if (!SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA )) {
            Principal user = Polymap.instance().getUser();
            User umuser = UserRepository.instance().findUser( user.getName() );
            if (umuser != null) {
                addressSearch.preset( umuser.address().get().city().get() );
            }
        }
        
        mapViewer.addToolbarItem( new ContributionItem() {
            public void fill( Composite parent ) {
                Button btn = getSite().toolkit().createButton( parent, i18n.get( "pdfA4" ), SWT.PUSH );
                btn.setToolTipText( i18n.get( "pdfA4Tip" ) );
                btn.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_SUBMIT );
                btn.addSelectionListener( new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent ev ) {
                        createPDF( PageSize.A4 );
                    }
                });
            }
        });
        
        mapViewer.addToolbarItem( new ContributionItem() {
            public void fill( Composite parent ) {
                Button btn = getSite().toolkit().createButton( parent, i18n.get( "pdfA3" ), SWT.PUSH );
                btn.setToolTipText( i18n.get( "pdfA3Tip" ) );
                btn.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_SUBMIT );
                btn.addSelectionListener( new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent ev ) {
                        createPDF( PageSize.A3 );
                    }
                });
            }
        });
    }
    
    
    protected void createPDF( final Rectangle pageSize ) {
        String title = i18n.get( "pdfTitle", AzvPlugin.df.format( new Date() ) );
        // in Display thread to allow dialog to be displayed
        final byte[] bytes = mapViewer.createPdf( pageSize, title );
        if (bytes == null) {
            return;
        }
        
        String url = DownloadServiceHandler.registerContent( new ContentProvider() {
            @Override
            public String getFilename() {
                return i18n.get( "pdfFilename", AzvPlugin.df.format( new Date() ) ); 
            }
            @Override
            public String getContentType() {
                return "application/pdf"; //$NON-NLS-1$
            }
            @Override
            public InputStream getInputStream() throws Exception {
                return new ByteArrayInputStream( bytes );
            }
            @Override
            public boolean done( boolean success ) {
                return true;
            }
        });  
        ExternalBrowser.open( "download_window", url, //$NON-NLS-1$
                ExternalBrowser.NAVIGATION_BAR | ExternalBrowser.STATUS );
    }
    
}
