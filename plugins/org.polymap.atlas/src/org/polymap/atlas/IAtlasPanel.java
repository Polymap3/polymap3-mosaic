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
package org.polymap.atlas;

import org.eclipse.swt.widgets.Composite;

/**
 * An Atlas panel is is the main element of the Atlas UI.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IAtlasPanel {

    /**
     * Initializes the panel and checks if it is valid fpor the given site
     * and context.
     * 
     * @param site
     * @param context
     * @return True if the panel is valid for the given site and context.
     */
    public boolean init( IAtlasPanelSite site, IAtlasPanelContext context );
    
    public Composite createContents( Composite parent );
    
}
