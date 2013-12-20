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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.email.EmailService;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.model.AzvRepository;
import org.polymap.azv.model.Entsorgungsliste;
import org.polymap.azv.ui.NutzerAnVorgangCaseAction;
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
public class EntsorgungStornoCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( EntsorgungStornoCaseAction.class );

    public static final IMessages           i18n = Messages.forPrefix( "EntsorgungStorno" );
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>    mcase;
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2> repo;

    private ICaseActionSite                 site;

    private AzvRepository                   azvRepo = AzvRepository.instance();

    private Entsorgungsliste                liste;
    
    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        if (mcase.get() != null && repo.get() != null
                && (mcase.get().getNatures().contains( AzvPlugin.CASE_ENTSORGUNG ) )
                && mcase.get().get( EntsorgungCaseAction.KEY_LISTE ) != null
                && !SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA )) {

            String id = mcase.get().get( EntsorgungCaseAction.KEY_LISTE );
            liste = azvRepo.findEntity( Entsorgungsliste.class, id );
            return true;
        }
        return false;
    }


    @Override
    public void fillStatus( CaseStatus status ) {
        if (liste == null || liste.geschlossen().get()) {
            status.put( "Status", "Antrag kann nicht mehr storniert werden." );
            site.setValid( false );
        }
    }


    @Override
    public void submit() throws Exception {
        liste.mcaseIds().get().remove( mcase.get().getId() );
        azvRepo.commitChanges();
        
        repo.get().closeCase( mcase.get(), "Storniert", "Antrag wurde storniert." );
        repo.get().commitChanges();
        
        // email
        String caseUser = mcase.get().get( NutzerAnVorgangCaseAction.KEY_USER );
        if (caseUser != null) {
            User user = UserRepository.instance().findUser( caseUser );
            String salu = user.salutation().get() != null ? user.salutation().get() : "";
            String header = "Sehr geehrte" + (salu.equalsIgnoreCase( "Herr" ) ? "r " : " ") + salu + " " + user.name().get();
            Email email = new SimpleEmail();
            email.setCharset( "ISO-8859-1" );
            email.addTo( user.email().get() )
                    .setSubject( i18n.get( "emailSubject") )
                    .setMsg( i18n.get( "email", header, liste.name().get() ) );
            EmailService.instance().send( email );
        }
        
        Polymap.getSessionDisplay().asyncExec( new Runnable() {
            public void run() {
                site.getContext().closePanel();
            }
        });
    }

}