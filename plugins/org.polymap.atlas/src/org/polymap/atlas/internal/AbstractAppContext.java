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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.eclipse.core.runtime.IPath;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.atlas.IAppContext;
import org.polymap.atlas.IPanel;

/**
 * Provides default implementation for property handling and panel hierarchy.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class AbstractAppContext
        implements IAppContext {

    private static Log log = LogFactory.getLog( AbstractAppContext.class );
    
    /** The property suppliers. */
    private Set<ContextSupplier>        suppliers = new HashSet();
    /** The panel hierarchy. */ 
    private Map<IPath,IPanel>           panels = new HashMap();
    
    
    @Override
    public Iterable<IPanel> panels( final IPath path ) {
        return Maps.filterKeys( panels, new Predicate<IPath>() {
            public boolean apply( IPath input ) {
                return path.isPrefixOf( input ) 
                        && path.segmentCount() + 1 == input.segmentCount();
            }
        }).values();
    }

    
    public Iterable<IPanel> allPanels( final IPath path ) {
        return Maps.filterKeys( panels, new Predicate<IPath>() {
            public boolean apply( IPath input ) {
                return path.isPrefixOf( input );
            }
        }).values();
    }

    
    public void addPanel( IPath path, IPanel panel ) {
        if (panels.put( path, panel ) != null) {
            throw new IllegalStateException( "Panel already exists at: " + path );
        }
    }
    

    public void removePanels( IPath path ) {
        throw new RuntimeException( "not implemented yet" );
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
    public void addSupplier( ContextSupplier supplier ) {
        if (!suppliers.add( supplier )) {
            throw new IllegalArgumentException( "Supplier already registered." );
        }
    }

    
    @Override
    public void removeSupplier( ContextSupplier supplier ) {
        suppliers.remove( supplier );
    }

    
    @Override
    public <T> T get( final Object consumer, final String key ) {
        Iterable<T> result = Iterables.transform( suppliers, new Function<ContextSupplier,T>() {
            public T apply( ContextSupplier supplier ) {
                try {
                    return supplier.get( consumer, key );
                }
                catch (Exception e) {
                    log.warn( "", e );
                    return null;
                }
            }
        });
        // filter null values and return first
        return Iterables.getOnlyElement( Iterables.filter( result, Predicates.notNull() ), null );
    }
    
}
