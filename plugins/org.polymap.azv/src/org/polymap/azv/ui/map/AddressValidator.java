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
package org.polymap.azv.ui.map;

import org.json.JSONObject;

import com.google.common.collect.Iterables;

import org.polymap.core.runtime.IMessages;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.fulltext.FullTextIndex;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AddressValidator
        implements IFormFieldValidator {

    public static final IMessages   i18n = Messages.forPrefix( "AddressValidator" ); //$NON-NLS-1$

    private FullTextIndex   addressIndex = AzvPlugin.instance().addressIndex();

    private String          propName;


    public AddressValidator( String propName ) {
        this.propName = propName;
    }

    @Override
    public String validate( Object fieldValue ) {
        try {
            if (fieldValue == null || fieldValue.toString().length() == 0) {
                return null;                    
            }
            //Timer timer = new Timer();
            Iterable<JSONObject> results = addressIndex.search( propName + ":" + fieldValue, 1 ); //$NON-NLS-1$
            int count = Iterables.size( results );                
            return count == 0 ? i18n.get( "keineDaten", fieldValue) : null;
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