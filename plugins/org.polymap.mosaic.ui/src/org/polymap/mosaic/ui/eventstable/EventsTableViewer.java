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
package org.polymap.mosaic.ui.eventstable;

import static org.polymap.mosaic.ui.MosaicUiPlugin.*;

import java.util.Arrays;
import java.util.List;

import java.beans.PropertyChangeEvent;

import org.geotools.data.FeatureSource;
import org.geotools.feature.NameImpl;
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
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableViewer;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicCaseEvent;
import org.polymap.mosaic.server.model2.MosaicCaseEvent2;
import org.polymap.mosaic.server.model2.MosaicRepository2;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EventsTableViewer
        extends FeatureTableViewer {

    private static Log log = LogFactory.getLog( EventsTableViewer.class );

    private static final FastDateFormat df = FastDateFormat.getInstance( "dd.MM.yyyy" );

    private MosaicRepository2       repo;
    
    private IMosaicCase             mcase;
    
    private Filter                  baseFilter;

    private FeatureSource           fs;

    private FeatureType             schema;
    
    
    public EventsTableViewer( Composite parent, IMosaicCase mcase, int style ) {
        super( parent, /*SWT.VIRTUAL | SWT.V_SCROLL | SWT.FULL_SELECTION |*/ SWT.NONE );
        this.mcase = mcase;

        EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {
            public boolean apply( PropertyChangeEvent input ) {
                return input.getSource() == EventsTableViewer.this.mcase;
            }
        });
        
        this.repo = MosaicRepository2.instance();
        this.baseFilter = ff.equals( ff.property( "caseId" ), ff.literal( mcase.getId() ) );
        
        fs = repo.featureSource( IMosaicCaseEvent.class );
        schema = fs.getSchema();
        PropertyDescriptor prop = schema.getDescriptor( new NameImpl( "", "name" ) );
        addColumn( new DefaultFeatureTableColumn( prop ).setWeight( 2, 60 ) );
        
        prop = schema.getDescriptor( new NameImpl( "", "type" ) );
        addColumn( new DefaultFeatureTableColumn( prop ).setWeight( 1, 60 ).setHeader( "Art" ) );
        
        prop = schema.getDescriptor( new NameImpl( "", "timestamp" ) );
        addColumn( new DateColumn( prop ).setWeight( 1, 60 ) );
        
        setContent( fs, baseFilter );
    }

    
    @Override
    public void dispose() {
        EventManager.instance().unsubscribe( this );
        super.dispose();
    }


    @EventHandler(display=true)
    protected void caseChanged( PropertyChangeEvent ev ) {
        refresh();        
    }
    
    
    public List<IMosaicCaseEvent> getSelected() {
        return ImmutableList.copyOf( Iterables.transform( Arrays.asList( getSelectedElements() ), new EventFinder() ) );
    }


    /**
     * 
     */
    class EventFinder implements Function<IFeatureTableElement,IMosaicCaseEvent> {
        public IMosaicCaseEvent apply( IFeatureTableElement input ) {
            return repo.entity( MosaicCaseEvent2.class, input.fid() );
        }
    };

    
    /**
     * 
     */
    class DateColumn
            extends DefaultFeatureTableColumn {

        public DateColumn(PropertyDescriptor prop) {
            super( prop );
            setHeader( "Datum" );
            setLabelProvider( new ColumnLabelProvider() {
                @Override
                public String getText( Object elm ) {
                    IMosaicCaseEvent event = new EventFinder().apply( (IFeatureTableElement)elm );
                    return event != null ? df.format( event.getTimestamp() ) : "?";
                }
            });
        }
    }
    
}
