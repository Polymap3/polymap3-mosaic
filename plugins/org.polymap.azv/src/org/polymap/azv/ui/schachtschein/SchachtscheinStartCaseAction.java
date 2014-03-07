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
package org.polymap.azv.ui.schachtschein;

import static org.polymap.azv.model.AdresseMixin.KEY_CITY;
import static org.polymap.azv.model.AdresseMixin.KEY_NUMBER;
import static org.polymap.azv.model.AdresseMixin.KEY_POSTALCODE;
import static org.polymap.azv.model.AdresseMixin.KEY_STREET;
import static org.polymap.rhei.field.Validators.AND;
import static org.polymap.rhei.fulltext.address.Address.FIELD_CITY;
import static org.polymap.rhei.fulltext.address.Address.FIELD_NUMBER;
import static org.polymap.rhei.fulltext.address.Address.FIELD_POSTALCODE;
import static org.polymap.rhei.fulltext.address.Address.FIELD_STREET;
import static org.polymap.rhei.fulltext.FullTextIndex.FIELD_GEOM;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.beans.PropertyChangeEvent;

import org.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Point;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.app.FormContainer;
import org.polymap.rhei.batik.toolkit.ConstraintData;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.field.BeanPropertyAdapter;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.fulltext.FullTextIndex;
import org.polymap.rhei.fulltext.address.AddressFinder;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.ui.PlzValidator;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.model.AdresseMixin;
import org.polymap.azv.model.NutzerMixin;
import org.polymap.azv.model.OrtMixin;
import org.polymap.azv.ui.NotEmptyValidator;
import org.polymap.azv.ui.map.AddressValidator;
import org.polymap.azv.ui.map.DrawFeatureMapAction;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.MosaicCaseEvents;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.KVPropertyAdapter;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.CaseStatus;
import org.polymap.mosaic.ui.casepanel.DefaultCaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseActionSite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SchachtscheinStartCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( SchachtscheinStartCaseAction.class );

    public static final IMessages           i18n = Messages.forPrefix( "SchachtscheinStart" );

    private static final FastDateFormat     df = FastDateFormat.getInstance( "dd.MM.yyyy" );

    
    // instance *******************************************
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>        mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2>  repo;

    private ICaseActionSite                     site;
    
    private CaseStatus                          caseStatus;

    private BasedataForm                        form;

    private IPanelSection                       contentSection;

    private BasedataForm                        contentForm;

    private IAction                             caseAction;

    /** */
