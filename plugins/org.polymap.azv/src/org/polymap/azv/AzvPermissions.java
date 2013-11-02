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
package org.polymap.azv;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import org.polymap.core.model2.Entity;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.SessionSingleton;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.security.UserPrincipal;

import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;

import org.polymap.mosaic.server.model.IMosaicCase;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AzvPermissions
        extends SessionSingleton {

    private static Log log = LogFactory.getLog( AzvPermissions.class );
    
    public static AzvPermissions instance() {
        return instance( AzvPermissions.class );
    }
    
    
    // instance *******************************************
    
    private UserPrincipal       principal;
    
    private UserRepository      repo;
    
    private User                user;
    
    
    protected boolean checkPrincipal() {
        if (principal == null) {
            this.principal = (UserPrincipal)Polymap.instance().getUser();
            if (principal != null) {
                this.repo = UserRepository.instance();
                this.user = repo.findUser( principal.getName() );
            }
        }
        return principal != null;   
    }

    
    public User getUser() {
        checkPrincipal();
        return user;
    }


    /**
     * Subject can be:
     * <ul>
     * <li>{@link Class} - check access to Entity type</li>
     * <li>{@link Entity} </li>
     * <li>{@link IMosaicCase} </li>
     * <li>String - check the given role/group for current user</li>
     * </ul>
     *
     * @param subject The subject to check access to. 
     */
    public boolean check( Object subject ) {
        if (!checkPrincipal()) {
            return false;
        }
        else if (SecurityUtils.isAdmin()) {
            return true;
        }
        else if (subject instanceof IMosaicCase) {
            return checkCase( (IMosaicCase)subject );
        }
        else if (subject instanceof String) {
            return checkRole( (String)subject );
        }
        else {
            throw new RuntimeException( "Unknown subject type: " + subject );
        }
    }
    

    protected boolean checkCase( IMosaicCase mcase ) {
        // Mitarbeiter: Vorgänge, für die ich Rolle habe
        if (SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA )) {
            Set<String> natures = mcase.getNatures();
            Set<String> groups = ImmutableSet.copyOf( repo.groupsOf( user ) );
            return Sets.intersection( natures, groups ).isEmpty();
        }
        // Kunde: meine Vorgänge
        else {
            String caseuser = mcase.get( "user" );
            return caseuser != null && caseuser.equals( user.username().get() );
        }
    }


    protected boolean checkRole( String role ) {
        return Iterables.contains( repo.groupsOf( user ), role );     
    }
    
}
