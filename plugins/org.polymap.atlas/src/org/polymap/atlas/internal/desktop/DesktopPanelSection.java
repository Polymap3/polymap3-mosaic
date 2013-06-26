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
package org.polymap.atlas.internal.desktop;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Scrollable;

import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.atlas.toolkit.ConstraintData;
import org.polymap.atlas.toolkit.ILayoutElement;
import org.polymap.atlas.toolkit.IPanelSection;
import org.polymap.atlas.toolkit.LayoutConstraint;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
class DesktopPanelSection
        implements IPanelSection {

    private static Log log = LogFactory.getLog( DesktopPanelSection.class );
    
    private PanelSection            control;
    
    private int                     level;

    private List<LayoutConstraint>  constraints = new ArrayList( 3 );        
    
    
    public void dispose() {
    }

    
    @Override
    public ILayoutElement addConstraint( LayoutConstraint constraint ) {
        constraints.add( constraint );
        return this;
    }

    
    public DesktopPanelSection( DesktopToolkit tk, Composite parent, int[] styles ) {
        control = new PanelSection( parent, SWT.NO_FOCUS | SWT.BORDER );
        control.setData( "panelSection", this );
        control.setExpanded( true );
        control.setMenu( parent.getMenu() );
        control.setLayout( new FillLayout() );

//        FontData[] defaultFont = parent.getFont().getFontData();
//        FontData bold = new FontData(defaultFont[0].getName(), defaultFont[0].getHeight(), SWT.BOLD);
//        control.setFont( Graphics.getFont( bold ) );
//        control.setTitleBarForeground( DesktopToolkit.COLOR_SECTION_TITLE_FG );
//        control.setTitleBarBackground( DesktopToolkit.COLOR_SECTION_TITLE_BG );
//        control.setTitleBarBorderColor( Graphics.getColor( new RGB( 0x80, 0x80, 0xa0 ) ) );

        control.getTitleControl().setData( WidgetUtil.CUSTOM_VARIANT, DesktopToolkit.CUSTOM_VARIANT_VALUE + "-section-title"  );
        //control.getSeparatorControl().setData( WidgetUtil.CUSTOM_VARIANT, DesktopToolkit.CUSTOM_VARIANT_VALUE + "-section-separator"  );
        //control.getTextClient().setData( WidgetUtil.CUSTOM_VARIANT, DesktopToolkit.CUSTOM_VARIANT_VALUE + "-section-client"  );
        //control.getDescriptionControl().setData( WidgetUtil.CUSTOM_VARIANT, DesktopToolkit.CUSTOM_VARIANT_VALUE + "-section"  );
        
        Label sep = new Label( control, SWT.SEPARATOR | SWT.HORIZONTAL );
        sep.setData( WidgetUtil.CUSTOM_VARIANT, DesktopToolkit.CUSTOM_VARIANT_VALUE + "-section-separator"  );
        control.setSeparatorControl( sep );
        
        Composite client = tk.adapt( new Composite( control, tk.stylebits( SWT.NO_FOCUS ) ) );

        //client.setLayout( new ConstraintLayout() );
        client.setLayout( new PanelSectionLayout() );
        control.setClient( client );
        
        level = getParentPanel() != null ? getParentPanel().getLevel()+1 : 0;
    }

    
    /**
     * Do the layout of this newly added children. 
     */
    protected void controlAdded( Control c ) {
        log.info( "control added: " + c );
    }

    protected void controlRemoved( Control c ) {
        log.info( "control removed: " + c );
    }

    
    @Override
    public Composite getControl() {
        return control;
    }

    
    @Override
    public Composite getBody() {
        return (Composite)control.getClient();
    }


    @Override
    public IPanelSection getParentPanel() {
        // parent -> section -> client
        for (Composite cursor=control.getParent(); cursor!=null; cursor=cursor.getParent()) {
            Object result = cursor.getData( "panelSection" );
            if (result != null) {
                return (IPanelSection)result;
            }
        }
        return null;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public String getTitle() {
        return control.getText();
    }

    @Override
    public IPanelSection setTitle( String title ) {
        control.setText( title );
        return this;
    }

    @Override
    public boolean isExpanded() {
        return control.isExpanded();
    }

    @Override
    public IPanelSection setExpanded( boolean expanded ) {
        control.setExpanded( expanded );
        return this;
    }

    
    /**
     * 
     */
    static class PanelSection
            extends Section {
        
        public PanelSection( Composite parent, int style ) {
            super( parent, style );
        }

        public Control getTitleControl() {
            return textLabel;
        }
    }
    
    
    /**
     * 
     */
    static class PanelSectionLayout
            extends Layout {
        
        public int marginWidth = 8;

        public int marginHeight = 8;
        
        public int spacing = 8;

        
        @Override
        protected void layout( Composite composite, boolean flushCache ) {
            Rectangle clientArea = composite.getClientArea();
            Control[] children = composite.getChildren();
            int count = children.length;

            if (count > 0) {
                int width = clientArea.width - marginWidth * 2;
                int height = clientArea.height - marginHeight * 2;
                height -= (count - 1) * spacing;
                
                int x = clientArea.x + marginWidth;
                int y = clientArea.y + marginHeight;

                int cellHeight = height / count;
                int extra = height % count;
                
                for (int i=0; i<count; i++) {
                    int childHeight = cellHeight;
                    if (i == 0) {
                        childHeight += extra / 2;
                    } 
                    else if (i == count - 1) {
                        childHeight += (extra + 1) / 2;
                    }
                    
                    children[i].setBounds( x, y, width, childHeight );
                    y += childHeight + spacing;
                }
            }
        }

        
        @Override
        protected Point computeSize( Composite composite, int wHint, int hHint, boolean flushCache ) {
            Control[] children = composite.getChildren();
            int count = children.length;
            int maxWidth = 0, maxHeight = 0;

            for (int i = 0; i < count; i++) {
                Control child = children[i];
                int w = wHint, h = hHint;
                if (count > 0) {
                    // vertical
                    if (hHint != SWT.DEFAULT) {
                        h = Math.max( 0, (hHint - (count - 1) * spacing) / count );
                    }
                }
                Point size = computeChildSize( child, w, h, flushCache );
                maxWidth = Math.max( maxWidth, size.x );
                maxHeight = Math.max( maxHeight, size.y );
            }
            int width = 0, height = 0;
            // vertical
            width = maxWidth;
            height = count * maxHeight;
            if (count != 0) {
                height += (count - 1) * spacing;
            }
            
            width += marginWidth * 2;
            height += marginHeight * 2;
            
            if (wHint != SWT.DEFAULT) {
                width = wHint;
            }
            if (hHint != SWT.DEFAULT) {
                height = hHint;
            }
            return new Point( width, height );
        }
        
        
        protected Iterable<Control> prioritizeControls() {
            throw new RuntimeException( "not yet implemented." );
        }
        
        
        protected Point computeChildSize( Control control, int wHint, int hHint, boolean flushCache ) {
            ConstraintData data = (ConstraintData)control.getLayoutData ();
            if (data == null) {
                data = new ConstraintData();
                control.setLayoutData( data );
            }
            Point size = null;
            if (wHint == SWT.DEFAULT && hHint == SWT.DEFAULT) {
                size = data.computeSize( control, wHint, hHint, flushCache );
            } 
            else {
                // TEMPORARY CODE
                int trimX, trimY;
                if (control instanceof Scrollable) {
                    Rectangle rect = ((Scrollable)control).computeTrim( 0, 0, 0, 0 );
                    trimX = rect.width;
                    trimY = rect.height;
                } 
                else {
                    trimX = trimY = control.getBorderWidth () * 2;
                }
                int w = wHint == SWT.DEFAULT ? wHint : Math.max (0, wHint - trimX);
                int h = hHint == SWT.DEFAULT ? hHint : Math.max (0, hHint - trimY);
                size = data.computeSize( control, w, h, flushCache );
            }
            return size;
        }

    }
    
}
