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
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.ui.map.DrawFeatureMapAction;
import org.polymap.azv.ui.map.HomeMapAction;
import org.polymap.azv.ui.map.MapViewer;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.DefaultCaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseActionSite;
import org.polymap.openlayers.rap.widget.base_types.Style;
import org.polymap.openlayers.rap.widget.base_types.StyleMap;
import org.polymap.openlayers.rap.widget.controls.DrawFeatureControl;
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

    private MapViewer                       mapViewer;

    private VectorLayer                     vectorLayer;

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        return mcase.get() != null && repo.get() != null
                && mcase.get().getNatures().contains( AzvPlugin.CASE_SCHACHTSCHEIN );
    }


    @Override
    public void dispose() {
        mapViewer.dispose();
    }


    @Override
    public void fillContentArea( Composite parent ) {
        // section
        IPanelSection mapSection = site.toolkit().createPanelSection( parent, "Ort" );
        mapSection.addConstraint( new PriorityConstraint( 10 ) );
        Composite body = mapSection.getBody();
        body.setLayout( ColumnLayoutFactory.defaults().margins( 0 ).columns( 1, 1 ).spacing( 2 ).create() );

        // map widget
        mapViewer = new MapViewer();
        mapViewer.createContents( body, site.getPanelSite() );
        mapViewer.getControl().setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 500 ) );

        WMSLayer alk = new WMSLayer( "ALK", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-ALK" );
        alk.setVisibility( false );
        mapViewer.addLayer( alk );
        
        WMSLayer kanal = new WMSLayer( "Kanal", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-Kanal" );
        kanal.setVisibility( false );
        mapViewer.addLayer( kanal );
        
        WMSLayer wasser = new WMSLayer( "Wasser", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-Wasser" );
        wasser.setVisibility( false );
        mapViewer.addLayer( wasser );
        
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
        
        mapViewer.addLayer( vectorLayer );
        
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

                mapViewer.getMap().setCenter( p.getX(), p.getY() );
                mapViewer.getMap().zoomTo( 15 );
            }
            catch (ParseException e) {
                log.warn( "", e );
            }
        }
    }
    
    
    protected void createToolbar( Composite parent ) {
        mapViewer.addToolbarItem( new HomeMapAction( mapViewer ) );

        final DrawFeatureMapAction drawFeatureAction = new DrawFeatureMapAction( 
                mapViewer, vectorLayer, DrawFeatureControl.HANDLER_POINT );
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
        mapViewer.addToolbarItem( drawFeatureAction );
    }
    
}
