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
package org.polymap.azv.ui;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.IAction;

import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;

import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.CaseStatus;
import org.polymap.mosaic.ui.casepanel.DefaultCaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseActionSite;
import org.polymap.mosaic.ui.eventstable.EventsTableViewer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EreignisseCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( EreignisseCaseAction.class );

    private static final FastDateFormat df = FastDateFormat.getInstance( "dd.MM.yyyy" );
    
    private ICaseActionSite                 site;
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>    mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2> repo;

    private EventsTableViewer               viewer;
    
    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        return mcase.get() != null && repo.get() != null;
    }


    @Override
    public void dispose() {
        if (viewer != null) {
            viewer.dispose();
            viewer = null;
        }
    }


    @Override
    public void fillAction( IAction action ) {
    }


    @Override
    public void fillStatus( CaseStatus status ) {
        status.put( "Vorgang", mcase.get().getName(), 100 );
        //IMosaicCaseEvent created = Iterables.getFirst( mcase.get().getEvents(), null );
        status.put( "Angelegt am", df.format( mcase.get().getCreated() ), 99 );
    }


    @Override
    public void fillContentArea( Composite parent ) {
        // events table
        IPanelSection eventsSection = site.toolkit().createPanelSection( parent, "Ereignisse" );
        eventsSection.addConstraint( new PriorityConstraint( 0 ) );
        eventsSection.getBody().setLayout( FormLayoutFactory.defaults().create() );
        viewer = new EventsTableViewer( eventsSection.getBody(), repo.get(), mcase.get(), SWT.NONE );
        viewer.getTable().setLayoutData( FormDataFactory.filled().height( 200 ).width( 400 ).create() );
    }

}
