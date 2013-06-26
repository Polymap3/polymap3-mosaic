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
package org.polymap.atlas.internal.cp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class Prioritized {

    private static Log log = LogFactory.getLog( Prioritized.class );
    
    /**
     * Sort the {@link Prioritized} elements in <b>descending</b> order.
     *
     * @param elms List of {@link Prioritized} elements.
     */
    public static <T> ArrayList<T> sort( List elms ) {
        ArrayList<T> result = new ArrayList( elms );
        Collections.sort( result, new Comparator<T>() {
            @Override
            public int compare( T o1, T o2 ) {
                Prioritized p1 = (Prioritized)o1;
                Prioritized p2 = (Prioritized)o2;
                return p2.getPriority().compareTo( p1.getPriority() );
            }
        });
        return result;
    }
    
    
    // instance *******************************************
    
    private Comparable          priority;

    
    public Prioritized( Comparable priority ) {
        this.priority = priority;
    }
    
    public Comparable getPriority() {
        return priority;
    }

}
