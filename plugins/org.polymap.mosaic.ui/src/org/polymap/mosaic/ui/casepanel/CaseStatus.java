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

import java.util.HashMap;
import java.util.Map;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.ImmutableSortedSet;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;

/**
 * Represents the elements of the status section of an {@link CasePanel}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CaseStatus {

    private static Log log = LogFactory.getLog( CaseStatus.class );
    
    public static final int             DEFAULT_PRIORITY = 0;
    
    private Map<String,Entry>           entries = new HashMap();
    
    
    public static class Entry
            implements Comparable<Entry> {
        
        private String      key;
        private String      value;
        private int         priority;
        
        protected Entry( String key, String value, int priority ) {
            this.key = key;
            this.value = value;
            this.priority = priority;
        }
        public String getKey() {
            return key;
        }
        public String getValue() {
            return value;
        }
        public int compareTo( Entry rhs ) {
            return priority != rhs.priority ? rhs.priority - priority : rhs.hashCode() - hashCode();
        }
    }
    
    public void addListener( PropertyChangeListener listener ) {
        EventManager.instance().subscribe( listener, new EventFilter<PropertyChangeEvent>() {
            public boolean apply( PropertyChangeEvent input ) {
                return input.getSource() == CaseStatus.this;
            }
        });
    }
    
    public boolean removeListener( PropertyChangeListener listener ) {
        return EventManager.instance().unsubscribe( listener );
    }
    
    public boolean put( String key, String value, int priority ) {
        Entry found = entries.get( key );
        boolean result;
        if (found != null) {
            found.priority = priority;
            found.value = value;
            result = false;
        }
        else {
            entries.put( key, new Entry( key, value, priority ) );
            result = true;
        }
        EventManager.instance().publish( new PropertyChangeEvent( this, key, null, value ) );
        return result;
    }
    
    public boolean put( String key, String value ) {
        Entry found = entries.get( key );
        boolean result;
        if (found != null) {
            found.value = value;
            result = false;
        }
        else {
            entries.put( key, new Entry( key, value, DEFAULT_PRIORITY ) );
            result = true;
        }
        EventManager.instance().publish( new PropertyChangeEvent( this, key, null, value ) );
        return result;
    }
    
    /**
     * Sorted entries of this status instance.
     */
    public Iterable<Entry> entries() {
        return ImmutableSortedSet.copyOf( entries.values() );
    }

//    public Set<String> keys() {
//    }
//
//    public Set<String> values() {
//    }
}
