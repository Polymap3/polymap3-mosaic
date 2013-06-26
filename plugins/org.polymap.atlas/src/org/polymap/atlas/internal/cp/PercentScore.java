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

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PercentScore
        implements IScore {

    public static final PercentScore NULL = new PercentScore( 0 );
    
    public static final PercentScore INVALID = new PercentScore( -1 );
    
    private int             value;
    
    
    public PercentScore( int value ) {
        assert value == -1 || value >= 0 && value <= 100: "Value must be in the range 0..100: " + value;
        this.value = value;
    }

    @Override
    public String toString() {
        return "PercentScore[" + value + "%]";
    }

    public int getValue() {
        return value;
    }
    
    @Override
    public IScore add( IScore o ) {
        PercentScore other = (PercentScore)o;
        return other != INVALID ? new PercentScore( (value + other.value / 2 ) ) : INVALID;
    }

    @Override
    public IScore prioritize( int priority ) {
        assert priority >= 1;
        int remain = 100 - value;
        return new PercentScore( value + (remain - (remain / priority)) );
    }

    @Override
    public int compareTo( IScore o ) {
        return value - ((PercentScore)o).value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals( Object obj ) {
        if (obj == this) {
            return true;
        }
        else if (obj instanceof PercentScore) {
            return value == ((PercentScore)obj).value;            
        }
        return false;
    }

}
