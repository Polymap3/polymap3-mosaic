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

import org.opengis.feature.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.security.UserPrincipal;
import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.app.FormContainer;
import org.polymap.rhei.field.BeanPropertyAdapter;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.PlainValuePropertyAdapter;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.um.Address;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.ui.LoginPanel;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model2.MosaicRepository2;
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

    public static final IMessages       i18n = Messages.forPrefix( "SchachtscheinStart" );

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>        mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2>  repo;

    /** Set by the {@link LoginPanel}. */
    private ContextProperty<UserPrincipal>      sessionUser;
    
    private ICaseActionSite                     site;
    
    private CaseStatus                          caseStatus;

    private BasedataForm                        form;

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        if (mcase.get() != null && repo.get() != null
                && mcase.get().getNatures().contains( AzvPlugin.CASE_SCHACHTSCHEIN )) {
            
            if (!SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA )) {
                User umuser = UserRepository.instance().findUser( sessionUser.get().getName() );
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
    public void fillStatus( CaseStatus status ) {
        caseStatus = status;
        caseStatus.put( "Laufende Nr.", mcase.get().getId() );
    }


    @Override
    public void createContents( Composite parent ) {
        // wenn hier noch kein Nutzer am Vorgang hängt, dann wird der eingeloggte
        // Nutzer verwendet
        String username = mcase.get().get( "user" );
        if (username == null) {
            username = sessionUser.get().getName();
            User umuser = UserRepository.instance().findUser( username );
            setUserOnCase( umuser );
            mcase.get().put( "user", username );
        }

        FillLayout playout = (FillLayout)parent.getLayout();
        playout.marginWidth *= 2;      
        playout.spacing *= 2;      

//        IPanelSection welcome = site.toolkit().createPanelSection( parent, i18n.get( "welcomeTitle" ) );
//        welcome.getBody().setLayout( new FillLayout() );
        site.toolkit().createFlowText( parent /*welcome.getBody()*/, i18n.get( "welcomeTxt" ) );

        Composite formContainer = site.toolkit().createComposite( parent );
        formContainer.setLayout( new FillLayout() );
        form = new BasedataForm();
        form.createContents( formContainer );
        site.setValid( false );
    }

    
    protected void setUserOnCase( User umuser ) {
        Address address = umuser.address().get();
        String value = address.city().get();
        if (value != null) {
            mcase.get().put( "city", value );
        }
        value = address.street().get();
        if (value != null) {
            mcase.get().put( "street", value );
        }
        value = address.number().get();
        if (value != null) {
            mcase.get().put( "number", value );
        }
        value = address.postalCode().get();
        if (value != null) {
            mcase.get().put( "postalcode", value );
        }
    }
    
    
    @Override
    public void submit() throws Exception {
        assert form.isValid();
        form.submit();
        form.dispose();
        form = null;
        repo.get().commitChanges();
    }


    @Override
    public void discard() {
        if (form != null) {
            form.dispose();
            form = null;
        }
    }


    /**
     * 
     */
    public class BasedataForm
            extends FormContainer {

        private IFormFieldListener          fieldListener;

        @Override
        public void createFormContent( final IFormEditorPageSite formSite ) {
            Composite body = formSite.getPageBody();
            body.setLayout( ColumnLayoutFactory.defaults().spacing( 10 ).margins( 10, 10 ).columns( 1, 1 ).create() );

            new FormFieldBuilder( body, new BeanPropertyAdapter( mcase.get(), "name" ) )
                    .setLabel( "Bezeichnung" ).setToolTipText( "Bezeichnung der Maßnahme" )
                    .setValidator( new NotNullValidator() ).create().setFocus();
            
            new FormFieldBuilder( body, new BeanPropertyAdapter( mcase.get(), "description" ) )
                    .setLabel( "Beschreibung" ).setToolTipText( "Beschreibung der Maßnahme" )
                    .setField( new TextFormField() ).setValidator( new NotNullValidator() ).create()
                    .setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 60 ) );

            Composite city = site.toolkit().createComposite( body );
            Property prop = new PlainValuePropertyAdapter( "postalcode", mcase.get().get( "postalcode" ) );
            new FormFieldBuilder( city, prop )
                    .setLabel( "PLZ / Ort" ).setToolTipText( "Postleitzahl und Ortsname" )
                    .setField( new StringFormField() )/*.setValidator( new NotNullValidator() )*/.create();

            prop = new PlainValuePropertyAdapter( "city", mcase.get().get( "city" ) );
            new FormFieldBuilder( city, prop )
                    .setLabel( IFormFieldLabel.NO_LABEL )
                    .setField( new StringFormField() )/*.setValidator( new NotNullValidator() )*/.create();

            Composite street = site.toolkit().createComposite( body );
            prop = new PlainValuePropertyAdapter( "street", mcase.get().get( "street" ) );
            new FormFieldBuilder( street, prop )
                    .setLabel( "Straße / Nummer" ).setToolTipText( "Straße und Hausnummer" )
                    .setField( new StringFormField() )/*.setValidator( new NotNullValidator() )*/.create();

            prop = new PlainValuePropertyAdapter( "number", mcase.get().get( "number" ) );
            new FormFieldBuilder( street, prop )
                    .setLabel( IFormFieldLabel.NO_LABEL )
                    .setField( new StringFormField() )/*.setValidator( new NotNullValidator() )*/.create();

            
//            //new FormFieldBuilder( body, new PropertyAdapter( entity.get().antragsteller() ) ).create();
//            new FormFieldBuilder( body, new PropertyAdapter( entity.get().startDate() ) )
//                    .setLabel( "Beginn" ).setToolTipText( "Geplanter Beginn der Arbeiten" ).create();
//            new FormFieldBuilder( body, new PropertyAdapter( entity.get().endDate() ) )
//                    .setLabel( "Ende" ).setToolTipText( "Geplantes Ende der Arbeiten" ).create();
//            new FormFieldBuilder( body, new PropertyAdapter( entity.get().bemerkungen() ) )
//                    .setField( new TextFormField() ).create()
//                    .setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 80 ) );
            
//            // address
//            Composite plzLine = site.getToolkit().createComposite( body );
//            plzLine.setLayout( new FillLayout( SWT.HORIZONTAL ) );
//            new FormFieldBuilder( plzLine, new PropertyAdapter( entity.get().plz() ) )
//                    .setValidator( new NotNullValidator() ).setLabel( "PLZ/Ort" ).setToolTipText( "Postleitzahl der Baumaßnahme" ).create();
//            new FormFieldBuilder( plzLine, new PropertyAdapter( entity.get().ort() ) )
//                    .setValidator( new NotNullValidator() ).setLabel( IFormFieldLabel.NO_LABEL ).setToolTipText( "Ort der Baumaßnahme" ).create();
//            new FormFieldBuilder( body, new PropertyAdapter( entity.get().strasse() ) )
//                    .setValidator( new NotNullValidator() ).setLabel( "Strasse" ).setToolTipText( "Adresse der Baumaßnahme" ).create();
//
//            // submit
//            submitBtn = site.getToolkit().createButton( body, "BEANTRAGEN", SWT.PUSH );
//            submitBtn.addSelectionListener( new SelectionAdapter() {
//                public void widgetSelected( SelectionEvent ev ) {
//                    try {
//                        site.submitEditor();
//                        AzvRepository.instance().commitChanges();
//                        getContext().closePanel();
//                    }
//                    catch (Exception e) {
//                        BatikApplication.handleError( "Vorgang konnte nicht angelegt werden.", e );
//                    }
//                }
//            });
//            
            // address listener
            formSite.addFieldListener( fieldListener = new IFormFieldListener() {
                public void fieldChange( FormFieldEvent ev ) {
                    
                    site.setValid( formSite.isValid() );
                    
                    if (ev.getEventCode() == VALUE_CHANGE && ev.getFieldName().equals( "name" )) {
                        caseStatus.put( "Vorgang", (String)ev.getNewValue() );
                    }
                }
            });
//            activateStatusAdapter( getSite() );
//        }
        }
    }

}
