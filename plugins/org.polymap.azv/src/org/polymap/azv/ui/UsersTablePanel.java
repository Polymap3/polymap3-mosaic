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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.security.UserPrincipal;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PropertyAccessEvent;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.MaxWidthConstraint;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.ui.LoginPanel;
import org.polymap.rhei.um.ui.PersonForm;
import org.polymap.rhei.um.ui.UsersTableViewer;

import org.polymap.azv.AZVPlugin;
import org.polymap.azv.Messages;
import org.polymap.mosaic.ui.MosaicUiPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UsersTablePanel
        extends DefaultPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( UsersTablePanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "azv", "users" );

    public static final IMessages       i18n = Messages.forPrefix( "UsersTablePanel" );

    /** Set by the {@link LoginPanel}. */
    private ContextProperty<UserPrincipal>  user;
    
    private IPanelToolkit               tk;

    private UsersTableViewer            viewer;

    private Composite                   formArea;
    
    private UserRepository              umrepo;


    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        if (site.getPath().size() == 1 ) {
            this.tk = site.toolkit();
            this.umrepo = UserRepository.instance();
            
            // wait for user to log in
            user.addListener( this, new EventFilter<PropertyAccessEvent>() {
                public boolean apply( PropertyAccessEvent input ) {
                    return input.getType() == PropertyAccessEvent.TYPE.SET;
                }
            });
            getSite().setTitle( "" );
            return true;
        }
        return false;
    }

    
    @Override
    public void dispose() {
        user.removeListener( this );
        super.dispose();
    }


    @EventHandler
    protected void userLoggedIn( PropertyAccessEvent ev ) {
        if (SecurityUtils.isUserInGroup( AZVPlugin.ROLE_MA )) {
            getSite().setTitle( "Nutzer/Kunden" );
        }
    }

    
    @Override
    public void createContents( Composite parent ) {
        IPanelSection contents = tk.createPanelSection( parent, null );
        contents.addConstraint( new MinWidthConstraint( 500, 1 ) )
                .addConstraint( new MaxWidthConstraint( 800, 1 ) );

        // table area
        Composite tableArea = tk.createComposite( contents.getBody(), SWT.BORDER );
        tableArea.setLayout( FormLayoutFactory.defaults().create() );
        viewer = new UsersTableViewer( tableArea, umrepo.find( User.class, null ), SWT.NONE );
        viewer.getTable().setLayoutData( FormDataFactory.filled().height( 500 ).create() );
        viewer.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent ev ) {
                for (Control child: formArea.getChildren()) {
                    child.dispose();
                }
                createUserSection( viewer.getSelectedUser() );
            }
        });
        viewer.addDoubleClickListener( new IDoubleClickListener() {
            public void doubleClick( DoubleClickEvent event ) {
                for (Control child: formArea.getChildren()) {
                    child.dispose();
                }
                createUserSection( viewer.getSelectedUser() );
            }
        });
        
        // form area
        formArea = tk.createComposite( contents.getBody(), SWT.BORDER );
        tk.createLabel( formArea, "Wählen Sie einen Nutzer in der Tabelle" );
    }

    
    protected void createUserSection( final User umuser ) {
        // person section
//        IPanelSection personSection = tk.createPanelSection( formArea, "Nutzerdaten" );
//        Composite body = personSection.getBody();
        Composite body = formArea;
        body.setLayout( ColumnLayoutFactory.defaults().spacing( 10 ).margins( 20, 20 ).create() );

        final PersonForm personForm = new PersonForm( getSite(), umuser );
        personForm.createContents( body );

        // change btn
        Button okBtn = tk.createButton( body, i18n.get( "okBtn" ), SWT.PUSH );
        okBtn.setEnabled( false );
        okBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                try {
                    personForm.submit();
                    umrepo.commitChanges();
                    viewer.refresh();
                }
                catch (Exception e) {
                    UserRepository.instance().revertChanges();
                    throw new RuntimeException( e );
                }
            }
        });
        
        // delete btn
        Button deleteBtn = tk.createButton( body, i18n.get( "deleteBtn" ), SWT.PUSH );
        deleteBtn.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_DISCARD );
        deleteBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                try {
                    umrepo.deleteUser( umuser );
                    umrepo.commitChanges();
                    viewer.refresh();
                }
                catch (Exception e) {
                    UserRepository.instance().revertChanges();
                    throw new RuntimeException( e );
                }
            }
        });
        
        formArea.getParent().layout( true );
        getSite().layout( true );
    }
    
    
    @Override
    public PanelIdentifier id() {
        return ID;
    }
    
}
