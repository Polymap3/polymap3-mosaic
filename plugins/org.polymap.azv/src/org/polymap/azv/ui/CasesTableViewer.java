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
package org.polymap.azv.ui;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.rwt.graphics.Graphics;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.polymap.azv.model.Schachtschein;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CasesTableViewer
        extends TableViewer {

    private static Log log = LogFactory.getLog( CasesTableViewer.class );
    
    private Iterable<Schachtschein>     elms;

    public CasesTableViewer( Composite parent, Iterable<Schachtschein> elms ) {
        super( parent, /*SWT.VIRTUAL | SWT.V_SCROLL | SWT.FULL_SELECTION |*/ SWT.NONE );

        this.elms = elms;
        
        getTable().setLinesVisible( true );
        getTable().setHeaderVisible( false );
        getTable().setLayout( new TableLayout() );
        
        TableViewerColumn statusColumn = new TableViewerColumn( this, SWT.CENTER );
        statusColumn.getColumn().setWidth( 80 );
        statusColumn.setLabelProvider( new ColumnLabelProvider() {
            public String getText( Object elm ) {
                switch (((Schachtschein)elm).antragStatus().get()) {
                    case 0: return "OFFEN";
                    case 1: return "ERLEDIGT";
                    default: return "Hmmm...";
                }
            }
            public Color getBackground( Object elm ) {
                switch (((Schachtschein)elm).antragStatus().get()) {
                    case 0: return Graphics.getColor( 0xff, 0xcc, 0x00 );
                    case 1: return Graphics.getColor( 0x6e, 0x20, 0xbe );
                    default: return Graphics.getColor( 0xf0, 0xf0, 0xf0 );
                }
            }
            public Color getForeground( Object elm ) {
                return Graphics.getColor( 0xff, 0xff, 0xff );                
            }
            public Font getFont( Object element ) {
                FontData[] defaultFont = getTable().getFont().getFontData();
                FontData bold = new FontData(defaultFont[0].getName(), defaultFont[0].getHeight(), SWT.BOLD);
                return Graphics.getFont( bold );
            }
        });
        TableViewerColumn nameColumn = new TableViewerColumn( this, SWT.NONE );
        nameColumn.getColumn().setText( "Bezeichnung" );
        nameColumn.getColumn().setWidth( 150 );
        nameColumn.setLabelProvider( new ColumnLabelProvider() {
            public String getText( Object elm ) {
                return ((Schachtschein)elm).beschreibung().get();
            }
        });
        TableViewerColumn dateColumn = new TableViewerColumn( this, SWT.NONE );
        dateColumn.getColumn().setText( "Datum" );
        dateColumn.getColumn().setWidth( 150 );
        dateColumn.setLabelProvider( new ColumnLabelProvider() {
            public String getText( Object elm ) {
                Date date = ((Schachtschein)elm).startDate().get();
                return date != null ? date.toLocaleString() : "-";
            }
        });
        
        setContentProvider( new ArrayContentProvider() );
        setInput( Iterables.toArray( elms, Schachtschein.class ) );
    }

    
    @Override
    public void refresh() {
        setInput( Iterables.toArray( elms, Schachtschein.class ) );
    }
}
