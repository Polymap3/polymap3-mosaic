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

import java.util.List;

import java.beans.PropertyChangeEvent;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;

import org.eclipse.rwt.graphics.Graphics;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.model.AzvStatusMixin;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicCaseEvent;
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

    private CaseStatus                      caseStatus;

    private String                          mcaseStatus;
    
    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        if (mcase.get() != null && repo.get() != null) {
            /*&& SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA )*/;

            // listen to changes
            EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {
                public boolean apply( PropertyChangeEvent input ) {
                    return caseStatus != null && input.getSource() == mcase.get();
                }
            });
            return true;
        }
        return false;
    }


    @Override
    public void dispose() {
        EventManager.instance().unsubscribe( this );
        caseStatus = null;
        if (viewer != null) {
            viewer.dispose();
            viewer = null;
        }
    }


    @Override
    public void fillStatus( CaseStatus status ) {
        this.caseStatus = status;
        updateStatus( null );
    }

    
    @EventHandler(display=true,delay=500)
    protected void updateStatus( List<PropertyChangeEvent> evs ) {
        if (caseStatus == null) {
            return;
        }
        @SuppressWarnings("hiding")
        IMosaicCase mcase = this.mcase.get();
        site.getPanelSite().setTitle( "Vorgang: " + (mcase.getName().isEmpty() ? "[Neu angelegt]" : mcase.getName()) );
        
        caseStatus.put( "Art", Joiner.on( "," ).join( mcase.getNatures() ), 101, Graphics.getColor( 0xDD, 0xE1, 0xE3 ) );
        if (mcase.getName() != null && !mcase.getName().isEmpty()) {
            caseStatus.put( "Bezeichnung", mcase.getName(), 100 );
        }
        caseStatus.put( "Angelegt", df.format( mcase.getCreated() ), 99 );
        
        // set status field only if mcase status has changed or status field is empty;
        // don't change if other party has set (before my event arrived)
//        if (!mcase.getStatus().equals( mcaseStatus ) || caseStatus.get( "Status" ) == null) {
            mcaseStatus = mcase.getStatus();
            
//            if (IMosaicCaseEvent.TYPE_CLOSED.equals( mcaseStatus )) {
//                caseStatus.put( "Status", "Erledigt", -1, AzvPlugin.instance().okColor.get() );            
//            }
//            else if (IMosaicCaseEvent.TYPE_OPEN.equals( mcaseStatus ) && caseStatus.get( "Status" ) == null) {
//                caseStatus.put( "Status", "Anlegen", -1, AzvPlugin.instance().openColor.get() );
//            }
            
            String azvStatus = AzvStatusMixin.ofCase( mcase );
            if (azvStatus == null) {
                caseStatus.put( "Status", "NEU", -1, AzvPlugin.instance().openColor.get() );
            }
            else if (azvStatus.equals( AzvPlugin.EVENT_TYPE_ABGEBROCHEN )) {
                caseStatus.put( "Status", "Abgebrochen", -1, AzvPlugin.instance().discardColor.get() );
            }
            else if (azvStatus.equals( AzvPlugin.EVENT_TYPE_STORNIERT )) {
                caseStatus.put( "Status", "Storniert", -1, AzvPlugin.instance().discardColor.get() );
            }
            else if (azvStatus.equals( AzvPlugin.EVENT_TYPE_BEANTRAGT )) {
                caseStatus.put( "Status", "Beantragt", -1, AzvPlugin.instance().openColor.get() );
            }
            else if (azvStatus.equals( AzvPlugin.EVENT_TYPE_ANFREIGABE )) {
                caseStatus.put( "Status", "An Freigabe", -1, AzvPlugin.instance().openColor.get() );
            }
            else if (azvStatus.equals( AzvPlugin.EVENT_TYPE_ANBEARBEITUNG )) {
                caseStatus.put( "Status", "An Bearbeitung", -1, AzvPlugin.instance().openColor.get() );
            }
            else if (azvStatus.equals( AzvPlugin.EVENT_TYPE_FREIGABE )) {
                caseStatus.put( "Status", "Freigegeben", -1, AzvPlugin.instance().okColor.get() );
            }
            else if (IMosaicCaseEvent.TYPE_CLOSED.equals( mcase.getStatus() )) {
                caseStatus.put( "Status", "Erledigt", -1, AzvPlugin.instance().okColor.get() );
            }
            else {
                caseStatus.put( "Status", "???" );
            }
//        }
    }

    
//    @Override
//    public void fillContentArea( Composite parent ) {
//        // events table
//        IPanelSection eventsSection = site.toolkit().createPanelSection( parent, "Ereignisse" );
//        eventsSection.addConstraint( new PriorityConstraint( 0 ) );
//        eventsSection.getBody().setLayout( FormLayoutFactory.defaults().create() );
//        viewer = new EventsTableViewer( eventsSection.getBody(), repo.get(), mcase.get(), SWT.NONE );
//        viewer.getTable().setLayoutData( FormDataFactory.filled().height( 200 ).width( 400 ).create() );
//    }

}
