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
package org.polymap.atlas.internal.desktop;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.runtime.Polymap;

import org.polymap.atlas.IAtlasToolkit;


/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DesktopToolkit
        implements IAtlasToolkit {

    public static final String  CUSTOM_VARIANT_VALUE = "desktop";

    private FormColors          colors;


    @Override
    public Label createLabel( Composite parent, String text, int... styles ) {
        Label control = adapt( new Label( parent, stylebits( styles ) ), false, false );
        if (text != null) {
            control.setText( text );
        }
        return control;
    }

    @Override
    public Label createLink( Composite parent, String text, int... styles ) {
        Label result = createLabel( parent, text, styles );
        result.setCursor( new Cursor( Polymap.getSessionDisplay(), SWT.CURSOR_HAND ) );
        result.setForeground( Graphics.getColor( 0x00, 0x00, 0xff ) );
        return result;
    }

    @Override
    public Button createPushButton( Composite parent, String text, Image icon, SelectionListener l ) {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public Composite createComposite( Composite parent, int... styles ) {
        return adapt( new Composite( parent, stylebits( styles ) ) );
    }

    @Override
    public Section createSection( Composite parent, String title, int... styles ) {
        Section result = adapt( new Section( parent, stylebits( styles ) | SWT.NO_FOCUS ) );
        result.setText( title );

        result.setMenu( parent.getMenu() );
//        if (result.toggle != null) {
//            section.toggle.setHoverDecorationColor(colors
//                    .getColor(IFormColors.TB_TOGGLE_HOVER));
//            section.toggle.setDecorationColor(colors
//                    .getColor(IFormColors.TB_TOGGLE));
//        }

//        result.setFont( boldFontHolder.getBoldFont(parent.getFont()));

//        if ((sectionStyle & Section.TITLE_BAR) != 0
//                || (sectionStyle & Section.SHORT_TITLE_BAR) != 0) {
//            colors.initializeSectionToolBarColors();
//            result.setTitleBarBackground( colors.getColor( IFormColors.TB_BG ) );
//            result.setTitleBarBorderColor( colors.getColor( IFormColors.TB_BORDER ) );
//        }
        // call setTitleBarForeground regardless as it also sets the label color
//        result.setTitleBarForeground( colors.getColor( IFormColors.TB_TOGGLE ) );

        FontData[] defaultFont = parent.getFont().getFontData();
        FontData bold = new FontData(defaultFont[0].getName(), defaultFont[0].getHeight(), SWT.BOLD);
        result.setFont( Graphics.getFont( bold ) );
        result.setTitleBarForeground( Graphics.getColor( new RGB( 0x80, 0x80, 0xb0 ) ) );
        result.setTitleBarBackground( Graphics.getColor( new RGB( 0xd0, 0xd0, 0xe0 ) ) );
        result.setTitleBarBorderColor( Graphics.getColor( new RGB( 0x80, 0x80, 0xa0 ) ) );

        Composite client = createComposite( result );
        result.setClient( client );

        FillLayout layout = new FillLayout( SWT.VERTICAL );
        layout.spacing = 1;
        layout.marginWidth = 2;
        layout.marginHeight = 2;
        client.setLayout( layout );
        return result;
    }

    protected <T extends Composite> T adapt( T composite ) {
        composite.setData( WidgetUtil.CUSTOM_VARIANT, CUSTOM_VARIANT_VALUE );

//        composite.setBackground( colors.getBackground() );
//        composite.addMouseListener( new MouseAdapter() {
//            public void mouseDown( MouseEvent e ) {
//                ((Control)e.widget).setFocus();
//            }
//        } );
//        if (composite.getParent() != null) {
//            composite.setMenu( composite.getParent().getMenu() );
//        }
        return composite;
    }


    /**
     * Adapts a control to be used in a form that is associated with this toolkit.
     * This involves adjusting colors and optionally adding handlers to ensure focus
     * tracking and keyboard management.
     *
     * @param control a control to adapt
     * @param trackFocus if <code>true</code>, form will be scrolled horizontally
     *        and/or vertically if needed to ensure that the control is visible when
     *        it gains focus. Set it to <code>false</code> if the control is not
     *        capable of gaining focus.
     * @param trackKeyboard if <code>true</code>, the control that is capable of
     *        gaining focus will be tracked for certain keys that are important to
     *        the underlying form (for example, PageUp, PageDown, ScrollUp,
     *        ScrollDown etc.). Set it to <code>false</code> if the control is not
     *        capable of gaining focus or these particular key event are already used
     *        by the control.
     */
    public <T extends Control> T adapt( T control, boolean trackFocus, boolean trackKeyboard) {
        control.setData( WidgetUtil.CUSTOM_VARIANT, CUSTOM_VARIANT_VALUE );

//        control.setBackground( colors.getBackground() );
//        control.setForeground( colors.getForeground() );

//        if (control instanceof ExpandableComposite) {
//            ExpandableComposite ec = (ExpandableComposite)control;
//            if (ec.toggle != null) {
//                if (trackFocus)
//                    ec.toggle.addFocusListener( visibilityHandler );
//                if (trackKeyboard)
//                    ec.toggle.addKeyListener( keyboardHandler );
//            }
//            if (ec.textLabel != null) {
//                if (trackFocus)
//                    ec.textLabel.addFocusListener( visibilityHandler );
//                if (trackKeyboard)
//                    ec.textLabel.addKeyListener( keyboardHandler );
//            }
//            return;
//        }

//        if (trackFocus) {
//            control.addFocusListener( visibilityHandler );
//        }
//        if (trackKeyboard) {
//            control.addKeyListener( keyboardHandler );
//        }
        return control;
    }


    protected int stylebits( int... styles ) {
        int result = SWT.NONE;
        for (int style : styles) {
            assert style != 0;
            result |= style;
        }
        return result;
    }

}
