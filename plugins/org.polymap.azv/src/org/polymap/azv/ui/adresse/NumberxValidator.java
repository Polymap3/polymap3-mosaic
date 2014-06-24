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
package org.polymap.azv.ui.adresse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.polymap.rhei.fulltext.address.Address;

/**
 * Split number and affix and search number in the {@link Address#FIELD_NUMBER}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NumberxValidator
        extends AddressValidator {

    public static final Pattern     NUMBERX_PATTERN = Pattern.compile( "([0-9]+)[ ]*([a-zA-Z]?)" );


    public NumberxValidator( String propName ) {
        super( Address.FIELD_NUMBER );
    }

    
    @Override
    public String validate( Object fieldValue ) {
        try {
            if (fieldValue == null || fieldValue.toString().length() == 0) {
                return null;                    
            }
            
            Matcher match = NUMBERX_PATTERN.matcher( (CharSequence)fieldValue );
            return match.find()
                    ? super.validate( match.group( 1 ) )
                    : "Hausnummer/Zusatz unkorrekt: " + fieldValue;
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
    @Override
    public Object transform2Model( Object fieldValue ) throws Exception {
        return fieldValue;
    }

    @Override
    public Object transform2Field( Object modelValue ) throws Exception {
        return modelValue;
    }

}