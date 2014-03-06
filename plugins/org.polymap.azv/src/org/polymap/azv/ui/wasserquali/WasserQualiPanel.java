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

import static org.polymap.rhei.fulltext.FullTextIndex.FIELD_GEOM;

import java.util.Iterator;
import org.geotools.feature.FeatureCollection;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.data.PipelineFeatureSource;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.Layers;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.app.FormContainer;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.NeighborhoodConstraint;
import org.polymap.rhei.batik.toolkit.NeighborhoodConstraint.Neighborhood;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.ui.map.AddressForm;
import org.polymap.azv.ui.map.HomeMapAction;
import org.polymap.azv.ui.map.MapViewer;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.openlayers.rap.widget.base_types.Style;
import org.polymap.openlayers.rap.widget.base_types.StyleMap;
import org.polymap.openlayers.rap.widget.features.VectorFeature;
import org.polymap.openlayers.rap.widget.geometry.PointGeometry;
import org.polymap.openlayers.rap.widget.layers.VectorLayer;

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
    
    public static final FilterFactory2  ff = MosaicUiPlugin.ff;

    private MapViewer                   mapViewer;

//    private FeatureSource               addressFs;
//
//    private SimpleFeature               search;

    private Composite                   body;

    private PipelineFeatureSource       resultFs;

    private VectorLayer                 vectorLayer;

    private Composite                   resultParent;

    private IPanelSection               addressSection;


    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        site.setTitle( "Wasserhärten und -Qualitäten" );
        site.setIcon( BatikPlugin.instance().imageForName( "resources/icons/waterdrop-filter.png" ) );
        return false;
    }

    
    @Override
    public PanelIdentifier id() {
        return ID;
    }


    @Override
    public void createContents( Composite parent ) {
        body = parent;
        createWelcomeSection( body ).addConstraint( 
                AzvPlugin.MIN_COLUMN_WIDTH, new PriorityConstraint( 10 ) );
        
        createAddressSection( body ).addConstraint( 
                AzvPlugin.MIN_COLUMN_WIDTH, new PriorityConstraint( 5 ) );

        createMapSection( body ).addConstraint( 
                AzvPlugin.MIN_COLUMN_WIDTH, new PriorityConstraint( 3 ) );
    }

    
    protected IPanelSection createWelcomeSection( Composite parent ) {
        IPanelSection section = getSite().toolkit().createPanelSection( parent, "Wie gut ist mein Wasser?" );
        getSite().toolkit().createFlowText( section.getBody(), i18n.get( "welcomeTxt" ) );
        return section;
    }


    protected IPanelSection createAddressSection( Composite parent ) {
        addressSection = getSite().toolkit().createPanelSection( parent, "Adresse eingeben" );
        addressSection.getBody().setLayout( ColumnLayoutFactory.defaults().spacing( 10 ).columns( 1, 1 ).create() );

            AddressForm form = new AddressForm( getSite() ) {
                protected void showResults( Iterable<JSONObject> addresses ) {
                    try {
                        int count = Iterables.size( addresses );
                        if (count == 0) {
                            getSite().setStatus( new Status( IStatus.WARNING, AzvPlugin.ID, "Diese Adresse existiert nicht." ) );                        
                        }
                        else if (count > 1) {
                            getSite().setStatus( new Status( IStatus.WARNING, AzvPlugin.ID, "Die Angabe ist zu ungenau. Es exitieren mehrere solche Adressen." ) );                        
                        }
                        else {
                            showResult( Iterables.getFirst( addresses, null ) );
                        }
                    }
                    catch (Exception e) {
                        throw new RuntimeException( e );
                    }
                }
            };
            form.createContents( getSite().toolkit().createComposite( addressSection.getBody() ) );
            
            //resultParent = getSite().toolkit().createComposite( section.getBody() );
        return addressSection;
    }
    
    
    protected IPanelSection createMapSection( Composite parent ) {
        IPanelSection section = getSite().toolkit().createPanelSection( parent, null );
        section.getBody().setLayout( FormLayoutFactory.defaults().create() );

        mapViewer = new MapViewer();
        mapViewer.createContents( section.getBody(), getSite() );
        mapViewer.getControl().setLayoutData( FormDataFactory.filled().height( 550 ).create() );

        // after olwidget is initialized
        mapViewer.addToolbarItem( new HomeMapAction( mapViewer ) );
        //mapViewer.addToolbarItem( new AddressSearchMapAction( mapViewer ) );
        
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
        mapViewer.getMap().addLayer( vectorLayer );
        return section;
    }

    
    protected void showResult( JSONObject address ) throws Exception {
        if (resultParent == null) {
            IPanelSection resultSection = getSite().toolkit().createPanelSection( body, "Ihre Wasserqualität" );
            resultSection.addConstraint( AzvPlugin.MIN_COLUMN_WIDTH, 
                    new PriorityConstraint( 4 ), 
                    new NeighborhoodConstraint( addressSection.getControl(), Neighborhood.BOTTOM, 1 ) );
            resultParent = resultSection.getBody();
            getSite().toolkit().createLabel( resultSection.getBody(), "Bitte geben Sie einen Adresse ein." )
                    .setLayoutData( FormDataFactory.filled().width( 300 ).create() );
        }        
        for (Control child : resultParent.getChildren()) {
            child.dispose();
        }
        
        if (resultFs == null) {
            ILayer layer = ProjectRepository.instance().visit( Layers.finder( "wasserquali", "Wasserquali" ) );
            assert layer != null : "Keine Ebene 'Wasserquali' gefunden!";
            resultFs = PipelineFeatureSource.forLayer( layer, false );
        }

        Point p = (Point)address.get( FIELD_GEOM );
        Filter filter = ff.contains( 
                ff.property( resultFs.getSchema().getGeometryDescriptor().getLocalName() ), 
                ff.literal( p ) );
        log.info( "Filter: " + filter );
        FeatureCollection results = resultFs.getFeatures( filter );

        // result form
        resultParent.setLayout( ColumnLayoutFactory.defaults().create() );
        if (results.isEmpty()) {
            getSite().toolkit().createLabel( resultParent, "An diesem Ort liegen keine Daten vor." );
        }
        else {
            Iterator it = results.iterator();
            ResultForm form = new ResultForm( (SimpleFeature)it.next() );
            form.createContents( getSite().toolkit().createComposite( resultParent ) );
            results.close( it );
        }
        resultParent.layout();
        body.layout();
        getSite().layout( true );
        
        // map
        vectorLayer.destroyFeatures();
        VectorFeature vectorFeature = new VectorFeature( new PointGeometry( p.getX(), p.getY() ) );
        vectorLayer.addFeatures( vectorFeature );
        mapViewer.getMap().setCenter( p.getX(), p.getY() );
        mapViewer.getMap().zoomTo( 8 );
    }
    
    
    /**
     * 
     */
    public class ResultForm
            extends FormContainer {

        /** Wasserquali feature */
        private SimpleFeature           feature;
        
        @SuppressWarnings("hiding")
        private Composite               body;
        
        public ResultForm( SimpleFeature feature ) {
            this.feature = feature;
        }

        @Override
        public void createFormContent( final IFormEditorPageSite formSite ) {
            body = formSite.getPageBody();
            body.setLayout( ColumnLayoutFactory.defaults().spacing( 3 ).margins( 8 ).columns( 1, 1 ).create() );

            SimpleFeatureType schema = feature.getFeatureType();
            for (AttributeDescriptor descriptor : schema.getAttributeDescriptors()) {
                if (Geometry.class.isAssignableFrom( descriptor.getType().getBinding() )) {
                    // skip
                }
                else {
                    new FormFieldBuilder( body, feature.getProperty( descriptor.getLocalName() ) ).create();
                }
            }
            setEnabled( false );
        }
    }
    
    
