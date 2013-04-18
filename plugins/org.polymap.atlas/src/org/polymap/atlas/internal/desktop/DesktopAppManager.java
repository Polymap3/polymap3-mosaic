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

import com.google.common.base.Predicate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.window.Window;

import org.eclipse.core.runtime.IStatus;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.atlas.IApplicationLayouter;
import org.polymap.atlas.IAtlasToolkit;
import org.polymap.atlas.IPanel;
import org.polymap.atlas.IPanelSite;
import org.polymap.atlas.PanelChangeEvent;
import org.polymap.atlas.PanelChangeEvent.TYPE;
import org.polymap.atlas.PanelIdentifier;
import org.polymap.atlas.PanelPath;
import org.polymap.atlas.internal.AtlasComponentFactory;
import org.polymap.atlas.internal.DefaultAppContext;
import org.polymap.atlas.internal.PanelContextInjector;
import org.polymap.atlas.internal.desktop.DesktopPanelNavigator.PLACE;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DesktopAppManager
        implements IApplicationLayouter {

    private static Log log = LogFactory.getLog( DesktopAppManager.class );

    private DesktopToolkit              tk = new DesktopToolkit();

    private DesktopAppContext           context = new DesktopAppContext();

    private DesktopAppWindow            mainWindow;

    private DesktopPanelNavigator       panelNavi;

    private Composite                   panelArea;

    private IPanel                      activePanel;


    @Override
    public Window initMainWindow( Display display ) {
        // panel navigator area
        panelNavi = new DesktopPanelNavigator( context, tk );
        panelNavi.add( new SearchField( ), PLACE.SEARCH );
        panelNavi.add( new PanelToolbar( this ), PLACE.PANEL_TOOLBAR );
        panelNavi.add( new PanelNavigator( this ), PLACE.PANEL_NAVI );
        panelNavi.add( new PanelSwitcher( this ), PLACE.PANEL_SWITCHER );

        // mainWindow
        mainWindow = new DesktopAppWindow( this ) {
            @Override
            protected Composite fillNavigationArea( Composite parent ) {
                return panelNavi.createContents( parent );
            }
            @Override
            protected Composite fillPanelArea( Composite parent ) {
                panelArea = tk.createComposite( parent, SWT.BORDER );
                panelArea.setLayout( new FormLayout() );
                tk.createLabel( panelArea, "Panels..." );
                return panelArea;
            }
        };
        // open root panel / after main window is created
        display.asyncExec( new Runnable() {
            public void run() {
                openPanel( new PanelIdentifier( "azvstart" ) );
            }
        });
        return mainWindow;
    }


    @Override
    public void dispose() {
    }


    /**
     * Opens the {@link IPanel} for the given id and adds it to the top of the current
     * panel path.
     *
     * @param panelId
     * @throws IllegalStateException If no panel could be found for the given id.
     */
    protected IPanel openPanel( PanelIdentifier panelId ) {
        // find and initialize panels
        final PanelPath prefix = activePanel != null ? activePanel.getSite().getPath() : PanelPath.ROOT;
        List<IPanel> panels = AtlasComponentFactory.instance().createPanels( new Predicate<IPanel>() {
            public boolean apply( IPanel panel ) {
                new PanelContextInjector( panel, context ).run();
                PanelPath path = prefix.append( panel.id() );
                return panel.init( new DesktopPanelSite( path ), context );
            }
        });

        // add to context
        for (IPanel panel : panels) {
            context.addPanel( panel );
        }

        // clear panelArea
        for (Control child : panelArea.getChildren()) {
            child.dispose();
        }

        //
        IPanel panel = context.getPanel( prefix.append( panelId ) );
        if (panel == null) {
            throw new IllegalStateException( "No panel for ID: " + panelId );
        }
        EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.OPENING ) );
        panel.createContents( panelArea );
        activePanel = panel;
        EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.OPENED ) );
        panelArea.layout( true );

        return activePanel;
    }


    protected DesktopAppContext getContext() {
        return context;
    }


    /**
     *
     */
    class DesktopAppContext
            extends DefaultAppContext {

        @Override
        public IPanel openPanel( PanelIdentifier panelId ) {
            return DesktopAppManager.this.openPanel( panelId );
        }
    }


    /**
     *
     */
    protected class DesktopPanelSite
            implements IPanelSite {

        private PanelPath           path;

        private String              title = "Untitled";

        /** Toolbar tools: {@link IAction} or {@link IContributionItem}. */
        private List                tools = new ArrayList();


        protected DesktopPanelSite( PanelPath path ) {
            assert path != null;
            this.path = path;
        }

        @Override
        public PanelPath getPath() {
            return path;
        }

        @Override
        public void changeStatus( IStatus status ) {
            mainWindow.setStatus( status );
        }

        @Override
        public void addToolbarAction( IAction action ) {
            tools.add( action );
        }

        @Override
        public void addToolbarItem( IContributionItem item ) {
            tools.add( item );
        }

        public List getTools() {
            return tools;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public void setTitle( String title ) {
            this.title = title;
        }

        @Override
        public void addSidekick() {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        public IAtlasToolkit toolkit() {
            return tk;
        }

    }

}
