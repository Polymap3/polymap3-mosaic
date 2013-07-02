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
package org.polymap.atlas.toolkit;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.atlas.IPanel;

/**
 * The factory for basic UI elements used by {@link IPanel} instances.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IPanelToolkit {

    /**
     *
     * @see Label#Label(Composite, int)
     * @param parent
     * @param text
     * @param styles
     * @return Newly created control instance.
     */
    public Label createLabel( Composite parent, String text, int... styles );

    public Label createFlowText( Composite parent, String text, int... styles );

    public Label createLink( Composite parent, String text, int... styles );

    public Button createButton( Composite parent, String text, int... styles );

    public Composite createComposite( Composite parent, int... styles );

    public Section createSection( Composite parent, String title, int... styles );

    public Text createText( Composite parent, String defaultText, int... styles );

    /**
     * Creates a top level section.
     * 
     * @param parent
     * @param title The heading of this section, or null if no heading.
     * @param styles One of the {@link IPanelSection} constants.
     * @return Newly created viewer instance.
     */
    public IPanelSection createPanelSection( Composite parent, String title, int... styles );

    /**
     * Equivalent of calling <code>createPanelSection( parent.getBody(), title, styles )</code>.
     * 
     * @see #createPanelSection(Composite, String, int...)
     */
    public IPanelSection createPanelSection( ILayoutContainer parent, String title, int... styles );

}
