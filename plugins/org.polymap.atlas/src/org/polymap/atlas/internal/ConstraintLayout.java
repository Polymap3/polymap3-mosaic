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
package org.polymap.atlas.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

import org.polymap.atlas.internal.cp.BestFirstBacktrackSolver;
import org.polymap.atlas.internal.cp.IOptimizationGoal;
import org.polymap.atlas.internal.cp.ISolution;
import org.polymap.atlas.internal.cp.ISolver;
import org.polymap.atlas.internal.cp.ISolver.ScoredSolution;
import org.polymap.atlas.internal.cp.PercentScore;
import org.polymap.atlas.internal.cp.Prioritized;
import org.polymap.atlas.toolkit.ConstraintData;
import org.polymap.atlas.toolkit.LayoutConstraint;
import org.polymap.atlas.toolkit.PriorityConstraint;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ConstraintLayout
        extends Layout {

    private static Log log = LogFactory.getLog( ConstraintLayout.class );

    public int                  marginWidth = 8;

    public int                  marginHeight = 8;
    
    public int                  spacing = 8;
    
    private LayoutSolution      solution;

    
    private void computeSolution( Composite composite, boolean flushCache ) {
        assert solution == null || solution.composite == composite;
        
        if (solution == null || flushCache) {
            ISolver solver = new BestFirstBacktrackSolver( 100, 10 );
            solver.addGoal( new ElementsFitColumnGoal( 3 ) );
//            solver.addGoal( new PriorityOnTopGoal( 2 ) );
//            solver.addGoal( new MaxColumnsGoal( this ), 1 );
//            solver.addGoal( new ElementRelationGoal( this ), 1 );

            for (Control child : composite.getChildren()) {
                ConstraintData data = (ConstraintData)child.getLayoutData();
                if (data != null) {
                    data.fillSolver( solver );
                }
            }
            List<ScoredSolution> results = solver.solve( new LayoutSolution( composite ) );
            solution = (LayoutSolution)results.get( results.size()-1 ).solution;
        }
    }
    
    
    @Override
    protected void layout( Composite composite, boolean flushCache ) {
        // compute solution
        computeSolution( composite, flushCache );

        // layout elements
        Rectangle clientArea = composite.getClientArea();
        int colX = marginWidth;

        for (LayoutColumn column : solution.columns) {
            assert column.width > 0;
            int elmY = marginHeight;

            for (LayoutElement elm : column) {
                assert elm.height >= 0;
                elm.control.setBounds( colX, elmY, column.width, elm.height );
                
                elmY += elm.height + spacing;
            }
            
            colX += column.width + spacing;
        }
    }

    
    @Override
    protected Point computeSize( Composite composite, int wHint, int hHint, boolean flushCache ) {
        // compute solution
        computeSolution( composite, flushCache );
        
        return new Point( 250, 250 );
    }

    
    /**
     * 
     */
    static class LayoutSolution
            implements ISolution {

        public Composite                composite;
        
        public List<LayoutColumn>       columns = new ArrayList();
        
        
        public LayoutSolution( Composite composite ) {
            this.composite = composite;
            this.columns.add( new LayoutColumn( Arrays.asList( composite.getChildren() ) ) );
        }
        
        public LayoutSolution( LayoutSolution other ) {
            this.composite = other.composite;
            for (LayoutColumn column : other.columns) {
                columns.add( new LayoutColumn( column ) );                
            }
        }

        @Override
        public String surrogate() {
            int result = 0;
            for (LayoutColumn column : columns) {
                for (LayoutElement elm : column) {
                    result ^= elm.hashCode();                
                }
            }
            return String.valueOf( result );
        }

        @Override
        public LayoutSolution copy() {
            return new LayoutSolution( this );
        }

        /** Returns a new List containing all elements. */
        public List<LayoutElement> elements() {
            List<LayoutElement> result = new ArrayList();
            for (LayoutColumn column : columns) {
                result.addAll( column );
            }
            return result;
        }
    }
    

    /**
     * 
     */
    static class LayoutColumn
            extends ArrayList<LayoutElement> {
    
        public int      width;
        
        public LayoutColumn( List<Control> controls ) {
            super( controls.size() );
            for (Control control : controls) {
                add( new LayoutElement( control ) );
            }
        }

        public LayoutColumn( Collection<? extends LayoutElement> c ) {
            super( c );
        }

        public int computeMinWidth( int wHint ) {
            int result = 0;
            for (LayoutElement elm : this) {
                Point elmSize = elm.control.computeSize( wHint, SWT.DEFAULT );
                result = Math.max( result, elmSize.x );
            }
            return result;
        }
        
        public void justifyElements() {
            assert width > 0;
            for (LayoutElement elm : this) {
                elm.height = elm.control.computeSize( width, SWT.DEFAULT ).y;
            }
        }
    }


    /**
     * 
     */
    static class LayoutElement {
        
        public Control  control;
        
        public int      x, y, height;
        
        public LayoutElement( Control control ) {
            assert control != null;
            this.control = control;
        }

        public <T extends LayoutConstraint> T constraint( Class<T> type, T defaultValue ) {
            ConstraintData data = (ConstraintData)control.getData();
            return data != null ? data.constraint( type ) : defaultValue;
        }

        @Override
        public int hashCode() {
            return control.hashCode();
        }

        @Override
        public boolean equals( Object obj ) {
            return control.equals( ((LayoutElement)obj).control );
        }
    }
    
    
    /**
     * 
     */
    static class ElementsFitColumnGoal
            extends Prioritized
            implements IOptimizationGoal<LayoutSolution,PercentScore> {

        public ElementsFitColumnGoal( Comparable priority ) {
            super( priority );
        }

        @Override
        public boolean optimize( LayoutSolution solution ) {
            // compute columns width
            for (LayoutColumn column : solution.columns) {
                column.width = Math.max( 100, column.computeMinWidth( 100 ) );
            }
            // set element heights
            for (LayoutColumn column : solution.columns) {
                column.justifyElements();
            }
            return true;
        }

        @Override
        public PercentScore score( LayoutSolution solution ) {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }
    }
    
    
    /**
     * 
     */
    static class PriorityOnTopGoal
            extends Prioritized
            implements IOptimizationGoal<LayoutSolution,PercentScore> {

        public PriorityOnTopGoal( int priority ) {
            super( priority );
        }

        @Override
        public boolean optimize( LayoutSolution solution ) {
            for (LayoutColumn column : solution.columns) {
                LayoutElement prev = null;
                int index = 0;
                for (LayoutElement elm : column) {
                    if (prev != null) {
                        PriorityConstraint prevPrio = prev.constraint( PriorityConstraint.class, new PriorityConstraint( 0, 0 ) );
                        PriorityConstraint elmPrio = elm.constraint( PriorityConstraint.class, new PriorityConstraint( 0, 0 ) );
                        
                        if (prevPrio.getValue() < elmPrio.getValue()) {
                            column.set( index-1, elm );
                            column.set( index, prev );
                            return true;
                        }
                    }
                    prev = elm;
                    ++index;
                }
            }
            return false;
        }

        @Override
        public PercentScore score( LayoutSolution solution ) {
            int elmPercent = 100 / solution.elements().size();
            int result = 100;
            
            for (LayoutColumn column : solution.columns) {
                LayoutElement prev = null;
                int index = 0;
                for (LayoutElement elm : column) {
                    if (prev != null) {
                        PriorityConstraint prevPrio = prev.constraint( PriorityConstraint.class, new PriorityConstraint( 0, 0 ) );
                        PriorityConstraint elmPrio = elm.constraint( PriorityConstraint.class, new PriorityConstraint( 0, 0 ) );
                        
                        if (prevPrio.getValue() < elmPrio.getValue()) {
                            result -= elmPercent;
                        }
                    }
                    prev = elm;
                    ++index;
                }
            }
            return new PercentScore( result );
        }
    }


    /**
     * 
     */
    static class EqualColumnHeightGoal
            implements IOptimizationGoal<LayoutSolution,PercentScore> {

        @Override
        public boolean optimize( LayoutSolution solution ) {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }

        @Override
        public PercentScore score( LayoutSolution solution ) {
            // XXX Auto-generated method stub
            throw new RuntimeException( "not yet implemented." );
        }
        
    }

}
