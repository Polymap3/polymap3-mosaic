/*
 * polymap.org
 * Copyright 2013, Falko Bräutigam. All rights reserved.
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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.atlas.toolkit.IPanelToolkit;

/**
 * The interface between the {@link IPanel} and the Atlas UI.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IPanelSite {

    /**
     * The whole path of the panel including the name of the panel as last segment.
     */
    public PanelPath getPath();

    /**
     * Changes the status of the panel. {@link Status#OK_STATUS} signals that the
     * panel has valid state. If status is not valid then the given message is
     * displayed.
     *
     * @param status The current status of the panel. {@link Status#OK_STATUS}
     *        signals that the panel has valid state.
     */
    public void setStatus( IStatus status );

    public IStatus getStatus();
    
    public void addToolbarAction( IAction action );

    public void addToolbarItem( IContributionItem item );

    public void addSidekick();

    public IPanelToolkit toolkit();

    public void setTitle( String string );

    public String getTitle();

//    /**
//     * Registers the given {@link EventHandler event handler}.
//     *
//     * @see EventHandler
//     * @see EventManager
//     * @param handler
//     */
//    public void addEventHandler( Object handler );
//
//    public void removeEventHandler( Object handler );

}
