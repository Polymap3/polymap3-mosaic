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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.atlas.IAppContext;
import org.polymap.atlas.IPanel;
import org.polymap.atlas.PanelChangeEvent;
import org.polymap.atlas.PanelIdentifier;
import org.polymap.atlas.PanelPath;

/**
 * Provides default implementation for property handling and panel hierarchy.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultAppContext
        implements IAppContext {

    private static Log log = LogFactory.getLog( DefaultAppContext.class );

    /** The property suppliers. */
    private Set<ContextSupplier>        suppliers = new HashSet();

    /** The panel hierarchy. */
    private Map<PanelPath,IPanel>       panels = new HashMap();


    @Override
    public IPanel openPanel( PanelIdentifier panelId, final String contextKey, final Object contextValue ) {
        addSupplier( new ContextSupplier() {
            public Object get( Object consumer, String key ) {
                return key.equals( contextKey ) ? contextValue : null;
            }
        });
        return openPanel( panelId );
    }


    public IPanel getPanel( PanelPath path ) {
        return panels.get( path );
    }


    @Override
    public Iterable<IPanel> findPanels( Predicate<IPanel> filter ) {
        return Iterables.filter( panels.values(), filter );
    }


    public void addPanel( IPanel panel ) {
        if (panels.put( panel.getSite().getPath(), panel ) != null) {
            throw new IllegalStateException( "Panel already exists at: " + panel.getSite().getPath() );
        }
    }


    public void removePanels( PanelPath path ) {
        throw new RuntimeException( "not implemented yet" );
    }


    @Override
    public void addEventHandler( Object handler, EventFilter<PanelChangeEvent>... filters ) {
        EventManager.instance().subscribe( handler, filters );
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
        for (ContextSupplier supplier : suppliers) {
            try {
                Object value = supplier.get( consumer, key );
                if (value != null) {
                    return (T)value;
                }
            }
            catch (Exception e) {
                log.warn( "", e );
            }
        }
        return null;
    }

}
