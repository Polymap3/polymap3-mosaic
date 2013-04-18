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

import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.eclipse.ui.forms.widgets.Section;

/**
 * The factory for basic UI elements used by {@link IPanel} instances.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface IAtlasToolkit {

    /**
     *
     * @see Label#Label(Composite, int)
     * @param parent
     * @param text
     * @param styles
     * @return Newly created control instance.
     */
    public Label createLabel( Composite parent, String text, int... styles );

    public Label createLink( Composite parent, String text, int... styles );

    public Button createPushButton( Composite parent, String text, Image icon, SelectionListener l );

    public Composite createComposite( Composite parent, int... styles );

    public Section createSection( Composite parent, String title, int... styles );

}
