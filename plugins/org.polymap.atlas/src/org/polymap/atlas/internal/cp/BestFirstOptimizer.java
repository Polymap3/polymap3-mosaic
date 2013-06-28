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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.Timer;

/**
 * 
 * <p>
 * This optimizer finds also invalid solutions that fail on one or more given
 * constraints.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BestFirstOptimizer
        extends DefaultSolver {

    private static Log log = LogFactory.getLog( BestFirstOptimizer.class );

    private int                             timeoutMillis;

    /** The surrogates of the seen solutions so far. */
    private Set<String>                     seen = new HashSet();
    
    private SolutionQueue<ExScoredSolution> queue, terminals;
    
    
    /**
     * 
     * @param timeoutMillis Max time in milliseconds to spent in optimizer.
     * @param maxQueueSize
     */
    public BestFirstOptimizer( int timeoutMillis, int maxQueueSize ) {
        this.timeoutMillis = timeoutMillis;
        this.queue = new SolutionQueue( maxQueueSize );
        this.terminals = new SolutionQueue( maxQueueSize );
    }

    
    @Override
    public List<ScoredSolution> solve( ISolution start ) {
        //assert goals().size() > 0;
        
        Timer timer = new Timer();
        ArrayList<IOptimizationGoal> goals = Prioritized.sort( goals() );
        List<IConstraint> constraints = constraints();

        // start solution
        queue.add( new ExScoredSolution( start, PercentScore.NULL ) );
        
        //
        int loops = 0; 
        for ( ;!queue.isEmpty() && timer.elapsedTime() < timeoutMillis; loops++) {
            // current best solution
            ExScoredSolution best = queue.getLast();
            
            // find next optimization step
            ISolution optimized = null;
            while (optimized == null && best.goalsIndex < goals.size()) {
                IOptimizationGoal goal = goals.get( best.goalsIndex );

                optimized = best.solution.copy();
                if (!goal.optimize( optimized )
                        || seen.contains( optimized.surrogate() )) {
                    optimized = null;
                }
                best.goalsIndex ++;
            }
            // no optimization found -> terminal
            if (optimized == null) {
                queue.removeLast();
                terminals.add( best );
            }
            // score optimized solution -> queue
            else {
                IScore optimizedScore = new PercentScore( 1 );
                for (IOptimizationGoal goal : goals) {
                    IScore s = goal.score( optimized );
                    optimizedScore = optimizedScore != null ? optimizedScore.add( s ) : s;
                }
                for (IConstraint constraint : constraints) {
                    IScore s = constraint.score( optimized );
                    optimizedScore = optimizedScore.add( s );
                }
                queue.add( new ExScoredSolution( optimized, optimizedScore ) );
                seen.add( optimized.surrogate() );
            }            
        }
        
        SolutionQueue<ScoredSolution> result = new SolutionQueue( queue.maxSize );
        result.addAll( queue );
        result.addAll( terminals );
        log.info( "solutions found: queue=" + queue.size() + ", terminals=" + terminals.size() 
                + ", maxScore=" + result.getLast().score
                + ", loops=" + loops + ", seen=" + seen.size()
                + " (" + timer.elapsedTime() + "ms)" );
        return result;
    }


    /**
     * Bound priority queue. 
     */
    class SolutionQueue<T extends ScoredSolution>
            extends LinkedList<T> {
    
        protected int         maxSize;
    
        public SolutionQueue( int maxSize ) {
            this.maxSize = maxSize;
        }
    
        @Override
        public boolean add( T elm ) {
            int index = Collections.binarySearch( this, elm );
            if (index >= 0) {
                super.add( index, elm );
            } else {
                super.add( ~index, elm );                
            }
            if (size() > maxSize) {
                remove( 0 );
            }
            return true;
        }
    }


    /**
     * 
     */
    class ExScoredSolution
            extends ScoredSolution {
        
        public int      goalsIndex = 0;
    
        public ExScoredSolution( ISolution solution, IScore score ) {
            super( solution, score );
        }
    }
    
}
