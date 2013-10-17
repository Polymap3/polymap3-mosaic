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
package org.polymap.mosaic.ui.casestable;

import java.util.Arrays;
import java.util.List;

import org.geotools.data.FeatureSource;
import org.geotools.feature.NameImpl;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.Filter;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.rwt.graphics.Graphics;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.data.ui.featuretable.SimpleFeatureTableElement;

import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicCaseEvent;
import org.polymap.mosaic.server.model2.MosaicCase2;
import org.polymap.mosaic.server.model2.MosaicRepository2;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CasesTableViewer
        extends FeatureTableViewer {

    private static Log log = LogFactory.getLog( CasesTableViewer.class );

    private static final FastDateFormat df = FastDateFormat.getInstance( "dd.MM.yyyy" );

    private MosaicRepository2       repo;
    
    private Filter                  baseFilter;

    private FeatureSource           fs;

    private FeatureType             schema;
    
    
    public CasesTableViewer( Composite parent, Filter baseFilter, int style ) {
        super( parent, /*SWT.VIRTUAL | SWT.V_SCROLL | SWT.FULL_SELECTION |*/ SWT.NONE );

        this.repo = MosaicRepository2.instance();
        this.baseFilter = baseFilter != null ? baseFilter : Filter.INCLUDE;
        
        fs = repo.featureSource( IMosaicCase.class );
        schema = fs.getSchema();
        addColumn( new StatusColumn() );
        PropertyDescriptor nameProp = schema.getDescriptor( new NameImpl( "", "name" ) );
        addColumn( new DefaultFeatureTableColumn( nameProp ).setWeight( 2, 100 ) );
        addColumn( new DateColumn() );
        
        setContent( fs, baseFilter );
    }

    
    public List<IMosaicCase> getSelected() {
        return ImmutableList.copyOf( Iterables.transform( Arrays.asList( getSelectedElements() ), new CaseFinder() ) );
    }


    /**
     * 
     */
    class CaseFinder implements Function<IFeatureTableElement,IMosaicCase> {
        public IMosaicCase apply( IFeatureTableElement input ) {
            return repo.entity( MosaicCase2.class, input.fid() );
        }
    };

    
    /**
     * 
     */
    class StatusColumn
            extends DefaultFeatureTableColumn {

        public StatusColumn() {
            super( schema.getDescriptor( new NameImpl( "", "name" ) ) );
            setWeight( 1, 80 );
            setHeader( "" );
            setLabelProvider( new ColumnLabelProvider() {
                @Override
                public String getText( Object elm ) {
                    Feature feature = ((SimpleFeatureTableElement)elm).feature();
                    MosaicCase2 mcase = repo.entityForState( MosaicCase2.class, feature );
                    List<IMosaicCaseEvent> events = ImmutableList.copyOf( mcase.getEvents() );
                    if (events.size() == 1) {
                        return "NEU";
                    }
                    else {
                        IMosaicCaseEvent last = events.get( events.size()-1 );
                        log.info( "event type: " +  last.getEventType() );
                        if (last.getEventType().equals( IMosaicCaseEvent.TYPE_CLOSED )) {
                            return "ERLEDIGT";                            
                        }
                        else {
                            return "OFFEN";
                        }
                    }
                }
                @Override
                public Color getBackground( Object elm ) {
                    return Graphics.getColor( 0x6e, 0x20, 0xbe );
//                    switch (((IMosaicCase)elm).antragStatus().get()) {
//                        case 0: return Graphics.getColor( 0xff, 0xcc, 0x00 );
//                        case 1: return Graphics.getColor( 0x6e, 0x20, 0xbe );
//                        default: return Graphics.getColor( 0xf0, 0xf0, 0xf0 );
//                    }
                }
                @Override
                public Color getForeground( Object elm ) {
                    return Graphics.getColor( 0xff, 0xff, 0xff );                
                }
                @Override
                public Font getFont( Object element ) {
                    FontData[] defaultFont = getTable().getFont().getFontData();
                    FontData bold = new FontData(defaultFont[0].getName(), defaultFont[0].getHeight(), SWT.BOLD);
                    return Graphics.getFont( bold );
                }
            });
        }
    }

    
    /**
     * 
     */
    class DateColumn
            extends DefaultFeatureTableColumn {

        public DateColumn() {
            super( schema.getDescriptor( new NameImpl( "", "name" ) ) );
            setWeight( 1, 80 );
            setHeader( "Angelegt am" );
            setLabelProvider( new ColumnLabelProvider() {
                @Override
                public String getText( Object elm ) {
                    IMosaicCase mc = new CaseFinder().apply( (IFeatureTableElement)elm );
                    IMosaicCaseEvent event = Iterables.getFirst( mc.getEvents(), null );
                    return event != null ? df.format( event.getTimestamp() ) : "?";
                }
            });
        }
    }
    
}
