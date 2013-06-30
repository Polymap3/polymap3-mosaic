/*
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.atlas.ContextProperty;
import org.polymap.atlas.IAppContext;
import org.polymap.atlas.IPanel;
import org.polymap.atlas.IPanelSite;
import org.polymap.atlas.PanelIdentifier;
import org.polymap.atlas.app.DefaultFormPanel;
import org.polymap.atlas.app.FormContainer;
import org.polymap.atlas.toolkit.ConstraintData;
import org.polymap.atlas.toolkit.IPanelSection;
import org.polymap.atlas.toolkit.IPanelToolkit;
import org.polymap.atlas.toolkit.MinWidthConstraint;
import org.polymap.atlas.toolkit.PriorityConstraint;
import org.polymap.azv.model.Schachtschein;
import org.polymap.openlayers.rap.widget.OpenLayersWidget;
import org.polymap.openlayers.rap.widget.base_types.Bounds;
import org.polymap.openlayers.rap.widget.base_types.Projection;
import org.polymap.openlayers.rap.widget.controls.LayerSwitcherControl;
import org.polymap.openlayers.rap.widget.controls.MousePositionControl;
import org.polymap.openlayers.rap.widget.controls.NavigationControl;
import org.polymap.openlayers.rap.widget.controls.ScaleControl;
import org.polymap.openlayers.rap.widget.controls.ScaleLineControl;
import org.polymap.openlayers.rap.widget.layers.WMSLayer;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SchachtscheinPanel
        extends DefaultFormPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( SchachtscheinPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "schachtschein" );

    private ContextProperty<Schachtschein>  entity;

    private IPanelToolkit                   tk;
    
    private IPanelSection                   baseSection, mapSection;
    

    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        if (super.init( site, context )) {
            // is there an entity in the context?
            if (entity.get() != null) {
                return true;
            }
        }
        return false;
    }


    @Override
    public PanelIdentifier id() {
        return ID;
    }


    @Override
    public void createFormContent( IFormEditorPageSite pageSite ) {
        getSite().setTitle( "Schachtschein" );
        tk = getSite().toolkit();
        
        IPanelSection contents = tk.createPanelSection( pageSite.getPageBody(), null );

        // baseSection
        baseSection = tk.createPanelSection( contents, "Basisdaten - Schachtschein" );
        baseSection.getControl().setLayoutData( new ConstraintData( 
                new PriorityConstraint( 3, 1 ), new MinWidthConstraint( 400, 1 ) ) );
        new BasedataForm().createContents( baseSection );

        // mapSection
        mapSection = tk.createPanelSection( contents, "Ort", SWT.BORDER );
        Composite body = mapSection.getBody();
        body.setLayout( ColumnLayoutFactory.defaults().create() );
        
        OpenLayersWidget olwidget = new OpenLayersWidget( body, SWT.MULTI | SWT.WRAP, "openlayers/full/OpenLayers-2.12.1.js" );
        olwidget.setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 500 ) );

        // init map
        String srs = "EPSG:4326";
        Projection proj = new Projection( srs );
        String units = srs.equals( "EPSG:4326" ) ? "degrees" : "m";
        float maxResolution = srs.equals( "EPSG:4326" ) ? (360/256) : 500000;
        Bounds maxExtent = new Bounds( 12.80, 53.00, 14.30, 54.50 );
        //Bounds maxExtent = new Bounds( 3358000, 5916000, 3456000, 5986000 );
        olwidget.createMap( proj, proj, units, maxExtent, maxResolution );
        
//        OSMLayer osm = new OSMLayer( "OSM", "http://tile.openstreetmap.org/${z}/${x}/${y}.png", 9 );
        WMSLayer osm = new WMSLayer( "OSM", "http://ows.terrestris.de/osm-basemap/service", "OSM-WMS-Deutschland" );
        osm.setIsBaseLayer( true );
        olwidget.getMap().addLayer( osm );
        olwidget.getMap().addControl( new NavigationControl() );
        olwidget.getMap().addControl( new LayerSwitcherControl() );
        olwidget.getMap().addControl( new MousePositionControl() );
        olwidget.getMap().addControl( new ScaleLineControl() );
        olwidget.getMap().addControl( new ScaleControl() );

        olwidget.getMap().zoomToExtent( maxExtent, true );
        olwidget.getMap().zoomTo( 9 );
    }


    /**
     * 
     */
    public class BasedataForm
            extends FormContainer {

        @Override
        public void createFormContent( IFormEditorPageSite site ) {
            Composite body = site.getPageBody();
            body.setLayout( ColumnLayoutFactory.defaults().spacing( 10 ).margins( 20, 20 ).create() );

            new FormFieldBuilder( body, new PropertyAdapter( entity.get().beschreibung() ) )
                    .setValidator( new NotNullValidator() ).create().setFocus();
            //new FormFieldBuilder( body, new PropertyAdapter( entity.get().antragsteller() ) ).create();
            new FormFieldBuilder( body, new PropertyAdapter( entity.get().startDate() ) )
                    .setLabel( "Beginn" ).setToolTipText( "Geplanter Beginn der Arbeiten" ).create();
            new FormFieldBuilder( body, new PropertyAdapter( entity.get().endDate() ) )
                    .setLabel( "Ende" ).setToolTipText( "Geplantes Ende der Arbeiten" ).create();
            new FormFieldBuilder( body, new PropertyAdapter( entity.get().bemerkungen() ) )
                    .setField( new TextFormField() ).create()
                    .setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 80 ) );
            
            activateStatusAdapter( getSite() );
        }
    }

}
