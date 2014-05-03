/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
import org.polymap.azv.model.AdresseMixin;
import org.polymap.azv.model.AzvVorgang;
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
public abstract class StartCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( StartCaseAction.class );

    public static final FastDateFormat     df = FastDateFormat.getInstance( "dd.MM.yyyy" ); //$NON-NLS-1$

    
    // instance *******************************************
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    protected ContextProperty<IMosaicCase>        mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    protected ContextProperty<MosaicRepository2>  repo;

    protected ICaseActionSite                     site;
    
    protected CaseStatus                          caseStatus;

    protected BasedataForm                        form;

    protected IPanelSection                       contentSection;

    protected BasedataForm                        contentForm;

    protected IAction                             caseAction;


    protected abstract String i18n( String key, Object... args);
    
    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;

        // wenn Kunde und noch kein Name gesetzt ist
        if (!SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA ) 
                && !SecurityUtils.isAdmin()
                && mcase.get().getName().length() == 0) {

            // Adresse vom Nutzer eintragen
            User user = mcase.get().as( AzvVorgang.class ).setSessionUser();
            AdresseMixin address = mcase.get().as( AdresseMixin.class );
            address.setAdresseVonNutzer( user );

            // open action
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    site.activateCaseAction( site.getActionId() );
                }
            });

            // update action on status change
            EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {
                public boolean apply( PropertyChangeEvent input ) {
                    return caseAction != null && caseStatus != null && input.getSource() == mcase.get();
                }
            });
        }
        return true;
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
        //status.put( i18n( "laufendeNr" ), StringUtils.right( mcase.get().getId(), 6 ) );
        //status.put( i18n( "ort" ), mcase.get().as( AdresseMixin.class ).adresse(), 1 );
    }


    @Override
    public void createContents( Composite parent ) {
        // noch kein Nutzer am Vorgang -> Nutzer/Adresse vom user eintragen
        AzvVorgang vorgang = mcase.get().as( AzvVorgang.class );
        if (vorgang.user() == null) {
            vorgang.setSessionUser();
        }

        site.toolkit().createFlowText( parent /*welcome.getBody()*/, i18n( "welcomeTxt" ) ) //$NON-NLS-1$
                .setLayoutData( new ConstraintData( AzvPlugin.MIN_COLUMN_WIDTH, new PriorityConstraint( 100 ) ) );

        Composite formContainer = site.toolkit().createComposite( parent );
        formContainer.setLayout( new FillLayout() );
        form = new BasedataForm();
        form.createContents( formContainer );
        site.setSubmitEnabled( false );
        
        site.createSubmit( formContainer, i18n( "uebernehmen" ) );
    }

    
    @Override
    public void submit() throws Exception {
        assert form.isValid();
        form.submit();
        form.dispose();
        form = null;
        repo.get().commitChanges();
        
        site.getPanelSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, i18n( "uebernommen" ) ) );
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
        
        // do not leave 'empty' CasePanel after close button
        if (mcase.get().getName().isEmpty()) {
            site.getPanelSite().setStatus( new Status( IStatus.INFO, AzvPlugin.ID, i18n( "keineBasisdaten" ) ) );
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    site.getContext().closePanel( site.getPanelSite().getPath() );
                }
            });
        }
    }


    @Override
    public void fillContentArea( Composite parent ) {
        contentSection = site.toolkit().createPanelSection( parent, i18n( "sectionTitle" ) );
        contentSection.addConstraint( new PriorityConstraint( 100 ), AzvPlugin.MIN_COLUMN_WIDTH );
        contentSection.getBody().setLayout( new FillLayout() );

        if (mcase.get().getName().length() > 0) {
            contentForm = new BasedataForm();
            //contentForm.setFieldBuilderFactory( AzvPlugin.LABEL_FIELD_FACTORY );
            contentForm.createContents( contentSection.getBody() );
            contentForm.getBody().setLayout( ColumnLayoutFactory.defaults().spacing( 3 ).margins( 8 ).create() );
            contentForm.setEnabled( false );

//            casedataForm = new CasedataForm();
//            casedataForm.createContents( contentSection.getBody() );
//            casedataForm.getBody().setLayout( ColumnLayoutFactory.defaults().spacing( 3 ).margins( 8 ).create() );
//            casedataForm.setEnabled( false );
        }
        else {
            site.toolkit().createLabel( contentSection.getBody(), i18n( "nochKeineDaten" ) )
                    .setForeground( MosaicUiPlugin.COLOR_RED.get() );
        }
    }

    
