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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ConstraintLayout
        extends Layout {

    private static Log log = LogFactory.getLog( ConstraintLayout.class );

    public int                  marginWidth = 10;

    public int                  marginHeight = 10;
    
    public int                  spacing = 10;
    
    private LayoutSolution      solution;
    
    private Rectangle           clientArea;

    
    private boolean computeSolution( Composite composite, boolean flushCache ) {
        assert solution == null || solution.composite == composite;
        
        if (solution == null || flushCache) {
            clientArea = composite.getClientArea();
            if (clientArea.width <= 0 || clientArea.width > 1800
                    || clientArea.height < 0) {
                //log.info( "Invalid client area: " + clientArea );
                return false;
            }
            
            log.info( "LAYOUT: " + composite.hashCode() + " -> " + clientArea );
            
            ISolver solver = new BestFirstOptimizer( 200, 10 );
            solver.addGoal( new PriorityOnTopGoal( 1 ) );
            solver.addGoal( new MinOverallHeightGoal( Math.max( 1000, clientArea.height ), 2 ) );
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
        return solution != null;
    }
    
    
    @Override
    protected void layout( Composite composite, boolean flushCache ) {
        // compute solution
        if (computeSolution( composite, flushCache )) {
            // layout elements
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
    }

    
    @Override
    protected Point computeSize( Composite composite, int wHint, int hHint, boolean flushCache ) {
        Point result = null;
        
        // compute solution
        if (computeSolution( composite, flushCache )) {
            // find heighest column
            LayoutColumn maxColumn = null;
            for (LayoutColumn column : solution.columns) {
                maxColumn = maxColumn == null || column.height >= maxColumn.height ? column : maxColumn;
            }
            int height = maxColumn.height + (2*marginHeight) + ((maxColumn.size()-1)*spacing);
            result = new Point( SWT.DEFAULT, height );
            //log.info( "    computeSize: " + result );
        }
        else {
            result = new Point( wHint, hHint );
        }
        return result;
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
            assert clientArea.width > 0;
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
        
        /** Initialize columns width/height. */
        public void justifyElements() {
            int columnWidth = defaultColumnWidth();
            // compute columns width
            for (LayoutColumn column : columns) {
                column.width = columnWidth;
            }
            // set element heights
            for (LayoutColumn column : columns) {
                column.justifyElements();
            }
        }

        /** Equal width for all columns. */
        public int defaultColumnWidth() {
            int result = (clientArea.width - (marginWidth*2) - ((columns.size()-1) * spacing)) / columns.size();
            assert result > 0;
            return result;
        }
    }
    

    /**
     * 
     */
    public static class LayoutColumn
            extends ArrayList<LayoutElement> {
    
        public int      width, height;
        
        public LayoutColumn( List<Control> controls ) {
            super( controls.size() );
            for (Control control : controls) {
                add( new LayoutElement( control ) );
            }
        }

        public LayoutColumn( LayoutColumn other ) {
            super( other );
            this.width = other.width;
            this.height = other.height;
        }

        public int computeMinWidth( int wHint ) {
            // minimum: wHint == 0
            int result = wHint;
            for (LayoutElement elm : this) {
                result = Math.max( result, elm.computeWidth( wHint ) );
            }
            return result;
        }
        
        public void justifyElements() {
            assert width > 0;
            height = 0;
            for (LayoutElement elm : this) {
                elm.height = elm.control.computeSize( width, SWT.DEFAULT ).y;
                height += elm.height;
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
            // sort ascending height
            ArrayList<LayoutElement> heightSortedElms = new ArrayList( solution.elements() );
            Collections.sort( heightSortedElms, new Comparator<LayoutElement>() {
                public int compare( LayoutElement elm1, LayoutElement elm2 ) {
                    assert elm1.height > 0 && elm2.height > 0;
                    return elm1.height - elm2.height;
                }
            });
            
            
//            // sort (descending) all elms using their y pos in column
//            Multimap<Integer,LayoutElement> heightSortedElms = TreeMultimap.create( Ordering.natural(), Ordering.arbitrary() );
//            for (LayoutColumn column : solution.columns) {
//                int y = 0; //Integer.MAX_VALUE;
//                for (LayoutElement elm : column) {
//                    heightSortedElms.put( y, elm );
//                    y += elm.height;
//                }
//            }

            int elmPercent = 100 / solution.elements().size();
            int result = 0;

            LayoutElement prev = null;
            for (LayoutElement elm : heightSortedElms) {
                if (prev != null) {
                    PriorityConstraint prevPrio = prev.constraint( PriorityConstraint.class, new PriorityConstraint( 0, 0 ) );
                    PriorityConstraint elmPrio = elm.constraint( PriorityConstraint.class, new PriorityConstraint( 0, 0 ) );

                    if (prevPrio.getValue() > elmPrio.getValue()) {
                        result += elmPercent;
                    }
                }
                prev = elm;
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
            assert clientHeight > 0;
            this.clientHeight = clientHeight;
        }

        @Override
        public boolean optimize( LayoutSolution solution ) {
            // first idea: more than 1 column
            if (solution.columns.size() == 1) {
                solution.columns.add( new LayoutColumn( Collections.EMPTY_LIST ) );
            }

            // find min/max height column
            LayoutColumn minColumn = null, maxColumn = null;
            for (LayoutColumn column : solution.columns) {
                minColumn = minColumn == null || column.height < minColumn.height ? column : minColumn;
                maxColumn = maxColumn == null || column.height >= maxColumn.height ? column : maxColumn;
            }
            assert minColumn != maxColumn : "minColumn:" + minColumn.height + ", maxColumn:" + maxColumn.height;
            
            // biggest column has just 1 elm -> nothing to optimize
            if (maxColumn.size() == 1) {
                return false;
            }
            
            LayoutElement elm = maxColumn.remove( maxColumn.size() - 1 );
            minColumn.add( 0, elm );

            solution.justifyElements();
            return true;
        }

        @Override
        public PercentScore score( LayoutSolution solution ) {
            int maxColumnHeight = 0, minColumnHeight = Integer.MAX_VALUE;
            int columnWidth = solution.defaultColumnWidth();
            
            for (LayoutColumn column : solution.columns) {
                // avoid empty column produced by the random optimization
                if (column.size() == 0) {
                    return PercentScore.INVALID;
                }
                // avoid columns to small for columnWidth
                if (column.computeMinWidth( 0 ) > columnWidth) {
                    return PercentScore.INVALID;
                }
                
                assert column.height >= 0;
                maxColumnHeight = Math.max( column.height, maxColumnHeight );
                minColumnHeight = Math.min( column.height, minColumnHeight );
            }
            
//            // result: ratio = max/min
//            double ratio = (100d / maxColumnHeight * minColumnHeight);
//            PercentScore result = new PercentScore( (int)ratio );
            
            // might be > 100
            int heightPercent = (int)(100d / clientHeight * maxColumnHeight);
            PercentScore result = new PercentScore( 100 - Math.min( 100, heightPercent ) );
            assert result.getValue() >= 0 && result.getValue() <= 100;
            return result;
        }
        
    }

}
