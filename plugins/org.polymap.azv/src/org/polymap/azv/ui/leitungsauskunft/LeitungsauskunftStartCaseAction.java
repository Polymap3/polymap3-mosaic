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
package org.polymap.azv.ui.leitungsauskunft;

import java.beans.PropertyChangeEvent;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.ui.PlzValidator;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.ui.NotEmptyValidator;
import org.polymap.azv.ui.NutzerAnVorgangCaseAction;
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
public class LeitungsauskunftStartCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( LeitungsauskunftStartCaseAction.class );

    public static final IMessages       i18n = Messages.forPrefix( "LeitungsauskunftStart" );

    private static final FastDateFormat df = AzvPlugin.df;

    public static final String          KEY_STREET = "street";
    public static final String          KEY_NUMBER = "number";
    public static final String          KEY_CITY = "city";
    public static final String          KEY_POSTALCODE = "postalcode";
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>        mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2>  repo;

    private ICaseActionSite                     site;
    
    private CaseStatus                          caseStatus;

    private BasedataForm                        form;

    private IPanelSection                       contentSection;

    private BasedataForm                        contentForm;

    private IAction caseAction;

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        if (mcase.get() != null && repo.get() != null
                && mcase.get().getNatures().contains( AzvPlugin.CASE_LEITUNGSAUSKUNFT )) {
            
            // wenn Kunde und noch kein Name gesetzt ist
            if (!SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA )
                    && mcase.get().getName().length() == 0) {
                
                User umuser = UserRepository.instance().findUser( Polymap.instance().getUser().getName() );
                setUserOnCase( umuser );
                // open action
                Polymap.getSessionDisplay().asyncExec( new Runnable() {
                    public void run() {
                        site.activateCaseAction( site.getActionId() );
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
        
        EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {
            public boolean apply( PropertyChangeEvent input ) {
                return caseAction != null && input.getSource() == mcase.get();
            }
        });
    }

    
    @EventHandler(display=true)
    protected void updateAction( PropertyChangeEvent ev ) {
        if (MosaicCaseEvents.contains( mcase.get().getEvents(), AzvPlugin.EVENT_TYPE_BEANTRAGT )) {
            caseAction.setText( null );
            caseAction.setImageDescriptor( null );
            caseAction.setEnabled( false );
        }
    }

    
    @Override
    public void fillStatus( CaseStatus status ) {
        caseStatus = status;
        String id = mcase.get().getId();
        status.put( "Laufende Nr.", StringUtils.right( id, 6 ) );
    }


    @Override
    public void createContents( Composite parent ) {
        // wenn hier noch kein Nutzer am Vorgang hängt, dann wird der eingeloggte
        // Nutzer verwendet
        String username = mcase.get().get( NutzerAnVorgangCaseAction.KEY_USER );
        if (username == null) {
            username = Polymap.instance().getUser().getName();
            User umuser = UserRepository.instance().findUser( username );
            setUserOnCase( umuser );
            mcase.get().put( NutzerAnVorgangCaseAction.KEY_USER, username );
        }

        site.toolkit().createFlowText( parent, i18n.get( "welcomeTxt" ) )
                .setLayoutData( new ConstraintData( AzvPlugin.MIN_COLUMN_WIDTH, new PriorityConstraint( 100 ) ) );

        Composite formContainer = site.toolkit().createComposite( parent );
        formContainer.setLayout( new FillLayout() );
        form = new BasedataForm();
        form.createContents( formContainer );
        site.setSubmitEnabled( false );
        
        site.createSubmit( formContainer, "Übernehmen" );
    }

    
    protected void setUserOnCase( User umuser ) {
//        Address address = umuser.address().get();
//        String value = address.city().get();
//        if (value != null) {
//            mcase.get().put( KEY_CITY, value );
//        }
//        value = address.street().get();
//        if (value != null) {
//            mcase.get().put( KEY_STREET, value );
//        }
//        value = address.number().get();
//        if (value != null) {
//            mcase.get().put( KEY_NUMBER, value );
//        }
//        value = address.postalCode().get();
//        if (value != null) {
//            mcase.get().put( KEY_POSTALCODE, value );
//        }
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

        // create/reload form
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
        contentSection = site.toolkit().createPanelSection( parent, "Daten" );
        contentSection.addConstraint( new PriorityConstraint( 100 ), AzvPlugin.MIN_COLUMN_WIDTH );
        contentSection.getBody().setLayout( new FillLayout() );

        if (mcase.get().getName().length() > 0) {
            contentForm = new BasedataForm();
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

        @Override
        public void createFormContent( final IFormEditorPageSite formSite ) {
            body = formSite.getPageBody();
            body.setLayout( ColumnLayoutFactory.defaults().spacing( 5 ).margins( 10, 10 ).columns( 1, 1 ).create() );

            new FormFieldBuilder( body, new BeanPropertyAdapter( mcase.get(), "name" ) )
                    .setLabel( "Bezeichnung" ).setToolTipText( "Bezeichnung der Maßnahme" )
                    .setValidator( new NotEmptyValidator() ).create().setFocus();
            
            new FormFieldBuilder( body, new BeanPropertyAdapter( mcase.get(), "description" ) )
                    .setLabel( "Beschreibung" ).setToolTipText( "Beschreibung der Maßnahme" )
                    .setField( new TextFormField() ).setValidator( new NotEmptyValidator() ).create()
                    .setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 60 ) );

            Composite street = site.toolkit().createComposite( body );
            new FormFieldBuilder( street, new KVPropertyAdapter( mcase.get(), KEY_STREET ) )
                    .setLabel( "Straße / Nummer" ).setToolTipText( "Straße und Hausnummer" )
                    .setField( new StringFormField() )/*.setValidator( new NotEmptyValidator() )*/.create();

            new FormFieldBuilder( street, new KVPropertyAdapter( mcase.get(), KEY_NUMBER ) )
                    .setLabel( IFormFieldLabel.NO_LABEL )
                    .setField( new StringFormField() )/*.setValidator( new NotEmptyValidator() )*/.create();

            Composite city = site.toolkit().createComposite( body );
            new FormFieldBuilder( city, new KVPropertyAdapter( mcase.get(), KEY_POSTALCODE ) )
                    .setLabel( "PLZ / Ort" ).setToolTipText( "Postleitzahl und Ortsname" )
                    .setField( new StringFormField() ).setValidator( new PlzValidator() ).create();

            new FormFieldBuilder( city, new KVPropertyAdapter( mcase.get(), KEY_CITY ) )
                    .setLabel( IFormFieldLabel.NO_LABEL )
                    .setField( new StringFormField() )/*.setValidator( new NotEmptyValidator() )*/.create();

            // address listener
            formSite.addFieldListener( fieldListener = new IFormFieldListener() {
                public void fieldChange( FormFieldEvent ev ) {
                    if (ev.getEventCode() != VALUE_CHANGE) {
                        return;
                    }
                    boolean valid = formSite.isValid();
                    boolean dirty = formSite.isDirty();
                    log.info( "dirty=" + dirty + ", valid=" + valid );
                    site.setSubmitEnabled( valid & dirty );
                    
//                    if (ev.getFieldName().equals( "name" )) {
//                        caseStatus.put( "Bezeichnung", (String)ev.getNewValue() );
//                    }
                }
            });
//            activateStatusAdapter( site.getPanelSite() );
        }

        public Composite getBody() {
            return body;
        }
    }

}
