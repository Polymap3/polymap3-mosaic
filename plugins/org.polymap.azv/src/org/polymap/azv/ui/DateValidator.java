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
package org.polymap.azv.ui;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.rhei.field.IFormFieldValidator;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DateValidator
        implements IFormFieldValidator {

    private static Log log = LogFactory.getLog( DateValidator.class );


    @Override
    public String validate( Object fieldValue ) {
        Date date = (Date)fieldValue;
        return date == null || !date.after( new Date() )
            ? "Dieses Feld darf nicht leer sein und muss ein Datum in der Zukunft enthalten."
            : null;
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
