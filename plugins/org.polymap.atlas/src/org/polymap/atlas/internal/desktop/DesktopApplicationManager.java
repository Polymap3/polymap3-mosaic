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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

import org.polymap.atlas.IApplicationLayouter;
import org.polymap.atlas.IAtlasToolkit;
import org.polymap.atlas.IPanel;
import org.polymap.atlas.IPanelSite;
import org.polymap.atlas.internal.AtlasComponentFactory;
import org.polymap.atlas.internal.desktop.DesktopPanelNavigator.PLACE;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DesktopApplicationManager
        implements IApplicationLayouter {

    private static Log log = LogFactory.getLog( DesktopApplicationManager.class );
    
    private DesktopToolkit              tk = new DesktopToolkit();

    private DesktopAppContext           context = new DesktopAppContext();
    
    private DesktopApplicationWindow    mainWindow;

    private DesktopPanelNavigator       panelNavi;

    private Composite                   panelArea;
    

    @Override
    public Window initMainWindow( Display display ) {
        // panel navigator
        panelNavi = new DesktopPanelNavigator( context, tk );
        panelNavi.add( new DesktopSearchField( ), PLACE.SEARCH );
        
        // mainWindow
        mainWindow = new DesktopApplicationWindow( null ) {
            @Override
            protected Composite fillNavigationArea( Composite parent ) {
                panelNavi = new DesktopPanelNavigator( context, tk );
                panelNavi.add( new DesktopSearchField( ), PLACE.SEARCH );
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
                openPanel( Path.ROOT, null );
            }
        });
        
        return mainWindow;
    }

    
    @Override
    public void dispose() {
    }

    
    protected void openPanel( final IPath prefix, String name ) {
        // find and initialize panels
        List<IPanel> panels = AtlasComponentFactory.instance().createPanels( new Predicate<IPanel>() {
            public boolean apply( IPanel panel ) {
                IPath path = prefix.append( panel.getName() );
                return panel.init( new DesktopPanelSite( path ), context );
            }
        });
        
        // add to context
        for (IPanel panel : panels) {
            context.addPanel( panel.getPanelSite().getPath(), panel );
        }

        //
        for (Control child : panelArea.getChildren()) {
            child.dispose();
        }
        
        //
        IPanel panel = panels.get( 0 );
        panel.createContents( panelArea );
        panelArea.layout( true );
    }

    
    /**
     * 
     */
    protected class DesktopPanelSite
            implements IPanelSite {

        private IPath               path;
        
        protected DesktopPanelSite( IPath path ) {
            assert path != null;
            this.path = path;
        }
        
        @Override
        public IPath getPath() {
            return path;
        }

        @Override
        public void changeStatus( IStatus status ) {
            mainWindow.setStatus( status );
        }

        @Override
        public void addToolbarAction( IAction action ) {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        public void addToolbarItem( IContributionItem item ) {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
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
