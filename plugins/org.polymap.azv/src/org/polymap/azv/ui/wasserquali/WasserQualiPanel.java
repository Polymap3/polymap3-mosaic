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
package org.polymap.azv.ui.wasserquali;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.polymap.core.runtime.IMessages;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.azv.Messages;
import org.polymap.azv.ui.map.AddressSearchMapAction;
import org.polymap.azv.ui.map.HomeMapAction;
import org.polymap.azv.ui.map.MapViewer;
import org.polymap.openlayers.rap.widget.layers.WMSLayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WasserQualiPanel
        extends DefaultPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( WasserQualiPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "WasserQuali" );

    public static final IMessages       i18n = Messages.forPrefix( "WasserQualiPanel" );

    private MapViewer                   mapViewer;


    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        site.setTitle( "Wasserhärten und -Qualitäten" );
        return false;
    }

    
    @Override
    public PanelIdentifier id() {
        return ID;
    }


    @Override
    public void createContents( Composite parent ) {
//        IPanelSection contents = getSite().toolkit().createPanelSection( panelBody, null );  //"Wasserhärten und -Qualitäten" );
//        contents.addConstraint( new MinWidthConstraint( 500, 1 ) )
//                .addConstraint( new MaxWidthConstraint( 800, 1 ) );
//        Composite body = contents.getBody();
//        //body.setLayout( ColumnLayoutFactory.defaults().margins( 0 ).columns( 1, 1 ).spacing( 2 ).create() );
//        body.setLayout( FormLayoutFactory.defaults().spacing( 5 ).margins( 20 ).create() );

        mapViewer = new MapViewer();
        mapViewer.createContents( parent, getSite() );

        WMSLayer wasser = new WMSLayer( "Wasser", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-Wasserquali" );
        wasser.setVisibility( true );
        mapViewer.addLayer( wasser );
        
        WMSLayer hydranten = new WMSLayer( "Hydranten", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-Hydranten" );
        hydranten.setVisibility( false );
        mapViewer.addLayer( hydranten );

        // after olwidget is initialized
        mapViewer.addToolbarItem( new HomeMapAction( mapViewer ) );
        mapViewer.addToolbarItem( new AddressSearchMapAction( mapViewer ) );
    }

}
