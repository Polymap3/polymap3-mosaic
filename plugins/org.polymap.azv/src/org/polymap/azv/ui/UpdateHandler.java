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
package org.polymap.azv.ui;

import java.util.List;

import java.beans.PropertyChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.polymap.core.runtime.SessionContext;
import org.polymap.core.runtime.event.Event;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

import org.polymap.mosaic.server.model.IMosaicCase;

/**
 * Listen to global modifications of {@link IMosaicCase} instances. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class UpdateHandler {

    private static Log log = LogFactory.getLog( UpdateHandler.class );
    
    private SessionContext              session = SessionContext.current();
    
    private Button                      control;

    private IPanelToolkit               tk;

    private List<PropertyChangeEvent>   events;
    
    
    public UpdateHandler( IPanelToolkit tk ) {
        this.tk = tk;
        
        EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {
            public boolean apply( PropertyChangeEvent input ) {
                // XXX update just the element that has changed / or added / or removed
                return !EventManager.publishSession().equals( session )
                        && input.getSource() instanceof IMosaicCase;
            }
        });
    }

    
    protected abstract void doUpdate( @SuppressWarnings("hiding") List<PropertyChangeEvent> events );
    
    
    @EventHandler(display=true,delay=1000,scope=Event.Scope.JVM)
    protected void caseChanged( @SuppressWarnings("hiding") List<PropertyChangeEvent> events ) {
        if (control != null && !control.isDisposed()) {
            this.events = events;
//            control.setImage( BatikPlugin.instance().imageForName( "resources/icons/info-red.png" ) );
            control.setToolTipText( "Die Daten eines Vorgangs haben sich geändert.\nWenn Sie diesen Knopf betätigen, dann werden die Änderungen übernommen and damit sichtbar." );
            control.setEnabled( true );
        }
    }


    protected Control createControl( Composite parent ) {
        assert control == null;
        
        control = tk.createButton( parent, null );
        control.setImage( BatikPlugin.instance().imageForName( "resources/icons/info.png" ) );
        control.setToolTipText( "Es liegen keine Änderungen in Vorgängen vor." );
        control.setEnabled( false );

        control.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent e ) {
                assert events != null;
                doUpdate( events );
                control.setImage( BatikPlugin.instance().imageForName( "resources/icons/info.png" ) );
                control.setEnabled( false );
            }
        });
        return control;
    }

}
