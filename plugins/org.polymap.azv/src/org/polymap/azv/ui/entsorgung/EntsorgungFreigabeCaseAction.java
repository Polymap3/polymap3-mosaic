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
package org.polymap.azv.ui.entsorgung;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;

import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.um.User;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.model.AzvRepository;
import org.polymap.azv.model.Entsorgungsliste;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.CaseStatus;
import org.polymap.mosaic.ui.casepanel.DefaultCaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseActionSite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EntsorgungFreigabeCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( EntsorgungFreigabeCaseAction.class );

    public static final IMessages           i18n = Messages.forPrefix( "EntsorgungFreigabe" );
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>    mcase;
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2> repo;

    private ICaseActionSite                 site;

    private User                            umuser;

    private CaseStatus                      caseStatus;

    private AzvRepository                   azvRepo = AzvRepository.instance();
    
    private Map<String,Entsorgungsliste>    listen = new TreeMap();

    private List                            list;
    
    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        if (mcase.get() != null && repo.get() != null
                && (mcase.get().getNatures().contains( AzvPlugin.CASE_ENTSORGUNG ) )
                //&& mcase.get().get( EntsorgungCaseAction.KEY_LISTE ) != null
                && SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA )
                && SecurityUtils.isUserInGroup( AzvPlugin.ROLE_ENTSORGUNG )) {

            Polymap.getSessionDisplay().asyncExec( new Runnable() {
                public void run() {
                    site.activateCaseAction( site.getActionId() );
                }
            });
            return true;
        }
        return false;
    }


    @Override
    public void fillStatus( CaseStatus status ) {
        this.caseStatus = status;
        //status.put( "Liste", "" );
    }


    @Override
    public void createContents( Composite parent ) {
        site.toolkit().createFlowText( parent, i18n.get( "welcomeTxt", EntsorgungsListenPanel.ID ) );

        // liste
        Query<Entsorgungsliste> query = azvRepo.findEntities( Entsorgungsliste.class, null, 0, -1 );
        for (Entsorgungsliste liste : query) {
            listen.put( liste.name().get(), liste );
        }
        
        Composite formContainer = site.toolkit().createComposite( parent );
        formContainer.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
        
        list = site.toolkit().createList( formContainer, SWT.SINGLE, SWT.BORDER );
        list.setLayoutData( FormDataFactory.filled().bottom( -1 ).height( 240 ).create() );
        list.setItems( Iterables.toArray( listen.keySet(), String.class ) );
        list.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                site.setValid( list.getSelectionCount() > 0 );
            }
        });
        String listeId = mcase.get().get( EntsorgungCaseAction.KEY_LISTE );
        if (listeId != null) {
            Entsorgungsliste liste = azvRepo.findEntity( Entsorgungsliste.class, listeId );
            list.setSelection( new String[] {liste.name().get()} );
        }

        
        site.setValid( false );
        
        Button submitBtn = site.createSubmit( formContainer, "Termin zuordnen" );
        submitBtn.setLayoutData( FormDataFactory.filled().top( list ).height( 28 ).create() );
    }


    @Override
    public void submit() throws Exception {
        String prevListeId = mcase.get().get( EntsorgungCaseAction.KEY_LISTE );
        if (prevListeId != null) {
            Entsorgungsliste liste = azvRepo.findEntity( Entsorgungsliste.class, prevListeId );
            Collection<String> mcaseIds = liste.mcaseIds().get();
            mcaseIds.remove( mcase.get().getId() );
            liste.mcaseIds().set( mcaseIds );
            //mcase.get().put( EntsorgungCaseAction.KEY_LISTE, null );
        }
        
        Entsorgungsliste liste = listen.get( list.getItem( list.getSelectionIndex() ) );
        liste.mcaseIds().get().add( mcase.get().getId() );
        mcase.get().put( EntsorgungCaseAction.KEY_LISTE, liste.id() );
        azvRepo.commitChanges();
        
        repo.get().newCaseEvent( mcase.get(), "Terminiert", 
                "Entsorgung wurde der Liste zugeordnet: " + liste.name().get(), 
                AzvPlugin.EVENT_TYPE_TERMINIERT  );
        repo.get().commitChanges();
        
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                site.getContext().closePanel();
            }
        });
    }

}
