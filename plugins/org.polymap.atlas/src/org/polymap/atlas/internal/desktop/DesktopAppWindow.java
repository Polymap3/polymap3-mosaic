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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.window.ApplicationWindow;

import org.eclipse.core.runtime.IStatus;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.ui.FormDataFactory;

import org.polymap.atlas.AtlasPlugin;
import org.polymap.atlas.PanelChangeEvent;
import org.polymap.atlas.PanelChangeEvent.TYPE;
import org.polymap.atlas.internal.desktop.DesktopAppManager.DesktopPanelSite;

/**
 * The main application window for the desktop.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
abstract class DesktopAppWindow
        extends ApplicationWindow {

    private static Log log = LogFactory.getLog( DesktopAppWindow.class );

    private DesktopAppManager       appManager;


    public DesktopAppWindow( DesktopAppManager appManager ) {
        super( null );
        this.appManager = appManager;
        addStatusLine();
        //addCoolBar( SWT.HORIZONTAL );
    }


    protected abstract Composite fillNavigationArea( Composite parent );

    protected abstract Composite fillPanelArea( Composite parent );


    @Override
    protected Control createContents( Composite parent ) {
        setStatus( "Status..." );

        Composite contents = new Composite( parent, SWT.NONE );
        contents.setLayout( new FormLayout() );

        Composite navi = fillNavigationArea( contents );
        navi.setLayoutData( FormDataFactory.filled().bottom( -1 ).height( 30 ).create() );

        Composite panels = fillPanelArea( contents );
        panels.setLayoutData( FormDataFactory.filled().top( navi, 10 ).create() );

        appManager.getContext().addEventHandler( this, new EventFilter<PanelChangeEvent>() {
            public boolean apply( PanelChangeEvent input ) {
                return input.getType() == TYPE.ACTIVATED;
            }
        });
        return contents;
    }


    @EventHandler(display=true)
    protected void panelChanged( PanelChangeEvent ev ) {
        DesktopPanelSite panelSite = (DesktopPanelSite)ev.getSource().getSite();
        getShell().setText( "Mosaic - " + panelSite.getTitle() );
        getShell().layout();
    }


    public void setStatus( IStatus status ) {
        assert status != null;
        switch (status.getSeverity()) {
            case IStatus.ERROR: {
                getStatusLineManager().setErrorMessage( 
                        AtlasPlugin.instance().imageForName( "icons/errorstate.gif" ), status.getMessage() );
                break;
            }
            case IStatus.WARNING: {
                getStatusLineManager().setMessage( 
                        AtlasPlugin.instance().imageForName( "icons/warningstate.gif" ), status.getMessage() );
                break;
            }
            default: {
                getStatusLineManager().setMessage( status.getMessage() );
            }
        }
    }


    @Override
    protected void configureShell( Shell shell ) {
        super.configureShell( shell );
        shell.setText( "Mosaic" );
        shell.setTouchEnabled( true );
        shell.setMaximized( true );
    }


    @Override
    protected int getShellStyle() {
        return SWT.CLOSE;
    }


    @Override
    protected boolean showTopSeperator() {
        return false;
    }

}
