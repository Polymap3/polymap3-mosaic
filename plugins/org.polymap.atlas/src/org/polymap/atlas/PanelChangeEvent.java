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

import java.util.EventObject;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class PanelChangeEvent
        extends EventObject {

    /** The types of {@link PanelChangeEvent}. */
    enum TYPE {
        BEFORE_CLOSE,
        AFTER_CLOSE,
        BEFORE_OPEN,
        AFTER_OPEN
    }
    
    // instance *******************************************
    
    private TYPE            type;
    
    public PanelChangeEvent( IPanel source, TYPE type ) {
        super( source );
        this.type = type;
    }

    @Override
    public IPanel getSource() {
        return getSource();
    }
    
}
