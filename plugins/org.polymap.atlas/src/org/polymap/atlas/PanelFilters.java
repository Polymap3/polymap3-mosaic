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

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Provides predicates to be used as filters in
 * {@link IAppContext#findPanels(Predicate)}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PanelFilters {

    /**
     *
     */
    public static final Predicate<IPanel> all() {
        return Predicates.alwaysTrue();
    }

    /**
     *
     */
    public static final Predicate<IPanel> withPrefix( final PanelPath prefix ) {
        return new Predicate<IPanel>() {
            public boolean apply( IPanel input ) {
                return prefix.isPrefixOf( input.getSite().getPath() );
            }
        };
    }

}
