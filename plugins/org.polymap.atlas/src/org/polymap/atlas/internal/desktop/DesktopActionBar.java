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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.IContributionItem;

import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.ui.FormDataFactory;

import org.polymap.atlas.IAppContext;
import org.polymap.atlas.PanelChangeEvent;
import org.polymap.atlas.toolkit.IPanelToolkit;

/**
 * The mail action bar displayed at the top of the main window.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class DesktopActionBar {

    private static Log log = LogFactory.getLog( DesktopActionBar.class );

    enum PLACE {
        SEARCH,
        PANEL_TOOLBAR,
        PANEL_NAVI,
        PANEL_SWITCHER
    }

    // instance *******************************************

    private IAppContext                 context;

    private IPanelToolkit               tk;

    private Map<PLACE,IContributionItem> items = new HashMap();


    public DesktopActionBar( IAppContext context, IPanelToolkit tk ) {
        this.context = context;
        this.tk = tk;
        context.addEventHandler( this );
    }


    public IContributionItem add( IContributionItem item, PLACE place ) {
        return items.put( place, item );
    }


    public Composite createContents( Composite parent ) {
        Composite contents = tk.createComposite( parent );
        FormLayout layout = new FormLayout();
        layout.spacing = 10;
        contents.setLayout( layout );

        // search
        IContributionItem search = items.get( PLACE.SEARCH );
        Composite searchComposite = null;
        if (search != null) {
            searchComposite = new Composite( contents, SWT.NONE );
            search.fill( searchComposite );
            searchComposite.setLayoutData( FormDataFactory.filled().left( 80 ).right( 100 ).create() );
        }
        // panel toolbar
        IContributionItem tb = items.get( PLACE.PANEL_TOOLBAR );
        Composite tbComposite = null;
        if (tb != null) {
            tbComposite = new Composite( contents, SWT.NONE );
            tb.fill( tbComposite );
            tbComposite.setLayoutData( searchComposite != null
                    ? FormDataFactory.filled().left( 60 ).right( searchComposite ).create()
                    : FormDataFactory.filled().left( 80 ).right( 100 ).create());
        }
        // panel switcher
        IContributionItem switcher = items.get( PLACE.PANEL_SWITCHER );
        Composite switcherComposite = null;
        if (switcher != null) {
            switcherComposite = new Composite( contents, SWT.NONE );
            switcher.fill( switcherComposite );
            switcherComposite.setLayoutData( FormDataFactory.filled().left( -1 ).right( tbComposite ).create() );
        }
        // panel navi
        IContributionItem navi = items.get( PLACE.PANEL_NAVI );
        if (navi != null) {
            Composite naviComposite = new Composite( contents, SWT.NONE );
            navi.fill( naviComposite );
            naviComposite.setLayoutData( FormDataFactory.filled().right( switcherComposite ).create() );
        }

        return contents;
    }


    @EventHandler(display=true)
    protected void panelChanged( PanelChangeEvent ev ) {
    }

}
