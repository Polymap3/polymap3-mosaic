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
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import org.geotools.geometry.jts.ReferencedEnvelope;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.layout.RowLayoutFactory;

import org.polymap.core.data.util.Geometries;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.IPanelSite;

import org.polymap.azv.AzvPlugin;
import org.polymap.openlayers.rap.widget.OpenLayersWidget;
import org.polymap.openlayers.rap.widget.base.OpenLayersEventListener;
import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.base_types.Bounds;
import org.polymap.openlayers.rap.widget.base_types.OpenLayersMap;
import org.polymap.openlayers.rap.widget.base_types.Projection;
import org.polymap.openlayers.rap.widget.base_types.Size;
import org.polymap.openlayers.rap.widget.controls.LayerSwitcherControl;
import org.polymap.openlayers.rap.widget.controls.LoadingPanelControl;
import org.polymap.openlayers.rap.widget.controls.MousePositionControl;
import org.polymap.openlayers.rap.widget.controls.NavigationControl;
import org.polymap.openlayers.rap.widget.controls.PanZoomBarControl;
import org.polymap.openlayers.rap.widget.controls.ScaleLineControl;
import org.polymap.openlayers.rap.widget.layers.GridLayer;
import org.polymap.openlayers.rap.widget.layers.Layer;
import org.polymap.openlayers.rap.widget.layers.WMSLayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MapViewer
        implements OpenLayersEventListener {

    private static Log log = LogFactory.getLog( MapViewer.class );
    
    private IPanelSite              site;

    private OpenLayersWidget        olwidget;

    private String                  srs;

    private OpenLayersMap           map;

    private List<WMSLayer>          layers = new ArrayList();

    private List<WMSLayer>          visibleLayers = new ArrayList();

    private Composite               contents;

    private List<IContributionItem> toolbarItems = new ArrayList();

    private Composite               toolbar;

    private ReferencedEnvelope      bbox;

    
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


    public MapViewer addLayer( Layer layer, boolean isBaseLayer ) {
        layer.setIsBaseLayer( isBaseLayer );
        if (layer instanceof GridLayer) {
            ((GridLayer)layer).setTileSize( new Size( 800, 800 ) );
            ((GridLayer)layer).setBuffer( 0 );
        }
        map.addLayer( layer );
        
        if (layer instanceof WMSLayer) {
            layers.add( (WMSLayer)layer );
            if (!isBaseLayer) {
                visibleLayer( layer, true );
            }
        }
        return this;
    }
    
    
    public MapViewer visibleLayer( Layer layer, boolean visible ) {
        assert layers.contains( layer );
        layer.setVisibility( visible );
        if (visible) {
            visibleLayers.add( (WMSLayer)layer );
        } else {
            visibleLayers.remove( layer );
        }
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
        contents.setLayout( FormLayoutFactory.defaults().spacing( 5 ).margins( 0 ).create() );

        // toolbar
        toolbar = site.toolkit().createComposite( contents );
        toolbar.setLayoutData( FormDataFactory.filled().bottom( -1 ).create() );
        
        // map widget (styling/background color from azv.css)
        olwidget = new OpenLayersWidget( contents, SWT.MULTI | SWT.WRAP | SWT.BORDER, "openlayers/full/OpenLayers-2.12.1.js" );
        olwidget.setLayoutData( FormDataFactory.filled().top( toolbar ).create() );

        srs = "EPSG:25833";
        Projection proj = new Projection( srs );
        String units = srs.equals( "EPSG:4326" ) ? "degrees" : "m";
        float maxResolution = -1; //srs.equals( "EPSG:4326" ) ? (360/256) : 50000;
        //Bounds maxExtent = new Bounds( 12.80, 53.00, 14.30, 54.50 );
        Bounds maxExtent = new Bounds( 330000, 5820000, 477000, 6078174.895021 );
        olwidget.createMap( proj, proj, units, maxExtent, maxResolution );
        map = olwidget.getMap();

        map.addControl( new NavigationControl( true ) );
        map.addControl( new PanZoomBarControl() );
        map.addControl( new LayerSwitcherControl() );
        map.addControl( new MousePositionControl() );
        map.addControl( new ScaleLineControl() );
//        map.addControl( new ScaleControl() );
        map.addControl( new LoadingPanelControl() );

        //OSMLayer osm = new OSMLayer( "OSM", "http://tile.openstreetmap.org/${z}/${x}/${y}.png", 9 );
        WMSLayer topo = new WMSLayer( "Topo MV", "http://www.geodaten-mv.de/dienste/gdimv_topomv", "gdimv_topomv" );
        addLayer( topo, true );
        //topo.setTileSize( new Size( 600, 600 ) );

        WMSLayer osm = new WMSLayer( "OSM", "http://ows.terrestris.de/osm-basemap/service", "OSM-WMS-Deutschland" );
        addLayer( osm, true );
//        osm.setIsBaseLayer( true );
//        osm.setTileSize( new Size( 600, 600 ) );
//        osm.setBuffer( 0 );
//        map.addLayer( osm );
//        layers.add( osm );

        WMSLayer dop = new WMSLayer( "DOP", "http://www.geodaten-mv.de/dienste/adv_dop", "mv_dop" );
        addLayer( dop, true );

//        WMSLayer wasser = new WMSLayer( "Wasser", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-Wasserquali" );
//        wasser.setIsBaseLayer( false );
//        wasser.setVisibility( true );
//        map.addLayer( wasser );
//        
//        WMSLayer hydranten = new WMSLayer( "Hydranten", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-Hydranten" );
//        hydranten.setIsBaseLayer( false );
//        hydranten.setVisibility( false );
//        map.addLayer( hydranten );

       // map.setRestrictedExtend( maxExtent );
        map.zoomToExtent( maxExtent, true );
        map.zoomTo( 2 );

        // map events
        HashMap<String, String> payload = new HashMap<String, String>();
        payload.put( "left", "event.object.getExtent().toArray()[0]" );
        payload.put( "bottom", "event.object.getExtent().toArray()[1]" );
        payload.put( "right", "event.object.getExtent().toArray()[2]" );
        payload.put( "top", "event.object.getExtent().toArray()[3]" );
        map.events.register( this, OpenLayersMap.EVENT_MOVEEND, payload );

        // after olwidget is initialized
        createToolbar();        
    }

    
    protected void createToolbar() {
        toolbar.setLayout( RowLayoutFactory.fillDefaults().fill( true ).create() );
        
        for (IContributionItem item : toolbarItems) {
            item.fill( toolbar );
        }
    }

    
    public byte[] createPdf( Rectangle pageSize, String title ) {
        InputStream htmlIn = null; 
        try {
            // pdf
            ByteArrayOutputStream bout = new ByteArrayOutputStream( 200*1024 );
            PdfCreator pdf = new PdfCreator( pageSize, bout );

            // image
            int w = (int)(pageSize.getWidth() - pageSize.getBorderWidthLeft() - pageSize.getBorderWidthRight());
            int h = (int)(pageSize.getHeight() - pageSize.getBorderWidthTop() - pageSize.getBorderWidthBottom());
            java.awt.Image image = new WmsMapImageCreator( visibleLayers ).createImage( bbox, w, h );

            Image pdfImage = Image.getInstance( image, null, false );
            pdfImage.scalePercent( 80 );
            pdfImage.setAlignment( Image.MIDDLE );
            //pdfImage.setBorder( Rectangle.BOX );
            //pdfImage.setBorderWidth( 1 );
            pdf.document().add( pdfImage );
            
            // html footer
            URL res = AzvPlugin.instance().getBundle().getResource( "resources/mapfooter.html" );
            String html = IOUtils.toString( htmlIn = res.openStream(), "UTF8" );
            html = StringUtils.replace( html, "{0}", title );
            html = StringUtils.replace( html, "{1}", "???" );
            html = StringUtils.replace( html, "{2}", AzvPlugin.df.format( new Date() ) );
            pdf.addHtml( html );
            
            pdf.close();
            
            return bout.toByteArray();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
        finally {
            IOUtils.closeQuietly( htmlIn );
        }
    }
    

    /*
     * Processes events triggered by the OpenLayers map. 
     */
    public void process_event( OpenLayersObject obj, String name, HashMap<String,String> payload ) {
        if (olwidget.getMap() != obj) {
            return;
        }
        // map zoom/pan
        String left = payload.get( "left" );
        if (left != null) {
            try {
                bbox = new ReferencedEnvelope(
                        Double.parseDouble( payload.get( "left" ) ),
                        Double.parseDouble( payload.get( "right" ) ),
                        Double.parseDouble( payload.get( "bottom" ) ),
                        Double.parseDouble( payload.get( "top" ) ),
                        Geometries.crs( srs ) );
                log.debug( "### process_event: bbox= " + bbox );
            }
            catch (Exception e) {
                log.error( "unhandled:", e );
            }
        }
    }

}
