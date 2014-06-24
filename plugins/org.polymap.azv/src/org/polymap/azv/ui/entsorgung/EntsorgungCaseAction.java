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

import static org.polymap.azv.model.AdresseMixin.KEY_CITY;
import static org.polymap.azv.model.AdresseMixin.KEY_NUMBER;
import static org.polymap.azv.model.AdresseMixin.KEY_POSTALCODE;
import static org.polymap.azv.model.AdresseMixin.KEY_STREET;
import static org.polymap.azv.model.EntsorgungVorgang.KEY_BEMERKUNG;
import static org.polymap.azv.model.EntsorgungVorgang.KEY_KUNDENNUMMER;
import static org.polymap.azv.model.EntsorgungVorgang.KEY_LISTE;
import static org.polymap.azv.model.EntsorgungVorgang.KEY_NAME;

import java.util.Map;
import java.util.TreeMap;

import org.opengis.feature.Property;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;
import org.qi4j.api.unitofwork.NoSuchEntityException;

import com.google.common.base.Joiner;

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
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

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
import org.polymap.rhei.um.ui.PlzValidator;
import org.polymap.rhei.um.ui.RegisterPanel;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.model.AdresseMixin;
import org.polymap.azv.model.AzvRepository;
import org.polymap.azv.model.AzvVorgang;
import org.polymap.azv.model.EntsorgungVorgang;
import org.polymap.azv.model.Entsorgungsliste;
import org.polymap.azv.ui.NotEmptyValidator;
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
 * Antrag auf Entsorgung stellen. 
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EntsorgungCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( EntsorgungCaseAction.class );

    public static final IMessages           i18n = Messages.forPrefix( "Entsorgung" ); //$NON-NLS-1$
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>    mcase;

    private EntsorgungVorgang                 entsorgung;
    
    private AzvVorgang                      azvVorgang;
    
    private AdresseMixin                    adresse;
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2> repo;

    private ICaseActionSite                 site;

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
            
            _site.getPanelSite().setIcon( AzvPlugin.instance().imageForName( "resources/icons/truck-filter.png" ) );
            
            // default setting from user
//            UserPrincipal principal = (UserPrincipal)Polymap.instance().getUser();
            entsorgung = mcase.get().as( EntsorgungVorgang.class );
            adresse = mcase.get().as( AdresseMixin.class );
            azvVorgang = mcase.get().as( AzvVorgang.class );
            
//            if (principal != null
//                    && !SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA )
//                    && !SecurityUtils.isAdmin()
//                    && entsorgung.name.get() == null) {
//                
//                // Nutzer am Vorgang setzen
//                nutzer.setSessionUser();                
//            }

            // wenn Kunde und noch keine Liste gesetzt
            if (!SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA ) 
                    && entsorgung.liste.get() == null) {
                
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
        if (MosaicCaseEvents.contains( mcase.get(), AzvPlugin.EVENT_TYPE_BEANTRAGT )) {
            action.setText( null );
            action.setImageDescriptor( null );
            action.setEnabled( false );
        }
    }


    @Override
    public void fillStatus( CaseStatus status ) {
        this.caseStatus = status;
//        status.put( i18n.get( "vorgangStatusName" ), entsorgung.name.get(), 5 );

        AzvRepository azvRepo = AzvRepository.instance();
        String listeId = entsorgung.liste.get();
        if (listeId != null) {
            try {
                Entsorgungsliste liste = azvRepo.findEntity( Entsorgungsliste.class, listeId );
                status.put( i18n.get( "vorgangStatusTermin" ), liste.name().get(), 0 );
            }
            catch (NoSuchEntityException e) {
            }
        }

        //site.getPanelSite().setIcon( BatikPlugin.instance().imageForName( "resources/icons/truck-filter.png" ) );
    }


    @Override
    public void fillContentArea( Composite parent ) {
        // contentSection
        this.contentArea = parent;
        if (contentSection == null) {
            contentSection = site.toolkit().createPanelSection( parent, i18n.get( "datenTitle" ) );
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

            // für normale Nutzer: Statustext
            if (!SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA ) ) {
                IPanelSection sep = site.toolkit().createPanelSection( parent, i18n.get( "statusTitle" ) );
                sep.addConstraint( new PriorityConstraint( 100 ) );
                sep.getBody().setLayout( new FillLayout() );
                site.toolkit().createFlowText( sep.getBody(), i18n.get( "dataTxt" ) ); //$NON-NLS-1$
            }
        }
        else {
            site.toolkit().createLabel( contentSection.getBody(), i18n.get( "keineDaten" ) )
                    .setForeground( MosaicUiPlugin.COLOR_RED.get() );
        }
    }


    @Override
    public void createContents( Composite parent ) {
        // noch kein Nutzer am Vorgang -> session user eintragen
        User user = azvVorgang.user();
        if (user == null && Polymap.instance().getUser() != null) {
            user = azvVorgang.setSessionUser();
        }
        // noch keine Adresse -> vom Nutzer
        if (entsorgung.name.get() == null && user != null) {
            adresse.setAdresseVonNutzer( user );
            entsorgung.name.set( user.name().get() );
        }

        site.toolkit().createFlowText( parent, i18n.get( "welcomeTxt", RegisterPanel.ID ) ) //$NON-NLS-1$
                .setLayoutData( new ConstraintData( AzvPlugin.MIN_COLUMN_WIDTH, new PriorityConstraint( 100 ) ) );

        Composite formContainer = site.toolkit().createComposite( parent );
        formContainer.setLayout( new FillLayout() );
        form = new DataForm();
        form.createContents( formContainer );
        site.setSubmitEnabled( false );
        
        site.createSubmit( formContainer, i18n.get( "beantragen" ) );
    }

    
    @Override
    public void submit() throws Exception {
        form.submit();
        mcase.get().setName( Joiner.on( " " ).skipNulls().join( adresse.strasse.get(), adresse.nummer.get(), adresse.stadt.get() ) ); //$NON-NLS-1$
        
        String listeId = entsorgung.liste.get();
        AzvRepository azvRepo = AzvRepository.instance();
        Entsorgungsliste liste = azvRepo.findEntity( Entsorgungsliste.class, listeId );
//        liste.mcaseIds().get().add( mcase.get().getId() );
//        
//        azvRepo.commitChanges();
        
        repo.get().newCaseEvent( mcase.get(), liste.name().get(), 
                Joiner.on( " " ).skipNulls().join( liste.name().get(), adresse.strasse.get(), adresse.nummer.get(), adresse.stadt.get() ),  //$NON-NLS-1$
                AzvPlugin.EVENT_TYPE_BEANTRAGT  );
        repo.get().commitChanges();

        // email
        User user = azvVorgang.user();
        if (user != null) {
            AzvPlugin.sendEmail( user, i18n, liste.name().get() );
        }

        site.getPanelSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, i18n.get( "statusOk" ) ) );

        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                site.getContext().closePanel( site.getPanelSite().getPath() );
            }
        });

