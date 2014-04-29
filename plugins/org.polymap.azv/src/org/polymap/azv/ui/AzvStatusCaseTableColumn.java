/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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

import static org.polymap.azv.AzvPlugin.EVENT_TYPE_ABGEBROCHEN;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_ANBEARBEITUNG;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_ANFREIGABE;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_BEANTRAGT;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_FREIGABE;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_STORNIERT;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.runtime.IMessages;

import org.polymap.azv.Messages;
import org.polymap.azv.model.AzvVorgang;
import org.polymap.mosaic.server.model.IMosaicCaseEvent;
import org.polymap.mosaic.server.model2.MosaicCase2;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casestable.CasesTableViewer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AzvStatusCaseTableColumn
        extends DefaultFeatureTableColumn {

    private static Log log = LogFactory.getLog( AzvStatusCaseTableColumn.class );

    private static final IMessages  i18n = Messages.forPrefix( "StatusColumn" ); //$NON-NLS-1$

    private MosaicRepository2       repo;

    public AzvStatusCaseTableColumn( final MosaicRepository2 repo ) {
        super( CasesTableViewer.createDescriptor( "azvstatus", String.class ) ); //$NON-NLS-1$
        this.repo = repo;
        setWeight( 1, 90 );
        setHeader( "" ); //$NON-NLS-1$
        setAlign( SWT.LEFT );
        setSortable( false );

        setLabelProvider( new ColumnLabelProvider() {
            
            private MosaicCase2 mcase;
            private String azvStatus;
            
            @Override
            public String getText( Object elm ) {
                String fid = ((IFeatureTableElement)elm).fid();
                mcase = repo.entity( MosaicCase2.class, fid );
                
                azvStatus = AzvVorgang.azvStatusOf( mcase );
                if (azvStatus == null) {
                    return i18n.get( "neu" );                    
                }
                else if (azvStatus.equals( EVENT_TYPE_ABGEBROCHEN )) {
                    return i18n.get( "abbruch" );
                }
                else if (azvStatus.equals( EVENT_TYPE_STORNIERT )) {
                    return i18n.get( "storno" );
                }
                else if (azvStatus.equals( EVENT_TYPE_BEANTRAGT )) {
                    return i18n.get( "antrag" );
                }
                else if (azvStatus.equals( EVENT_TYPE_FREIGABE )) {
                    return i18n.get( "freigabe" );
                }
                else if (azvStatus.equals( EVENT_TYPE_ANFREIGABE )) {
                    return i18n.get( "anFreigabe" );
                }
                else if (azvStatus.equals( EVENT_TYPE_ANBEARBEITUNG )) {
                    return i18n.get( "anBearbeitung" );
                }
                else if (IMosaicCaseEvent.TYPE_CLOSED.equals( mcase.getStatus() )) {
                    return i18n.get( "erledigt" );
                }
                else {
                    return i18n.get( "unknown" );
                }
            }
            
            @Override
            public Color getBackground( Object elm ) {
                assert mcase != null && mcase.id().equals( ((IFeatureTableElement)elm).fid() );

                if (azvStatus == null) {
                    return MosaicUiPlugin.COLOR_NEW.get();                    
                }
                else if (azvStatus.equals( EVENT_TYPE_ABGEBROCHEN )
                        || azvStatus.equals( EVENT_TYPE_STORNIERT )) {
                    return MosaicUiPlugin.COLOR_RED.get();
                }
                else if (azvStatus.equals( EVENT_TYPE_BEANTRAGT )
                        || azvStatus.equals( EVENT_TYPE_ANFREIGABE )
                        || azvStatus.equals( EVENT_TYPE_ANBEARBEITUNG )) {
                    return MosaicUiPlugin.COLOR_OPEN.get();
                }
                else if (IMosaicCaseEvent.TYPE_CLOSED.equals( mcase.status.get() )
                        || azvStatus.equals( EVENT_TYPE_FREIGABE )) {
                    return MosaicUiPlugin.COLOR_CLOSED.get();
                }
                else {
                    return null;
                }
            }
            
            @Override
            public Color getForeground( Object elm ) {
                return MosaicUiPlugin.COLOR_STATUS_FOREGROUND.get();                
            }
            
            //                @Override
            //                public Font getFont( Object element ) {
            //                    FontData[] defaultFont = getTable().getFont().getFontData();
            //                    FontData bold = new FontData(defaultFont[0].getName(), defaultFont[0].getHeight(), SWT.BOLD);
            //                    return Graphics.getFont( bold );
            //                }
        });
    }
}
