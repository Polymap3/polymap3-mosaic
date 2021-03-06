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
package org.polymap.azv.model;

import static org.polymap.azv.AzvPlugin.EVENT_TYPE_ABGEBROCHEN;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_ANBEARBEITUNG;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_ANFREIGABE;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_BEANTRAGT;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_ERLEDIGT;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_FREIGABE;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_STORNIERT;

import java.util.Date;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.lang.time.FastDateFormat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.polymap.core.model2.Computed;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.Property;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.UserPrincipal;

import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;

import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicCaseEvent;
import org.polymap.mosaic.server.model.MosaicCaseEvents;
import org.polymap.mosaic.server.model2.MosaicCase2;
import org.polymap.mosaic.server.model2.MosaicRepository2;

/**
 * Erweitert einen Vorgang {@link MosaicCase2} um Properties, die für alle
 * AZV-Vorgänge gleich sind.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AzvVorgang
        extends KeyValuePropertyMixin {

    public static final String          KEY_USER = "user";
    public static final String          KEY_NUMMER = "laufendeNr";

    public static final Set<String>     AZV_STATUS = Sets.newHashSet( 
            EVENT_TYPE_BEANTRAGT, EVENT_TYPE_ANFREIGABE, EVENT_TYPE_ANBEARBEITUNG, 
            EVENT_TYPE_FREIGABE, EVENT_TYPE_STORNIERT, EVENT_TYPE_ABGEBROCHEN, EVENT_TYPE_ERLEDIGT );

    private static final LaufendeNrBuilder  LAUFENDE_NR_BUILDER = new LaufendeNrBuilder();
    
    
    public static String azvStatusOf( IMosaicCase mcase ) {
        for (IMosaicCaseEvent event : Lists.reverse( ImmutableList.copyOf( mcase.getEvents() ))) {
            if (AZV_STATUS.contains( event.getEventType() )) {
                return event.getEventType();
            }
        }
        return null;
    }

    
    public static IMosaicCase newCase( MosaicRepository2 repo, String name, String description, String nature ) {
        assert name != null && description != null && nature != null;
        IMosaicCase newCase = repo.newCase( name, description );
        newCase.addNature( nature );
        newCase.put( KEY_NUMMER, LAUFENDE_NR_BUILDER.next() );
        return newCase;
    }
    
    
    static class LaufendeNrBuilder {
        public static final FastDateFormat df1 = FastDateFormat.getInstance( "dd" );
        public static final FastDateFormat df2 = FastDateFormat.getInstance( "yyyyMMdd" );
        private String      day;
        private int         count;
        
        public synchronized String next() {
            String today = df1.format( new Date() );
            if (day == null || !today.equals( day )) {
                day = today;
                count = 0;
            }
            return df2.format( new Date() ) + count++;
        }
    }
    
    
    // instance *******************************************
    
    /**
     * 
     */
    @Nullable
    @NameInStore(KEY_USER)
    @Computed(MosaicCaseKeyProperty.class)
    public Property<String>             username;

    /**
     * 
     */
    @Nullable
    @NameInStore(KEY_NUMMER)
    @Computed(MosaicCaseKeyProperty.class)
    public Property<String>             laufendeNr;
    
    
    /**
     * 
     */
    public User user() {
        String _username = username.get();
        return _username != null ? UserRepository.instance().findUser( _username ) : null;
    }
    
    
    /**
     * 
     */
    public User setSessionUser() {
        UserPrincipal user = (UserPrincipal)Polymap.instance().getUser();
        User umuser = UserRepository.instance().findUser( user.getName() );
        assert umuser.username().get().equals( user.getName() );
        username.set( user.getName() );
        return umuser;
    }

    
    /**
     * Der {@link #azvStatusOf(IMosaicCase)} dieses Vorgangs.
     */
    public String azvStatus() {
        MosaicCase2 mcase = context.getCompositePart( MosaicCase2.class );
        return azvStatusOf( mcase );
    }
    
    
    public boolean azvStatusContains( String eventType ) {
        MosaicCase2 mcase = context.getCompositePart( MosaicCase2.class );
        return MosaicCaseEvents.contains( mcase, eventType );        
    }

}
