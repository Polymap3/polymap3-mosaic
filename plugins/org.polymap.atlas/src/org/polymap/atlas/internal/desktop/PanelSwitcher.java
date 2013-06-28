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

import static org.polymap.atlas.PanelFilters.withPrefix;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.atlas.IPanel;
import org.polymap.atlas.PanelChangeEvent;
import org.polymap.atlas.PanelChangeEvent.TYPE;
import org.polymap.atlas.PanelPath;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PanelSwitcher
        extends ContributionItem {

    private static Log log = LogFactory.getLog( PanelSwitcher.class );

    private DesktopAppManager           appManager;

    private List<PanelChangeEvent>      pendingStartEvents = new ArrayList();

    private Composite                   contents;

    private IPanel                      activePanel;


    public PanelSwitcher( DesktopAppManager appManager ) {
        this.appManager = appManager;

        appManager.getContext().addEventHandler( this, new EventFilter<PanelChangeEvent>() {
            public boolean apply( PanelChangeEvent input ) {
                return input.getType() == TYPE.ACTIVATED;
            }
        });
    }


    @Override
    public void fill( Composite parent ) {
        this.contents = parent;
        contents.setLayout( RowLayoutFactory.fillDefaults().spacing( 0 ).create() );

        // fire pending events
        for (PanelChangeEvent ev : pendingStartEvents) {
            panelChanged( ev );
        }
        pendingStartEvents.clear();
    }


    protected void updateUI() {
        // clear contents
        for (Control child : contents.getChildren()) {
            child.dispose();
        }
        
//        Label l = new Label( contents, SWT.NONE );
//        l.setText( " -> " );
        
        PanelPath prefix = activePanel.getSite().getPath().removeLast( 1 );
        for (final IPanel panel : appManager.getContext().findPanels( withPrefix( prefix ) )) {
            Button btn = new Button( contents, SWT.TOGGLE );
            btn.setText( panel.getSite().getTitle() );
            btn.setLayoutData( RowDataFactory.swtDefaults().hint( SWT.DEFAULT, 28 ).create() );
            
            if (panel.equals( activePanel )) {
//                FontData[] defaultFont = btn.getFont().getFontData();
//                FontData bold = new FontData( defaultFont[0].getName(), defaultFont[0].getHeight(), SWT.BOLD );
//                btn.setFont( Graphics.getFont( bold ) );
                
                //btn.setEnabled( false );
                btn.setSelection( true );
            }
            else {
                btn.addSelectionListener( new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent ev ) {
                        appManager.activatePanel( panel.id() );
                    }
                });
            }
        }
        
        contents.getParent().layout( true );
    }


    @EventHandler(display=true)
    protected void panelChanged( PanelChangeEvent ev ) {
        if (contents == null) {
            pendingStartEvents.add( ev );
            return;
        }
        // open
        if (ev.getType() == TYPE.ACTIVATED) {
            activePanel = ev.getSource();
            updateUI();
        }
    }

}
