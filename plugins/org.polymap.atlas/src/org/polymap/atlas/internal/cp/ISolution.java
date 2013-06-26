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
 * <p/>
 * Note that a solution implementation MUST provide a {@link #hashCode()} method.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ISolution {
        //extends Cloneable {

    public <S extends ISolution> S copy();
    
    
    /**
     * The surrogate (hash) code of a solution is used to identify this solution for
     * backtracking. Sub-classes MUST provide an implementation.
     */
    public String surrogate();
    
}
