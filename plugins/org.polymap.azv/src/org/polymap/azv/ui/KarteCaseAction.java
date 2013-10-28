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
package org.polymap.azv.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.opengis.feature.Feature;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.layout.RowLayoutFactory;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;

import org.polymap.azv.AzvPlugin;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.DefaultCaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseActionSite;
import org.polymap.openlayers.rap.widget.OpenLayersWidget;
import org.polymap.openlayers.rap.widget.base_types.Bounds;
import org.polymap.openlayers.rap.widget.base_types.OpenLayersMap;
import org.polymap.openlayers.rap.widget.base_types.Projection;
import org.polymap.openlayers.rap.widget.base_types.Size;
import org.polymap.openlayers.rap.widget.base_types.Style;
import org.polymap.openlayers.rap.widget.base_types.StyleMap;
import org.polymap.openlayers.rap.widget.controls.DrawFeatureControl;
import org.polymap.openlayers.rap.widget.controls.LayerSwitcherControl;
import org.polymap.openlayers.rap.widget.controls.MousePositionControl;
import org.polymap.openlayers.rap.widget.controls.NavigationControl;
import org.polymap.openlayers.rap.widget.controls.ScaleControl;
import org.polymap.openlayers.rap.widget.controls.ScaleLineControl;
import org.polymap.openlayers.rap.widget.features.VectorFeature;
import org.polymap.openlayers.rap.widget.geometry.PointGeometry;
import org.polymap.openlayers.rap.widget.layers.VectorLayer;
import org.polymap.openlayers.rap.widget.layers.WMSLayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class KarteCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( KarteCaseAction.class );

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>    mcase;
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2> repo;

    private ICaseActionSite                 site;

    private OpenLayersWidget                olwidget;

    private VectorLayer                     vectorLayer;

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        return mcase.get() != null && repo.get() != null
                && mcase.get().getNatures().contains( AzvPlugin.CASE_SCHACHTSCHEIN );
    }


    @Override
    public void dispose() {
        if (olwidget != null) {
            olwidget.getMap().dispose();
            olwidget.dispose();
            olwidget = null;
        }
    }


    @Override
    public void fillContentArea( Composite parent ) {
        // section
        IPanelSection mapSection = site.toolkit().createPanelSection( parent, "Ort" );
        mapSection.addConstraint( new PriorityConstraint( 10 ) );
        Composite body = mapSection.getBody();
        body.setLayout( ColumnLayoutFactory.defaults().margins( 0 ).columns( 1, 1 ).spacing( 2 ).create() );

        // map widget
        olwidget = new OpenLayersWidget( body, SWT.MULTI | SWT.WRAP | SWT.BORDER, "openlayers/full/OpenLayers-2.12.1.js" );
        olwidget.setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 450 ) );

        String srs = "EPSG:25833";
        Projection proj = new Projection( srs );
        String units = srs.equals( "EPSG:4326" ) ? "degrees" : "m";
        float maxResolution = srs.equals( "EPSG:4326" ) ? (360/256) : 125000;
        //Bounds maxExtent = new Bounds( 12.80, 53.00, 14.30, 54.50 );
        Bounds maxExtent = new Bounds( 330000, 5872837.393586, 477000, 6078174.895021 );
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

        WMSLayer alk = new WMSLayer( "ALK", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-ALK" );
        alk.setIsBaseLayer( false );
        alk.setVisibility( false );
        map.addLayer( alk );
        
        WMSLayer kanal = new WMSLayer( "Kanal", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-Kanal" );
        kanal.setIsBaseLayer( false );
        kanal.setVisibility( false );
        map.addLayer( kanal );
        
        WMSLayer wasser = new WMSLayer( "Wasser", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-Wasser" );
        wasser.setIsBaseLayer( false );
        wasser.setVisibility( false );
        map.addLayer( wasser );
        
        map.addControl( new NavigationControl() );
        //map.addControl( new PanZoomBarControl() );
        map.addControl( new LayerSwitcherControl() );
        map.addControl( new MousePositionControl() );
        map.addControl( new ScaleLineControl() );
        map.addControl( new ScaleControl() );

        map.zoomToExtent( maxExtent, true );
        map.zoomTo( 9 );
        
        // vector layer
        vectorLayer = new VectorLayer( "Markierung" );
        vectorLayer.setVisibility( true );
        vectorLayer.setIsBaseLayer( false );
        vectorLayer.setZIndex( 10000 );

        // style
        Style standard = new Style();
        standard.setAttribute( "strokeColor", "#ff0000" );
        standard.setAttribute( "strokeWidth", "4" );
        standard.setAttribute( "pointRadius", "10" );
        StyleMap styleMap = new StyleMap();
        styleMap.setIntentStyle( "default", standard );
        vectorLayer.setStyleMap( styleMap );
        
        map.addLayer( vectorLayer );
        
        // check if mcase has point
        drawMCasePoint();
        
        // toolbar
        createToolbar( body );        
    }

    
    protected void drawMCasePoint() {
        String wkt = mcase.get().get( "point" );
        if (wkt != null) {
            try {
                vectorLayer.destroyFeatures();
                
                Point p = (Point)new WKTReader().read( wkt );
                VectorFeature vectorFeature = new VectorFeature( new PointGeometry( p.getX(), p.getY() ) );
                vectorLayer.addFeatures( vectorFeature );
                //vectorLayer.redraw();

                olwidget.getMap().setCenter( p.getX(), p.getY() );
                olwidget.getMap().zoomTo( 15 );
            }
            catch (ParseException e) {
                log.warn( "", e );
            }
        }
    }
    
    
    protected void createToolbar( Composite parent ) {
        Composite toolbar = site.toolkit().createComposite( parent );
        toolbar.setLayout( RowLayoutFactory.fillDefaults().fill( true ).create() );
        
        Button btn = site.toolkit().createButton( toolbar, null, SWT.PUSH );
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

        final DrawFeatureMapAction drawFeatureAction = new DrawFeatureMapAction( 
                site, olwidget.getMap(), vectorLayer, DrawFeatureControl.HANDLER_POINT );
        drawFeatureAction.fill( toolbar );
        drawFeatureAction.addListener( new PropertyChangeListener() {
            @EventHandler(display=true)
            public void propertyChange( PropertyChangeEvent ev ) {
                Feature feature = (Feature)ev.getNewValue();
                Point point = (Point)feature.getDefaultGeometryProperty().getValue();
                String wkt = new WKTWriter().write( point );
                mcase.get().put( "point", wkt );
                repo.get().commitChanges();
                
                site.getPanelSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, "Markierung wurde gesetzt auf: " + point.toText() ) );
                
                drawFeatureAction.deactivate();
            }
        });
    }
    
}
