/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.security.UserPrincipal;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.um.UmPlugin;
import org.polymap.rhei.um.ui.LoginPanel.LoginForm;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AzvLoginPanel
        extends DefaultPanel {

    public static final PanelIdentifier ID = new PanelIdentifier( "azvlogin" ); //$NON-NLS-1$

    public static final IMessages       i18n = Messages.forPrefix( "LoginPanel" ); //$NON-NLS-1$

    private ContextProperty<UserPrincipal> user;

    private IPanelToolkit                  tk;

    
    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        this.tk = site.toolkit();
        // open only if called directly
        return false;
    }

    
    @Override
    public PanelIdentifier id() {
        return ID;
    }

    
    @Override
    public void createContents( Composite parent ) {
        getSite().setTitle( i18n.get( "title" ) );
        getSite().setIcon( BatikPlugin.instance().imageForName( "resources/icons/user.png" ) );
        
        // welcome
        IPanelSection welcomeSection = tk.createPanelSection( parent, i18n.get( "sectionTitle" ) );
        welcomeSection.addConstraint( new PriorityConstraint( 100 ), AzvPlugin.MIN_COLUMN_WIDTH );
        welcomeSection.getBody().setLayout( new FillLayout() );
        tk.createFlowText( welcomeSection.getBody(), i18n.get( "welcomeText" ) ); //$NON-NLS-1$

        // login
        IPanelSection section = tk.createPanelSection( parent, null );
        section.addConstraint( new PriorityConstraint( 10 ), AzvPlugin.MIN_COLUMN_WIDTH );
        
        LoginForm loginForm = new LoginForm( getContext(), getSite(), user ) {
            protected boolean login( String name, String passwd ) {
                if (super.login( name, passwd )) {
                    getSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, i18n.get( "statusOk" ) ) );
                    getContext().closePanel( getSite().getPath() );
                    return true;
                }
                else {
                    getSite().setStatus( new Status( IStatus.WARNING, UmPlugin.ID, i18n.get( "loginNichtKorrekt" ) ) );
                    return false;
                }
            }
            
        };        
        loginForm.setShowRegisterLink( true );
        loginForm.setShowStoreCheck( true );
        loginForm.setShowLostLink( true );
        loginForm.createContents( section );
    }
    
}