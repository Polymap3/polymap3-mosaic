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
package org.polymap.azv.ui.dienstbarkeiten;

import static org.polymap.azv.AzvPlugin.CASE_DIENSTBARKEITEN;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_ANFREIGABE;
import static org.polymap.azv.AzvPlugin.ROLE_BL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.security.SecurityUtils;

import org.polymap.azv.Messages;
import org.polymap.azv.model.AzvStatusMixin;
import org.polymap.azv.ui.FreigabeCaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseActionSite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DienstbarkeitenFreigabeCaseAction
        extends FreigabeCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( DienstbarkeitenFreigabeCaseAction.class );

    public static final IMessages       i18n = Messages.forPrefix( "DienstbarkeitenFreigabe" ); //$NON-NLS-1$

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        if (mcase.get() != null && repo.get() != null
                && SecurityUtils.isUserInGroup( ROLE_BL )
                && mcase.get().getNatures().contains( CASE_DIENSTBARKEITEN )
                && EVENT_TYPE_ANFREIGABE.equals( AzvStatusMixin.ofCase( mcase.get() ))) {
            return true;
        }
        return false;
    }


    @Override
    protected IMessages i18n() {
        return i18n;
    }
    
}
