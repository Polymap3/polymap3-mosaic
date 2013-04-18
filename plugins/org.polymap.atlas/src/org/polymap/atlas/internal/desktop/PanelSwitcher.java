/*
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.atlas.internal.desktop;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.ContributionItem;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;

import org.polymap.atlas.IPanel;
import org.polymap.atlas.PanelChangeEvent;
import org.polymap.atlas.PanelChangeEvent.TYPE;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PanelSwitcher
        extends ContributionItem {

    private static Log log = LogFactory.getLog( PanelSwitcher.class );

    private DesktopAppManager   appManager;

    private List<PanelChangeEvent>      pendingStartEvents = new ArrayList();

    private Composite                   contents;

    private IPanel                      activePanel;


    public PanelSwitcher( DesktopAppManager appManager ) {
        this.appManager = appManager;

        appManager.getContext().addEventHandler( this, new EventFilter<PanelChangeEvent>() {
            public boolean apply( PanelChangeEvent input ) {
                return input.getType() == TYPE.OPENED;
            }
        });
    }


    @Override
    public void fill( Composite parent ) {
        this.contents = parent;
        contents.setLayout( new FormLayout() );

        // fire pending events
        for (PanelChangeEvent ev : pendingStartEvents) {
            panelChanged( ev );
        }
        pendingStartEvents.clear();
    }


    protected void updateUI() {
    }


    @EventHandler(display=true)
    protected void panelChanged( PanelChangeEvent ev ) {
        if (contents == null) {
            pendingStartEvents.add( ev );
            return;
        }
        // open
        if (ev.getType() == TYPE.OPENED) {
            activePanel = ev.getSource();
            updateUI();
        }
    }

}
