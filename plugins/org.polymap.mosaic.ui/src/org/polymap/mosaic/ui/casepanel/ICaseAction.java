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

import org.eclipse.jface.action.IAction;

import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.toolkit.ConstraintLayout;

/**
 * Contribution to a case {@link CasePanel panel}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ICaseAction {

    /**
     * Initialize and check if this action is to be shown in the given application
     * context. Context properties can be accessed via injected {@link ContextProperty}
     * members. 
     * 
     * @return False if the action should not be shown for the given case.
     */
    public boolean init( ICaseActionSite site );
    
    public void dispose();
    
    /**
     * The action is displayed in the toolbar of the {@link CasePanel}. By default
     * the name, description and icon is initialized with the values specified in the
     * extension. This method allows to adjust these settings.
     * <p/>
     * If name and icon is set to null then there is no visible button in the toolbar.
     * 
     * @param action
     */
    public void fillAction( IAction action );

    /**
     * Provide content for the status area of the case panel. Priority values should
     * be in the range 0..100. The {@link CaseStatus#DEFAULT_PRIORITY} is 0. Less
     * important entries have smaller priority.
     * <p/>
     * Changes to the status object are reflected in the UI immediately.
     * 
     * @param status The status object to fill.
     */
    public void fillStatus( CaseStatus status );
    
    /**
     * Contribute UI to the content area of the case panel.
     *
     * @param parent
     */
    public void fillContentArea( Composite parent );
    
    /**
     * Creates the UI of the action area. If this method does not generate children
     * under parent then {@link #submit()} is immediatelly called if the user clicks
     * this action.
     * <p/>
     * The parent Composite has a {@link ConstraintLayout} layout manager set. This
     * can be changed if necessary. Margins and spacing should be transfered to new
     * layout manager.
     * 
     * @param parent
     */
    public void createContents( Composite parent );
    
    /**
     * Submit the changes made by the UI elements created by
     * {@link #createContents(Composite)}.
     * @throws Exception XXX
     */
    public void submit() throws Exception;

    /**
     * Discard changes made by the UI elements created by
     * {@link #createContents(Composite)}.
     */
    public void discard();

}
