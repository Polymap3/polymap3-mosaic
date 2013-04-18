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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;

import org.polymap.atlas.AtlasPlugin;
import org.polymap.atlas.PanelChangeEvent;
import org.polymap.atlas.PanelChangeEvent.TYPE;
import org.polymap.atlas.internal.desktop.DesktopAppManager.DesktopPanelSite;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class PanelToolbar
        extends ContributionItem {

    private static Log log = LogFactory.getLog( PanelToolbar.class );

    private DesktopAppManager   appManager;

    private Composite                   contents;

    private List<PanelChangeEvent>      pendingStartEvents = new ArrayList();


    public PanelToolbar( DesktopAppManager appManager ) {
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


    @EventHandler(display=true)
    protected void panelChanged( PanelChangeEvent ev ) {
        if (contents == null) {
            pendingStartEvents.add( ev );
            return;
        }
        // close
        if (ev.getType() == TYPE.CLOSED) {
            for (Control child : contents.getChildren()) {
                child.dispose();
            }
        }
        // open
        if (ev.getType() == TYPE.OPENED) {
            ToolBar tb = new ToolBar( contents, SWT.BORDER /*| SWT.FLAT*/ );
            tb.setLayoutData( SimpleFormData.filled().height( 28 ).create() );

            DesktopPanelSite panelSite = (DesktopPanelSite)ev.getSource().getSite();
            for (Object tool : panelSite.getTools()) {
                ToolItem item = null;

                // IAction
                if (tool instanceof IAction) {
                    final IAction action = (IAction)tool;
                    switch (action.getStyle()) {
                        case IAction.AS_CHECK_BOX:
                            item = new ToolItem( tb, SWT.CHECK );
                            break;
                        default:
                            item = new ToolItem( tb, SWT.PUSH );
                    }
                    item.setData( "source", tool );
                    if (action.getText() != null) {
                        item.setText( action.getText() );
                    }
                    if (action.getToolTipText() != null) {
                        item.setToolTipText( action.getToolTipText() );
                    }
                    ImageDescriptor image = action.getImageDescriptor();
                    if (image != null) {
                        item.setImage( AtlasPlugin.getDefault().imageForDescriptor( image, action.getText() + "_icon" ) );
                    }
                    item.addSelectionListener( new SelectionAdapter() {
                        public void widgetSelected( SelectionEvent se ) {
                            action.run();
                        }
                    });
                }
                else {
                    throw new RuntimeException( "Panel toolbar item type: " + tool );
                }
            }
            tb.layout( true );
//            contents.layout( true );
//            contents.getParent().layout( true );
            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    contents.layout( true );
                }
            });

        }
    }

}
