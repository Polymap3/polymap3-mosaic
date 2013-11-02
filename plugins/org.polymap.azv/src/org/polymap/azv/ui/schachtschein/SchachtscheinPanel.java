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
package org.polymap.azv.ui.schachtschein;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.app.BatikApplication;
import org.polymap.rhei.batik.app.DefaultFormPanel;
import org.polymap.rhei.batik.app.FormContainer;
import org.polymap.rhei.batik.toolkit.ConstraintData;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.PlainValuePropertyAdapter;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.azv.model.AzvRepository;
import org.polymap.azv.model.Schachtschein;
import org.polymap.azv.ui.NotNullValidator;
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
 * @deprecated
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SchachtscheinPanel
        extends DefaultFormPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( SchachtscheinPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "schachtschein" );

    private ContextProperty<Schachtschein>  entity;

    private IPanelToolkit                   tk;
    
    private IPanelSection                   baseSection, mapSection, geoSection;

    private OpenLayersWidget                olwidget;

    public Button submitBtn;
    

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

        // welcomeSection
        IPanelSection welcomeSection = tk.createPanelSection( contents, "Schachtschein" );
        welcomeSection.getControl().setLayoutData( new ConstraintData( new PriorityConstraint( 1 ), new MinWidthConstraint( 400, 1 ) ) );
        String msg = "Das Antragsverfahren Schachtschein ist Voraussetzung für die Durchführung von Baumaßnahmen. "
                + "Die Beantragung kann direkt hier im Portal erfolgen. Schachtscheine sind eine tolle Sache. Jeder sollte einen haben!";
        Label l = tk.createFlowText( welcomeSection.getBody(), msg/*, SWT.BORDER*/ );
        l.setLayoutData( new ConstraintData( new PriorityConstraint( 0 ), new MinWidthConstraint( 400, 1 ) ) );

        // baseSection
        baseSection = tk.createPanelSection( contents, "Basisdaten" );
        baseSection.getControl().setLayoutData( new ConstraintData( new PriorityConstraint( 1 ), new MinWidthConstraint( 400, 1 ) ) );
