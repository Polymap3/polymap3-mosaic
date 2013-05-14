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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.atlas.ContextProperty;
import org.polymap.atlas.IAppContext;
import org.polymap.atlas.IPanel;
import org.polymap.atlas.PanelChangeEvent;
import org.polymap.atlas.PanelPath;

/**
 * Provides default implementation for property handling and panel hierarchy.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultAppContext
        implements IAppContext {

    private static Log log = LogFactory.getLog( DefaultAppContext.class );

    /**
     * 
     */
    class PropertyValue {
        protected Object        value;
        protected String        scope;

        public PropertyValue( Object value, String scope ) {
            this.value = value;
            this.scope = scope;
        }
    }

    
    // instance *******************************************
    
    /** The property suppliers. */
    private List<PropertyValue>             properties = new ArrayList();

    /** The panel hierarchy. */
    private Map<PanelPath,IPanel>           panels = new HashMap();


    public IPanel getPanel( PanelPath path ) {
        return panels.get( path );
    }


    @Override
    public Iterable<IPanel> findPanels( Predicate<IPanel> filter ) {
        // make a copy so that contents is stable while iterating (remove)
        return new ArrayList( Collections2.filter( panels.values(), filter ) );
    }


    public void addPanel( IPanel panel ) {
        if (panels.put( panel.getSite().getPath(), panel ) != null) {
            throw new IllegalStateException( "Panel already exists at: " + panel.getSite().getPath() );
        }
    }


    public void removePanel( PanelPath path ) {
        if (panels.remove( path ) == null) {
            throw new IllegalStateException( "No Panel exists at: " + path );
        }
    }

    
//    protected IPanel openPanel( final PanelIdentifier panelId ) {
//        // find and initialize panels
//        final PanelPath prefix = activePanel != null ? activePanel.getSite().getPath() : PanelPath.ROOT;
//        List<IPanel> createdPanels = AtlasComponentFactory.instance().createPanels( new Predicate<IPanel>() {
//            public boolean apply( IPanel panel ) {
//                new PanelContextInjector( panel, DefaultAppContext.this ).run();
//                PanelPath path = prefix.append( panel.id() );
//                boolean wantsToBeShown = panel.init( new DesktopPanelSite( path ), this );
//                return panel.id().equals( panelId ) || wantsToBeShown;
//            }
//        });
//
//        // add to context
//        for (IPanel panel : createdPanels) {
//            addPanel( panel );
//        }
//
//        //
//        IPanel panel = getPanel( prefix.append( panelId ) );
//        if (panel == null) {
//            throw new IllegalStateException( "No panel for ID: " + panelId );
//        }
//        
//        EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.OPENING ) );
//        
//        Composite page = scrolledPanelContainer.createPage( panel.id() );
//        page.setLayout( new FillLayout() );
//        panel.createContents( page );
//        page.layout( true );
//        scrolledPanelContainer.showPage( panel.id() );
//        
//        Point panelSize = page.computeSize( SWT.DEFAULT, SWT.DEFAULT );
//        scrolledPanelContainer.setMinHeight( panelSize.y );
//
//        activePanel = panel;
//        EventManager.instance().publish( new PanelChangeEvent( panel, TYPE.OPENED ) );
//
//        return activePanel;
//    }


    @Override
    public void addEventHandler( Object handler, EventFilter<PanelChangeEvent>... filters ) {
        EventManager.instance().subscribe( handler, filters );
    }


    @Override
    public void removeEventHandler( Object handler ) {
        EventManager.instance().unsubscribe( handler );
    }


    public Object getPropertyValue( ContextProperty prop ) {
        PropertyValue result = findPropertyValue( prop );
        return result != null ? result.value : null;
    }
    

    public Object setPropertyValue( ContextProperty prop, Object value ) {
        PropertyValue found = findPropertyValue( prop );
        if (found != null) {
            Object result = found.value;
            found.value = value;
            return result;
        }
        else {
            properties.add( new PropertyValue( value, prop.getScope() ) );
            return null;
        }
    }

    
    protected PropertyValue findPropertyValue( ContextProperty prop ) {
        PropertyValue result = null;
        for (PropertyValue value : properties) {
            if (value.scope.equals( prop.getScope() )
                    && prop.getDeclaredType().isAssignableFrom( value.value.getClass() )) {
                if (result != null) {
                    throw new IllegalStateException( "Several matches for context property: " + prop );                    
                }
                result = value;
            }
        }
        return result;
    }
    

//    protected int subTypeDistance( Class<?> type, Class<?> subType ) {
//        Class<?> cursor = subType;
//        for (int i=0; cursor != null; i++) {
//            if (cursor.equals( type )) {
//                return i;
//            }
//            cursor = subType.getSuperclass();
//        }
//        throw new RuntimeException( "Types are not related: " + type.getSimpleName() + " - " + subType.getSimpleName() );
//    }

}