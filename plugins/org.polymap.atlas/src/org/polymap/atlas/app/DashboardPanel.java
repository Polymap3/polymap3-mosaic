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
package org.polymap.atlas.app;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;

import org.polymap.core.project.ui.util.SimpleFormData;
import org.polymap.atlas.AtlasPlugin;
import org.polymap.atlas.IAppContext;
import org.polymap.atlas.IPanel;
import org.polymap.atlas.IPanelSite;
import org.polymap.atlas.PanelIdentifier;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DashboardPanel
        implements IPanel {

    public static final PanelIdentifier     ID = new PanelIdentifier( "dashboard" );

    private IPanelSite      site;

    private IAppContext     context;


    @Override
    public boolean init( IPanelSite _site, IAppContext _context ) {
        this.site = _site;
        this.context = _context;

        site.setTitle( "Dashboard" );

        // info action
        Image icon = JFaceResources.getImage( Dialog.DLG_IMG_MESSAGE_INFO );
        Action infoAction = new Action( "Info" ) {
            public void run() {
                MessageDialog.openInformation( AtlasApplication.getShellToParentOn(),
                        "Information", "Atlas Client Version: " + AtlasPlugin.instance().getBundle().getVersion() );
            }
        };
        infoAction.setImageDescriptor( ImageDescriptor.createFromImage( icon ) );
        infoAction.setToolTipText( "Version Information" );
        site.addToolbarAction( infoAction );
        return true;
    }


    @Override
    public void dispose() {
    }


    @Override
    public PanelIdentifier id() {
        return ID;
    }


    @Override
    public void createContents( Composite parent ) {
        Composite contents = site.toolkit().createComposite( parent );
        contents.setLayout( new FormLayout() );

        Label l = site.toolkit().createLabel( contents, "Dashboard!" );
        l.setLayoutData( SimpleFormData.filled().create() );
    }


    @Override
    public IPanelSite getSite() {
        return site;
    }

}
