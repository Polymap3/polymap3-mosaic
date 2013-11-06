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

import org.opengis.feature.Property;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;
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
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.ui.NotNullValidator;
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
public class LeitungsauskunftStartCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( LeitungsauskunftStartCaseAction.class );

    public static final IMessages       i18n = Messages.forPrefix( "LeitungsauskunftStart" );

    private static final FastDateFormat df = AzvPlugin.df;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>        mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2>  repo;

    private ICaseActionSite                     site;
    
    private CaseStatus                          caseStatus;

    private BasedataForm                        form;

    
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
    public void fillStatus( CaseStatus status ) {
        caseStatus = status;
        String id = mcase.get().getId();
        status.put( "Laufende Nr.", StringUtils.right( id, 6 ) );
        status.put( "Vorgang", mcase.get().getName(), 100 );
        status.put( "Angelegt am", df.format( mcase.get().getCreated() ), 99 );
    }


    @Override
    public void createContents( Composite parent ) {
        // wenn hier noch kein Nutzer am Vorgang hängt, dann wird der eingeloggte
        // Nutzer verwendet
        String username = mcase.get().get( "user" );
        if (username == null) {
            username = Polymap.instance().getUser().getName();
            User umuser = UserRepository.instance().findUser( username );
            setUserOnCase( umuser );
            mcase.get().put( "user", username );
        }

        FillLayout playout = (FillLayout)parent.getLayout();
        playout.marginWidth *= 2;      
        playout.spacing *= 2;      

        site.toolkit().createFlowText( parent, i18n.get( "welcomeTxt" ) );

        Composite formContainer = site.toolkit().createComposite( parent );
        formContainer.setLayout( new FillLayout() );
        form = new BasedataForm();
        form.createContents( formContainer );
        site.setValid( false );
    }

    
    protected void setUserOnCase( User umuser ) {
//        Address address = umuser.address().get();
//        String value = address.city().get();
//        if (value != null) {
//            mcase.get().put( "city", value );
//        }
//        value = address.street().get();
//        if (value != null) {
//            mcase.get().put( "street", value );
//        }
//        value = address.number().get();
//        if (value != null) {
//            mcase.get().put( "number", value );
//        }
//        value = address.postalCode().get();
//        if (value != null) {
//            mcase.get().put( "postalcode", value );
//        }
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