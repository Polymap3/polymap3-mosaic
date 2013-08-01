/*
 * polymap.org Copyright 2013 Polymap GmbH. All rights reserved.
 * 
 * This is free software; you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation; either version 2.1 of the License, or (at your option) any later
 * version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 */
package org.polymap.azv.ui;

import org.polymap.rhei.field.IFormFieldValidator;

/**
 * 
 * @author <a href="http://www.polymap.de">Steffen Stundzig</a>
 */
public class NotNullValidator
        implements IFormFieldValidator {

     public String validate( Object value ) {
        if (value == null ||
                // wird auch für TextField verwendet, mit der Bedeutung: "nicht leer"
                (value instanceof String && ((String)value).length() == 0)) {
            return "Dieses Feld darf nicht leer sein";
        }
        return null;
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
