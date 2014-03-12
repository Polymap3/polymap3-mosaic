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
package org.polymap.azv.ui.schachtschein;

import static org.polymap.azv.AzvPlugin.CASE_SCHACHTSCHEIN;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_ANFREIGABE;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_FREIGABE;
import static org.polymap.azv.AzvPlugin.ROLE_BL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;

import org.eclipse.jface.action.IAction;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.email.EmailService;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.model.AzvStatusMixin;
import org.polymap.azv.model.NutzerMixin;
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
public class SchachtscheinFreigabeCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( SchachtscheinFreigabeCaseAction.class );

    public static final IMessages       i18n = Messages.forPrefix( "SchachtscheinFreigabe" ); //$NON-NLS-1$

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>        mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2>  repo;

    private ICaseActionSite                     site;

    private IAction                             action;

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        if (mcase.get() != null && repo.get() != null
                && SecurityUtils.isUserInGroup( ROLE_BL )
                && mcase.get().getNatures().contains( CASE_SCHACHTSCHEIN )
                && EVENT_TYPE_ANFREIGABE.equals( AzvStatusMixin.ofCase( mcase.get() ))) {
            return true;
        }
        return false;
    }


    @Override
    public void submit() throws Exception {
        MosaicRepository2 mosaic = repo.get();
        mosaic.newCaseEvent( mcase.get(), EVENT_TYPE_FREIGABE, i18n.get( "freigegeben" ), EVENT_TYPE_FREIGABE );
        mosaic.closeCase( mcase.get(), EVENT_TYPE_FREIGABE, i18n.get( "freigegeben" ) );
        mosaic.commitChanges();
        
        User user = mcase.get().as( NutzerMixin.class ).user();
                
        String salu = user.salutation().get() != null ? user.salutation().get() : ""; //$NON-NLS-1$
        String header = "Sehr geehrte" + (salu.equalsIgnoreCase( "Herr" ) ? "r " : " ") + salu + " " + user.name().get(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        Email email = new SimpleEmail();
        email.setCharset( "ISO-8859-1" ); //$NON-NLS-1$
        email.addTo( user.email().get() )
                .setSubject( i18n.get( "emailSubject") ) //$NON-NLS-1$
                .setMsg( i18n.get( "email", header ) ); //$NON-NLS-1$
        EmailService.instance().send( email );
        
        site.getPanelSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, i18n.get( "okTxt" ) ) ); //$NON-NLS-1$
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
