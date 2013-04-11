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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.IContributionItem;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.core.runtime.event.EventHandler;

import org.polymap.atlas.IAppContext;
import org.polymap.atlas.IAtlasToolkit;
import org.polymap.atlas.PanelChangeEvent;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class DesktopPanelNavigator {

    private static Log log = LogFactory.getLog( DesktopPanelNavigator.class );

    enum PLACE {
        SEARCH,
        PANEL_TOOLBAR,
        PANEL_NAVI,
        PANEL_PATH
    }
    
    // instance *******************************************
    
    private IAppContext                 context;
    
    private IAtlasToolkit               tk;
    
    private Map<PLACE,IContributionItem> items = new HashMap();

    
    public DesktopPanelNavigator( IAppContext context, IAtlasToolkit tk ) {
        this.context = context;
        this.tk = tk;
        context.addEventHandler( this );
    }

    
    public IContributionItem add( IContributionItem item, PLACE place ) {
        return items.put( place, item );
    }
    
    
    public Composite createContents( Composite parent ) {
        Composite contents = tk.createComposite( parent, SWT.BORDER );
        FormLayout layout = new FormLayout();
        layout.spacing = 10;
        contents.setLayout( layout );

        // search
        IContributionItem search = items.get( PLACE.SEARCH );
        if (search != null) {
            Composite searchComposite = new Composite( contents, SWT.NONE );
            search.fill( searchComposite );
            
            searchComposite.setLayoutData( SimpleFormData.filled().left( 80 ).right( 100 ).create() );
        }
        
        return contents;
    }
    
    
    @EventHandler(display=true)
    protected void panelChanged( PanelChangeEvent ev ) {
    }
    
}
