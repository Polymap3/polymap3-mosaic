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
package org.polymap.azv.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;
import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.atlas.Context;
import org.polymap.atlas.IAppContext;
import org.polymap.atlas.IPanel;
import org.polymap.atlas.IPanelSite;
import org.polymap.atlas.PanelIdentifier;
import org.polymap.atlas.app.DefaultFormPanel;
import org.polymap.azv.model.Schachtschein;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class SchachtscheinPanel
        extends DefaultFormPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( SchachtscheinPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "schachtschein" );

    @Context
    private Schachtschein               entity;


    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        if (super.init( site, context )) {
            // is there an entity in context?
            if (entity != null) {
                return true;
            }
        }
        return false;
    }


    @Override
    public PanelIdentifier id() {
        return ID;
    }


    @Override
    public void createFormContent( IFormEditorPageSite pageSite ) {
        getSite().setTitle( "Schachtschein" );
        Composite beschreibung = pageSite.newFormField( pageSite.getPageBody(),
                new PropertyAdapter( entity.beschreibung() ),
                new StringFormField(), null );
    }


//    protected Section createLinkSection( Composite parent ) {
//        Section section = site.toolkit().createSection( parent, "Aufgaben", Section.TITLE_BAR );
//        Composite client = (Composite)section.getClient();
//
//        client.setLayout( RowLayoutFactory.fillDefaults().type( SWT.VERTICAL ).create() );
//
//        return section;
//    }

}
