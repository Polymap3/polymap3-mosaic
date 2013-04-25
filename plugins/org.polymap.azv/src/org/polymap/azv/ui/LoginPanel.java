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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.polymap.core.security.UserPrincipal;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.atlas.DefaultPanel;
import org.polymap.atlas.IAppContext;
import org.polymap.atlas.IAppContext.ContextSupplier;
import org.polymap.atlas.IAtlasToolkit;
import org.polymap.atlas.IPanelSite;
import org.polymap.atlas.PanelIdentifier;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LoginPanel
        extends DefaultPanel {

    private static Log log = LogFactory.getLog( LoginPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "login" );

    private IAtlasToolkit           tk;

    private Text                    nameText;

    private Text                    pwdText;

    private Button loginBtn;

    
    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        this.tk = site.toolkit();
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
    }
    
    
    protected void login( String name, String passwd ) {
        final UserPrincipal user = new UserPrincipal( name ) {
            @Override
            public String getPassword() {
                return "fake";
            }
        };
        getContext().addSupplier( new ContextSupplier() {
            public Object get( Object consumer, String key ) {
                return key.equals( "user" ) ? user : null;
            }
        });
        getContext().closePanel();
    }
    
}
