/* 
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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

import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.unitofwork.NoSuchEntityException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.UserPrincipal;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.atlas.ContextProperty;
import org.polymap.atlas.DefaultPanel;
import org.polymap.atlas.IAppContext;
import org.polymap.atlas.IAtlasToolkit;
import org.polymap.atlas.IPanelSite;
import org.polymap.atlas.PanelIdentifier;
import org.polymap.azv.AZVPlugin;
import org.polymap.azv.model.AzvRepository;
import org.polymap.azv.model.Nutzer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LoginPanel
        extends DefaultPanel {

    private static Log log = LogFactory.getLog( LoginPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "login" );

    private ContextProperty<Nutzer>        nutzer;

    private ContextProperty<UserPrincipal> user;

    private IAtlasToolkit                  tk;

    private Text                           nameText;

    private Text                           pwdText;

    private Button                         loginBtn;

    
    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        this.tk = site.toolkit();
        
        //assert nutzer.get() == null;
        
        // open only if directly called
        return false;
    }

    
    @Override
    public PanelIdentifier id() {
        return ID;
    }

    
    @Override
    public void createContents( Composite panelBody ) {
        getSite().setTitle( "Login" );
        panelBody.setLayout( FormLayoutFactory.defaults().margins( DEFAULTS_SPACING ).create() );

        Composite contents = tk.createComposite( panelBody );
        contents.setLayoutData( FormDataFactory.offset( 0 ).top( 0, 50 ).left( 30 ).right( 70 ).width( 500 ).create() );
        contents.setLayout( FormLayoutFactory.defaults().spacing( 10 ).create() );
        
        Label l1 = tk.createLabel( contents, "EMail" );
        l1.setLayoutData( FormDataFactory.offset( 0 ).left( 0 ).top( 0 ).right( 0, 120 ).create() );
        nameText = tk.createText( contents, "", SWT.BORDER );
        nameText.setLayoutData( FormDataFactory.offset( 0 ).left( l1 ).top( 0 ).right( 100 ).create() );
        nameText.setFocus();
        nameText.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent ev ) {
                loginBtn.setEnabled( nameText.getText().length() > 0 && pwdText.getText().length() > 0 );
            }
        });

        Label l2 = tk.createLabel( contents, "Passwort" );
        l2.setLayoutData( FormDataFactory.offset( 0 ).left( 0 ).top( nameText ).right( 0, 120 ).create() );
        pwdText = tk.createText( contents, "", SWT.BORDER, SWT.PASSWORD );
        pwdText.setLayoutData( FormDataFactory.offset( 0 ).left( l2 ).top( nameText ).right( 100 ).create() );
        pwdText.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent ev ) {
                loginBtn.setEnabled( nameText.getText().length() > 0 && pwdText.getText().length() > 0 );
            }
        });
        
        loginBtn = tk.createButton( contents, "Anmelden" );
        loginBtn.setLayoutData( FormDataFactory.offset( 0 ).top( pwdText ).right( 100 ).create() );
        loginBtn.setEnabled( false );
        loginBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                login( nameText.getText(), pwdText.getText() );
            }
        });

        // XXX fake login
        nameText.setText( "admin" );
        pwdText.setText( "login" );
    }
    
    
    protected void login( final String name, final String passwd ) {
        // user
        try {
            Polymap.instance().login( name, passwd );
            user.set( (UserPrincipal)Polymap.instance().getUser() );
        }
        catch (LoginException e) {
            log.info( "Login: no user found for name: " + name );
        }
        
        // nutzer
        try {
            Nutzer found = AzvRepository.instance().findEntity( Nutzer.class, name );
            // FIXME check password
            nutzer.set( found );
            user.set( new UserPrincipal( name ) {
                @Override
                public String getPassword() {
                    return passwd;
                }
            });
        }
        catch (NoSuchEntityException e) {
            log.info( "Login: no Nutzer found for name: " + name );
        }
        
        // check and return
        if (user.get() != null || nutzer.get() != null) {
            getContext().closePanel();
        }
        else {
            getSite().setStatus( new Status( IStatus.WARNING, AZVPlugin.ID, "Nutzername oder Passwort sind nicht korrekt." ) );
        }
    }
    
}
