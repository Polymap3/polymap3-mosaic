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
package org.polymap.atlas;

import java.util.Arrays;

/**
 * Identifies an {@link IPanel}. The id can have several parts. Most significant part
 * comes first. This helpd to distinguish panels of same type but with different
 * data.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PanelIdentifier {

    private String[]            id;


    public PanelIdentifier( String... id ) {
        assert id != null && id.length > 0 : "Panel ID must contain at least 2 elements.";
        this.id = id;
    }

    public String id() {
        return id[0];
    }

    public String subId() {
        return id[1];
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode( id );
        return result;
    }

    @Override
    public boolean equals( Object obj ) {
        if (this == obj) {
            return true;
        }
        else if (obj instanceof PanelIdentifier) {
            PanelIdentifier other = (PanelIdentifier)obj;
            return Arrays.equals( id, other.id );
        }
        return false;
    }

    @Override
    public String toString() {
        return Arrays.toString( id );
    }

}
