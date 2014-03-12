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
package org.polymap.azv.ui.leitungsauskunft;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;

import org.eclipse.jface.action.IAction;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.email.EmailService;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.model.NutzerMixin;
import org.polymap.azv.ui.map.DrawFeatureMapAction;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicDocument;
import org.polymap.mosaic.server.model.MosaicCaseEvents;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.DefaultCaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseActionSite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LeitungsauskunftAntragCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( LeitungsauskunftAntragCaseAction.class );

    public static final IMessages               i18n = Messages.forPrefix( "LeitungsauskunftAntrag" ); //$NON-NLS-1$

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>        mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2>  repo;

    private ICaseActionSite                     site;

    private IAction                             action;

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        if (mcase.get() != null && repo.get() != null
                && mcase.get().getNatures().contains( AzvPlugin.CASE_LEITUNGSAUSKUNFT )) {
            
            EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {
                public boolean apply( PropertyChangeEvent input ) {
                    return action != null && DrawFeatureMapAction.EVENT_NAME.equals( input.getPropertyName() );
                }
            });

            return true;
        }
        return false;
    }

    
    @Override
    public void dispose() {
        EventManager.instance().unsubscribe( this );
        this.action = null;
    }


    @Override
    public void fillAction( @SuppressWarnings("hiding") IAction action ) {
        this.action = action;
        if (MosaicCaseEvents.contains( mcase.get(), AzvPlugin.EVENT_TYPE_BEANTRAGT )) {
            action.setText( null );
            action.setImageDescriptor( null );
            action.setEnabled( false );
        }
        else {
            updateEnabled();
        }
    }
    
    
    @Override
    public void submit() throws Exception {
        // dokumente
        File dir = new File( Polymap.getWorkspacePath().toFile(), "Dokumente/Leitungen" ); //$NON-NLS-1$
        for (File f : dir.listFiles()) {
            IMosaicDocument doc = repo.get().newDocument( mcase.get(), f.getName() );
            OutputStream out = doc.getOutputStream();
            FileInputStream in = new FileInputStream( f );
            try {
                IOUtils.copy( in, out );
            }
            finally {
                IOUtils.closeQuietly( out );
                IOUtils.closeQuietly( in );
            }
        }
        // event, commit
        repo.get().newCaseEvent( mcase.get(), "Beantragt", "", AzvPlugin.EVENT_TYPE_BEANTRAGT ); //$NON-NLS-1$ //$NON-NLS-2$
        repo.get().commitChanges();
     
        User user = mcase.get().as( NutzerMixin.class ).user();
                
        String salu = user.salutation().get() != null ? user.salutation().get() : ""; //$NON-NLS-1$
        String header = "Sehr geehrte" + (salu.equalsIgnoreCase( "Herr" ) ? "r " : " ") + salu + " " + user.name().get(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        Email email = new SimpleEmail();
        email.setCharset( "ISO-8859-1" ); //$NON-NLS-1$
        email.addTo( user.email().get() )
                .setSubject( i18n.get( "emailSubject") ) //$NON-NLS-1$
                .setMsg( i18n.get( "email", header ) ); //$NON-NLS-1$
        EmailService.instance().send( email );
        
        fillAction( action );
        
//        Polymap.getSessionDisplay().asyncExec( new Runnable() {
//            public void run() {
//                site.getContext().closePanel();
//            }
//        });
    }


    protected void updateEnabled() {
        action.setEnabled( false );
        if (mcase.get().getName().length() == 0) {
            action.setToolTipText( i18n.get( "keineBezeichnung" ) );
        }
        else if (mcase.get().get( "point" ) == null) { //$NON-NLS-1$
            action.setToolTipText( i18n.get( "keinOrt" ) );
        }
        else {
            action.setToolTipText( "" ); //$NON-NLS-1$
            action.setEnabled( true );
        }
    }
    
    /** Currently send be {@link DrawFeatureMapAction}. */
    @EventHandler(display=true)
    protected void mcaseChanged( PropertyChangeEvent ev ) {
        updateEnabled();
    }
    
}
