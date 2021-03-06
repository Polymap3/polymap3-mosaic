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

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.SelectionAdapter;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.toolkit.ConstraintData;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.ui.PersonForm;
import org.polymap.rhei.um.ui.RegisterPanel;
import org.polymap.rhei.um.ui.UsersTableSearchField;
import org.polymap.rhei.um.ui.UsersTableViewer;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.model.AzvVorgang;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.MosaicCaseEvents;
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
public class NutzerAnVorgangCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( NutzerAnVorgangCaseAction.class );

    public static final IMessages       i18n = Messages.forPrefix( "NutzerAnVorgang" ); //$NON-NLS-1$

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>    mcase;
    
    private AzvVorgang                      vorgang;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2>  repo;
    
    private ICaseActionSite                 site;

    private IPanelSection                   personSection;

    private CaseStatus                      caseStatus;

    private UsersTableViewer                viewer;

    private IAction                         caseAction;

    
    @Override
    public boolean init( ICaseActionSite _site ) {        
        if (mcase.get() != null && repo.get() != null) {
            this.site = _site;
            this.vorgang = mcase.get().as( AzvVorgang.class );
            
            // Mitarbeiter (und Admin)
            if (SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA )) {
                if (mcase.get().getNatures().contains( AzvPlugin.CASE_NUTZER )) {
                    return false;
                }

                if (vorgang.username.get() == null) {
                    // open action
                    Polymap.getSessionDisplay().asyncExec( new Runnable() {
                        public void run() {
                            site.activateCaseAction( site.getActionId() );
                            site.setSubmitEnabled( false );
                        }
                    });
                }
                return true;
            }
            // normale Nutzer
            else {
                // Status nur zeigen wenn nicht "NEU"
                site.setShowStatus( vorgang.azvStatus() != null );
            }
        }
        return false;
    }
    
    
    @Override
    public void fillStatus( CaseStatus status ) {
        this.caseStatus = status;
        User user = vorgang.user();
        if (user != null) {
            status.put( i18n.get( "vorgangStatusKunde" ), Joiner.on( ' ' ).skipNulls().join( user.firstname().get(), user.name().get() ), 101 );
        }
    }


    @Override
    public void fillAction( IAction action ) {
        this.caseAction = action;
        // nach Beantragung keine Änderung mehr
        if (MosaicCaseEvents.contains( mcase.get(), AzvPlugin.EVENT_TYPE_BEANTRAGT )) {
            action.setText( null );
            action.setImageDescriptor( null );
            action.setEnabled( false );
        }
    }


    @Override
    public void createContents( Composite parent ) {
        Composite welcome = site.toolkit().createComposite( parent );
        welcome.setLayoutData( new ConstraintData( AzvPlugin.MIN_COLUMN_WIDTH, new PriorityConstraint( 100 ) ) );
        site.toolkit().createFlowText( welcome, i18n.get( "welcomeMsg", RegisterPanel.ID ) ); //$NON-NLS-1$
        
        Composite formContainer = site.toolkit().createComposite( parent );
        formContainer.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
        
        viewer = new UsersTableViewer( formContainer, UserRepository.instance().find( User.class, null ), SWT.NONE );
        viewer.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent ev ) {
                site.setSubmitEnabled( !ev.getSelection().isEmpty() );
            }
        });
        viewer.addDoubleClickListener( new IDoubleClickListener() {
            public void doubleClick( DoubleClickEvent event ) {
            }
        });

        UsersTableSearchField searchField = new UsersTableSearchField( viewer, formContainer, 
                Arrays.asList( "name", "username", "firstname", "company", "street", "city" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        
        Button submitBtn = site.createSubmit( formContainer, i18n.get( "submit" ) );

        // layout
        searchField.getControl().setLayoutData( FormDataFactory.filled().bottom( -1 ).create() );
        viewer.getControl().setLayoutData( FormDataFactory.filled().top( searchField.getControl() ).bottom( submitBtn ).height( 250 ).create() );
        submitBtn.setLayoutData( FormDataFactory.filled().top( -1 ).height( 28 ).create() );
    }

    
    @Override
    public void submit() throws Exception {
        User user = new SelectionAdapter( viewer.getSelection() ).first( User.class );
        vorgang.username.set( user.username().get() );
        repo.get().commitChanges();
        
        if (caseAction != null) {
            caseAction.setEnabled( false );
        }
        
        caseStatus.put( i18n.get( "vorgangStatusKunde" ), Joiner.on( ' ' ).skipNulls().join( user.firstname().get(), user.name().get() ), 101 );

        for (Control child : personSection.getBody().getChildren()) {
            child.dispose();
        }
        PersonForm personForm = new PersonForm( site.getPanelSite(), user );
        personForm.createContents( personSection );
        personForm.getBody().setLayout( ColumnLayoutFactory.defaults().spacing( 3 ).margins( 8 ).create() );
        personForm.setEnabled( false );
    }

    
    @Override
    public void discard() {
        repo.get().rollbackChanges();
        
//        // do not left 'empty' CasePanel after close button
//        if (mcase.get().getName().isEmpty()) {
//            site.getPanelSite().setStatus( new Status( IStatus.INFO, AzvPlugin.ID, "Es wurden keine Kunde ausgewählt.\nDer Vorgang wurde daher geschlossen." ) );
//            Polymap.getSessionDisplay().asyncExec( new Runnable() {
//                public void run() {
//                    site.getContext().closePanel( site.getPanelSite().getPath() );
//                }
//            });
//        }
    }


    @Override
    public void fillContentArea( Composite parent ) {
        personSection = site.toolkit().createPanelSection( parent, i18n.get( "sectionTitle" ) );
        personSection.addConstraint( new PriorityConstraint( 1 ), AzvPlugin.MIN_COLUMN_WIDTH );
        personSection.getBody().setLayout( new FillLayout() );
        
        User user = vorgang.user();
        if (user != null) {
            PersonForm personForm = new PersonForm( site.getPanelSite(), user );
            personForm.createContents( personSection );
            personForm.getBody().setLayout( ColumnLayoutFactory.defaults().spacing( 3 ).margins( 8 ).create() );
            personForm.setEnabled( false );
        }
        else {
            Label l = site.toolkit().createLabel( personSection.getBody(), i18n.get( "keinKunde" ) );
            l.setData( "no_user_yet", Boolean.TRUE ); //$NON-NLS-1$
            l.setForeground( MosaicUiPlugin.COLOR_RED.get() );
        }
    }

}
