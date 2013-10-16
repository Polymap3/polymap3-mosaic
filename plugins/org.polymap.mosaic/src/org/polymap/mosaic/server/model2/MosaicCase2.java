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
package org.polymap.mosaic.server.model2;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Point;

import org.polymap.core.model2.Defaults;
import org.polymap.core.model2.Description;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.store.feature.SRS;
import org.polymap.core.project.IMap;

import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicCaseEvent;
import org.polymap.mosaic.server.model2.MosaicRepository2.EntityCreator;

/**
 * Provides the base Mosaic business case. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
//@Concerns( LogConcern.class )
//@Mixins( {TrackableMixin.class} )
@Description( "Provides meta data of all Mosaic cases in the store" )
@SRS( "EPSG:4326" )
@NameInStore( MosaicCase2.FEATURETYPE_NAME )
public class MosaicCase2
        extends Entity
        implements IMosaicCase {

    public static final String          FEATURETYPE_NAME = "Cases";
    
    @Nullable
    public Property<Point>              geom;

    public Property<String>             name;

    @Nullable
    public Property<String>             description;

    @Nullable
    @Defaults
    public Property<String>             natures;

//    @Nullable 
//    @Defaults
//    public Property<Date>               created;
//
//    @Nullable 
//    @Defaults
//    public Property<Date>               lastModified;

//    @Nullable
//    public Property<MosaicCaseEvent2>   created;
//    
//    /** First event is the creation event. */
//    @Defaults
//    public CollectionProperty<String>   eventIds;

    protected Property<String>          metaDataMapId;

    protected Property<String>          dataMapId;
    
    
    @Override
    public String getId() {
        return (String)id();
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public String getDescription() {
        return description.get();
    }

    @Override
    public List<String> getNatures() {
        return ImmutableList.copyOf( Splitter.on( ',' ).split( natures.get() ) );
    }

    @Override
    public void addNature( String nature ) {
        assert !nature.contains( "," );
        if (natures.get().length() == 0) {
            natures.set( nature );
        }
        else if (!Iterables.contains( getNatures(), nature )) {
            natures.set( Joiner.on( ',' ).join( natures.get(), nature ) );
        }
    }

    @Override
    public String put( final String key, final String value ) {
        // FIXME check if key already exists
        MosaicRepository2 repo = MosaicRepository2.instance();
        repo.newEntity( MosaicCaseKeyValue.class, null, new EntityCreator<MosaicCaseKeyValue>() {
            public void create( MosaicCaseKeyValue prototype ) throws Exception {
                prototype.caseId.set( getId() );
                prototype.key.set( key );
                prototype.value.set( value );
            }
        });
        return null;
    }

    @Override
    public String get( String key ) {
        MosaicRepository2 repo = MosaicRepository2.instance();
        FilterFactory2 ff = MosaicRepository2.ff;
        Filter filter = ff.and(
                ff.equals( ff.property( "key" ), ff.literal( key ) ),
                ff.equals( ff.property( "caseId" ), ff.literal( getId() ) ) );
        Collection<MosaicCaseKeyValue> result = repo.query( MosaicCaseKeyValue.class, filter ).maxResults( 2 ).execute();
        assert result.size() < 2;
        return result.size() == 1 ? Iterables.getOnlyElement( result, null ).value.get() : null;
    }

    @Override
    public Iterable<? extends IMosaicCaseEvent> getEvents() {
        MosaicRepository2 repo = MosaicRepository2.instance();
        FilterFactory2 ff = MosaicRepository2.ff;
        Filter filter = ff.equals( ff.property( "caseId" ), ff.literal( getId() ) );
        return repo.query( MosaicCaseEvent2.class, filter ).execute();

//        return Iterables.transform( eventIds, new Function<String,IMosaicCaseEvent>() {
//            MosaicRepository2 repo = MosaicRepository2.instance();
//            public IMosaicCaseEvent apply( String id ) {
//                return repo.entity( MosaicCaseEvent2.class, id );
//            }
//        });
    }

    @Override
    public void addEvent( IMosaicCaseEvent event ) {
        ((MosaicCaseEvent2)event).caseId.set( getId() );
//        eventIds.add( event.getId() );
    }

    @Override
    public IMap getMetaDataMap() {
        return MosaicRepository2.instance().projectRepo().findEntity( IMap.class, metaDataMapId.get() );
    }
    
    @Override
    public IMap getDataMap() {
        return MosaicRepository2.instance().projectRepo().findEntity( IMap.class, dataMapId.get() );
    }
    
}
