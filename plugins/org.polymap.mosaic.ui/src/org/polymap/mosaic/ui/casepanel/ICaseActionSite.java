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

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.polymap.core.runtime.event.EventHandler;

import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

/**
 * The primary interface between an {@link ICaseAction} and the {@link CasePanel} framework.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ICaseActionSite {

    public IPanelSite getPanelSite();
    
    public IAppContext getContext();

    /**
     * Specifies i fthe status 
     * @return this
     */
    public ICaseActionSite setShowStatus( boolean show );
    
    /**
     * False specifies that no default submit/discard buttons should
     * be generated.
     * @return this
     */
    public ICaseActionSite setShowSubmitButton( boolean show );
    
    /**
     * Sets the status of the submit button.
     * @return this
     */
    public ICaseActionSite setSubmitEnabled( boolean enabled );
    
    public String getActionId();
    
    /**
     * Activates the {@link ICaseAction} for the given actionId.
     * <p/>
     * Consider {@link Display#asyncExec(Runnable)} if called from the
     * {@link ICaseAction#init(ICaseActionSite)} method to allow the framework to
     * initialize properly before the method is called.
     * 
     * @param actionId
     */
    public void activateCaseAction( String actionId );
    
    /**
     * Programmatically trigger submit this action. The same action as the UI element
     * created by {@link #createSubmit(Composite, String)} would trigger. This also
     * closes the action area.
     */
    public void submit();
    
    /**
     * Programmatically triggers discard this action. This closes the action area
     * without any further oparation.
     */
    public void discard();
    
    /**
     * Listener for {@link CaseActionEvent} events.
     *
     * @param annotated The {@link EventHandler annotated} listener. 
     */
    public void addListener( Object annotated );

    boolean removeListener( Object annotated );

    public IPanelToolkit toolkit();

    public Button createSubmit( Composite formContainer, String string );
    
}
