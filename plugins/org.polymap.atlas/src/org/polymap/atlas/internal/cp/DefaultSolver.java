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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultSolver
        implements ISolver {

    private static Log log = LogFactory.getLog( DefaultSolver.class );

    /** Goals and their priorities. */
    private List<IOptimizationGoal>   goals = new ArrayList();

    /** Constraints and their priorities. */
    private List<IConstraint>         constraints = new ArrayList();


    @Override
    public void addGoal( IOptimizationGoal goal ) {
        goals.add( goal );
    }

    public List<IOptimizationGoal> goals() {
        return goals;
    }
    
    @Override
    public void addConstraint( IConstraint constraint ) {
        constraints.add( constraint );
    }
    
    public List<IConstraint> constraints() {
        return constraints;
    }
    
}
