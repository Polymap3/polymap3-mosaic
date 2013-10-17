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
package org.polymap.mosaic.ui.casepanel;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.Action;

import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;

/**
 * Provides default, no-op implementation of all methods. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DefaultCaseAction
        implements ICaseAction {

    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        return false;
    }

    @Override
    public void fillAction( Action action ) {
    }

    @Override
    public void fillStatus( CaseStatus status ) {
    }

    @Override
    public void fillContentArea( Composite parent ) {
    }

    @Override
    public void createContents( Composite parent ) {
    }

    @Override
    public void submit() {
    }

    @Override
    public void discard() {
    }
    
}
