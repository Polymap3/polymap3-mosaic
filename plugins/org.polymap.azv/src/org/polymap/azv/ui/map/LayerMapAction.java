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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.ContributionItem;

import org.polymap.rhei.batik.IPanelSite;

import org.polymap.openlayers.rap.widget.base_types.OpenLayersMap;
import org.polymap.openlayers.rap.widget.layers.Layer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LayerMapAction
        extends ContributionItem {

    private static Log log = LogFactory.getLog( LayerMapAction.class );

    private IPanelSite          site;
    
    private OpenLayersMap       map;

    private MapViewer           viewer;
    
    private Layer               layer;
    
    private boolean             visible;
    
    private String              label;
    
    
    public LayerMapAction( MapViewer viewer, Layer layer, String label, boolean visible ) {
        this.viewer = viewer;
        this.site = viewer.getPanelSite();
        this.map = viewer.getMap();
        this.layer = layer;
        this.layer.setVisibility( this.visible = visible );
        this.label = label;
    }

    
    @Override
    public void fill( Composite parent ) {
        Button btn = site.toolkit().createButton( parent, label, SWT.TOGGLE );
        btn.setToolTipText( "Ebene zu/abschalten: " + label );
        btn.setSelection( visible );
        //btn.setImage( BatikPlugin.instance().imageForName( "resources/icons/expand.png" ) );
        btn.setEnabled( true );
        btn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                layer.setVisibility( visible = !visible );
            }
        });
    }
    
}