//    /**
//     * 
//     */
//    public class AddressForm
//            extends FormContainer {
//
//        private IFormFieldListener          fieldListener;
//        
//        @SuppressWarnings("hiding")
//        private Composite                   body;
//        
//        private SimpleFeature               address;
//        
//        
//        @Override
//        public void createFormContent( final IFormEditorPageSite formSite ) {
//            body = formSite.getPageBody();
//            body.setLayout( ColumnLayoutFactory.defaults().spacing( 5 ).margins( 10, 10 ).columns( 1, 1 ).create() );
//
//            Composite street = getSite().toolkit().createComposite( body );
//            new FormFieldBuilder( street, search.getProperty( "strasse" ) )
//                    .setLabel( "Straße + Nr." ).setValidator( new AddressValidator( "strasse" ) ).create().setFocus();
//
//            new FormFieldBuilder( street, search.getProperty( "nummer" ) )
//                    .setLabel( IFormFieldLabel.NO_LABEL ).setValidator( new AddressValidator( "nummer" ) ).create();
//
//            Composite city = getSite().toolkit().createComposite( body );
//            new FormFieldBuilder( city, search.getProperty( "plz" ) )
//                    .setLabel( "PLZ + Ort" ).setValidator( new AddressValidator( "plz" ) ).create();
//
//            new FormFieldBuilder( city, search.getProperty( "ort" ) )
//                    .setLabel( IFormFieldLabel.NO_LABEL ).setValidator( new AddressValidator( "ort" ) ).create();
//
//            // field listener
//            formSite.addFieldListener( fieldListener = new IFormFieldListener() {
//                public void fieldChange( FormFieldEvent ev ) {
//                    if (ev.getEventCode() == VALUE_CHANGE) {
//                        if (formSite.isValid()) {
//                            try {
//                                formSite.submitEditor();
//                                log.info( "VALID: " + search );
//                                getSite().setStatus( Status.OK_STATUS );
//                                SimpleFeature nextAddress = findAddress();
//                                if (nextAddress != null) {
//                                    if (address == null || !nextAddress.getID().equals( address.getID() )) {
//                                        address = nextAddress;
//                                        showResult( address );
//                                    }
//                                }
//                                else {
//                                    getSite().setStatus( new Status( IStatus.WARNING, AzvPlugin.ID, "Diese Adresse existiert nicht." ) );
//                                }
//                            }
//                            catch (Exception e) {
//                                throw new RuntimeException( e );
//                            }
//                        }
//                    }
//                }
//            });
////            activateStatusAdapter( site.getPanelSite() );
//        }
//
//        protected SimpleFeature findAddress() {
//            List<Filter> props = new ArrayList();
//            for (String propName : new String[] {"strasse", "nummer", "plz", "ort"}) {
//                props.add( ff.equals( ff.property( propName ), ff.literal( search.getAttribute( propName ) ) ) );
//            }
//            Filter filter = ff.and( props );
//            log.info( "Filter: " + filter );
//            FeatureIterator it = null;
//            try {
//                it = addressFs.getFeatures( filter ).features();
//                return it.hasNext() ? (SimpleFeature)it.next() : null;
//            }
//            catch (IOException e) {
//                throw new RuntimeException( e );
//            }
//            finally {
//                if (it != null) { it.close(); }
//            }
//        }
//    }
//
//    
//    /**
//     * 
//     */
//    class AddressValidator
//            implements IFormFieldValidator {
//        
//        private String      propName;
//        
//    
//        public AddressValidator( String propName ) {
//            this.propName = propName;
//        }
//
//        @Override
//        public String validate( Object fieldValue ) {
//            try {
//                if (fieldValue == null) {
//                    return "Feld darf nicht leer sein.";                    
//                }
//                Timer timer = new Timer();
//                Filter filter = ff.equals( ff.property( propName ), ff.literal( fieldValue ) );
//                FeatureCollection features = addressFs.getFeatures( filter );
//                log.info( "Features for '" + propName + "'==" + fieldValue + ": " + features.size() + " (" + timer.elapsedTime() + "ms)" );
//                return features.isEmpty() ? "Wert existiert nicht in der Adressdatenbank: " + fieldValue : null;
//            }
//            catch (IOException e) {
//                throw new RuntimeException( e );
//            }
//        }
//    
//        @Override
//        public Object transform2Model( Object fieldValue ) throws Exception {
//            return fieldValue;
//        }
//    
//        @Override
//        public Object transform2Field( Object modelValue ) throws Exception {
//            return modelValue;
//        }
//        
//    }

}
