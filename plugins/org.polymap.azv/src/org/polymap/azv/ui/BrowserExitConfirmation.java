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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.rap.ui.branding.IExitConfirmation;

import org.polymap.core.runtime.IMessages;

import org.polymap.azv.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class BrowserExitConfirmation
        implements IExitConfirmation {

    private static Log log = LogFactory.getLog( BrowserExitConfirmation.class );

    private static final IMessages      i18n = Messages.forPrefix( "BrowserExitConfirmation" );

    @Override
    public boolean showExitConfirmation() {
        return true;
    }

    @Override
    public String getExitConfirmationText() {
        return i18n.get( "msg" );
    }
    
}
