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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

import org.polymap.atlas.internal.cp.ISolver;

/**
 * Layout data to be used for child widgets of {@link ILayoutContainer}s such
 * as {@link IPanelSection}.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ConstraintData {

    private static Log log = LogFactory.getLog( ConstraintData.class );
    
    protected int                   defaultWidth = -1, defaultHeight = -1;

    protected int                   currentWhint, currentHhint, currentWidth = -1, currentHeight = -1;
    
    protected ArrayList<LayoutConstraint> constraints;

    
    public ConstraintData( LayoutConstraint... constraints ) {
        this.constraints = new ArrayList( Arrays.asList( constraints ) );
    }
    
    
//    public ConstraintData addConstraint( LayoutConstraint constraint ) {
//        // already exists?
//        for (ListIterator<LayoutConstraint> it=constraints.listIterator(); it.hasNext(); ) {
//            if (it.next().getClass().equals( constraint.getClass() )) {
//                it.set( constraint );
//                return this;
//            }
//        }
//        // no? -> add
//        constraints.add( constraint );
//        return this;
//    }
    
    
    public void fillSolver( ISolver solver ) {
        for (LayoutConstraint constraint : constraints) {
            solver.addConstraint( constraint );
        }
    }

    
    public <T extends LayoutConstraint> T constraint( Class<T> type, T defaultValue ) {
        return (T)Iterables.find( constraints, Predicates.instanceOf( type ), defaultValue );
    }
    
    
    public Point computeSize( Control control, int wHint, int hHint, boolean flushCache ) {
        if (flushCache) {
            flushCache();
        }
        if (wHint == SWT.DEFAULT && hHint == SWT.DEFAULT) {
            if (defaultWidth == -1 || defaultHeight == -1) {
                Point size = control.computeSize( wHint, hHint, flushCache );
                defaultWidth = size.x;
                defaultHeight = size.y;
            }
            return new Point( defaultWidth, defaultHeight );
        }
        if (currentWidth == -1 || currentHeight == -1 || wHint != currentWhint || hHint != currentHhint) {
            Point size = control.computeSize( wHint, hHint, flushCache );
            currentWhint = wHint;
            currentHhint = hHint;
            currentWidth = size.x;
            currentHeight = size.y;
        }
        return new Point( currentWidth, currentHeight );
    }
    
    
    protected void flushCache () {
        defaultWidth = defaultHeight = -1;
        currentWidth = currentHeight = -1;
    }

}
