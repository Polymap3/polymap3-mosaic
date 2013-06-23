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

import org.eclipse.swt.widgets.Composite;

/**
 * A panel section is the main layout component of the Atlas UI. It consists of a
 * title and a body. The body can contain plain widgets or sub-sections. The child
 * elements are layouted in a column by default. The layout can be controlled via
 * constraints (see {@link Column#addConstraint(LayoutConstraint)}).
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IPanelSection
        extends ILayoutContainer, ILayoutElement {

    /** Style constant: make an IPanelSection expandable */
    public static final int         EXPANDABLE = 1;
    
    public IPanelSection getParentPanel();
    
    /**
     * The level in the section hierarchy. Panel section created by calling
     * {@link IPanelToolkit#createPanelSection(Composite, String, int...)} return
     * <code>0</code> here.
     */
    public int getLevel();
    
    public IPanelSection setExpanded( boolean expanded );
    
    public boolean isExpanded();
    
    public String getTitle();
    
    public IPanelSection setTitle( String title );
    
    
//    /**
//     * 
//     * <p/>
//     * Calling this method with a number greater than <code>0</code> effectively
//     * creates a new column.
//     * 
//     * @param num The number of the column, starting with <code>0</code>.
//     * @return The column representation for the given column number.
//     * @throws IllegalArgumentException If the the given column number is greater
//     *         than the greatest column + 1.
//     */
//    public Column column( int num );
//    
//    /**
//     * 
//     */
//    public abstract class Column
//            implements ILayoutContainer {
//        
//        private List<LayoutConstraint>  constraints = new ArrayList();        
//        
//        /**
//         * The container of this column. The returned {@link Composite} has a
//         * {@link RowLayout} set with default settings for spacing and margins.
//         */
//        public abstract Composite getControl();
//        
//        /**
//         * Effectively removes this column from the {@link IPanelSection}.
//         */
//        public abstract void dispose();
//
//        @Override
//        public Column addConstraint( LayoutConstraint constraint ) {
//            constraints.add( constraint );
//            return this;
//        }
//    }
    
}
