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
package org.polymap.azv.ui.entsorgung;

import org.opengis.feature.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.app.FormContainer;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.um.User;

import org.polymap.azv.AzvPermissions;
import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.ui.NotNullValidator;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.KVPropertyAdapter;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.CaseStatus;
import org.polymap.mosaic.ui.casepanel.DefaultCaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseActionSite;

/**
 * Antrag auf Entsorgung stellen. 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EntsorgungCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( EntsorgungCaseAction.class );

    public static final IMessages           i18n = Messages.forPrefix( "Entsorgung" );
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>    mcase;
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2> repo;

    private ICaseActionSite                 site;

    private User                            umuser;

    private CaseStatus                      caseStatus;

    private DataForm                        form;

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        if (mcase.get() != null && repo.get() != null
                && (mcase.get().getNatures().contains( AzvPlugin.CASE_ENTSORGUNG ) )) {
            
            umuser = AzvPermissions.instance().getUser();
//            setUserOnCase( umuser );

            if (mcase.get().get( "termin" ) == null) {
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
        this.caseStatus = status;
        //status.put( "Entsorgung", "[Geben Sie einen Adresse an]", 10 );
        status.put( "Termin", mcase.get().get( "termin" ), 0 );

        site.getPanelSite().setIcon( BatikPlugin.instance().imageForName( "resources/icons/truck-filter.png" ) );
    }


    @Override
    public void fillContentArea( Composite parent ) {
        if (mcase.get().get( "termin" ) != null) {
            IPanelSection section = site.toolkit().createPanelSection( parent, "Daten" );
            section.addConstraint( new PriorityConstraint( 1 ) );
            section.getBody().setLayout( new FillLayout() );

            new DataForm().createContents( section.getBody() );
            site.setValid( false );
        }
    }


    @Override
    public void createContents( Composite parent ) {
//        // wenn hier noch kein Nutzer am Vorgang hängt, dann wird der eingeloggte
//        // Nutzer verwendet
//        String username = mcase.get().get( "user" );
//        if (username == null) {
//            username = sessionUser.get().getName();
//            User umuser = UserRepository.instance().findUser( username );
//            setUserOnCase( umuser );
//            mcase.get().put( "user", username );
//        }

        FillLayout playout = (FillLayout)parent.getLayout();
        playout.marginWidth *= 2;      
        playout.spacing *= 2;      

        site.toolkit().createFlowText( parent, i18n.get( "welcomeTxt" ) );

        Composite formContainer = site.toolkit().createComposite( parent );
        formContainer.setLayout( new FillLayout() );
        form = new DataForm();
        form.createContents( formContainer );
        site.setValid( false );
    }

    
    @Override
    public void submit() throws Exception {
        form.submit();
        IMosaicCase mc = mcase.get();
        mc.setName( Joiner.on( " " ).skipNulls().join( mc.get( "street" ), mc.get( "number" ), mc.get( "city" ) ) );
        
        repo.get().newCaseEvent( mcase.get(), mcase.get().get( "termin" ), 
                Joiner.on( " " ).skipNulls().join( mc.get( "termin" ), mc.get( "street" ), mc.get( "number" ), mc.get( "city" ) ), 
                AzvPlugin.EVENT_TYPE_BEANTRAGT  );
        repo.get().commitChanges();
        
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                site.getContext().closePanel();
            }
        });
    }


    /**
     * 
     */
    public class DataForm
            extends FormContainer {

        private IFormFieldListener          fieldListener;

        @Override
        public void createFormContent( final IFormEditorPageSite formSite ) {
            Composite body = formSite.getPageBody();
            body.setLayout( ColumnLayoutFactory.defaults().spacing( 10 ).margins( 10, 10 ).columns( 1, 1 ).create() );

            new FormFieldBuilder( body, new KVPropertyAdapter( mcase.get(), "termin" ) )
                    .setLabel( "Termin" ).setToolTipText( "Gewünschter Termin der Entsorgung" )
                    .setValidator( new NotNullValidator() ).create().setFocus();
    
            new FormFieldBuilder( body, new KVPropertyAdapter( mcase.get(), "bemerkung" ) )
                    .setLabel( "Bemerkung" ).setToolTipText( "Besondere Hinweise für das Entsorgungsunternehmen" )
                    .setField( new TextFormField() ).create()
                    .setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 60 ) );
    
            Composite city = site.toolkit().createComposite( body );
            Property prop = new KVPropertyAdapter( mcase.get(), "postalcode" );
            new FormFieldBuilder( city, prop )
                    .setLabel( "PLZ / Ort" ).setToolTipText( "Postleitzahl und Ortsname" )
                    .setField( new StringFormField() ).setValidator( new NotNullValidator() ).create();

            prop = new KVPropertyAdapter( mcase.get(), "city" );
            new FormFieldBuilder( city, prop )
                    .setLabel( IFormFieldLabel.NO_LABEL )
                    .setField( new StringFormField() ).setValidator( new NotNullValidator() ).create();

            Composite street = site.toolkit().createComposite( body );
            prop = new KVPropertyAdapter( mcase.get(), "street" );
            new FormFieldBuilder( street, prop )
                    .setLabel( "Straße / Nummer" ).setToolTipText( "Straße und Hausnummer" )
                    .setField( new StringFormField() ).setValidator( new NotNullValidator() ).create();

            prop = new KVPropertyAdapter( mcase.get(), "number" );
            new FormFieldBuilder( street, prop )
                    .setLabel( IFormFieldLabel.NO_LABEL )
                    .setField( new StringFormField() ).setValidator( new NotNullValidator() ).create();

            
            // field listener
            formSite.addFieldListener( fieldListener = new IFormFieldListener() {
                
                @SuppressWarnings("hiding")
                private String      street, number, city;
                
                public void fieldChange( FormFieldEvent ev ) {
                    if (ev.getEventCode() != VALUE_CHANGE) {
                        return;
                    }
                    site.setValid( formSite.isValid() );
                    
                    if (ev.getFieldName().equals( "termin" )) {
                        caseStatus.put( "Termin", ev.getNewValue().toString() );
                    }
                    else if (ev.getFieldName().equals( "city" )) {
                        city = ev.getNewValue();
                    }
                    else if (ev.getFieldName().equals( "number" )) {
                        number = ev.getNewValue();
                    }
                    else if (ev.getFieldName().equals( "street" )) {
                        street = ev.getNewValue();
                    }
//                    caseStatus.put( "Entsorgung", Joiner.on( " " ).skipNulls().join( 
//                            street, number, city ) );
                }
            });
            activateStatusAdapter( site.getPanelSite() );
        }
    }

}
