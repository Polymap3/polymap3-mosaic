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

import java.util.EventObject;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PanelChangeEvent
        extends EventObject {

    /** The types of {@link PanelChangeEvent}. */
    public enum TYPE {
        INITIALIZING,
        INITIALIZED,
        DISPOSING,
        DISPOSED,
        ACTIVATING,
        ACTIVATED,
        DEACTIVATING,
        DEACTIVATED
    }
    
    // instance *******************************************
    
    private TYPE            type;
    
    public PanelChangeEvent( IPanel source, TYPE type ) {
        super( source );
        this.type = type;
    }

    @Override
    public IPanel getSource() {
        return (IPanel)super.getSource();
    }

    public TYPE getType() {
        return type;
    }
    
}
