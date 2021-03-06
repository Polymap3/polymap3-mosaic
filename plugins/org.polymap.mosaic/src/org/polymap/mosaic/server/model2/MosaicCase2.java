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

import static org.polymap.mosaic.server.model2.MosaicRepository2.ff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.beans.PropertyChangeEvent;

import javax.annotation.Nullable;

import org.opengis.filter.Filter;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Point;

import org.polymap.core.model2.Concerns;
import org.polymap.core.model2.DefaultValue;
import org.polymap.core.model2.Defaults;
import org.polymap.core.model2.Description;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.Property;
import org.polymap.core.model2.runtime.event.PropertyChangeSupport;
import org.polymap.core.model2.store.feature.SRS;
import org.polymap.core.project.IMap;
import org.polymap.core.runtime.CachedLazyInit;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicCaseEvent;
import org.polymap.mosaic.server.model2.MosaicRepository2.EntityCreator;

/**
 * Provides the base Mosaic business case. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
//@Concerns( LogConcern.class )
@Description( "Provides meta data of all Mosaic cases in the store" )
@SRS( "EPSG:4326" )
@NameInStore( MosaicCase2.FEATURETYPE_NAME )
@Concerns( {PropertyChangeSupport.class} )
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
    
    @Defaults
    public Property<Date>               created;

    @Defaults
    public Property<Date>               lastModified;
    
    /** One of: {@link IMosaicCaseEvent#TYPE_OPEN}, {@link IMosaicCaseEvent#TYPE_CLOSED} */
    @DefaultValue(IMosaicCaseEvent.TYPE_OPEN)
    public Property<String>             status;


//    /** First event is the creation event. */
//    @Defaults
//    public CollectionProperty<String>   eventIds;

    protected Property<String>          metaDataMapId;

    protected Property<String>          dataMapId;
    
    protected Property<String>          documentsDir;
    
    private Lazy<Collection<IMosaicCaseEvent>> events = new CachedLazyInit( 1024 );

    private Lazy<Map<String,MosaicCaseKeyValue>> keyValues = new CachedLazyInit( 1024 ); 
    

    protected MosaicRepository2 repo() {
        return MosaicRepository2.session( context.getUnitOfWork() );
    }
    
    @Override
    public String getId() {
        return (String)id();
    }

    @Override
    public String getName() {
        return name.get();
    }

    @Override
    public void setName( String value ) {
        name.set( value );
        repo().newCaseEvent( this, "Name: " + value, "Der Name wurde geändert auf: " + value, "Wert"  );
        EventManager.instance().publish( new PropertyChangeEvent( this, "name", null, value ) );
    }

    @Override
    public String getDescription() {
        return description.get();
    }

    @Override
    public void setDescription( String value ) {
        description.set( value );
        repo().newCaseEvent( this, "Beschreibung: " + value, "Die Beschreibung wurde geändert auf: " + value, "Wert"  );
    }

    @Override
    public Date getCreated() {
        return created.get();
    }

    @Override
    public Date getLastModified() {
        return lastModified.get();
    }

    @Override
    public String getStatus() {
        return status.get();
    }

    @Override
    public Set<String> getNatures() {
        return ImmutableSet.copyOf( Splitter.on( ',' ).split( natures.get() ) );
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
        // XXX No event. This is done mostly before case is created. Not important for user?
        //repo().newCaseEvent( this, "Natur: " + nature, "Der Natur des Vorgangs wurde gesetzt auf: " + natures.get(), "Wert"  );
    }

    @Override
    public String put( final String key, final String value ) {
        final MosaicRepository2 repo = repo();
        MosaicCaseKeyValue current = getKeyValue( key );
        if (current != null) {
            current.value.set( value );
            
//            repo.newCaseEvent( this, "Wert: " + value, 
//                    "Der Wert \"" + key + "\" wurde geändert auf: " + value, "Wert"  );
        }
        else {
            current = repo.newEntity( MosaicCaseKeyValue.class, null, new EntityCreator<MosaicCaseKeyValue>() {
                public void create( MosaicCaseKeyValue prototype ) throws Exception {
                    prototype.caseId.set( getId() );
                    prototype.key.set( key );
                    prototype.value.set( value );

//                    repo.newCaseEvent( MosaicCase2.this, "Neuer Wert: " + value, 
//                            "Der Wert \"" + key + "\" wurde angelegt mit: " + value, "Wert"  );
                }
            });
            // XXX race cond between getKeyValue() and this put()
            keyValues.clear(); //get().put( key, current );
        }
        EventManager.instance().publish( new PropertyChangeEvent( this, key, null, value ) );
        return null;
    }

    @Override
    public String get( String key ) {
        MosaicCaseKeyValue result = getKeyValue( key );
        return result != null ? result.value.get() : null;
    }

    protected MosaicCaseKeyValue getKeyValue( String key ) {
        return keyValues.get( new Supplier<Map<String,MosaicCaseKeyValue>>() {
            public Map<String,MosaicCaseKeyValue> get() {
                Filter filter = ff.equals( ff.property( "caseId" ), ff.literal( getId() ) );
                Collection<MosaicCaseKeyValue> rs = repo().query( MosaicCaseKeyValue.class, filter ).execute();
                
                Map<String, MosaicCaseKeyValue> result = new HashMap( 32 );
                for (MosaicCaseKeyValue keyValue : rs) {
                    result.put( keyValue.key.get(), keyValue );
                }
                return result;
            }
        }).get( key );
        
//        FilterFactory2 ff = MosaicRepository2.ff;
//        Filter filter = ff.and(
//                ff.equals( ff.property( "key" ), ff.literal( key ) ),
//                ff.equals( ff.property( "caseId" ), ff.literal( getId() ) ) );
//        Collection<MosaicCaseKeyValue> results = repo().query( MosaicCaseKeyValue.class, filter ).maxResults( 2 ).execute();
//        // check multiple elms
//        MosaicCaseKeyValue result = Iterables.getOnlyElement( results, null );
//        return result.size() == 1 ?  : null;
    }
    
    @Override
    public Iterable<IMosaicCaseEvent> getEvents() {
        return events.get( new Supplier<Collection<IMosaicCaseEvent>>() {
            public Collection<IMosaicCaseEvent> get() {
                Filter filter = ff.equals( ff.property( "caseId" ), ff.literal( getId() ) );
                
                Collection<MosaicCaseEvent2> result = repo().query( MosaicCaseEvent2.class, filter ).execute();
                return new ArrayList( result );
            }
        });

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
        events.clear();
        
        lastModified.set( new Date() );
        PropertyChangeEvent ev = new PropertyChangeEvent( this, "events", null, event );
        EventManager.instance().publish( ev );
    }

    @Override
    public IMap getMetaDataMap() {
        MosaicRepository2 repo = MosaicRepository2.session( context.getUnitOfWork() );
        return repo.projectRepo().findEntity( IMap.class, metaDataMapId.get() );
    }
    
    @Override
    public IMap getDataMap() {
        MosaicRepository2 repo = MosaicRepository2.session( context.getUnitOfWork() );
        return repo.projectRepo().findEntity( IMap.class, dataMapId.get() );
    }
    
}
