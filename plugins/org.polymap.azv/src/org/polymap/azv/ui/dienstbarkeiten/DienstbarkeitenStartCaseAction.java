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
package org.polymap.azv.ui.dienstbarkeiten;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.runtime.IMessages;
import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.ui.StartCaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseActionSite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DienstbarkeitenStartCaseAction
        extends StartCaseAction {

    private static Log log = LogFactory.getLog( DienstbarkeitenStartCaseAction.class );

    public static final IMessages       i18n = Messages.forPrefix( "DienstbarkeitenStart" ); //$NON-NLS-1$

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        if (mcase.get() != null && repo.get() != null
                && mcase.get().getNatures().contains( AzvPlugin.CASE_DIENSTBARKEITEN )) {

            //_site.getPanelSite().setIcon( AzvPlugin.instance().imageForName( "resources/icons/letters-filter.png" ) );
            
            super.init( _site );

            return true;
        }
        return false;
    }


    @Override
    protected String i18n( String key, Object... args ) {
        return i18n.get( key, args );
    }

}
