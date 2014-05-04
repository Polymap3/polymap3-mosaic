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
import org.polymap.mosaic.server.model2.MosaicCase2;

/**
 * Erweitert einen Vorgang {@link MosaicCase2} um Key-Felder für Adresse der
 * Entsorgung.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EntsorgungVorgang
        extends KeyValuePropertyMixin {

    public static final String      KEY_LISTE = "liste";
    public static final String      KEY_NAME = "name";
    public static final String      KEY_KUNDENNUMMER = "kundennummer";
    public static final String      KEY_BEMERKUNG = "bemerkung";

    @Nullable
    @NameInStore(KEY_LISTE)
    @Computed(MosaicCaseKeyProperty.class)
    public Property<String>         liste;
    
    @Nullable
    @NameInStore(KEY_NAME)
    @Computed(MosaicCaseKeyProperty.class)
    public Property<String>         name;
    
    @Nullable
    @NameInStore(KEY_KUNDENNUMMER)
    @Computed(MosaicCaseKeyProperty.class)
    public Property<String>         kundennummer;
    
    @Nullable
    @NameInStore(KEY_BEMERKUNG)
    @Computed(MosaicCaseKeyProperty.class)
    public Property<String>         bemerkung;

}