//        // update panel action and status
//        fillStatus( caseStatus );
//        fillAction( caseAction );
//        fillContentArea( contentArea );
    }


    @Override
    public void discard() {
        repo.get().rollbackChanges();
        
        // do not left 'empty' CasePanel after close button
        if (mcase.get().getName().isEmpty()) {
            site.getPanelSite().setStatus( new Status( IStatus.INFO, AzvPlugin.ID, i18n.get( "keineBasisdaten" ) ) );
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
                    .setLabel( i18n.get( "name" ) ).setToolTipText( i18n.get( "nameTip" ) )
                    .setValidator( new NotEmptyValidator() ).create().setFocus();

            new FormFieldBuilder( body, new KVPropertyAdapter( mcase.get(), KEY_KUNDENNUMMER ) )
                    .setLabel( i18n.get( "kundennummer" ) ).setToolTipText( i18n.get( "kundennummerTip" ) )
                    .create();

            Query<Entsorgungsliste> listen = AzvRepository.instance().findEntities( Entsorgungsliste.class, null, 0, -1 );
            Map<String,String> picklistMap = new TreeMap();
            for (Entsorgungsliste liste : listen) {
                if (BooleanUtils.isNotTrue( liste.geschlossen().get() )) {
                    picklistMap.put( liste.name().get(), liste.id() );
                }
            }
            new FormFieldBuilder( body, new KVPropertyAdapter( mcase.get(), KEY_LISTE ) )
                    .setLabel( i18n.get( "termin" ) ).setToolTipText( i18n.get( "terminTip" ) )
                    .setField( new PicklistFormField( picklistMap ) )
                    .setValidator( new NotEmptyValidator() ).create();

            new FormFieldBuilder( body, new KVPropertyAdapter( mcase.get(), KEY_BEMERKUNG ) )
                    .setLabel( i18n.get( "bemerkung" ) ).setToolTipText( i18n.get( "bemerkungTip" ) )
                    .setField( new TextFormField() ).create()
                    .setLayoutData( new ColumnLayoutData( SWT.DEFAULT, 60 ) );
    
            Composite street = site.toolkit().createComposite( body );
            street.setLayout( FormLayoutFactory.defaults().create() );

            Property prop = new KVPropertyAdapter( mcase.get(), KEY_STREET );
            new FormFieldBuilder( street, prop )
                    .setLabel( i18n.get( "strasseHNr" ) )
                    .setToolTipText( i18n.get( "strasseHNrTip" ) )
                    .setField( new StringFormField() )
                    .setValidator( new NotEmptyValidator() )
                    .create()
                    .setLayoutData( FormDataFactory.filled().right( 75 ).create() );

            prop = new KVPropertyAdapter( mcase.get(), KEY_NUMBER );
            new FormFieldBuilder( street, prop )
                    .setLabel( IFormFieldLabel.NO_LABEL )
                    .setField( new StringFormField() )
                    .setValidator( new NotEmptyValidator() )
                    .create()
                    .setLayoutData( FormDataFactory.filled().left( 75 ).create() );

            Composite city = site.toolkit().createComposite( body );
            prop = new KVPropertyAdapter( mcase.get(), KEY_POSTALCODE );
            new FormFieldBuilder( city, prop )
                    .setLabel( i18n.get( "plzOrt" ) ).setToolTipText( i18n.get( "plzOrtTip" ) )
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
                        if (id != null) {
                            Entsorgungsliste liste = AzvRepository.instance().findEntity( Entsorgungsliste.class, id );
                            caseStatus.put( i18n.get( "vorgangStatusTermin" ), liste.name().get() );
                        }
                    }
//                    else if (ev.getFieldName().equals( KEY_NAME )) {
//                        caseStatus.put( i18n.get( "vorgangStatusName" ), ev.getNewValue().toString() );
//                    }
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
