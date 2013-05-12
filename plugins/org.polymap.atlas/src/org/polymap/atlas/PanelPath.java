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
package org.polymap.atlas;

import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterators;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PanelPath
        implements Iterable<PanelIdentifier> {

    private static Log log = LogFactory.getLog( PanelPath.class );

    public static final PanelPath       ROOT = new PanelPath();


    private ArrayList<PanelIdentifier>  segments = new ArrayList();


    protected PanelPath() {
    }

    public PanelPath( PanelIdentifier root ) {
        segments.add( root );
    }

    public PanelPath( PanelPath other ) {
        segments.addAll( other.segments );
    }

    public PanelIdentifier segment( int index ) {
        return segments.get( index );
    }

    public PanelIdentifier lastSegment() {
        return segments.get( segments.size() - 1 );
    }

    public PanelPath append( PanelIdentifier next ) {
        PanelPath result = new PanelPath( this );
        result.segments.add( next );
        return result;
    }

    public PanelPath removeLast( int count ) {
        assert count >= 0;
        PanelPath result = new PanelPath( this );
        for (int i=count; i>0; i--) {
            result.segments.remove( i );
        }
        return result;
    }

    public int size() {
        return segments.size();
    }

//    public boolean isRoot() {
//        throw new RuntimeException( "not yet implemented" );
//    }

    public boolean isPrefixOf( PanelPath path ) {
        return segments.size() <= path.segments.size()
                && segments.equals( path.segments.subList( 0, segments.size() ) );
    }

    @Override
    public Iterator<PanelIdentifier> iterator() {
        return Iterators.unmodifiableIterator( segments.iterator() );
    }

    @Override
    public int hashCode() {
        return segments.hashCode();
    }

    @Override
    public boolean equals( Object obj ) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof PanelPath) {
            PanelPath other = (PanelPath)obj;
            return segments.equals( other.segments );
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder( 128 ).append( "PanelPath[" );
        for (PanelIdentifier segment : segments) {
            buf.append( "/" ).append( segment.id() );
        }
        return buf.append( "]" ).toString();
    }

}
