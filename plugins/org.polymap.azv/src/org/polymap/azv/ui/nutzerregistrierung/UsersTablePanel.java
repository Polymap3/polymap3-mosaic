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

import static org.polymap.azv.AzvPlugin.ROLE_DIENSTBARKEITEN;
import static org.polymap.azv.AzvPlugin.ROLE_ENTSORGUNG;
import static org.polymap.azv.AzvPlugin.ROLE_HYDRANTEN;
import static org.polymap.azv.AzvPlugin.ROLE_LEITUNGSAUSKUNFT;
import static org.polymap.azv.AzvPlugin.ROLE_LEITUNGSAUSKUNFT2;
import static org.polymap.azv.AzvPlugin.ROLE_MA;
import static org.polymap.azv.AzvPlugin.ROLE_BL;
import static org.polymap.azv.AzvPlugin.ROLE_SCHACHTSCHEIN;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.security.UserPrincipal;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PropertyAccessEvent;
import org.polymap.rhei.batik.toolkit.ConstraintData;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.ui.LoginPanel;
import org.polymap.rhei.um.ui.PersonForm;
import org.polymap.rhei.um.ui.UsersTableSearchField;
import org.polymap.rhei.um.ui.UsersTableViewer;

import org.polymap.azv.AzvPlugin;
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

    public static final PanelIdentifier ID = new PanelIdentifier( "azv", "users" ); //$NON-NLS-1$ //$NON-NLS-2$

    public static final IMessages       i18n = Messages.forPrefix( "UsersTablePanel" ); //$NON-NLS-1$

    /** Set by the {@link LoginPanel}. */
    @Context(scope="org.polymap.azv.ui")
    private ContextProperty<UserPrincipal>  user;
    
    private IPanelToolkit               tk;

    private UsersTableViewer            viewer;

    private Composite                   formArea;
    
    private UserRepository              umrepo;


    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        this.tk = site.toolkit();
        this.umrepo = UserRepository.instance();

        if (site.getPath().size() == 1 ) {
            // wait for user to log in
            user.addListener( this, new EventFilter<PropertyAccessEvent>() {
                public boolean apply( PropertyAccessEvent input ) {
                    return input.getType() == PropertyAccessEvent.TYPE.SET;
                }
            });
            getSite().setTitle( null );
            return true;
        }
        return false;
    }

    
    @Override
    public void dispose() {
        user.removeListener( this );
        super.dispose();
    }


    @EventHandler(display=true)
    protected void userLoggedIn( PropertyAccessEvent ev ) {
        if (SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA )) {
            getSite().setTitle( i18n.get( "title" ) );
            getSite().setIcon( BatikPlugin.instance().imageForName( "resources/icons/users-filter.png" ) ); //$NON-NLS-1$
        }
    }

    
    @Override
    public void createContents( Composite parent ) {
        Composite contents = parent;

        // table area
        Composite tableArea = tk.createComposite( contents, SWT.BORDER );
        tableArea.setLayoutData( new ConstraintData( 
                new PriorityConstraint( 100 ), AzvPlugin.MIN_COLUMN_WIDTH ) );
        tableArea.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
        
        viewer = new UsersTableViewer( tableArea, umrepo.find( User.class, null ), SWT.NONE );
        viewer.getTable().setLayoutData( FormDataFactory.filled().height( 500 ).create() );
        viewer.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent ev ) {
                updateUserSection( viewer.getSelectedUser() );
            }
        });
        
        UsersTableSearchField searchField = new UsersTableSearchField( viewer, tableArea, 
                Arrays.asList( "name", "username", "firstname", "company", "street", "city" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        FormDataFactory.filled().bottom( -1 ).applyTo( searchField.getControl() );
        FormDataFactory.filled().top( searchField.getControl() ).applyTo( viewer.getControl() );
        
        // form area
        formArea = tk.createComposite( contents );
        tk.createLabel( formArea, i18n.get( "nutzerWaehlen" ) );
    }

    
    protected void updateUserSection( final User umuser ) {
        formArea.setLayout( ColumnLayoutFactory.defaults().spacing( 5 ).margins( 20, 20 ).create() );
        for (Control child : formArea.getChildren()) {
            child.dispose();
        }
        if (umuser == null) {
            return;
        }
        final PersonForm personForm = new PersonForm( getSite(), umuser );
        personForm.createContents( formArea );

        // permissions
        Composite permissions = tk.createComposite( formArea );
        permissions.setLayout( new FillLayout( SWT.VERTICAL ) );
        List<String> roles = Lists.newArrayList( 
                ROLE_LEITUNGSAUSKUNFT, ROLE_LEITUNGSAUSKUNFT2, ROLE_SCHACHTSCHEIN,
                ROLE_ENTSORGUNG, ROLE_DIENSTBARKEITEN, ROLE_HYDRANTEN, ROLE_MA, ROLE_BL
                );
        Set<String> groups = new HashSet( umrepo.groupsOf( umuser ) );
        final Set<String> asignedRoles = new HashSet();
        final Set<String> resignedRoles = new HashSet();
        for (final String role : roles) {
            final Button btn = tk.createButton( permissions, role, SWT.CHECK );
            btn.setSelection( groups.contains( role ) );
            btn.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent ev ) {
                    if (btn.getSelection()) {
                        umrepo.asignGroup( umuser, role );
                        asignedRoles.add( role );
                    } 
                    else {
                        umrepo.resignGroup( umuser, role );
                        resignedRoles.add( role );
                    }
                }
            });
        }
        
        // change btn
        Button okBtn = tk.createButton( formArea, i18n.get( "okBtn" ), SWT.PUSH ); //$NON-NLS-1$
        okBtn.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_SUBMIT );
        //okBtn.setEnabled( false );
        okBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                try {
                    personForm.submit();
                    umrepo.commitChanges();
                    viewer.update( umuser, null );
                    
                    if (!asignedRoles.isEmpty()) {
                        IMessages emailI18n = Messages.forPrefix( "NutzerFreigabe" ); //$NON-NLS-1$
                        AzvPlugin.sendEmail( umuser, emailI18n, asignedRoles.toString() );
                    }
                    
                    getSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, i18n.get( "okStatus" ) ) );
                }
                catch (Exception e) {
                    UserRepository.instance().revertChanges();
                    getSite().setStatus( new Status( IStatus.ERROR, AzvPlugin.ID, i18n.get( "fehlerStatus" ) ) );
                    throw new RuntimeException( e );
                }
            }
        });
        
        // delete btn
        Button deleteBtn = tk.createButton( formArea, i18n.get( "deleteBtn" ), SWT.PUSH ); //$NON-NLS-1$
        deleteBtn.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_DISCARD );
        deleteBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                try {
                    for (Control child : formArea.getChildren()) {
                        child.dispose();
                    }
                    umrepo.deleteUser( umuser );
                    umrepo.commitChanges();

                    viewer.reload();
                    getSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, i18n.get( "geloeschtStatus" ) ) );
                }
                catch (Exception e) {
                    UserRepository.instance().revertChanges();
                    getSite().setStatus( new Status( IStatus.ERROR, AzvPlugin.ID, i18n.get( "geloeschtFehlerStatus" ) ) );
                    throw new RuntimeException( e );
                }
            }
        });
        formArea.layout( true );
        formArea.getParent().layout( true );
        getSite().layout( true );
    }
    
    
    @Override
    public PanelIdentifier id() {
        return ID;
    }
    
}
