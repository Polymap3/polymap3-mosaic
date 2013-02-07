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

import org.eclipse.core.runtime.IPath;

/**
 * An app context is shared by all {@link IPanel} instances in the same panel
 * hierachy.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IApplicationContext {

    public <T extends IPanel> T openSibling( String extId, String name );
    
    public <T extends IPanel> T openChild( String extId, String name );
    
    public Iterable<IPanel> panelsAt( IPath path );
    
    /**
     * Registers the given {@link EventHandler event handler} for event types:
     * <ul>
     * <li>{@link PanelChangeEvent}</li>
     * </ul> 
     *
     * @see EventHandler
     * @see EventManager
     * @param handler
     */
    public void addEventHandler( Object handler );

    public void removeEventHandler( Object handler );

    /**
     * Store the given value for the given key in this context.
     * 
     * @return The previous value, or null if no value was stored for this key.
     */
    public <T> T put( String key, T value );
    
    public <T> T putIfAbsent( String key, T value );
    
    public <T> T get( String key );
    
}
