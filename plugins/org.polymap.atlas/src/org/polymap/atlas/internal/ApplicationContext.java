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
package org.polymap.atlas.internal;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import org.eclipse.core.runtime.IPath;

import org.polymap.core.runtime.event.EventManager;

import org.polymap.atlas.IPanel;
import org.polymap.atlas.IApplicationContext;

/**
 * Default implementation of {@link IApplicationContext}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ApplicationContext
        implements IApplicationContext {

    private Map<String,Object>         data = new HashMap();
    
    private Multimap<IPath,IPanel>     panels = ArrayListMultimap.create();
    
    
    @Override
    public <T extends IPanel> T openSibling( String extId, String name ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public <T extends IPanel> T openChild( String extId, String name ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public Iterable<IPanel> panelsAt( IPath path ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public void addEventHandler( Object handler ) {
        EventManager.instance().subscribe( handler );
    }

    
    @Override
    public void removeEventHandler( Object handler ) {
        EventManager.instance().unsubscribe( handler );
    }
    
    
    @Override
    public <T> T put( String key, T value ) {
        synchronized (data) {
            return (T)data.put( key, value );
        }
    }


    @Override
    public <T> T putIfAbsent( String key, T value ) {
        synchronized (data) {
            T previous = (T)data.put( key, value );
            if (previous != null) {
                data.put( key, previous );
                return previous;
            }
            else {
                return null;
            }
        }
    }


    @Override
    public <T> T get( String key ) {
        return (T)data.get( key );
    }
    
}
