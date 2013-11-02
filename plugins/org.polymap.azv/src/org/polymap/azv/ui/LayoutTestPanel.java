/* 
 * polymap.org
 * Copyright (C) 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.azv.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.polymap.core.security.SecurityUtils;

import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.MaxWidthConstraint;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayoutTestPanel
        extends DefaultPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( LayoutTestPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "layouttest" );

    
    @Override
    public PanelIdentifier id() {
        return ID;
    }


    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        return site.getPath().size() == 1 && SecurityUtils.isAdmin();

    }


    @Override
    public void createContents( Composite parent ) {
        getSite().setTitle( "LayoutTest" );
        IPanelSection contents = getSite().toolkit().createPanelSection( parent, null );
        contents.addConstraint( new MinWidthConstraint( 500, 1 ) )
                .addConstraint( new MaxWidthConstraint( 1000, 1 ) );
        
//        Button btn1 = tk.createButton( contents.getBody(), "1", SWT.PUSH, SWT.WRAP );
//        btn1.setLayoutData( new ConstraintData( 
//                new PriorityConstraint( 1, 1 ), new MinWidthConstraint( 500, 1 ) ) );
//        Button btn2 = tk.createButton( contents.getBody(), "2 (xxxxxxxxxxxxxxxxxxxxx)", SWT.PUSH  );
//        btn2.setLayoutData( new ConstraintData( new PriorityConstraint( 2, 1 ) ) );
//        Button btn3 = tk.createButton( contents.getBody(), "3", SWT.PUSH  );
    }
    
}