//    private Map<String,String>                  searchAddress = new HashMap();

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        if (mcase.get() != null && repo.get() != null
                && mcase.get().getNatures().contains( AzvPlugin.CASE_SCHACHTSCHEIN )) {
            
            // wenn Kunde und noch kein Name gesetzt ist
            if (!SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA ) 
                    && !SecurityUtils.isAdmin()
                    && mcase.get().getName().length() == 0) {

                // Nutzer am Vorgang setzen
                User user = mcase.get().as( NutzerMixin.class ).setSessionUser();
                
                // Adresse vom Nutzer eintragen
                AdresseMixin address = mcase.get().as( AdresseMixin.class );
                address.setAdresseVonNutzer( user );
//                Object dummy = null;
//                dummy = address.nummer.get() != null ? searchAddress.put( FIELD_NUMBER, address.nummer.get() ) : null;
//                dummy = address.plz.get() != null ? searchAddress.put( FIELD_POSTALCODE, address.nummer.get() ) : null;
//                dummy = address.stadt.get() != null ? searchAddress.put( FIELD_CITY, address.nummer.get() ) : null;
//                dummy = address.strasse.get() != null ? searchAddress.put( FIELD_STREET, address.nummer.get() ) : null;

                // open action
                Polymap.getSessionDisplay().asyncExec( new Runnable() {
                    public void run() {
                        site.activateCaseAction( site.getActionId() );
                    }
                });
    
                // update action on status change
                EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {
                    public boolean apply( PropertyChangeEvent input ) {
                        return caseStatus != null && input.getSource() == mcase.get();
                    }
                });
            }
            return true;
        }
        return false;
    }


    @Override
    public void dispose() {
        EventManager.instance().unsubscribe( this );
        caseAction = null;
        caseStatus = null;
    }


    @Override
    public void fillAction( IAction action ) {
        this.caseAction = action;
        updateAction( null );
    }

    
    @EventHandler(display=true, delay=500)
    protected void updateAction( List<PropertyChangeEvent> evs ) {
        if (MosaicCaseEvents.contains( mcase.get(), AzvPlugin.EVENT_TYPE_BEANTRAGT )) {
            caseAction.setText( null );
            caseAction.setImageDescriptor( null );
            caseAction.setEnabled( false );
        }
    }

    
    @Override
    public void fillStatus( CaseStatus status ) {
        caseStatus = status;
        status.put( "Laufende Nr.", StringUtils.right( mcase.get().getId(), 6 ) );
        status.put( "Ort", mcase.get().as( AdresseMixin.class ).adresse(), 1 );
    }


    @Override
    public void createContents( Composite parent ) {
        // noch kein Nutzer am Vorgang -> Nutzer/Adresse vom user eintragen
        NutzerMixin nutzer = mcase.get().as( NutzerMixin.class );
        if (nutzer.user() == null) {
            nutzer.setSessionUser();
        }

//        IPanelSection welcome = site.toolkit().createPanelSection( parent, i18n.get( "welcomeTitle" ) );
//        welcome.getBody().setLayout( new FillLayout() );
        site.toolkit().createFlowText( parent /*welcome.getBody()*/, i18n.get( "welcomeTxt" ) )
                .setLayoutData( new ConstraintData( AzvPlugin.MIN_COLUMN_WIDTH, new PriorityConstraint( 100 ) ) );

        Composite formContainer = site.toolkit().createComposite( parent );
        formContainer.setLayout( new FillLayout() );
        form = new BasedataForm();
        form.createContents( formContainer );
        site.setSubmitEnabled( false );
        
        site.createSubmit( formContainer, "Übernehmen" );
    }

    
    @Override
    public void submit() throws Exception {
        assert form.isValid();
        form.submit();
        form.dispose();
        form = null;
        repo.get().commitChanges();
        
        site.getPanelSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, "Daten wurden übernommen" ) );
        fillStatus( caseStatus );
        
        if (contentForm != null) {
            contentForm.reloadEditor();
        }
        else {
            contentSection.getBody().getChildren()[0].dispose();
            contentForm = new BasedataForm();
            contentForm.createContents( contentSection.getBody() );
            contentForm.getBody().setLayout( ColumnLayoutFactory.defaults().spacing( 3 ).margins( 8 ).create() );
            contentForm.setEnabled( false );            
        }

    }


    @Override
    public void discard() {
        if (form != null) {
            form.dispose();
            form = null;
        }
        repo.get().rollbackChanges();
        
        // do not left 'empty' CasePanel after close button
        if (mcase.get().getName().isEmpty()) {
            site.getPanelSite().setStatus( new Status( IStatus.INFO, AzvPlugin.ID, "Es wurden keine Basisdaten eingegeben.\nDer Vorgang wurde daher geschlossen." ) );
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    site.getContext().closePanel( site.getPanelSite().getPath() );
                }
            });
        }
    }


    @Override
    public void fillContentArea( Composite parent ) {
        contentSection = site.toolkit().createPanelSection( parent, "Maßnahme" );
        contentSection.addConstraint( new PriorityConstraint( 100 ), AzvPlugin.MIN_COLUMN_WIDTH );
        contentSection.getBody().setLayout( new FillLayout() );

        if (mcase.get().getName().length() > 0) {
            contentForm = new BasedataForm();
            //contentForm.setFieldBuilderFactory( AzvPlugin.LABEL_FIELD_FACTORY );
            contentForm.createContents( contentSection.getBody() );
            contentForm.getBody().setLayout( ColumnLayoutFactory.defaults().spacing( 3 ).margins( 8 ).create() );
            contentForm.setEnabled( false );
        }
        else {
            site.toolkit().createLabel( contentSection.getBody(), "Noch keine Daten." )
                    .setForeground( MosaicUiPlugin.COLOR_RED.get() );
        }
    }


    /**
     * 
     */
    public class BasedataForm
            extends FormContainer {

        private IFormFieldListener      fieldListener;
        
        private Composite               body;

        
        public Composite getBody() {
            return body;
        }

        @Override
        public void createFormContent( final IFormEditorPageSite formSite ) {
            body = formSite.getPageBody();
            body.setLayout( ColumnLayoutFactory.defaults().spacing( 5 ).margins( 10, 10 ).columns( 1, 1 ).create() );

            createField( body, new BeanPropertyAdapter( mcase.get(), "name" ) )
                    .setLabel( "Bezeichnung" ).setToolTipText( "Bezeichnung der Maßnahme" )
                    .setValidator( new NotEmptyValidator() ).create().setFocus();
            
            createField( body, new BeanPropertyAdapter( mcase.get(), "description" ) )
                    .setLabel( "Beschreibung" ).setToolTipText( "Beschreibung der Maßnahme" )
                    .setField( new TextFormField() ).setValidator( new NotEmptyValidator() ).create()
                    .setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 60 ) );

            Composite city = site.toolkit().createComposite( body );

            createField( city, new KVPropertyAdapter( mcase.get(), KEY_POSTALCODE ) )
                    .setLabel( "PLZ / Ort" ).setToolTipText( "Postleitzahl und Ortsname" )
                    .setField( new StringFormField() )
                    .setValidator( AND( new PlzValidator(), new AddressValidator( FIELD_POSTALCODE ) ) ).create();

            createField( city, new KVPropertyAdapter( mcase.get(), KEY_CITY ) )
                    .setLabel( IFormFieldLabel.NO_LABEL )
                    .setField( new StringFormField() )
                    .setValidator( new AddressValidator( FIELD_CITY ) ).create();

            Composite street = site.toolkit().createComposite( body );
            createField( street, new KVPropertyAdapter( mcase.get(), KEY_STREET ) )
                    .setLabel( "Straße / Nummer" ).setToolTipText( "Straße und Hausnummer" )
                    .setField( new StringFormField() )
                    .setValidator( new AddressValidator( FIELD_STREET ) ).create();

            createField( street, new KVPropertyAdapter( mcase.get(), KEY_NUMBER ) )
                    .setLabel( IFormFieldLabel.NO_LABEL )
                    .setField( new StringFormField() )
                    .setValidator( new AddressValidator( FIELD_NUMBER ) ).create();

            // field listener
            formSite.addFieldListener( fieldListener = new IFormFieldListener() {
                public void fieldChange( FormFieldEvent ev ) {                    
                    if (ev.getEventCode() != VALUE_CHANGE) {
                        return;
                    }
//                    // collect search address fields
//                    switch (ev.getFieldName()) {
//                        case KEY_POSTALCODE: search.put( FIELD_POSTALCODE, (String)ev.getNewValue() ); break;
//                        case KEY_CITY: search.put( FIELD_CITY, (String)ev.getNewValue() ); break;
//                        case KEY_STREET: search.put( FIELD_STREET, (String)ev.getNewValue() ); break;
//                        case KEY_NUMBER: search.put( FIELD_NUMBER, (String)ev.getNewValue() ); break;
//                    }
                    //
                    boolean valid = formSite.isValid();
                    boolean dirty = formSite.isDirty();
                    site.setSubmitEnabled( valid & dirty );

                    if (dirty && valid ) {
                        Map<String,String> search = new HashMap();
                        search.put( FIELD_POSTALCODE, (String)formSite.getFieldValue( KEY_POSTALCODE ) );
                        search.put( FIELD_CITY, (String)formSite.getFieldValue( KEY_CITY ) );
                        search.put( FIELD_STREET, (String)formSite.getFieldValue( KEY_STREET ) );
                        search.put( FIELD_NUMBER, (String)formSite.getFieldValue( KEY_NUMBER ) );
                        
                        FullTextIndex addressIndex = AzvPlugin.instance().addressIndex();
                        Iterable<JSONObject> addresses = new AddressFinder( addressIndex ).maxResults( 1 ).find( search );
                        JSONObject address = Iterables.getFirst( addresses, null );
                        if (address != null) {
                            Point geom = (Point)address.get( FIELD_GEOM );
                            log.info( "Point: " + geom );
                            
                            OrtMixin ort = mcase.get().as( OrtMixin.class );
                            ort.setGeom( geom );

                            EventManager.instance().publish( new PropertyChangeEvent( this, DrawFeatureMapAction.EVENT_NAME, null, geom ) );            
                        }
                        else {
                            site.getPanelSite().setStatus( new Status( IStatus.WARNING, AzvPlugin.ID, "Diese Adresse existiert leider nicht in unserer Datenbank." ) );                        
                        }
                    }
                }
            });

//            activateStatusAdapter( site.getPanelSite() );
        }
    }

}
