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

/**
 * The interface an {@link IAtlasPanel} can use to access the system.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IAtlasPanelSite {

    /**
     * Changes the status of the panel. {@link Status#OK_STATUS} signals that the
     * panel has valid state. If status is not valid then the given message is
     * displayed.
     * 
     * @param status The current status of the panel. {@link Status#OK_STATUS}
     *        signals that the panel has valid state.
     */
    public void changeStatus( IStatus status );
    
    public void addToolbarAction( IAction action );
    
    public void addToolbarItem( IContributionItem item );
    
    public void addSidekick();
    
    public IAtlasToolkit toolkit();
    
}
