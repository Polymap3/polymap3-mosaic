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

import static org.polymap.azv.ui.entsorgung.EntsorgungMixin.*;

import java.util.Map;
import java.util.TreeMap;

import org.opengis.feature.Property;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;

import org.qi4j.api.query.Query;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.IAction;

import org.eclipse.ui.forms.widgets.ColumnLayoutData;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.security.UserPrincipal;
import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.app.FormContainer;
import org.polymap.rhei.batik.toolkit.ConstraintData;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.TextFormField;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.email.EmailService;
import org.polymap.rhei.um.ui.PlzValidator;
import org.polymap.rhei.um.ui.RegisterPanel;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.model.AzvRepository;
import org.polymap.azv.model.Entsorgungsliste;
import org.polymap.azv.ui.NotEmptyValidator;
import org.polymap.azv.ui.NutzerAnVorgangCaseAction;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.MosaicCaseEvents;
import org.polymap.mosaic.server.model2.MosaicCase2;
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

    private EntsorgungMixin                 entsorgung;
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2> repo;

    private ICaseActionSite                 site;

    private User                            umuser;

    private CaseStatus                      caseStatus;

    private DataForm                        form;

    private IAction                         caseAction;

    private Composite                       contentArea;

    private IPanelSection                   contentSection;

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        if (mcase.get() != null && repo.get() != null
                && (mcase.get().getNatures().contains( AzvPlugin.CASE_ENTSORGUNG ) )) {
            
            // default setting from user
            UserPrincipal principal = (UserPrincipal)Polymap.instance().getUser();
            entsorgung = mcase.get().as( EntsorgungMixin.class );
            
            if (principal != null
                    && !SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA )
                    && !SecurityUtils.isAdmin()
                    && entsorgung.name.get() == null) {
                UserRepository umrepo = UserRepository.instance();
                umuser = umrepo.findUser( principal.getName() );
                EntsorgungMixin mixin = ((MosaicCase2)mcase.get()).as( EntsorgungMixin.class );
                mixin.strasse.set( umuser.address().get().street().get() );
                mixin.nummer.set( umuser.address().get().number().get() );
                mixin.stadt.set( umuser.address().get().city().get() );
                mixin.plz.set( umuser.address().get().postalCode().get() );
                
                entsorgung.name.set( umuser.name().get() );

                String caseUser = mcase.get().get( NutzerAnVorgangCaseAction.KEY_USER );
                if (caseUser == null) {
                    String username = Polymap.instance().getUser().getName();
                    mcase.get().put( NutzerAnVorgangCaseAction.KEY_USER, username );
                }
            }

            // wenn Kunde und noch keine Liste gesetzt
            if (!SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA ) && entsorgung.liste.get() == null) {
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
    public void fillAction( IAction action ) {
        this.caseAction = action;
        if (Iterables.find( mcase.get().getEvents(), MosaicCaseEvents.contains( AzvPlugin.EVENT_TYPE_BEANTRAGT ), null) != null) {
            action.setText( null );
            action.setImageDescriptor( null );
            action.setEnabled( false );
        }
    }


    @Override
    public void fillStatus( CaseStatus status ) {
        this.caseStatus = status;
        status.put( "Name", entsorgung.name.get(), 5 );

        AzvRepository azvRepo = AzvRepository.instance();
        String listeId = entsorgung.liste.get();
        if (listeId != null) {
            Entsorgungsliste liste = azvRepo.findEntity( Entsorgungsliste.class, listeId );
            status.put( "Termin", liste.name().get(), 0 );
        }

        //site.getPanelSite().setIcon( BatikPlugin.instance().imageForName( "resources/icons/truck-filter.png" ) );
    }


    @Override
    public void fillContentArea( Composite parent ) {
        // contentSection
        this.contentArea = parent;
        if (contentSection == null) {
            contentSection = site.toolkit().createPanelSection( parent, "Daten" );
            contentSection.addConstraint( new PriorityConstraint( 10 ), AzvPlugin.MIN_COLUMN_WIDTH );
            contentSection.getBody().setLayout( new FillLayout() );
        }
        else {
            contentSection.getBody().getChildren()[0].dispose();
        }
        // content: form or label
        if (entsorgung.liste.get() != null) {
            DataForm contentForm = new DataForm();
            contentForm.createContents( contentSection.getBody() );
            contentForm.getBody().setLayout( ColumnLayoutFactory.defaults().spacing( 3 ).margins( 8 ).columns( 1, 1 ).create() );
            contentForm.setEnabled( false );

            IPanelSection sep = site.toolkit().createPanelSection( parent, "Status" );
            sep.addConstraint( new PriorityConstraint( 0 ) );
            sep.getBody().setLayout( new FillLayout() );
            site.toolkit().createFlowText( sep.getBody(), i18n.get( "dataTxt" ) );
        }
        else {
            site.toolkit().createLabel( contentSection.getBody(), "Noch keine Daten." )
                    .setForeground( MosaicUiPlugin.COLOR_RED.get() );
        }
    }


    @Override
    public void createContents( Composite parent ) {
//        // wenn hier noch kein Nutzer am Vorgang hängt, dann wird der eingeloggte
//        // Nutzer verwendet
//        String username = mcase.get().get( KEY_USER );
//        if (username == null) {
//            username = sessionUser.get().getName();
//            User umuser = UserRepository.instance().findUser( username );
//            setUserOnCase( umuser );
//            mcase.get().put( KEY_USER, username );
//        }

        site.toolkit().createFlowText( parent, i18n.get( "welcomeTxt", RegisterPanel.ID ) )
                .setLayoutData( new ConstraintData( AzvPlugin.MIN_COLUMN_WIDTH, new PriorityConstraint( 100 ) ) );

        Composite formContainer = site.toolkit().createComposite( parent );
        formContainer.setLayout( new FillLayout() );
        form = new DataForm();
        form.createContents( formContainer );
        site.setSubmitEnabled( false );
        
        site.createSubmit( formContainer, "Entsorgung beantragen" );
    }

    
    @Override
    public void submit() throws Exception {
        form.submit();
        mcase.get().setName( Joiner.on( " " ).skipNulls().join( entsorgung.strasse.get(), entsorgung.nummer.get(), entsorgung.stadt.get() ) );
        
        AzvRepository azvRepo = AzvRepository.instance();
        String listeId = entsorgung.liste.get();
        Entsorgungsliste liste = azvRepo.findEntity( Entsorgungsliste.class, listeId );
        liste.mcaseIds().get().add( mcase.get().getId() );
        
        azvRepo.commitChanges();
        
        repo.get().newCaseEvent( mcase.get(), liste.name().get(), 
                Joiner.on( " " ).skipNulls().join( liste.name().get(), entsorgung.strasse.get(), entsorgung.nummer.get(), entsorgung.stadt.get() ), 
                AzvPlugin.EVENT_TYPE_BEANTRAGT  );
        repo.get().commitChanges();

        // email
        String caseUser = mcase.get().get( NutzerAnVorgangCaseAction.KEY_USER );
        if (caseUser != null) {
            User user = UserRepository.instance().findUser( caseUser );
            String salu = user.salutation().get() != null ? user.salutation().get() : "";
            String header = "Sehr geehrte" + (salu.equalsIgnoreCase( "Herr" ) ? "r " : " ") + salu + " " + user.name().get();
            Email email = new SimpleEmail();
            email.setCharset( "ISO-8859-1" );
            email.addTo( user.email().get() )
                    .setSubject( i18n.get( "emailSubject") )
                    .setMsg( i18n.get( "email", header, liste.name().get() ) );
            EmailService.instance().send( email );
        }

        // update panel action and status
        fillStatus( caseStatus );
        fillAction( caseAction );
        fillContentArea( contentArea );
        site.getPanelSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, "Der Antrag wurde entgegen genommen. Sie erhalten eine EMail." ) );
    }


    @Override
    public void discard() {
        repo.get().rollbackChanges();
        
        // do not left 'empty' CasePanel after close button
        if (mcase.get().getName().isEmpty()) {
            site.getPanelSite().setStatus( new Status( IStatus.INFO, AzvPlugin.ID, "Es wurden keine Basisdaten eingegeben. Der Vorgang wurde daher komplett abgebrochen." ) );
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    site.getContext().closePanel( site.getPanelSite().getPath() );
                }
            });
        }
    }


    /**
     * 
     */
    public class DataForm
            extends FormContainer {

        private IFormFieldListener          fieldListener;
        
        private Composite                   body;

        @Override
        public void createFormContent( final IFormEditorPageSite formSite ) {
            body = formSite.getPageBody();
            body.setLayout( ColumnLayoutFactory.defaults().spacing( 5 ).margins( 10, 10 ).columns( 1, 1 ).create() );

            new FormFieldBuilder( body, new KVPropertyAdapter( mcase.get(), KEY_NAME ) )
                    .setLabel( "Name" ).setToolTipText( "Ihr Name" )
                    .setValidator( new NotEmptyValidator() ).create().setFocus();

            new FormFieldBuilder( body, new KVPropertyAdapter( mcase.get(), KEY_KUNDENNUMMER ) )
                    .setLabel( "Kundennummer" ).setToolTipText( "Ihre Kundennummer (siehe letzte Rechnung)" )
                    .create();

            Query<Entsorgungsliste> listen = AzvRepository.instance().findEntities( Entsorgungsliste.class, null, 0, -1 );
            Map<String,String> picklistMap = new TreeMap();
            for (Entsorgungsliste liste : listen) {
                if (BooleanUtils.isNotTrue( liste.geschlossen().get() )) {
                    picklistMap.put( liste.name().get(), liste.id() );
                }
            }
            new FormFieldBuilder( body, new KVPropertyAdapter( mcase.get(), KEY_LISTE ) )
                    .setLabel( "Termin" ).setToolTipText( "Gewünschter Termin der Entsorgung" )
                    .setField( new PicklistFormField( picklistMap ) )
                    .setValidator( new NotEmptyValidator() ).create();

            new FormFieldBuilder( body, new KVPropertyAdapter( mcase.get(), KEY_BEMERKUNG ) )
                    .setLabel( "Bemerkung" ).setToolTipText( "Besondere Hinweise für das Entsorgungsunternehmen" )
                    .setField( new TextFormField() ).create()
                    .setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 60 ) );
    
            Composite street = site.toolkit().createComposite( body );
            Property prop = new KVPropertyAdapter( mcase.get(), KEY_STREET );
            new FormFieldBuilder( street, prop )
                    .setLabel( "Straße / Nummer" ).setToolTipText( "Straße und Hausnummer" )
                    .setField( new StringFormField() ).setValidator( new NotEmptyValidator() ).create();

            prop = new KVPropertyAdapter( mcase.get(), KEY_NUMBER );
            new FormFieldBuilder( street, prop )
                    .setLabel( IFormFieldLabel.NO_LABEL )
                    .setField( new StringFormField() ).setValidator( new NotEmptyValidator() ).create();

            Composite city = site.toolkit().createComposite( body );
            prop = new KVPropertyAdapter( mcase.get(), KEY_POSTALCODE );
            new FormFieldBuilder( city, prop )
                    .setLabel( "PLZ / Ort" ).setToolTipText( "Postleitzahl und Ortsname" )
                    .setField( new StringFormField() ).setValidator( new PlzValidator( new NotEmptyValidator() ) ).create();

            prop = new KVPropertyAdapter( mcase.get(), KEY_CITY );
            new FormFieldBuilder( city, prop )
                    .setLabel( IFormFieldLabel.NO_LABEL )
                    .setField( new StringFormField() ).setValidator( new NotEmptyValidator() ).create();

            
            // field listener
            formSite.addFieldListener( fieldListener = new IFormFieldListener() {
                
                @SuppressWarnings("hiding")
                private String      street, number, city;
                
                public void fieldChange( FormFieldEvent ev ) {
                    if (ev.getEventCode() != VALUE_CHANGE) {
                        return;
                    }
                    site.setSubmitEnabled( formSite.isDirty() && formSite.isValid() );
                    
                    if (ev.getFieldName().equals( KEY_LISTE )) {
                        String id = ev.getNewValue();
                        Entsorgungsliste liste = AzvRepository.instance().findEntity( Entsorgungsliste.class, id );
                        caseStatus.put( "Termin", liste.name().get() );
                    }
                    else if (ev.getFieldName().equals( KEY_NAME )) {
                        caseStatus.put( "Name", ev.getNewValue().toString() );
                    }
                    else if (ev.getFieldName().equals( KEY_CITY )) {
                        city = ev.getNewValue();
                    }
                    else if (ev.getFieldName().equals( KEY_NUMBER )) {
                        number = ev.getNewValue();
                    }
                    else if (ev.getFieldName().equals( KEY_STREET )) {
                        street = ev.getNewValue();
                    }
//                    caseStatus.put( "Entsorgung", Joiner.on( " " ).skipNulls().join( 
//                            street, number, city ) );
                }
            });
//            activateStatusAdapter( site.getPanelSite() );
        }

        public Composite getBody() {
            return body;
        }
        
    }

}
