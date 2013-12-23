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

import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.form.IFormEditorPageSite;

import org.polymap.mosaic.ui.casepanel.ICaseActionSite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class FormStatusAdapter
        implements IFormFieldListener {

    private ICaseActionSite         caseSite;
    
    private IFormEditorPageSite     formSite;

    public FormStatusAdapter( ICaseActionSite caseSite, IFormEditorPageSite formSite ) {
        this.caseSite = caseSite;
        this.formSite = formSite;
        this.formSite.addFieldListener( this );
    }
    
    public void dispose() {
        formSite.removeFieldListener( this );
    }
    
    @Override
    public void fieldChange( FormFieldEvent ev ) {                    
        caseSite.setDirty( formSite.isDirty() );
        caseSite.setValid( formSite.isValid() );
    }
    
}
