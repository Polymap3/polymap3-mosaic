/* 
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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
package org.polymap.atlas.internal.desktop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.window.ApplicationWindow;

import org.polymap.core.project.ui.util.SimpleFormData;

/**
 * The main application window for the desktop.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class DesktopApplicationWindow
        extends ApplicationWindow {

    private static Log log = LogFactory.getLog( DesktopApplicationWindow.class );

    
    public DesktopApplicationWindow( Shell parentShell ) {
        super( parentShell );
        addStatusLine();
        addCoolBar( SWT.HORIZONTAL );
       // parentShell.setText( "Atlas!" );
    }


    @Override
    protected Control createContents( Composite parent ) {
        setStatus( "prima" );
        
        Composite contents = new Composite( parent, SWT.NONE );
        contents.setLayout( new FormLayout() );
        
        Label l = new Label( contents, SWT.NONE );
        l.setText( "Hallo..." );
        
        Button b = new Button( contents, SWT.PUSH );
        b.setText( "Push" );
        b.setLayoutData( new SimpleFormData().top( l, 10 ).create() );
        b.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                log.info( "widgetSelected(): ..." );
            }
            public void widgetDefaultSelected( SelectionEvent e ) {
                log.info( "widgetDefaultSelected(): ..." );
            }
        });
        return contents;
    }


    @Override
    protected boolean showTopSeperator() {
        return false;
    }


    @Override
    protected Point getInitialSize() {
        return new Point( 800, 600 );
    }
    
}
