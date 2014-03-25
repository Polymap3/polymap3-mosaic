/* 
 * polymap.org
 * Copyright (C) 2013-2014, Falko Bräutigam. All rights reserved.
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

import static com.google.common.collect.Iterables.concat;
import static java.util.Collections.singleton;

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
import com.vividsolutions.jts.geom.Envelope;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.layout.RowLayoutFactory;

import org.polymap.core.data.util.Geometries;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.IPanelSite;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
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
    
    public static final IMessages   i18n = Messages.forPrefix( "MapViewer" ); //$NON-NLS-1$

    private IPanelSite              site;

    private OpenLayersWidget        olwidget;

    private String                  srs;

    private OpenLayersMap           map;

    private List<WMSLayer>          layers = new ArrayList();

    private WMSLayer                visibleBaseLayer;

    /** The currently visible layers, excluding the {@link #visibleBaseLayer}. */
    private List<WMSLayer>          visibleLayers = new ArrayList();

    private Composite               contents;

    private List<IContributionItem> toolbarItems = new ArrayList();

    private Composite               toolbar;

    private ReferencedEnvelope      mapExtent;
    
//    private float                   mapScale = -1;

    private WMSLayer                dop;

    
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

    public void zoomToExtent( Envelope envelope ) {
        getMap().zoomToExtent( new Bounds( envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY()), true );
        try {
            mapExtent = new ReferencedEnvelope( envelope, Geometries.crs( srs ) );
            updateLayerVisibility();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
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
                setLayerVisible( layer, true );
            }
            else {
                visibleBaseLayer = visibleBaseLayer == null ? (WMSLayer)layer : visibleBaseLayer;
            }
        }
        return this;
    }
    
    
    public MapViewer setLayerVisible( Layer layer, boolean visible ) {
        assert layers.contains( layer );
        
        if (layer.isBaseLayer()) {
            map.setBaseLayer( layer );
            visibleBaseLayer = (WMSLayer)layer;
        } 
        else {
            layer.setVisibility( visible );
            if (visible) {
                visibleLayers.add( (WMSLayer)layer );
            } else {
                visibleLayers.remove( layer );
            }
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

        // layers
        int suffix = 1;
        while (i18n.contains( "layerName"+suffix )) { //$NON-NLS-1$
            WMSLayer layer = new WMSLayer( i18n.get( "layerName"+suffix ),  //$NON-NLS-1$
                    i18n.get( "layerWmsUrl"+suffix ), //$NON-NLS-1$
                    i18n.get( "layerWmsName"+suffix ) ); //$NON-NLS-1$
            addLayer( layer, true );
            if (layer.getName().toLowerCase().contains( "dop" )) {
                dop = layer;
            }
            suffix ++;
        }

//        //OSMLayer osm = new OSMLayer( "OSM", "http://tile.openstreetmap.org/${z}/${x}/${y}.png", 9 );
//        WMSLayer topo = new WMSLayer( "Topo MV", "http://www.geodaten-mv.de/dienste/gdimv_topomv", "gdimv_topomv" );
//        addLayer( topo, true );
//        //topo.setTileSize( new Size( 600, 600 ) );
//
//        WMSLayer osm = new WMSLayer( "OSM", "http://ows.terrestris.de/osm-basemap/service", "OSM-WMS-Deutschland" );
//        addLayer( osm, true );
////        osm.setIsBaseLayer( true );
////        osm.setTileSize( new Size( 600, 600 ) );
////        osm.setBuffer( 0 );
////        map.addLayer( osm );
////        layers.add( osm );
//
//        WMSLayer dop = new WMSLayer( "DOP", "http://www.geodaten-mv.de/dienste/adv_dop", "mv_dop" );
//        addLayer( dop, true );

       // map.setRestrictedExtend( maxExtent );
        map.zoomToExtent( maxExtent, true );
        map.zoomTo( 2 );

        // map events
        HashMap<String, String> payload = new HashMap<String, String>();
        payload.put( "left", "event.object.getExtent().toArray()[0]" );
        payload.put( "bottom", "event.object.getExtent().toArray()[1]" );
        payload.put( "right", "event.object.getExtent().toArray()[2]" );
        payload.put( "top", "event.object.getExtent().toArray()[3]" );
        payload.put( "scale", map.getJSObjRef() + ".getScale()" );
        map.events.register( this, OpenLayersMap.EVENT_MOVEEND, payload );
        map.events.register( this, OpenLayersMap.EVENT_ZOOMEND, payload );

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
            Iterable<WMSLayer> printLayers = concat( singleton( visibleBaseLayer ), visibleLayers );
            java.awt.Image image = new WmsMapImageCreator( printLayers ).createImage( mapExtent, w, h );

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
    
    
    private WMSLayer    userDefinedBaseLayer;
    
    public void updateLayerVisibility() {
        // XXX default map width if no layout yet
        int imageWidth = olwidget.getSize().x > 0 ? olwidget.getSize().x : 500;
        // XXX no geodetic CRS supported
        double mapScale = mapExtent.getWidth() / (imageWidth / 90/*dpi*/ * 0.0254);
        
        if (mapScale <= 0 || dop == null) {
            return;
        }
        else if (mapScale < 5000) {
            if (visibleBaseLayer != dop) {
                userDefinedBaseLayer = visibleBaseLayer;
                setLayerVisible( dop, true );
            }
        }
        else if (mapScale > 5000) {
            // XXX das zurück umschalten funktioniert nicht, da das umschalten per
            // layerSwitcher nicht ausgewertet wird
            if (userDefinedBaseLayer != null && visibleBaseLayer != userDefinedBaseLayer) {
                setLayerVisible( userDefinedBaseLayer, true );
                userDefinedBaseLayer = null;
            }
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
                mapExtent = new ReferencedEnvelope(
                        Double.parseDouble( payload.get( "left" ) ),
                        Double.parseDouble( payload.get( "right" ) ),
                        Double.parseDouble( payload.get( "bottom" ) ),
                        Double.parseDouble( payload.get( "top" ) ),
                        Geometries.crs( srs ) );
//                mapScale = Float.parseFloat( payload.get( "scale" ) );
//                log.info( "scale=" + mapScale + ", mapExtent= " + mapExtent );
                
                updateLayerVisibility();
            }
            catch (Exception e) {
                log.error( "unhandled:", e );
            }
        }
    }

}
