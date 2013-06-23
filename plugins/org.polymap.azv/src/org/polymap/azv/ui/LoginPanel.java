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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.UserPrincipal;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.data.entityfeature.PlainValuePropertyAdapter;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.StringFormField.Style;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.atlas.ContextProperty;
import org.polymap.atlas.DefaultPanel;
import org.polymap.atlas.IAppContext;
import org.polymap.atlas.IPanelSite;
import org.polymap.atlas.PanelIdentifier;
import org.polymap.atlas.app.FormContainer;
import org.polymap.atlas.toolkit.IPanelSection;
import org.polymap.atlas.toolkit.IPanelToolkit;
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

    private IPanelToolkit                  tk;

    
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
        
        IPanelSection section = tk.createPanelSection( panelBody, "Anmelden" );
        
        new LoginForm( nutzer, user ) {
            protected void login( String name, String passwd ) {
                super.login( name, passwd );

                if (user.get() != null || nutzer.get() != null) {
                    getContext().closePanel();
                }
                else {
                    getSite().setStatus( new Status( IStatus.WARNING, AZVPlugin.ID, "Nutzername oder Passwort sind nicht korrekt." ) );
                }
            }
            
        }.createContents( section );
    }
    
    
    
    /**
     * 
     */
    public static class LoginForm
            extends FormContainer {

        protected ContextProperty<Nutzer>        nutzer;

        protected ContextProperty<UserPrincipal> user;

        protected Button                         loginBtn;

        protected String                         username = "admin", password = "login";

        private IFormEditorPageSite              formSite;
        
        
        public LoginForm( ContextProperty<Nutzer> nutzer, ContextProperty<UserPrincipal> user ) {
            this.nutzer = nutzer;
            this.user = user;
        }


        @Override
        public void createFormContent( IFormEditorPageSite site ) {
            formSite = site;
            Composite body = site.getPageBody();
            // username
            new FormFieldBuilder( body, new PlainValuePropertyAdapter( "username", username ) )
                    .setField( new StringFormField() ).create().setFocus();
            // password
            new FormFieldBuilder( body, new PlainValuePropertyAdapter( "password", password ) )
                    .setField( new StringFormField( Style.PASSWORD ) ).create();
            // btn
            loginBtn = site.getToolkit().createButton( body, "Anmelden", SWT.PUSH );
            loginBtn.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent ev ) {
                    login( username, password );
                }
            });
            
            // listener
            site.addFieldListener( new IFormFieldListener() {
                public void fieldChange( FormFieldEvent ev ) {
                    if (ev.getFieldName().equals( "username" ) ) {
                        username = ev.getNewValue();
                    }
                    else if (ev.getFieldName().equals( "password" ) ) {
                        password = ev.getNewValue();
                    }
                    else {
                        throw new IllegalStateException( "Unknown form field: " + ev.getFieldName() );
                    }
                    if (loginBtn != null) {
                        loginBtn.setEnabled( username.length() > 0 && password.length() > 0 );
                    }
                }
            });
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
            
            // check
            if (user.get() != null || nutzer.get() != null) {
                formSite.clearFields();
                loginBtn.dispose();
                
                formSite.getToolkit().createLabel( formSite.getPageBody(), user.get().getName() );
            }
        }
    }        
        
}
