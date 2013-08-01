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

import org.polymap.atlas.internal.cp.PercentScore;
import org.polymap.atlas.toolkit.ConstraintLayout.LayoutSolution;

/**
 * The priority of an element inside a container. Higher priorities are displayed
 * more eye-catching. In most cases this means that higher priorities are displayed
 * on top of the panel.
 */
public class PriorityConstraint
        extends LayoutConstraint {
    
    private int         value = -1;


    public PriorityConstraint( int value, int priority ) {
        super( priority );
        this.value = value;
    }

    
    public int getValue() {
        return value;
    }


    @Override
    public PercentScore score( LayoutSolution solution ) {
        // this is soft constraint; it is enforced by the optimizer goal
        return PercentScore.NULL;
    }
    
}