//    /**
//     * 
//     */
//    public class CasedataForm
//            extends FormContainer {
//
//        private IFormFieldListener      fieldListener;
//        
//        private Composite               body;
//        
//        
//        public Composite getBody() {
//            return body;
//        }
//        
//        @Override
//        public void createFormContent( final IFormEditorPageSite formSite ) {
//            body = formSite.getPageBody();
//            body.setLayout( ColumnLayoutFactory.defaults().spacing( 5 ).margins( 10, 10 ).columns( 1, 1 ).create() );
//
//            createField( body, new KVPropertyAdapter( mcase.get(), AzvVorgang.KEY_NUMMER ) )
//                    .setLabel( i18n( "laufendeNr" ) ).setToolTipText( i18n( "laufendeNrTip" ) )
//                    .setEnabled( false ).create();
//        }
//    }


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

            createField( body, new BeanPropertyAdapter( mcase.get(), "name" ) ) //$NON-NLS-1$
                    .setLabel( i18n( "bezeichnung" ) ).setToolTipText( i18n( "bezeichnungTip" ) )
                    .setValidator( new NotEmptyValidator() ).create().setFocus();
            
            createField( body, new BeanPropertyAdapter( mcase.get(), "description" ) ) //$NON-NLS-1$
                    .setLabel( i18n( "beschreibung" ) ).setToolTipText( i18n( "beschreibungTip" ) )
                    .setField( new TextFormField() ).setValidator( new NotEmptyValidator() ).create()
                    .setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 60 ) );

            Composite street = site.toolkit().createComposite( body );
            createField( street, new KVPropertyAdapter( mcase.get(), KEY_STREET ) )
                    .setLabel( i18n( "strasseHnr" ) ).setToolTipText( i18n( "strasseHnrTip" ) )
                    .setField( new StringFormField() )
                    .setValidator( new AddressValidator( FIELD_STREET ) ).create();

            createField( street, new KVPropertyAdapter( mcase.get(), KEY_NUMBER ) )
                    .setLabel( IFormFieldLabel.NO_LABEL )
                    .setField( new StringFormField() )
                    .setValidator( new AddressValidator( FIELD_NUMBER ) ).create();

            Composite city = site.toolkit().createComposite( body );
            createField( city, new KVPropertyAdapter( mcase.get(), KEY_POSTALCODE ) )
                    .setLabel( i18n( "plzOrt" ) ).setToolTipText( i18n( "plzOrtTip" ) )
                    .setField( new StringFormField() )
                    .setValidator( AND( new PlzValidator(), new AddressValidator( FIELD_POSTALCODE ) ) ).create();

            createField( city, new KVPropertyAdapter( mcase.get(), KEY_CITY ) )
                    .setLabel( IFormFieldLabel.NO_LABEL )
                    .setField( new StringFormField() )
                    .setValidator( new AddressValidator( FIELD_CITY ) ).create();

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
                            log.info( "Point: " + geom ); //$NON-NLS-1$
                            
                            OrtMixin ort = mcase.get().as( OrtMixin.class );
                            ort.setGeom( geom );

                            EventManager.instance().publish( new PropertyChangeEvent( StartCaseAction.this, DrawFeatureMapAction.EVENT_NAME, null, geom ) );            
                        }
                        else {
                            site.getPanelSite().setStatus( new Status( IStatus.WARNING, AzvPlugin.ID, i18n( "adresseExistiertNicht" ) ) );                        
                        }
                    }
                }
            });

//            activateStatusAdapter( site.getPanelSite() );
        }
    }

}
