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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

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
    
    protected List<LayoutConstraint> constraints = new ArrayList( 3 );

    
    public ConstraintData( LayoutConstraint... constraints ) {
        this.constraints.addAll( Arrays.asList( constraints ) );
    }
    
    
    public ConstraintData addConstraint( LayoutConstraint constraint ) {
        constraints.add( constraint );
        return this;
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