//        Label l1 = tk.createFlowText( baseSection.getBody(), "Schachtschein." );
//        l1.setLayoutData( new ConstraintData( new PriorityConstraint( 0, 1 ), new MinWidthConstraint( 400, 1 ) ) );
//        
//        String msg = "Das Antragsverfahren Schachtschein ist Voraussetzung für die Durchführung von Baumaßnahmen. "
//                + "Die Beantragung kann direkt hier im Portal erfolgen. Schachtscheine sind eine tolle Sache. Jeder sollte einen haben!";
//        Label l = tk.createFlowText( baseSection.getBody(), msg, SWT.BORDER );
//        l.setLayoutData( new ConstraintData( new PriorityConstraint( 0, 1 ), new MinWidthConstraint( 400, 1 ) ) );
        new BasedataForm().createContents( baseSection );

        // geoSection
        geoSection = tk.createPanelSection( contents, "Geodaten" );
        geoSection.getControl().setLayoutData( new ConstraintData( /*new PriorityConstraint( 1, 1 ),*/ new MinWidthConstraint( 400, 1 ) ) );
        msg = "Upload von Shapefile oder DXF.";
        l = tk.createLabel( geoSection.getBody(), msg );
        l.setLayoutData( new ConstraintData( new PriorityConstraint( 3 ), new MinWidthConstraint( 400, 1 ) ) );
        new UploadForm().createContents( geoSection );

        // mapSection
        mapSection = tk.createPanelSection( contents, "Ort", SWT.BORDER );
        Composite body = mapSection.getBody();
        body.setLayout( ColumnLayoutFactory.defaults().spacing( 10 ).create() );

        // map widget
        olwidget = new OpenLayersWidget( body, SWT.MULTI | SWT.WRAP, "openlayers/full/OpenLayers-2.12.1.js" );
        olwidget.setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 420 ) );

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
    public static class UploadForm
            extends FormContainer {

        private IFormFieldListener      fieldListener;
        
        @Override
        public void createFormContent( final IFormEditorPageSite site ) {
            Composite body = site.getPageBody();
            body.setLayout( ColumnLayoutFactory.defaults().spacing( 10 ).margins( 10, 10 ).columns( 1, 1 ).create() );

            // upload field
            new FormFieldBuilder( body, new PlainValuePropertyAdapter( "upload", null ) )
                    .setField( new StringFormField() ).create().setFocus();
        }
    }
    
    
    /**
     * 
     */
    public static class AddressSearchForm
            extends FormContainer {

        private IFormFieldListener      fieldListener;
        
        @Override
        public void createFormContent( final IFormEditorPageSite site ) {
            Composite body = site.getPageBody();
            body.setLayout( ColumnLayoutFactory.defaults().spacing( 10 ).margins( 20, 20 ).create() );
            // search field
            new FormFieldBuilder( body, new PlainValuePropertyAdapter( "search", null ) )
                    .setField( new StringFormField() ).create().setFocus();
            // btn
            final Button okBtn = site.getToolkit().createButton( body, "ANZEIGEN", SWT.PUSH );
            okBtn.setEnabled( false );
            okBtn.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent ev ) {
                    //log.info( "Suchstring: " + site.getFieldValue( "search" ) );
                }
            });
            
            // listener
            site.addFieldListener( fieldListener = new IFormFieldListener() {
                public void fieldChange( FormFieldEvent ev ) {
                    if (ev.getEventCode() == VALUE_CHANGE && okBtn != null) {
                        okBtn.setEnabled( site.isValid() );
                    }
                }
            });
        }
    }
    
    
    /**
     * 
     */
    public class BasedataForm
            extends FormContainer {

        private IFormFieldListener          fieldListener;

        @Override
        public void createFormContent( final IFormEditorPageSite site ) {
            Composite body = site.getPageBody();
            body.setLayout( ColumnLayoutFactory.defaults().spacing( 10 ).margins( 10, 10 ).columns( 1, 1 ).create() );

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
            
            // address
            Composite plzLine = site.getToolkit().createComposite( body );
            plzLine.setLayout( new FillLayout( SWT.HORIZONTAL ) );
            new FormFieldBuilder( plzLine, new PropertyAdapter( entity.get().plz() ) )
                    .setValidator( new NotNullValidator() ).setLabel( "PLZ/Ort" ).setToolTipText( "Postleitzahl der Baumaßnahme" ).create();
            new FormFieldBuilder( plzLine, new PropertyAdapter( entity.get().ort() ) )
                    .setValidator( new NotNullValidator() ).setLabel( IFormFieldLabel.NO_LABEL ).setToolTipText( "Ort der Baumaßnahme" ).create();
            new FormFieldBuilder( body, new PropertyAdapter( entity.get().strasse() ) )
                    .setValidator( new NotNullValidator() ).setLabel( "Strasse" ).setToolTipText( "Adresse der Baumaßnahme" ).create();

            // submit
            submitBtn = site.getToolkit().createButton( body, "BEANTRAGEN", SWT.PUSH );
            submitBtn.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent ev ) {
                    try {
                        site.submitEditor();
                        AzvRepository.instance().commitChanges();
                        getContext().closePanel();
                    }
                    catch (Exception e) {
                        BatikApplication.handleError( "Vorgang konnte nicht angelegt werden.", e );
                    }
                }
            });
            
            // address listener
            site.addFieldListener( fieldListener = new IFormFieldListener() {
                public void fieldChange( FormFieldEvent ev ) {
                    if (ev.getEventCode() == VALUE_CHANGE && ev.getFieldName().equals( "ort" )) {
                        if (ev.getNewValue().equals( "Anklam" )) {
                            olwidget.getMap().setCenter( 13.70, 53.85 );
                            olwidget.getMap().zoomTo( 13 );
                        }
                        else if (ev.getNewValue().equals( "Friedland" )) {
                            olwidget.getMap().setCenter( 13.54, 53.66 );
                            olwidget.getMap().zoomTo( 13 );
                        }
                        else if (ev.getNewValue().equals( "Jarmen" )) {
                            olwidget.getMap().setCenter( 13.33, 53.92 );
                            olwidget.getMap().zoomTo( 13 );
                        }
                        else {
                            log.info( "unbekannter Ort: " + ev.getNewValue() );
                        }
                    }
                }
            });
            activateStatusAdapter( getSite() );
        }
    }

}
