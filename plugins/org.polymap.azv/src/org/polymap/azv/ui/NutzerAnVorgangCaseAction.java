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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.ui.SelectionAdapter;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.ui.PersonForm;
import org.polymap.rhei.um.ui.UsersTableViewer;

import org.polymap.azv.AZVPlugin;
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
public class NutzerAnVorgangCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( NutzerAnVorgangCaseAction.class );

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>    mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2>  repo;
    
    private ICaseActionSite                 site;

    private User                            umuser;

    private IPanelSection                   personSection;

    private CaseStatus                      caseStatus;

    private UsersTableViewer                viewer;

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        if (mcase.get() != null && repo.get() != null
                && SecurityUtils.isUserInGroup( AZVPlugin.ROLE_MA )
                && !mcase.get().getNatures().contains( AZVPlugin.CASE_NUTZER )) {
            
            this.site = _site;
            String username = mcase.get().get( "user" );
            if (username != null) {
                umuser = UserRepository.instance().findUser( username );
            }
            else {
                // open action
                Polymap.getSessionDisplay().asyncExec( new Runnable() {
                    public void run() {
                        site.activateCaseAction( site.getActionId() );
                        site.setValid( false );
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
        if (umuser != null) {
            status.put( "Kunde", Joiner.on( ' ' ).skipNulls().join( umuser.firstname().get(), umuser.name().get() ), 101 );
        }
    }


    @Override
    public void createContents( Composite parent ) {
        FillLayout layout = (FillLayout)parent.getLayout();
        layout.marginWidth = layout.marginHeight = layout.marginWidth / 2;
        
        viewer = new UsersTableViewer( parent, UserRepository.instance().find( User.class, null ), SWT.NONE );
        viewer.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent ev ) {
                site.setValid( !ev.getSelection().isEmpty() );
            }
        });
        viewer.addDoubleClickListener( new IDoubleClickListener() {
            public void doubleClick( DoubleClickEvent event ) {
            }
        });
    }

    
    @Override
    public void submit() throws Exception {
        umuser = new SelectionAdapter( viewer.getSelection() ).first( User.class );
        String username = umuser.username().get();
        mcase.get().put( "user", username );
        repo.get().commitChanges();
        
        umuser = UserRepository.instance().findUser( username );
        caseStatus.put( "Kunde", Joiner.on( ' ' ).skipNulls().join( umuser.firstname().get(), umuser.name().get() ), 101 );

        for (Control child : personSection.getBody().getChildren()) {
            child.dispose();
        }
        PersonForm personForm = new PersonForm( site.getPanelSite(), umuser );
        personForm.createContents( personSection );
        personSection.getBody().setEnabled( false );
    }

    
    @Override
    public void fillContentArea( Composite parent ) {
        personSection = site.toolkit().createPanelSection( parent, "Nutzerdaten" );
        personSection.addConstraint( new PriorityConstraint( 1, 10 ) );
        personSection.getBody().setLayout( new FillLayout() );
        
        if (umuser != null) {
            PersonForm personForm = new PersonForm( site.getPanelSite(), umuser );
            personForm.createContents( personSection );
            personSection.getBody().setEnabled( false );            
        }
        else {
            site.toolkit().createLabel( personSection.getBody(), "Noch kein Kunde zugewiesen" )
                    .setData( "no_user_yet", Boolean.TRUE );
        }
    }

}
