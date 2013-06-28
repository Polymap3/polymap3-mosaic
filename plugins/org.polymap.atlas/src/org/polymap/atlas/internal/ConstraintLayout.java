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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

import org.polymap.atlas.internal.cp.BestFirstOptimizer;
import org.polymap.atlas.internal.cp.IOptimizationGoal;
import org.polymap.atlas.internal.cp.ISolution;
import org.polymap.atlas.internal.cp.ISolver;
import org.polymap.atlas.internal.cp.ISolver.ScoredSolution;
import org.polymap.atlas.internal.cp.PercentScore;
import org.polymap.atlas.internal.cp.Prioritized;
import org.polymap.atlas.toolkit.ConstraintData;
import org.polymap.atlas.toolkit.LayoutConstraint;
import org.polymap.atlas.toolkit.MaxWidthConstraint;
import org.polymap.atlas.toolkit.MinWidthConstraint;
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
            ISolver solver = new BestFirstOptimizer( 200, 10 );
            solver.addGoal( new PriorityOnTopGoal( 2 ) );
//            solver.addGoal( new MinOverallHeightGoal( composite.getClientArea().height, 2 ) );
//            solver.addGoal( new MaxColumnsGoal( this ), 1 );
//            solver.addGoal( new ElementRelationGoal( this ), 1 );

            for (Control child : composite.getChildren()) {
                ConstraintData data = (ConstraintData)child.getLayoutData();
                if (data != null) {
                    data.fillSolver( solver );
                }
            }
            LayoutSolution start = new LayoutSolution( composite );
            start.justifyElements();
            
            List<ScoredSolution> results = solver.solve( start );
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
    public class LayoutSolution
            implements ISolution {

        public Composite                composite;
        
        public ArrayList<LayoutColumn>  columns = new ArrayList( 3 );
        
        
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
            int result = 1;
            for (LayoutColumn column : columns) {
                result = 31 * result + column.width;
                for (LayoutElement elm : column) {
                    result = 31 * result + elm.hashCode();
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
        
        public LayoutElement remove( int index ) {
            int c = 0;
            for (LayoutColumn column : columns) {
                for (Iterator<LayoutElement> it=column.iterator(); it.hasNext(); c++) {
                    LayoutElement elm = it.next();
                    if (c == index) {
                        it.remove();
                        return elm;
                    }
                }
            }
            throw new IllegalArgumentException( "Invalid index: index=" + index + ", size=" + c );
        }
        
        public void justifyElements() {
            int clientWidth = composite.getClientArea().width;
            int columnWidth = (clientWidth / columns.size()) - (marginWidth*2) - ((columns.size()-1) * spacing);
            
            // compute columns width
            for (LayoutColumn column : columns) {
                column.width = column.computeMaxWidth( columnWidth );
            }
            // set element heights
            for (LayoutColumn column : columns) {
                column.justifyElements();
            }
        }
    }
    

    /**
     * 
     */
    public static class LayoutColumn
            extends ArrayList<LayoutElement> {
    
        public int      width;
        
        public LayoutColumn( List<Control> controls ) {
            super( controls.size() );
            for (Control control : controls) {
                add( new LayoutElement( control ) );
            }
        }

        public LayoutColumn( LayoutColumn other ) {
            super( other );
            this.width = other.width;
        }

        public int computeMaxWidth( int wHint ) {
            int result = wHint;
            for (LayoutElement elm : this) {
                result = Math.min( result, elm.computeWidth( wHint ) );
            }
            return Math.min( wHint, result );
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
    public static class LayoutElement {
        
        public Control  control;
        
        public int      height;
        
        public LayoutElement( Control control ) {
            assert control != null;
            this.control = control;
        }

        public <T extends LayoutConstraint> T constraint( Class<T> type, T defaultValue ) {
            ConstraintData data = (ConstraintData)control.getLayoutData();
            return data != null ? data.constraint( type, defaultValue ) : defaultValue;
        }

        public int computeWidth( int wHint ) {
            int width = control.computeSize( wHint, SWT.DEFAULT ).x;
            // min constraint
            MinWidthConstraint minConstraint = constraint( MinWidthConstraint.class, null );
            width = minConstraint != null ? Math.max( minConstraint.getValue(), width ) : width;
            // max constraint
            MaxWidthConstraint maxConstraint = constraint( MaxWidthConstraint.class, null );
            width = maxConstraint != null ? Math.min( maxConstraint.getValue(), width ) : width;
            return width;
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
    static class MinOverallHeightGoal
            extends Prioritized
            implements IOptimizationGoal<LayoutSolution,PercentScore> {

        private static final Random rand = new Random();
        
        private int                 clientHeight;

        public MinOverallHeightGoal( int clientHeight, Comparable priority ) {
            super( priority );
            this.clientHeight = clientHeight;
        }

        @Override
        public boolean optimize( LayoutSolution solution ) {
            int elmIndex = rand.nextInt( solution.elements().size() );
            LayoutElement elm = solution.remove( elmIndex );
            
            if (solution.columns.size() == 1) {
                solution.columns.add( new LayoutColumn( Collections.singletonList( elm.control ) ) );
            }
            else {
                int columnIndex = rand.nextInt( solution.columns.size() );
                LayoutColumn column = solution.columns.get( columnIndex );
                column.add( elm );
            }
            solution.justifyElements();
            return true;
        }

        @Override
        public PercentScore score( LayoutSolution solution ) {
            int maxColumnHeight = 0, minColumnHeight = Integer.MAX_VALUE;
            for (LayoutColumn column : solution.columns) {
                // avoid random column produced by the random optimization
                if (column.size() == 0) {
                    return PercentScore.INVALID;
                }
                
                int columnHeight = 0;
                for (LayoutElement elm : column) {
                    assert elm.height > 0;
                    columnHeight += elm.height;
                }
                maxColumnHeight = Math.max( columnHeight, maxColumnHeight );
                minColumnHeight = Math.min( columnHeight, minColumnHeight );
            }
            // kann größer als 100 sein
            int heightPercent = (int)(100d / clientHeight * maxColumnHeight);
            PercentScore result = new PercentScore( 100 - Math.min( 100, heightPercent ) );
            assert result.getValue() >= 0;
            return result;
        }
        
    }

}
