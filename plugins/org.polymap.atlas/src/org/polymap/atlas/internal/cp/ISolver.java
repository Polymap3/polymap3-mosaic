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

import java.util.List;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ISolver {

    public void addGoal( IOptimizationGoal goal );

    public void addConstraint( IConstraint constraint );
    
    public List<ScoredSolution> solve( ISolution start );
    
    
    /**
     * 
     */
    public class ScoredSolution
            implements Comparable<ScoredSolution>{
        
        public ISolution    solution;
        
        public IScore       score;

        public ScoredSolution( ISolution solution, IScore score ) {
            assert solution != null && score != null;
            this.solution = solution;
            this.score = score;
        }

        @Override
        public int compareTo( ScoredSolution other ) {
            return score.compareTo( other.score );
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((score == null) ? 0 : score.hashCode());
            result = prime * result + ((solution == null) ? 0 : solution.hashCode());
            return result;
        }

        @Override
        public boolean equals( Object obj ) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof ScoredSolution) {
                ScoredSolution other = (ScoredSolution)obj;
                return solution.equals( other.solution ) && score.equals( other.score );
            }
            return false;
        }
    }
    
}
