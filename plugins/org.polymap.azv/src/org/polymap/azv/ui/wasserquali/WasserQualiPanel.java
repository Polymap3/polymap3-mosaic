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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.eclipse.rwt.graphics.Graphics;

import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.MaxWidthConstraint;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;

import org.polymap.azv.Messages;
import org.polymap.openlayers.rap.widget.OpenLayersWidget;
import org.polymap.openlayers.rap.widget.base_types.Bounds;
import org.polymap.openlayers.rap.widget.base_types.OpenLayersMap;
import org.polymap.openlayers.rap.widget.base_types.Projection;
import org.polymap.openlayers.rap.widget.base_types.Size;
import org.polymap.openlayers.rap.widget.controls.LayerSwitcherControl;
import org.polymap.openlayers.rap.widget.controls.MousePositionControl;
import org.polymap.openlayers.rap.widget.controls.NavigationControl;
import org.polymap.openlayers.rap.widget.controls.PanZoomBarControl;
import org.polymap.openlayers.rap.widget.controls.ScaleControl;
import org.polymap.openlayers.rap.widget.controls.ScaleLineControl;
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

    private OpenLayersWidget olwidget;


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
    public void createContents( Composite panelBody ) {
        IPanelSection contents = getSite().toolkit().createPanelSection( panelBody, null );  //"Wasserhärten und -Qualitäten" );
        contents.addConstraint( new MinWidthConstraint( 500, 1 ) )
                .addConstraint( new MaxWidthConstraint( 800, 1 ) );
        Composite body = contents.getBody();
        //body.setLayout( ColumnLayoutFactory.defaults().margins( 0 ).columns( 1, 1 ).spacing( 2 ).create() );
        body.setLayout( FormLayoutFactory.defaults().spacing( 5 ).margins( 20 ).create() );

        // toolbar
        Composite toolbar = getSite().toolkit().createComposite( body );
        toolbar.setLayoutData( FormDataFactory.filled().bottom( -1 ).create() );
        
        // map widget
        olwidget = new OpenLayersWidget( body, SWT.MULTI | SWT.WRAP | SWT.BORDER, "openlayers/full/OpenLayers-2.12.1.js" );
        //olwidget.setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 450 ) );
        olwidget.setLayoutData( FormDataFactory.filled().top( toolbar ).create() );

        String srs = "EPSG:25833";
        Projection proj = new Projection( srs );
        String units = srs.equals( "EPSG:4326" ) ? "degrees" : "m";
        float maxResolution = srs.equals( "EPSG:4326" ) ? (360/256) : 125000;
        //Bounds maxExtent = new Bounds( 12.80, 53.00, 14.30, 54.50 );
        Bounds maxExtent = new Bounds( 330000, 5820000, 477000, 6078174.895021 );
        olwidget.createMap( proj, proj, units, maxExtent, maxResolution );
        OpenLayersMap map = olwidget.getMap();
        
        //OSMLayer osm = new OSMLayer( "OSM", "http://tile.openstreetmap.org/${z}/${x}/${y}.png", 9 );
        //WMSLayer osm = new WMSLayer( "OSM", "http://ows.terrestris.de/osm-basemap/service", "OSM-WMS-Deutschland" );
        WMSLayer topo = new WMSLayer( "Topo MV", "http://www.geodaten-mv.de/dienste/gdimv_topomv", "gdimv_topomv" );
        topo.setIsBaseLayer( true );
        topo.setTileSize( new Size( 600, 600 ) );
        topo.setBuffer( 0 );
        map.addLayer( topo );

        WMSLayer dop = new WMSLayer( "DOP", "http://www.geodaten-mv.de/dienste/adv_dop", "mv_dop" );
        dop.setIsBaseLayer( true );
        dop.setTileSize( new Size( 400, 400 ) );
        dop.setBuffer( 0 );
        map.addLayer( dop );

        WMSLayer wasser = new WMSLayer( "Wasser", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-Wasserquali" );
        wasser.setIsBaseLayer( false );
        wasser.setVisibility( true );
        map.addLayer( wasser );
        
        WMSLayer hydranten = new WMSLayer( "Hydranten", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-Hydranten" );
        hydranten.setIsBaseLayer( false );
        hydranten.setVisibility( false );
        map.addLayer( hydranten );

        map.addControl( new NavigationControl() );
        map.addControl( new PanZoomBarControl() );
        map.addControl( new LayerSwitcherControl() );
        map.addControl( new MousePositionControl() );
        map.addControl( new ScaleLineControl() );
        map.addControl( new ScaleControl() );

        map.zoomToExtent( maxExtent, true );
        map.zoomTo( 10 );

        // after olwidget is initialized
        createToolbar( toolbar );        
    }

    
    protected void createToolbar( Composite toolbar ) {
        toolbar.setLayout( RowLayoutFactory.fillDefaults().fill( true ).create() );
        
        Button btn = getSite().toolkit().createButton( toolbar, null, SWT.PUSH );
        btn.setToolTipText( "Gesamte Karte darstellen" );
        btn.setImage( BatikPlugin.instance().imageForName( "resources/icons/house-org.png" ) );
        btn.setEnabled( true );
        btn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                Bounds maxExtent = olwidget.getMap().getMaxExtent();
                olwidget.getMap().zoomToExtent( maxExtent, true );
                olwidget.getMap().zoomTo( 9 );
            }
        });

        final Text searchTxt = getSite().toolkit().createText( toolbar, "Suchen: Ort, PLZ, Straße", SWT.SEARCH, SWT.CANCEL );
        searchTxt.setLayoutData( RowDataFactory.swtDefaults().hint( 320, SWT.DEFAULT ).create() );
        //searchTxt.setLayoutData( FormDataFactory.filled().right( clearBtn ).create() );

        searchTxt.setToolTipText( "Suchbegriff: min. 3 Zeichen" );
        searchTxt.setForeground( Graphics.getColor( 0xa0, 0xa0, 0xa0 ) );
        searchTxt.addFocusListener( new FocusListener() {
            @Override
            public void focusLost( FocusEvent ev ) {
                if (searchTxt.getText().length() == 0) {
                    searchTxt.setText( "Suchen..." );
                    searchTxt.setForeground( Graphics.getColor( 0xa0, 0xa0, 0xa0 ) );
                    //clearBtn.setEnabled( false );
                }
            }
            @Override
            public void focusGained( FocusEvent ev ) {
                if (searchTxt.getText().startsWith( "Suchen" )) {
                    searchTxt.setText( "" );
                    searchTxt.setForeground( Graphics.getColor( 0x00, 0x00, 0x00 ) );
                }
            }
        });
