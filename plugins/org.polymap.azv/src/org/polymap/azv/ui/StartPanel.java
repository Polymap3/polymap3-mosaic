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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.atlas.DefaultPanel;
import org.polymap.atlas.IAppContext;
import org.polymap.atlas.IPanel;
import org.polymap.atlas.IPanelSite;
import org.polymap.atlas.PanelIdentifier;
import org.polymap.atlas.app.AtlasApplication;
import org.polymap.azv.model.AzvRepository;
import org.polymap.azv.model.Schachtschein;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Br�utigam</a>
 */
public class StartPanel
        extends DefaultPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( StartPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "azvstart" );


    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        return site.getPath().size() == 1;
    }


    @Override
    public PanelIdentifier id() {
        return ID;
    }


    @Override
    public Composite createContents( Composite parent ) {
        getSite().setTitle( "AZV" );
        Composite contents = getSite().toolkit().createComposite( parent );
        contents.setLayout( new FillLayout() );
        createLinkSection( contents );
        return contents;
    }


    protected Section createLinkSection( Composite parent ) {
        Section section = getSite().toolkit().createSection( parent, "Aufgaben", Section.TITLE_BAR );
        Composite client = (Composite)section.getClient();

        client.setLayout( RowLayoutFactory.fillDefaults().type( SWT.VERTICAL ).create() );

        Label l1 = getSite().toolkit().createLink( client, "Schachtschein beantragen" );
        l1.setToolTipText( "Einen neuen Schachtschein beantragen" );
        l1.setLayoutData( RowDataFactory.swtDefaults().create() );
        l1.addMouseListener( new MouseAdapter() {
            public void mouseUp( MouseEvent ev ) {
                try {
                    log.info( "Schachtschein!" );
                    Schachtschein entity = AzvRepository.instance().newSchachtschein();
                    getContext().openPanel( SchachtscheinPanel.ID, "entity", entity );
                }
                catch (Exception e) {
                    AtlasApplication.handleError( "Schachtschein konnte nicht angelegt werden.", e );
                }
            }
        });

        return section;
    }

}
