/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.mosaic.ui.casepanel;

import java.util.EventObject;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CaseActionEvent
        extends EventObject {

    /** The types of {@link CaseActionEvent}. */
    public enum TYPE {
        ACTIVATING,
        ACTIVATED
    }
    
    
    // instance *******************************************
    
    private TYPE            type;
    
    public CaseActionEvent( Object source, TYPE type ) {
        super( source );
        this.type = type;
    }

    public TYPE getType() {
        return type;
    }

}
