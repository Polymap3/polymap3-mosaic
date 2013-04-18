/*
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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
package org.polymap.atlas;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides handling of {@link IPanelSite} and {@link IAppContext} variables.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class DefaultPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( DefaultPanel.class );

    private IPanelSite      site;

    private IAppContext     context;


    /**
     * This default implementation always returns <code>true</code>.
     */
    @Override
    @SuppressWarnings("hiding")
    public boolean init( IPanelSite site, IAppContext context ) {
        this.site = site;
        this.context = context;
        return true;
    }


    /**
     * This default implementation does nothing.
     */
    @Override
    public void dispose() {
    }


    @Override
    public IPanelSite getSite() {
        return site;
    }


    public IAppContext getContext() {
        return context;
    }

}
