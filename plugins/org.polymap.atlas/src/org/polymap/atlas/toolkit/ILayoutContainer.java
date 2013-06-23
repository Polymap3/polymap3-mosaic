/* 
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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
package org.polymap.atlas.toolkit;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ILayoutContainer {

    /**
     * Returns the underlying control of this container.
     * <p/>
     * Do not use this to add widgets to the container, use {@link #getBody()}
     * instead.
     */
    public Composite getControl();
    
    
    /**
     * Returns the body composite this container. It can be used to add widgets to
     * this container. By default the {@link Layout} of the composite and layout data
     * of the widgets are managed by the container. Client code does not need to
     * worry about.
     */
    public Composite getBody();
    
}
