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
package org.polymap.atlas.toolkit;

import org.polymap.atlas.internal.cp.IConstraint;
import org.polymap.atlas.internal.cp.PercentScore;
import org.polymap.atlas.internal.cp.Prioritized;
import org.polymap.atlas.toolkit.ConstraintLayout.LayoutSolution;

/**
 * 
 */
public abstract class LayoutConstraint
        extends Prioritized
        implements IConstraint<LayoutSolution,PercentScore> {
    
    private Integer         priority;

    
    public LayoutConstraint( int priority ) {
        super( priority );
    }
    
    
    //protected abstract void layout( Composite composite, List<LayoutConstraint> constraints );
    
}


