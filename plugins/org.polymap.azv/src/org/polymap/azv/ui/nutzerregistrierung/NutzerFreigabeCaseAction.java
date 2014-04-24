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
package org.polymap.azv.ui.nutzerregistrierung;

import static org.polymap.azv.AzvPlugin.CASE_NUTZER;
import static org.polymap.azv.AzvPlugin.MIN_COLUMN_WIDTH;
import static org.polymap.azv.AzvPlugin.ROLE_ENTSORGUNG;
import static org.polymap.azv.AzvPlugin.ROLE_HYDRANTEN;
import static org.polymap.azv.AzvPlugin.ROLE_LEITUNGSAUSKUNFT;
import static org.polymap.azv.AzvPlugin.ROLE_LEITUNGSAUSKUNFT2;
import static org.polymap.azv.AzvPlugin.ROLE_MA;
import static org.polymap.azv.AzvPlugin.ROLE_BL;
import static org.polymap.azv.AzvPlugin.ROLE_SCHACHTSCHEIN;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.IAction;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.toolkit.ConstraintData;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.NeighborhoodConstraint;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.batik.toolkit.NeighborhoodConstraint.Neighborhood;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.email.EmailService;
import org.polymap.rhei.um.ui.PersonForm;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicCaseEvent;
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
public class NutzerFreigabeCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( NutzerFreigabeCaseAction.class );

    private static final FastDateFormat df = AzvPlugin.df;
    
    public static final IMessages       i18n = Messages.forPrefix( "NutzerFreigabe" ); //$NON-NLS-1$

    
    private ICaseActionSite                 site;
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>    mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2> repo;
    
    private UserRepository                  um;
    
    private User                            user;

    private String username;

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        IMosaicCase mycase = mcase.get();
        log.info( "CASE:" + mycase ); //$NON-NLS-1$
        if (mycase != null && mycase.getNatures().contains( CASE_NUTZER )) {
            // user data
            username = mycase.get( "user" ); //$NON-NLS-1$
            um = UserRepository.instance();
            user = um.findUser( username );
            
            // open action
            if (SecurityUtils.isUserInGroup( ROLE_MA )) {
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
        status.put( i18n.get( "status" ), Joiner.on( " " ).skipNulls().join( user.salutation().get(), user.firstname().get(), user.name().get() ), 98  ); //$NON-NLS-1$ //$NON-NLS-2$
    }


    @Override
    public void fillContentArea( Composite parent ) {
        IPanelSection personSection = site.toolkit().createPanelSection( parent, i18n.get( "title" ) );
        personSection.addConstraint( new PriorityConstraint( 1 ), MIN_COLUMN_WIDTH );
        personSection.getBody().setLayout( new FillLayout() );
        
        User umuser = um.findUser( username );
        if (umuser != null) {
            PersonForm personForm = new PersonForm( site.getPanelSite(), umuser );
            personForm.createContents( personSection );
            personForm.getBody().setLayout( ColumnLayoutFactory.defaults().spacing( 3 ).margins( 8 ).create() );
            personForm.setEnabled( false );            
        }
        else {
            site.toolkit().createLabel( personSection.getBody(), i18n.get( "nochKeinKunde" ) )
                    .setData( "no_user_yet", Boolean.TRUE ); //$NON-NLS-1$
        }
        
        // normaler Nutzer
        if (!SecurityUtils.isUserInGroup( ROLE_MA )) {
            IPanelSection txtSection = site.toolkit().createPanelSection( parent, i18n.get( "antragTitle" ) );
            txtSection.addConstraint( new PriorityConstraint( 10 ) );
            //txtSection.getBody().setLayout( new FillLayout() );
            
            if (mcase.get().getStatus().equals( IMosaicCaseEvent.TYPE_CLOSED )) {
                String roles = mcase.get().get( "roles" ); //$NON-NLS-1$
                site.toolkit().createFlowText( txtSection.getBody(), 
                        i18n.get( "freigeschalteteFunktionen", roles ) );                
            }
            else {
                site.toolkit().createFlowText( txtSection.getBody(), i18n.get( "userTxt" ) ); //$NON-NLS-1$
            }
        }
    }


    @Override
    public void fillAction( IAction action ) {
        // disable for ordinary users
        if (!SecurityUtils.isUserInGroup( ROLE_MA )) {
            action.setText( null );
            action.setImageDescriptor( null );
            action.setEnabled( false );
        }
    }


    @Override
    public void createContents( Composite parent ) {
        // welcome
        Composite welcome = site.toolkit().createComposite( parent );
        welcome.setLayoutData( new ConstraintData( AzvPlugin.MIN_COLUMN_WIDTH, new PriorityConstraint( 100 ) ) );
        site.toolkit().createFlowText( welcome, i18n.get( "welcomeTxt", UsersTablePanel.ID ) ); //$NON-NLS-1$

        // beantragte Rechte im user initialisieren
        String requestedRoles = mcase.get().get( "roles" );
        if (requestedRoles != null) {
            for (String role : Splitter.on( ',' ).split( requestedRoles ) ) {
                um.asignGroup( user, role );
            }
        }
        Set<String> roles = new HashSet( um.groupsOf( user ) );
        
        // MA, BL Rechte
        Composite right = site.toolkit().createComposite( parent, SWT.BORDER );
        right.setLayoutData( new ConstraintData( new PriorityConstraint( 10 ) ) );
        right.setLayout( ColumnLayoutFactory.defaults().margins( 20, 10 ).spacing( 5 ).create() );
        
        List<String> allRoles = Lists.newArrayList( ROLE_MA, ROLE_BL );
        for (final String role : allRoles) {
            createBtn( right, role ).setSelection( roles.contains( role ) );
        }

        // normale Rechte
        Composite left = site.toolkit().createComposite( parent, SWT.BORDER );
        left.setLayoutData( new ConstraintData( new NeighborhoodConstraint( right, Neighborhood.BOTTOM, 10 ) ) );
        left.setLayout( ColumnLayoutFactory.defaults().margins( 20, 10 ).spacing( 5 ).columns( 1, 1 ).create() );

        allRoles = Lists.newArrayList( 
                ROLE_LEITUNGSAUSKUNFT, ROLE_LEITUNGSAUSKUNFT2, ROLE_SCHACHTSCHEIN,
                ROLE_ENTSORGUNG, ROLE_HYDRANTEN );
        for (final String role : allRoles) {
            createBtn( left, role ).setSelection( roles.contains( role ) );
        }
        
        site.createSubmit( left, i18n.get( "ok" ) );
    }

    
    protected Button createBtn( Composite parent, final String role ) {
        final Button btn = site.toolkit().createButton( parent, role, SWT.CHECK );
        btn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                if (btn.getSelection()) {
                    um.asignGroup( user, role );
                } else {
                    um.resignGroup( user, role );
                }
            }
        });
        return btn;
    }

    
    @Override
    public void submit() throws Exception {        
        um.commitChanges();
        
        List<String> roles = um.groupsOf( user );

        MosaicRepository2 mosaic = repo.get();
        mcase.get().put( "roles", Iterables.toString( roles ) ); //$NON-NLS-1$
        mosaic.newCaseEvent( mcase.get(), AzvPlugin.EVENT_TYPE_FREIGABE, i18n.get( "eventBeschreibung", um.groupsOf( user ) ), AzvPlugin.EVENT_TYPE_FREIGABE );
        mosaic.closeCase( mcase.get(), AzvPlugin.EVENT_TYPE_FREIGABE, i18n.get( "eventBeschreibung", um.groupsOf( user ) ) );
        mosaic.commitChanges();
        
        String salu = user.salutation().get() != null ? user.salutation().get() : ""; //$NON-NLS-1$
        String header = "Sehr geehrte" + (salu.equalsIgnoreCase( "Herr" ) ? "r " : " ") + salu + " " + user.name().get(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        Email email = new SimpleEmail();
        email.setCharset( "ISO-8859-1" ); //$NON-NLS-1$
        email.addTo( user.email().get() )
                .setSubject( i18n.get( "emailSubject") ) //$NON-NLS-1$
                .setMsg( i18n.get( "email", header, roles ) ); //$NON-NLS-1$
        EmailService.instance().send( email );
        
        site.getPanelSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, i18n.get( "okTxt" ) ) ); //$NON-NLS-1$
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                site.getContext().closePanel( site.getPanelSite().getPath() );
            }
        });
    }


    @Override
    public void discard() {
        um.revertChanges();
        user = um.findUser( username );
    }
    
}
