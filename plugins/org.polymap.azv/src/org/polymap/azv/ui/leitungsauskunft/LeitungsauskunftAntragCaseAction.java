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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;

import org.eclipse.jface.action.IAction;

import org.polymap.core.runtime.IMessages;
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
import org.polymap.mosaic.server.model.IMosaicCaseEvent;
import org.polymap.mosaic.server.model.MosaicCaseEvents;
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
public class LeitungsauskunftAntragCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( LeitungsauskunftAntragCaseAction.class );

    public static final IMessages       i18n = Messages.forPrefix( "LeitungsauskunftAntrag" );

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>        mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2>  repo;

    private ICaseActionSite                     site;

    private IAction                             action;

    private CaseStatus                          caseStatus;

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        if (mcase.get() != null && repo.get() != null
                && mcase.get().getNatures().contains( AzvPlugin.CASE_LEITUNGSAUSKUNFT )) {
            return true;
        }
        return false;
    }

    
    @Override
    public void dispose() {
        EventManager.instance().unsubscribe( this );
        this.action = null;
        this.caseStatus = null;
    }


    @Override
    public void fillStatus( CaseStatus status ) {
        this.caseStatus = status;
        if (!mcase.get().getStatus().equals( IMosaicCaseEvent.TYPE_CLOSED )) {
            if (MosaicCaseEvents.contains( mcase.get().getEvents(), AzvPlugin.EVENT_TYPE_BEANTRAGT )) {
                status.put( "Status", "Beantragt" );
            }
        }
    }

    
    @Override
    public void fillAction( @SuppressWarnings("hiding") IAction action ) {
        this.action = action;
        if (MosaicCaseEvents.contains( mcase.get().getEvents(), AzvPlugin.EVENT_TYPE_BEANTRAGT )) {
            action.setText( null );
            action.setImageDescriptor( null );
            action.setEnabled( false );
        }
        else {
            updateEnabled();
        
            EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {
                public boolean apply( PropertyChangeEvent input ) {
                    return caseStatus != null && input.getSource() == mcase.get();
                }
            });
        }
    }
    
    
    @Override
    public void submit() throws Exception {
        repo.get().newCaseEvent( mcase.get(), "Beantragt", "", AzvPlugin.EVENT_TYPE_BEANTRAGT );
        repo.get().commitChanges();
     
        User user = mcase.get().as( NutzerMixin.class ).user();
                
        String salu = user.salutation().get() != null ? user.salutation().get() : "";
        String header = "Sehr geehrte" + (salu.equalsIgnoreCase( "Herr" ) ? "r " : " ") + salu + " " + user.name().get();
        Email email = new SimpleEmail();
        email.setCharset( "ISO-8859-1" );
        email.addTo( user.email().get() )
                .setSubject( i18n.get( "emailSubject") )
                .setMsg( i18n.get( "email", header ) );
        EmailService.instance().send( email );
        
        fillAction( action );
        fillStatus( caseStatus );
        
//        Polymap.getSessionDisplay().asyncExec( new Runnable() {
//            public void run() {
//                site.getContext().closePanel();
//            }
//        });
    }


    protected void updateEnabled() {
        action.setEnabled( false );
        if (mcase.get().getName().length() == 0) {
            action.setToolTipText( "Es fehlt der Name der Maßnahme" );
        }
        else if (mcase.get().get( "point" ) == null) {
            action.setToolTipText( "Es fehlt der Ort der Maßnahme" );
        }
        else {
            action.setToolTipText( "" );
            action.setEnabled( true );
        }
    }
    
    /** Currently send be {@link DrawFeatureMapAction}. */
    @EventHandler(display=true)
    protected void mcaseChanged( PropertyChangeEvent ev ) {
        updateEnabled();
        fillStatus( caseStatus );
    }
    
}
