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
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.security.SecurityUtils;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.ui.PersonForm;
import org.polymap.rhei.um.ui.UsersTableViewer;

import org.polymap.azv.AZVPlugin;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.CaseStatus;
import org.polymap.mosaic.ui.casepanel.DefaultCaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseAction;

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

    private IPanelSite                      site;

    private IAppContext                     context;

    private User                            user;

    private IPanelSection                   personSection;

    
    @Override
    public boolean init( IPanelSite _site, IAppContext _context ) {
        if (SecurityUtils.isUserInGroup( AZVPlugin.ROLE_MA)) {
            String username = mcase.get() != null ? mcase.get().get( "user" ) : null;
            if (true /*&& username == null*/) {
                this.site = _site;
                this.context = _context;
                
                if (username != null) {
                    user = UserRepository.instance().findUser( username );
                }
                return true;
            }
        }
        return false;
    }
    
    
    @Override
    public void fillStatus( CaseStatus status ) {
        if (user != null) {
            status.put( "Kunde", Joiner.on( ' ' ).skipNulls().join( user.firstname().get(), user.name().get() ), 101 );
        }
    }


    @Override
    public void createContents( Composite parent ) {
        UsersTableViewer viewer = new UsersTableViewer( parent, UserRepository.instance().find( User.class, null ), SWT.NONE );
    }

    
    @Override
    public void submit() {
        PersonForm personForm = new PersonForm( site, user );
        personForm.createContents( personSection );
        personSection.getBody().setEnabled( false );
    }

    
    @Override
    public void fillContentArea( Composite parent ) {
        personSection = site.toolkit().createPanelSection( parent, "Nutzerdaten" );
        personSection.addConstraint( new PriorityConstraint( 1, 10 ) );
        
        if (user != null) {
            PersonForm personForm = new PersonForm( site, user );
            personForm.createContents( personSection );
            personSection.getBody().setEnabled( false );            
        }
        else {
            site.toolkit().createLabel( personSection.getBody(), "Noch kein Kunde zugewiesen" )
                    .setData( "no_user_yet", Boolean.TRUE );
        }
    }

}
