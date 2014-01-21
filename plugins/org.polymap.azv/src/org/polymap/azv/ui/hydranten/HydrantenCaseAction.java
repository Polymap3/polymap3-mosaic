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

import org.apache.commons.lang.StringUtils;
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

import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;

import org.polymap.azv.AzvPlugin;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.CaseStatus;
import org.polymap.mosaic.ui.casepanel.DefaultCaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseActionSite;
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
 * @deprecated Siehe {@link HydrantenPanel}
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class HydrantenCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( HydrantenCaseAction.class );

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>        mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2>  repo;

    private ICaseActionSite                     site;

    private OpenLayersWidget                    olwidget;

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        if (mcase.get() != null && repo.get() != null
                && mcase.get().getNatures().contains( AzvPlugin.CASE_HYDRANTEN )) {
            // open action
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    site.activateCaseAction( site.getActionId() );
                }
            });
            return true;
        }
        return false;
    }

    
    @Override
    public void fillStatus( CaseStatus status ) {
        CaseStatus caseStatus = status;
        String id = mcase.get().getId();
        caseStatus.put( "Laufende Nr.", StringUtils.right( id, 6 ) );
    }

    
    @Override
    public void createContents( Composite parent ) {
        Composite body = parent;
        //body.setLayout( ColumnLayoutFactory.defaults().margins( 0 ).columns( 1, 1 ).spacing( 2 ).create() );
        body.setLayout( FormLayoutFactory.defaults().spacing( 5 ).margins( 5 ).create() );

        // toolbar
        Composite toolbar = site.toolkit().createComposite( body );
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

        WMSLayer hydranten = new WMSLayer( "Hydranten", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-Hydranten" );
        hydranten.setIsBaseLayer( false );
        hydranten.setVisibility( true );
        map.addLayer( hydranten );
        
        map.addControl( new NavigationControl() );
        map.addControl( new PanZoomBarControl() );
        map.addControl( new LayerSwitcherControl() );
        map.addControl( new MousePositionControl() );
        map.addControl( new ScaleLineControl() );
        map.addControl( new ScaleControl() );

        map.zoomToExtent( maxExtent, true );
        map.zoomTo( 10 );
        
        // after olwidget is created
        createToolbar( toolbar );
    }

    
    protected void createToolbar( Composite toolbar ) {
        toolbar.setLayout( RowLayoutFactory.fillDefaults().fill( true ).create() );
        
        Button btn = site.toolkit().createButton( toolbar, null, SWT.PUSH );
        btn.setToolTipText( "Gesamte Karte darstellen" );
        btn.setImage( BatikPlugin.instance().imageForName( "resources/icons/expand.png" ) );
        btn.setEnabled( true );
        btn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                Bounds maxExtent = olwidget.getMap().getMaxExtent();
                olwidget.getMap().zoomToExtent( maxExtent, true );
                olwidget.getMap().zoomTo( 9 );
            }
        });

        final Text searchTxt = site.toolkit().createText( toolbar, "Suchen: Ort, PLZ, Straße", SWT.SEARCH, SWT.CANCEL );
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
    }
    
}
