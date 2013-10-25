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

import static org.polymap.azv.AZVPlugin.CASE_NUTZER;
import static org.polymap.azv.AZVPlugin.ROLE_DIENSTBARKEITEN;
import static org.polymap.azv.AZVPlugin.ROLE_ENTSORGUNG;
import static org.polymap.azv.AZVPlugin.ROLE_HYDRANTEN;
import static org.polymap.azv.AZVPlugin.ROLE_LEITUNGSAUSKUNFT;
import static org.polymap.azv.AZVPlugin.ROLE_SCHACHTSCHEIN;
import static org.polymap.azv.AZVPlugin.ROLE_WASSERQUALITAET;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.IAction;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;

import org.polymap.azv.AZVPlugin;
import org.polymap.azv.Messages;
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
public class NutzerFreigabeCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( NutzerFreigabeCaseAction.class );

    private static final FastDateFormat df = FastDateFormat.getInstance( "dd.MM.yyyy" );
    
    public static final IMessages       i18n = Messages.forPrefix( "NutzerFreigabe" );

    
    private ICaseActionSite                 site;
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>    mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2> repo;
    
    private User                            user;

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        IMosaicCase mycase = mcase.get();
        log.info( "CASE:" + mycase );
        if (mycase != null && mycase.getNatures().contains( CASE_NUTZER )) {
            // user data
            String username = mycase.get( "user" );
            user = UserRepository.instance().findUser( username );
            return true;
        }
        return false;
    }


    @Override
    public void fillStatus( CaseStatus status ) {
        status.put( i18n.get( "status" ), Joiner.on( " " ).skipNulls().join( user.salutation().get(), user.firstname().get(), user.name().get() ), 98  );
    }


    @Override
    public void fillAction( IAction action ) {
        // keep default settings
    }


    @Override
    public void createContents( Composite parent ) {
        final UserRepository um = UserRepository.instance();
        
        Composite root = site.toolkit().createComposite( parent );
        root.setLayout( ColumnLayoutFactory.defaults().margins( 20, 0 ).spacing( 5 ).columns( 2, 2 ).create() );
        
        // left section
        Composite left = site.toolkit().createComposite( root );
        left.setLayout( ColumnLayoutFactory.defaults().margins( 20, 0 ).spacing( 5 ).columns( 1, 1 ).create() );
        Set<String> groups = new HashSet( um.groupsOf( user ) );

        List<String> roles = Lists.newArrayList( 
                ROLE_SCHACHTSCHEIN, ROLE_LEITUNGSAUSKUNFT, ROLE_DIENSTBARKEITEN,
                ROLE_ENTSORGUNG, ROLE_HYDRANTEN, ROLE_WASSERQUALITAET
                );
        for (final String role : roles) {
            final Button btn = site.toolkit().createButton( left, role, SWT.CHECK );
            btn.setSelection( groups.contains( role ) );
            btn.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent ev ) {
                    if (btn.getSelection()) {
                        um.asignGroup( user, role );
                    } else {
                        um.resignGroup( user, role );
                    }
                }
            });
        }

        // right section
        Composite right = site.toolkit().createComposite( root );
        right.setLayout( ColumnLayoutFactory.defaults().margins( 20, 0 ).spacing( 5 ).create() );
        Button btn = site.toolkit().createButton( right, "Interner Sachbearbeiter", SWT.CHECK );
        btn.setSelection( groups.contains( AZVPlugin.ROLE_MA ) );
        btn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                log.info( "ev: " + ev );
                um.asignGroup( user, AZVPlugin.ROLE_MA );
            }
        });
    }


    @Override
    public void submit() throws Exception {
        UserRepository um = UserRepository.instance();
        um.commitChanges();
        
        MosaicRepository2 mosaic = repo.get();
        mosaic.closeCase( mcase.get() );
        mosaic.commitChanges();
    }


    @Override
    public void discard() {
        UserRepository um = UserRepository.instance();
        um.revertChanges();
    }
    
}
