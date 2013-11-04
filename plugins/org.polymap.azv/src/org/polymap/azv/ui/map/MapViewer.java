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
package org.polymap.azv.ui.map;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.layout.RowLayoutFactory;

import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.IPanelSite;

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
import org.polymap.openlayers.rap.widget.layers.GridLayer;
import org.polymap.openlayers.rap.widget.layers.Layer;
import org.polymap.openlayers.rap.widget.layers.WMSLayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MapViewer {

    private static Log log = LogFactory.getLog( MapViewer.class );
    
    private IPanelSite          site;

    private OpenLayersWidget    olwidget;

    private Composite           contents;
    
    private List<IContributionItem> toolbarItems = new ArrayList();

    private Composite toolbar;

    
    public void dispose() {
        for (IContributionItem item : toolbarItems) {
            item.dispose();
        }
        toolbarItems.clear();

        if (olwidget != null) {
            olwidget.getMap().dispose();
            olwidget.dispose();
            olwidget = null;
        }
    }
    
    
    public Composite getControl() {
        return contents;
    }
    
    public IPanelSite getPanelSite() {
        return site;
    }
    
    public OpenLayersMap getMap() {
        return olwidget.getMap();
    }


    public MapViewer addLayer( Layer layer ) {
        layer.setIsBaseLayer( false );
        if (layer instanceof GridLayer) {
            ((GridLayer)layer).setTileSize( new Size( 400, 400 ) );
            ((GridLayer)layer).setBuffer( 0 );
        }
        olwidget.getMap().addLayer( layer );
        return this;
    }
    
    
    public void addToolbarItem( IContributionItem item ) {
        toolbarItems.add( item );
        if (toolbar != null) {
            item.fill( toolbar );
        }
    }


    public void createContents( Composite _body, IPanelSite _site ) {
        this.site = _site;
        this.contents = site.toolkit().createComposite( _body );
        //body.setLayout( ColumnLayoutFactory.defaults().margins( 0 ).columns( 1, 1 ).spacing( 2 ).create() );
        contents.setLayout( FormLayoutFactory.defaults().spacing( 5 ).margins( 0 ).create() );

        // toolbar
        toolbar = site.toolkit().createComposite( contents );
        toolbar.setLayoutData( FormDataFactory.filled().bottom( -1 ).create() );
        
        // map widget
        olwidget = new OpenLayersWidget( contents, SWT.MULTI | SWT.WRAP | SWT.BORDER, "openlayers/full/OpenLayers-2.12.1.js" );
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

//        WMSLayer wasser = new WMSLayer( "Wasser", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-Wasserquali" );
//        wasser.setIsBaseLayer( false );
//        wasser.setVisibility( true );
//        map.addLayer( wasser );
//        
//        WMSLayer hydranten = new WMSLayer( "Hydranten", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-Hydranten" );
//        hydranten.setIsBaseLayer( false );
//        hydranten.setVisibility( false );
//        map.addLayer( hydranten );

        map.addControl( new NavigationControl() );
        map.addControl( new PanZoomBarControl() );
        map.addControl( new LayerSwitcherControl() );
        map.addControl( new MousePositionControl() );
        map.addControl( new ScaleLineControl() );
        map.addControl( new ScaleControl() );

        map.zoomToExtent( maxExtent, true );
        map.zoomTo( 10 );

        // after olwidget is initialized
        createToolbar();        
    }

    
    protected void createToolbar() {
        toolbar.setLayout( RowLayoutFactory.fillDefaults().fill( true ).create() );
        
        for (IContributionItem item : toolbarItems) {
            item.fill( toolbar );
        }
    }

}
