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
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.eclipse.rwt.graphics.Graphics;

import org.eclipse.jface.action.ContributionItem;

import org.polymap.core.project.ui.util.SimpleFormData;

import org.polymap.atlas.AtlasPlugin;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class SearchField
        extends ContributionItem {

    private static Log log = LogFactory.getLog( SearchField.class );

    private Text            text;


    public SearchField() {
        super();
    }


    @Override
    public void fill( Composite parent ) {
        parent.setLayout( new FormLayout() );

        Button btn = new Button( parent, SWT.PUSH );
        btn.setToolTipText( "Start search" );
        btn.setImage( AtlasPlugin.instance().imageForName( "icons/zoom.png" ) );
        btn.setLayoutData( SimpleFormData.filled().left( -1 ).create() );

        text = new Text( parent, SWT.SEARCH | SWT.CANCEL );
        text.setLayoutData( SimpleFormData.filled().right( btn ).create() );

        text.setText( "Search..." );
        text.setForeground( Graphics.getColor( 0xa0, 0xa0, 0xa0 ) );
        text.addFocusListener( new FocusListener() {
            @Override
            public void focusLost( FocusEvent ev ) {
            }
            @Override
            public void focusGained( FocusEvent ev ) {
                if (text.getText().startsWith( "Search" )) {
                    text.setText( "" );
                    text.setForeground( Graphics.getColor( 0x00, 0x00, 0x00 ) );
                }
            }
        });
//        text.addModifyListener( new ModifyListener() {
//
//        });
    }

}
