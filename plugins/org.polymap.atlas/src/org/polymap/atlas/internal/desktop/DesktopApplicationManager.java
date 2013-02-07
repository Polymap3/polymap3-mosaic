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
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.window.Window;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;

import org.polymap.atlas.IApplicationContext;
import org.polymap.atlas.IApplicationLayouter;
import org.polymap.atlas.IPanelSite;
import org.polymap.atlas.IAtlasToolkit;
import org.polymap.atlas.internal.ApplicationContext;
import org.polymap.atlas.internal.AtlasComponentFactory;
import org.polymap.atlas.internal.AtlasComponentFactory.SiteSupplier;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DesktopApplicationManager
        implements IApplicationLayouter {

    private static Log log = LogFactory.getLog( DesktopApplicationManager.class );
    
    private DesktopToolkit              tk = new DesktopToolkit();

    private IApplicationContext         context = new ApplicationContext();
    
    private DesktopApplicationWindow    mainWindow;

    private DesktopPanelNavigator       panelNavi;

    private Composite                   panelArea;
    

    @Override
    public Window initMainWindow( Display display ) {
        // mainWindow
        mainWindow = new DesktopApplicationWindow( null ) {

            @Override
            protected Composite fillNavigationArea( Composite parent ) {
                panelNavi = new DesktopPanelNavigator( context, tk );
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
        
        // panels
        AtlasComponentFactory.instance().createPanels( context, new SiteSupplier() {
            public IPanelSite create( String name ) {
                return new DesktopPanelSite( new Path( name ) );
            }
        });
        
        return mainWindow;
    }

    
    @Override
    public void dispose() {
    }


    /**
     * 
     */
    class DesktopPanelSite
            implements IPanelSite {

        private IPath               path;
        
        protected DesktopPanelSite( IPath path ) {
            assert path != null;
            this.path = path;
        }
        
        @Override
        public IPath getPath() {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
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
