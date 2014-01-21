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
package org.polymap.azv.ui.map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.eclipse.rwt.graphics.Graphics;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.layout.RowDataFactory;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AddressSearchMapAction
        extends ContributionItem {

    private static Log log = LogFactory.getLog( AddressSearchMapAction.class );

    private MapViewer           viewer;

    private Text                searchTxt;
    
    
    public AddressSearchMapAction( MapViewer viewer ) {
        this.viewer = viewer;
    }


    public String getSearchText() {
        return !searchTxt.getText().startsWith( "Suchen" ) ? searchTxt.getText() : null;
    }
    
    
    @Override
    public void fill( Composite parent ) {
        searchTxt = viewer.getPanelSite().toolkit().createText( parent, "Suchen: Ort, PLZ, Straße", SWT.SEARCH, SWT.CANCEL );
        searchTxt.setLayoutData( RowDataFactory.swtDefaults().hint( 320, SWT.DEFAULT ).create() );
        //searchTxt.setLayoutData( FormDataFactory.filled().right( clearBtn ).create() );

        searchTxt.setToolTipText( "Suchbegriff: min. 3 Zeichen" );
        searchTxt.setForeground( Graphics.getColor( 0xa0, 0xa0, 0xa0 ) );
        searchTxt.addFocusListener( new FocusListener() {
            @Override
            public void focusLost( FocusEvent ev ) {
                if (searchTxt.getText().length() == 0) {
                    searchTxt.setText( "Suchen..." );
                    searchTxt.setForeground( Graphics.getColor( 0xa0, 0xa0, 0xa0 ) );
                    //clearBtn.setEnabled( false );
                }
            }
            @Override
            public void focusGained( FocusEvent ev ) {
                if (searchTxt.getText().startsWith( "Suchen" )) {
                    searchTxt.setText( "" );
                    searchTxt.setForeground( Graphics.getColor( 0x00, 0x00, 0x00 ) );
                }
            }
        });
//        searchTxt.addModifyListener( new ModifyListener() {
//            @Override
//            public void modifyText( ModifyEvent ev ) {
//                clearBtn.setEnabled( searchTxt.getText().length() > 0 );
//                if (filter != null) {
//                    viewer.removeFilter( filter );
//                }
//                if (searchTxt.getText().length() > 2) {
//                    if (filter != null) {
//                        viewer.removeFilter( filter );
//                    }
//                    viewer.addFilter( filter = new TextFilter( searchTxt.getText() ) );
//                }
//            }
//        });
        
//        final DrawFeatureMapAction drawFeatureAction = new DrawFeatureMapAction( 
//                site, olwidget.getMap(), vectorLayer, DrawFeatureControl.HANDLER_POINT );
//        drawFeatureAction.fill( toolbar );
//        drawFeatureAction.addListener( new PropertyChangeListener() {
//            @EventHandler(display=true)
//            public void propertyChange( PropertyChangeEvent ev ) {
//                Feature feature = (Feature)ev.getNewValue();
//                Point point = (Point)feature.getDefaultGeometryProperty().getValue();
//                String wkt = new WKTWriter().write( point );
//                mcase.get().put( "point", wkt );
//                repo.get().commitChanges();
//                
//                site.getPanelSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, "Markierung wurde gesetzt auf: " + point.toText() ) );
//                
//                drawFeatureAction.deactivate();
//            }
//        });
    }

}
