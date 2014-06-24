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
package org.polymap.azv.ui.nutzerregistrierung;

import static org.polymap.azv.AzvPlugin.ROLE_DIENSTBARKEITEN;
import static org.polymap.azv.AzvPlugin.ROLE_ENTSORGUNG;
import static org.polymap.azv.AzvPlugin.ROLE_HYDRANTEN;
import static org.polymap.azv.AzvPlugin.ROLE_LEITUNGSAUSKUNFT;
import static org.polymap.azv.AzvPlugin.ROLE_SCHACHTSCHEIN;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.SessionContext;

import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.azv.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class UserPermissionsSection {

    private static Log log = LogFactory.getLog( UserPermissionsSection.class );
    
    public static final IMessages   i18n = Messages.forPrefix( "UserPermissions" ); //$NON-NLS-1$

    public static final List<String> userRequestRoles = Lists.newArrayList( 
            ROLE_LEITUNGSAUSKUNFT, ROLE_SCHACHTSCHEIN, ROLE_ENTSORGUNG, ROLE_DIENSTBARKEITEN, ROLE_HYDRANTEN );


    /**
     * FIXME Dirty hack to give {@link NewUserOperationConcern} access to the requested roles. 
     */
    public static UserPermissionsSection instance() {
        SessionContext session = SessionContext.current();
        UserPermissionsSection result = session.getAttribute( UserPermissionsSection.class.getName() );
        session.setAttribute( UserPermissionsSection.class.getName(), null );
        return result;
    }
    
    
    // instance *******************************************
    
    private IPanelSite              panelSite;

    private Set<String>             initialRoles;
    
    private Set<String>             roles = new HashSet();
    
    
    public UserPermissionsSection( IPanelSite site, Set<String> userRoles ) {
        this.panelSite = site;
        this.initialRoles = userRoles;
        
        SessionContext.current().setAttribute( UserPermissionsSection.class.getName(), this );
    }

    
    public Set<String> getRoles() {
        return ImmutableSet.copyOf( roles );
    }

    
    public Composite createContent( Composite parent ) {
        IPanelToolkit tk = panelSite.toolkit();
        
        // permissions
        Composite permissions = tk.createComposite( parent );
        permissions.setLayout( new FillLayout( SWT.VERTICAL ) );

        for (final String role : userRequestRoles) {
            final Button btn = tk.createButton( permissions, role, SWT.CHECK );
            btn.setSelection( initialRoles.contains( role ) );
            btn.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent ev ) {
                    if (btn.getSelection()) {
                        roles.add( role );
                    } 
                    else {
                        roles.remove( role );
                    }
                }
            });
        }
        panelSite.layout( true );
        return permissions;
    }

}
