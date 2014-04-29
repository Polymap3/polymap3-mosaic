/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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

import static org.polymap.azv.AzvPlugin.CASE_LEITUNGSAUSKUNFT;
import static org.polymap.azv.AzvPlugin.CASE_SCHACHTSCHEIN;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_ANBEARBEITUNG;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_ANFREIGABE;
import static org.polymap.azv.AzvPlugin.ROLE_BL;

import java.util.Set;

import org.eclipse.jface.action.IAction;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.model.AzvVorgang;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.DefaultCaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseActionSite;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AnBearbeitungCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    public static final IMessages       i18n = Messages.forPrefix( "AnBearbeitung" );

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>        mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2>  repo;

    private ICaseActionSite                     site;

    private IAction                             action;


    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        if (mcase.get() != null && repo.get() != null) {
            
            Set<String> natures = mcase.get().getNatures();
            String azvStatus = AzvVorgang.azvStatusOf( mcase.get() );

            if (SecurityUtils.isUserInGroup( ROLE_BL )
                    && (natures.contains( CASE_LEITUNGSAUSKUNFT ) || natures.contains( CASE_SCHACHTSCHEIN ))
                    && EVENT_TYPE_ANFREIGABE.equals( azvStatus )) {
                return true;
            }
        }
        return false;
    }

    
    @Override
    public void submit() throws Exception {
        MosaicRepository2 mosaic = repo.get();
        mosaic.newCaseEvent( mcase.get(), EVENT_TYPE_ANBEARBEITUNG, "An Bearbeitung übermittelt", EVENT_TYPE_ANBEARBEITUNG );
        mosaic.commitChanges();
        
        site.getPanelSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, i18n.get( "okTxt" ) ) );
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                site.getContext().closePanel( site.getPanelSite().getPath() );
            }
        });
    }

}
