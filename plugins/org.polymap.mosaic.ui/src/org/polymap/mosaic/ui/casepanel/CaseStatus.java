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

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.ImmutableSortedSet;

/**
 * Represents the elements of the status section of an {@link CasePanel}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CaseStatus {

    private static Log log = LogFactory.getLog( CaseStatus.class );
    
    private List<Entry>         entries = new ArrayList();
    
    
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
    
    
    public boolean put( String key, String value, int priority ) {
        // FIXME no key check yet
        return entries.add( new Entry( key, value, priority ) );
    }
    
    /**
     * Sorted entries of this status instance.
     */
    public Iterable<Entry> entries() {
        return ImmutableSortedSet.copyOf( entries );
    }

//    public Set<String> keys() {
//    }
//
//    public Set<String> values() {
//    }
}
