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
package org.polymap.atlas.app;

import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.polymap.core.project.ui.util.SimpleFormData;

import org.polymap.atlas.IAppContext;
import org.polymap.atlas.IPanel;
import org.polymap.atlas.IPanelSite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DashboardPanel
        implements IPanel {

    private IPanelSite      site;
    
    private IAppContext     context;   
    
    @Override
    public boolean init( IPanelSite _site, IAppContext _context ) {
        this.site = _site;
        this.context = _context;
        return true;
    }

    @Override
    public void dispose() {
    }

    @Override
    public String getName() {
        return "dashboard";
    }

    @Override
    public Composite createContents( Composite parent ) {
        Composite contents = site.toolkit().createComposite( parent );
        contents.setLayout( new FormLayout() );
        
        Label l = site.toolkit().createLabel( contents, "Dashboard!" );
        l.setLayoutData( SimpleFormData.filled().create() );
        
        return contents;
    }

    @Override
    public IPanelSite getPanelSite() {
        return site;
    }
    
}