//        searchTxt.addModifyListener( new ModifyListener() {
//            @Override
//            public void modifyText( ModifyEvent ev ) {
//                clearBtn.setEnabled( searchTxt.getText().length() > 0 );
//                if (filter != null) {
//                    viewer.removeFilter( filter );
//                }
//                if (searchTxt.getText().length() > 2) {
//                    if (filter != null) {
//                        viewer.removeFilter( filter );
//                    }
//                    viewer.addFilter( filter = new TextFilter( searchTxt.getText() ) );
//                }
//            }
//        });
        
//        final DrawFeatureMapAction drawFeatureAction = new DrawFeatureMapAction( 
//                site, olwidget.getMap(), vectorLayer, DrawFeatureControl.HANDLER_POINT );
//        drawFeatureAction.fill( toolbar );
//        drawFeatureAction.addListener( new PropertyChangeListener() {
//            @EventHandler(display=true)
//            public void propertyChange( PropertyChangeEvent ev ) {
//                Feature feature = (Feature)ev.getNewValue();
//                Point point = (Point)feature.getDefaultGeometryProperty().getValue();
//                String wkt = new WKTWriter().write( point );
//                mcase.get().put( "point", wkt );
//                repo.get().commitChanges();
//                
//                site.getPanelSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, "Markierung wurde gesetzt auf: " + point.toText() ) );
//                
//                drawFeatureAction.deactivate();
//            }
//        });
    }

}
