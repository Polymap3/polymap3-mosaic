/* 
 * polymap.org
 * Copyright 2013, Falko Br�utigam. All rights reserved.
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
package org.polymap.atlas;

import org.eclipse.swt.widgets.Composite;

/**
 * The panel is the main visual component of the Atlas UI. It typically provides a
 * map view, an editor, wizard or a dashboard.
 * <p/>
 * A panel is identified by its path and name. The path defines the place in the
 * hierarchy of panel.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface IPanel {

    /**
     * Initializes the panel and checks if it is valid for the given site and
     * context.
     * 
     * @param site
     * @param context
     * @return True if the panel is valid for the given site and context.
     */
    public boolean init( IPanelSite site, IAppContext context );
    
    public void dispose();
    
    public String getName();
    
    public Composite createContents( Composite parent );
    
    public IPanelSite getPanelSite();
}
