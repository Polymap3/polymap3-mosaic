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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.window.Window;

import org.eclipse.ui.forms.widgets.ScrolledPageBook;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.event.EventManager;

import org.polymap.atlas.IApplicationLayouter;
import org.polymap.atlas.IAtlasToolkit;
import org.polymap.atlas.IPanel;
import org.polymap.atlas.IPanelSite;
import org.polymap.atlas.PanelChangeEvent;
import static org.polymap.atlas.PanelFilters.withPrefix;
import org.polymap.atlas.PanelChangeEvent.TYPE;
import org.polymap.atlas.PanelIdentifier;
import org.polymap.atlas.PanelPath;
import org.polymap.atlas.internal.AtlasComponentFactory;
import org.polymap.atlas.internal.DefaultAppContext;
import org.polymap.atlas.internal.PanelContextInjector;
import org.polymap.atlas.internal.desktop.DesktopActionBar.PLACE;

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

    private DesktopActionBar            actionBar;

    private ScrolledPageBook            scrolledPanelContainer;

    private IPanel                      activePanel;
    
//    private Map<PanelIdentifier,Composite> activatedPanels = new HashMap();


    @Override
    public Window initMainWindow( Display display ) {
        // panel navigator area
        actionBar = new DesktopActionBar( context, tk );
        actionBar.add( new SearchField( ), PLACE.SEARCH );
        actionBar.add( new PanelToolbar( this ), PLACE.PANEL_TOOLBAR );
        actionBar.add( new PanelNavigator( this ), PLACE.PANEL_NAVI );
        actionBar.add( new PanelSwitcher( this ), PLACE.PANEL_SWITCHER );

        // mainWindow
        mainWindow = new DesktopAppWindow( this ) {
            @Override
            protected Composite fillNavigationArea( Composite parent ) {
                return actionBar.createContents( parent );
            }
            @Override
            protected Composite fillPanelArea( Composite parent ) {
                scrolledPanelContainer = new ScrolledPageBook( parent, SWT.BORDER | SWT.V_SCROLL );
                scrolledPanelContainer.showEmptyPage();
                
//                scrolledPanelContainer = (ScrolledComposite)tk.createComposite( parent, SWT.BORDER, SWT.V_SCROLL );
//                panelArea = (Composite)scrolledPanelContainer.getContent();
//                panelArea.setLayout( new FillLayout( SWT.VERTICAL ) );
//                tk.createLabel( panelArea, "Panels..." );
                return scrolledPanelContainer;
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
    protected IPanel openPanel( final PanelIdentifier panelId ) {
        // find and initialize panels
        final PanelPath prefix = activePanel != null ? activePanel.getSite().getPath() : PanelPath.ROOT;
        List<IPanel> panels = AtlasComponentFactory.instance().createPanels( new Predicate<IPanel>() {
            public boolean apply( IPanel panel ) {
                new PanelContextInjector( panel, context ).run();
                EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.INITIALIZING ) );
                
                PanelPath path = prefix.append( panel.id() );
                boolean wantsToBeShown = panel.init( new DesktopPanelSite( path ), context );
                
                if (panel.id().equals( panelId ) || wantsToBeShown) {
                    EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.INITIALIZED ) );
                    return true;
                }
                return false;
            }
        });

        // add to context
        for (IPanel panel : panels) {
            context.addPanel( panel );
        }

        //
        IPanel panel = context.getPanel( prefix.append( panelId ) );
        if (panel == null) {
            throw new IllegalStateException( "No panel for ID: " + panelId );
        }

        // update UI
        EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.ACTIVATING ) );
        
        Composite page = scrolledPanelContainer.createPage( panel.id() );
        page.setLayout( new FillLayout() );
        panel.createContents( page );
        page.layout( true );
        scrolledPanelContainer.showPage( panel.id() );
        
        Point panelSize = page.computeSize( SWT.DEFAULT, SWT.DEFAULT );
        scrolledPanelContainer.setMinHeight( panelSize.y );

        activePanel = panel;
        EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.ACTIVATED ) );

        return activePanel;
    }

    
    protected void closePanel() {
        assert activePanel != null;
        
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.DEACTIVATING ) );
        scrolledPanelContainer.removePage( activePanel.id() );
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.DEACTIVATED ) );
        
        PanelPath activePath = activePanel.getSite().getPath();
        // remove/dispose activePanel and siblings
        for (IPanel panel : context.findPanels( withPrefix( activePath.removeLast( 1 ) ) )) {
            EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.DISPOSING ) );
            context.removePanel( panel.getSite().getPath() );
            panel.dispose();
            if (scrolledPanelContainer.hasPage( panel.id() )) {
                scrolledPanelContainer.removePage( panel.id() );
            }
            EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.DISPOSED ) );
        }
        
        // activate child panel
        activePath = activePath.removeLast( 1 );
        activePanel = context.getPanel( activePath );
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.ACTIVATING ) );
        scrolledPanelContainer.showPage( activePanel.id() );
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.ACTIVATED ) );
    }

    
    public void activatePanel( PanelIdentifier panelId ) {
        PanelPath prefix = activePanel != null ? activePanel.getSite().getPath().removeLast( 1 ) : PanelPath.ROOT;
        PanelPath activePath = prefix.append( panelId );

        // deactivating
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.DEACTIVATING ) );
        IPanel previous = activePanel = context.getPanel( activePath );
        EventManager.instance().publish( new PanelChangeEvent( previous, TYPE.DEACTIVATED ) );

        // activating
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.ACTIVATING ) );
        if (scrolledPanelContainer.hasPage( panelId )) {
            scrolledPanelContainer.showPage( panelId );
        }
        else {
            EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.ACTIVATING ) );
            Composite page = scrolledPanelContainer.createPage( activePanel.id() );
            page.setLayout( new FillLayout() );
            activePanel.createContents( page );
            page.layout( true );
            scrolledPanelContainer.showPage( activePanel.id() );
            
            Point panelSize = page.computeSize( SWT.DEFAULT, SWT.DEFAULT );
            scrolledPanelContainer.setMinHeight( panelSize.y );
        }
        EventManager.instance().publish( new PanelChangeEvent( activePanel, TYPE.ACTIVATED ) );
    }


    protected DesktopAppContext getContext() {
        return context;
    }

    
    public IPanel getActivePanel() {
        return activePanel;
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

        @Override
        public void closePanel() {
            DesktopAppManager.this.closePanel();
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
        
        private IStatus             status = Status.OK_STATUS;


        protected DesktopPanelSite( PanelPath path ) {
            assert path != null;
            this.path = path;
        }

        @Override
        public PanelPath getPath() {
            return path;
        }

        @Override
        public void setStatus( IStatus status ) {
            this.status = status;
            mainWindow.setStatus( status );
        }

        @Override
        public IStatus getStatus() {
            return status;
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
