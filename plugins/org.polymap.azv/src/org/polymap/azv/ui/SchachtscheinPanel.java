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

import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;

import org.polymap.atlas.ContextProperty;
import org.polymap.atlas.IAppContext;
import org.polymap.atlas.IPanel;
import org.polymap.atlas.IPanelSite;
import org.polymap.atlas.PanelIdentifier;
import org.polymap.atlas.app.DefaultFormPanel;
import org.polymap.azv.AZVPlugin;
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

    private ContextProperty<Schachtschein>  entity;

    private IFormEditorToolkit              tk;
    

    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        if (super.init( site, context )) {
            // is there an entity in the context?
            if (entity.get() != null) {
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
        tk = pageSite.getToolkit();
        Composite parent = pageSite.getPageBody();
        parent.setLayout( FormLayoutFactory.defaults().margins( DEFAULTS_SPACING ).create() );

        Composite contents = tk.createComposite( parent );
        contents.setLayoutData( FormDataFactory.offset( 0 ).left( 25 ).right( 75 ).width( 500 ).create() );
        contents.setLayout( FormLayoutFactory.defaults().spacing( DEFAULTS_SPACING*2 ).create() );

        getSite().setStatus( new Status( IStatus.WARNING, AZVPlugin.ID, "Es fehlen noch Eingaben..." ) );

        Composite base = createBaseSection( contents );
        base.setLayoutData( FormDataFactory.filled().bottom( -1 ).create() );
    }


    protected Composite createBaseSection( Composite parent ) {
        Section section = getSite().toolkit().createSection( parent, "Basisdaten", Section.TITLE_BAR );
        Composite client = (Composite)section.getClient();
        client.setLayout( ColumnLayoutFactory.defaults().columns( 1, 1 ).spacing( 5 ).create() );

        new FormFieldBuilder( client, new PropertyAdapter( entity.get().beschreibung() ) )
                .create().setFocus();
        Composite bemerkungen = new FormFieldBuilder( client, new PropertyAdapter( entity.get().bemerkungen() ) ).create();

        return section;
    }

}
