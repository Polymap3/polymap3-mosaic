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
package org.polymap.mosaic.ui.casepanel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.polymap.core.runtime.event.EventHandler;
import org.polymap.rhei.batik.IPanelSite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CaseStatusViewer
        implements PropertyChangeListener {

    private static Log log = LogFactory.getLog( CaseStatusViewer.class );
    
    protected CaseStatus            status = new CaseStatus();

    private IPanelSite              site;

    private Label                   flowText;

    
    public CaseStatusViewer( IPanelSite site ) {
        this.site = site;
    }

    
    public Control createContents( Composite parent ) {
        flowText = site.toolkit().createFlowText( parent, "..." );
        propertyChange( null );
        status.addListener( this );
        return flowText;
    }
    
    
    public void dispose() {
        if (status != null) {
            status.removeListener( this );
            status = null;
        }
    }

    
    @Override
    @EventHandler(display=true)
    public void propertyChange( PropertyChangeEvent ev ) {
        StringBuilder buf = new StringBuilder( 1024 );
        for (CaseStatus.Entry entry : status.entries()) {
            buf.append( buf.length() > 0 ? "&#160;&#160;|&#160;&#160;" : "" );
            buf.append( entry.getKey() ).append( ": " ).append( "<strong>" ).append( entry.getValue() ).append( "</strong>" );
        }
        flowText.setText( buf.toString() );
    }
    
}
