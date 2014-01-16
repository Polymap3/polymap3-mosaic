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

import javax.annotation.Nullable;

import org.polymap.core.model2.Computed;
import org.polymap.core.model2.NameInStore;
import org.polymap.core.model2.Property;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.UserPrincipal;

import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;

import org.polymap.mosaic.server.model2.MosaicCase2;

/**
 * Erweitert einen Vorgang {@link MosaicCase2} um ein Property für den username das
 * Nutzer des Vorgangs.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NutzerMixin
        extends KeyValuePropertyMixin {

    public static final String          KEY_USER = "user";

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
    
}
