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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.runtime.Polymap;

import org.polymap.atlas.toolkit.ILayoutContainer;
import org.polymap.atlas.toolkit.IPanelSection;
import org.polymap.atlas.toolkit.IPanelToolkit;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DesktopToolkit
        implements IPanelToolkit {

    public static final String  CUSTOM_VARIANT_VALUE = "atlas-panel";
    public static final Color   COLOR_SECTION_TITLE_FG = Graphics.getColor( new RGB( 0x54, 0x82, 0xb4 ) );
    public static final Color   COLOR_SECTION_TITLE_BG = Graphics.getColor( new RGB( 0xd7, 0xeb, 0xff ) );

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
    public Button createButton( Composite parent, String text, int... styles ) {
        Button control = adapt( new Button( parent, stylebits( styles ) ), true, true );
        if (text != null) {
            control.setText( StringUtils.upperCase( text, Polymap.getSessionLocale() ) );
        }
        return control;
    }
    
    @Override
    public Text createText( Composite parent, String defaultText, int... styles ) {
        Text control = adapt( new Text( parent, stylebits( styles ) ), true, true );
        if (defaultText != null) {
            control.setText( defaultText );
        }
        return control;
    }

    @Override
    public Composite createComposite( Composite parent, int... styles ) {
        boolean scrollable = ArrayUtils.contains( styles, SWT.V_SCROLL )
                || ArrayUtils.contains( styles, SWT.H_SCROLL );
        
        Composite result = null;
        if (scrollable) { 
            result = new ScrolledComposite( parent, stylebits( styles ) );
            ((ScrolledComposite)result).setExpandHorizontal( true );
            ((ScrolledComposite)result).setExpandVertical( true );
            
            Composite content = createComposite( result );
            ((ScrolledComposite)result).setContent( content );
            
            result.setLayout( new FillLayout() );
        }
        else {
            result = new Composite( parent, stylebits( styles ) );
        }
        return adapt( result );
    }

    
    @Override
    public Section createSection( Composite parent, String title, int... styles ) {
        Section result = adapt( new Section( parent, stylebits( styles ) | SWT.NO_FOCUS ) );
        result.setText( title );
        result.setExpanded( true );

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
        result.setTitleBarForeground( COLOR_SECTION_TITLE_FG );
        result.setTitleBarBackground( COLOR_SECTION_TITLE_BG );
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

    
    @Override
    public IPanelSection createPanelSection( Composite parent, String title, int... styles ) {
        DesktopPanelSection result = new DesktopPanelSection( this, parent, styles );
        adapt( result.getControl() );
        if (title != null) {
            result.setTitle( title );
        }
        return result;
    }

    
    @Override
    public IPanelSection createPanelSection( ILayoutContainer parent, String title, int... styles ) {
        return createPanelSection( parent.getBody(), title, styles );
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