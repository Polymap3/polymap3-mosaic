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

import org.eclipse.core.runtime.IPath;

/**
 * An app context is shared by all {@link IPanel} instances in the same panel
 * hierachy.
 * 
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public interface IAppContext {

    /**
     * 
     *
     * @param parent The path of the panel to open.
     * @param name The name of the panel to open
     * @return Null if the given panels was not found.
     */
    public IPanel openPanel( IPath parent, String name );
    
    /**
     * All direct children of the given path.
     *
     * @param path
     */
    public Iterable<IPanel> panels( IPath path );
    
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
     * 
     *
     * @param supplier The supplier to add. Must not be null.
     */
    public void addSupplier( ContextSupplier supplier );

    public void removeSupplier( ContextSupplier supplier );
    
    /**
     * Retrieves the property for the given name from this context.
     *
     * @param source The source of this call. This is given to the supplier.
     * @param key The key of the property.
     * @return The property for the given key.
     */
    public <T> T get( Object consumer, String key );
    

    /**
     * 
     */
    interface ContextSupplier {

        public <T> T get( Object consumer, String key );
            
    }
    
}
