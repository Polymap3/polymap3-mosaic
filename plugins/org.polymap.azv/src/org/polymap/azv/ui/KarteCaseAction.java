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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.itextpdf.text.PageSize;
import com.vividsolutions.jts.geom.Point;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.model.OrtMixin;
import org.polymap.azv.ui.map.DrawFeatureMapAction;
import org.polymap.azv.ui.map.HomeMapAction;
import org.polymap.azv.ui.map.MapViewer;
import org.polymap.azv.ui.map.PdfMapAction;
import org.polymap.azv.ui.map.ScaleMapAction;
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
 * Stellt die Karte mit Toolbar/tools für Schachtschein und Leistungsauskunft dar.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class KarteCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( KarteCaseAction.class );

    public static final IMessages       i18n = Messages.forPrefix( "Karte" ); //$NON-NLS-1$
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>    mcase;
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2> repo;

    private ICaseActionSite                 site;

    private MapViewer                       mapViewer;

    private VectorLayer                     vectorLayer;

    private DrawFeatureMapAction            drawFeatureAction;

    private VectorFeature                   vectorFeature;

    private IPanelSection                   mapSection;

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        return mcase.get() != null && repo.get() != null
                && (mcase.get().getNatures().contains( AzvPlugin.CASE_SCHACHTSCHEIN )
                 || mcase.get().getNatures().contains( AzvPlugin.CASE_LEITUNGSAUSKUNFT ));
    }


    @Override
    public void dispose() {
        drawFeatureAction.removeListener( this );
        drawFeatureAction.dispose();
        mapViewer.dispose();
    }


    @Override
    public void fillContentArea( Composite parent ) {
        // section
        String hex = MosaicUiPlugin.rgbToHex( MosaicUiPlugin.COLOR_RED.get().getRGB() );
        mapSection = site.toolkit().createPanelSection( parent, i18n.get( "titleOhneMarkierung", hex ) ); 
        mapSection.addConstraint( new PriorityConstraint( 10 ), AzvPlugin.MIN_COLUMN_WIDTH );
        Composite body = mapSection.getBody();
        body.setLayout( ColumnLayoutFactory.defaults().margins( 0 ).columns( 1, 1 ).spacing( 2 ).create() );

        // map widget
        mapViewer = new MapViewer();
        mapViewer.createContents( body, site.getPanelSite() );
        mapViewer.getControl().setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 500 ) );

        if (SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA )) {
            int suffix = 1;
            while (i18n.contains( "layerName"+suffix )) { //$NON-NLS-1$
                WMSLayer layer = new WMSLayer( i18n.get( "layerName"+suffix ),  //$NON-NLS-1$
                        i18n.get( "layerWmsUrl"+suffix ), //$NON-NLS-1$
                        i18n.get( "layerWmsName"+suffix ) ); //$NON-NLS-1$
                layer.setVisibility( false );
                mapViewer.addLayer( layer, false );
                suffix ++;
            }

//            WMSLayer wasser = new WMSLayer( "Wasser", "http://80.156.217.67:8080", "SESSION.Mosaic\\\\M-Wasser" );
//            wasser.setVisibility( false );
//            mapViewer.addLayer( wasser, false );
        }
        
        // vector layer
        vectorLayer = new VectorLayer( i18n.get( "layerMarkierung" ) );
        vectorLayer.setVisibility( true );
        vectorLayer.setIsBaseLayer( false );
        vectorLayer.setZIndex( 10000 );

        // style
        Style standard = new Style();
        standard.setAttribute( "strokeColor", i18n.get( "strokeColor" ) ); //$NON-NLS-1$
        standard.setAttribute( "strokeWidth", i18n.get( "strokeWidth" ) ); //$NON-NLS-1$
        standard.setAttribute( "pointRadius", i18n.get( "pointRadius" ) ); //$NON-NLS-1$
        StyleMap styleMap = new StyleMap();
        styleMap.setIntentStyle( "default", standard ); //$NON-NLS-1$
        vectorLayer.setStyleMap( styleMap );
        
        mapViewer.addLayer( vectorLayer, false );
        
        // check if mcase has point
        drawMosaicCasePoint();
        
        // toolbar
        createToolbar( body );        
    }

    
    protected void drawMosaicCasePoint() {
        Point geom = mcase.get().as( OrtMixin.class ).getGeom();
        if (geom != null) {
            vectorLayer.destroyFeatures();

            vectorFeature = new VectorFeature( new PointGeometry( geom.getX(), geom.getY() ) );
            vectorLayer.addFeatures( vectorFeature );
            //vectorLayer.redraw();

            mapViewer.zoomToExtent( geom.buffer( 50 ).getEnvelopeInternal() );
//            mapViewer.getMap().setCenter( geom.getX(), geom.getY() );
//            mapViewer.getMap().zoomTo( 10 );
            
            
            mapSection.setTitle( i18n.get( "title" ) );
        }
    }
    
    
    protected void createToolbar( Composite parent ) {
        mapViewer.addToolbarItem( new HomeMapAction( mapViewer ) );

        drawFeatureAction = new DrawFeatureMapAction( mcase.get(), mapViewer, vectorLayer, DrawFeatureControl.HANDLER_POINT );
        mapViewer.addToolbarItem( drawFeatureAction );

        EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {
            public boolean apply( PropertyChangeEvent input ) {
                return input.getPropertyName().equals( DrawFeatureMapAction.EVENT_NAME );
            }
        });

        if (SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA )) {
            mapViewer.addToolbarItem( new ScaleMapAction( mapViewer, 500 ) );
            mapViewer.addToolbarItem( new ScaleMapAction( mapViewer, 1000 ) );
            mapViewer.addToolbarItem( new PdfMapAction( mapViewer, i18n.get( "a4" ), PageSize.A4, mcase.get(), repo.get() ) );
            mapViewer.addToolbarItem( new PdfMapAction( mapViewer, i18n.get( "a3" ), PageSize.A3, mcase.get(), repo.get() ) );
        }
    }


    /**
     * Listen to changes of the location ({@link OrtMixin}) of the mcase. The event
     * is triggered by {@link DrawFeatureMapAction} and
     * {@link StartCaseAction} and others.
     * 
     * @param ev
     */
    @EventHandler(display=true)
    protected void ortGeomChanged( PropertyChangeEvent ev ) {
//        Point geom = (Point)ev.getNewValue();
//        mcase.get().as( OrtMixin.class ).setGeom( geom );
//        repo.get().commi tChanges();
        
        drawMosaicCasePoint();
        
        site.getPanelSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, i18n.get( "statusOk" ) ) );
        
        drawFeatureAction.deactivate();
    }
    
}
