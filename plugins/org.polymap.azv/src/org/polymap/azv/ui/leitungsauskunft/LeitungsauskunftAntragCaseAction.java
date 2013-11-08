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

import org.eclipse.jface.action.IAction;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.ui.map.DrawFeatureMapAction;
import org.polymap.mosaic.server.model.IMosaicCase;
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
            return true;
        }
        return false;
    }

    
    @Override
    public void dispose() {
        EventManager.instance().unsubscribe( this );
    }


    @Override
    public void fillAction( @SuppressWarnings("hiding") IAction action ) {
        this.action = action;
        updateEnabled();
        
        EventManager.instance().subscribe( this );
    }
    
    
    @Override
    public void submit() throws Exception {
        repo.get().newCaseEvent( mcase.get(), "Beantragt", "", AzvPlugin.EVENT_TYPE_BEANTRAGT );
        repo.get().commitChanges();
        
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                site.getContext().closePanel();
            }
        });
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
    }
    
}
