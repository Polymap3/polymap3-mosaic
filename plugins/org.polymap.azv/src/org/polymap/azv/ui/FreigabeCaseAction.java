/* 
 * polymap.org
 * Copyright (C) 2013-2014, Falko Bräutigam. All rights reserved.
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

import static org.polymap.azv.AzvPlugin.EVENT_TYPE_FREIGABE;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.IAction;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.um.User;
import org.polymap.azv.AzvPlugin;
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
public abstract class FreigabeCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( FreigabeCaseAction.class );

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    protected ContextProperty<IMosaicCase>      mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    protected ContextProperty<MosaicRepository2> repo;

    protected ICaseActionSite                   site;

    protected IAction                           action;

    
    protected abstract IMessages i18n();

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        return true;
    }


    @Override
    public void submit() throws Exception {
        MosaicRepository2 mosaic = repo.get();
        mosaic.newCaseEvent( mcase.get(), EVENT_TYPE_FREIGABE, i18n().get( "freigegeben" ), EVENT_TYPE_FREIGABE );
        mosaic.closeCase( mcase.get(), EVENT_TYPE_FREIGABE, i18n().get( "freigegeben" ) );
        mosaic.commitChanges();
        
        User user = mcase.get().as( AzvVorgang.class ).user();
        AzvPlugin.sendEmail( user, i18n() );
                
        site.getPanelSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, i18n().get( "okTxt" ) ) ); //$NON-NLS-1$

        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                site.getContext().closePanel( site.getPanelSite().getPath() );
            }
        });
    }


    @Override
    public void discard() {
        repo.get().rollbackChanges();
    }
    
}
