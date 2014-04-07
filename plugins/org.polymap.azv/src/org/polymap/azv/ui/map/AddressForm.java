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

import static org.polymap.rhei.fulltext.address.Address.FIELD_CITY;
import static org.polymap.rhei.fulltext.address.Address.FIELD_NUMBER;
import static org.polymap.rhei.fulltext.address.Address.FIELD_POSTALCODE;
import static org.polymap.rhei.fulltext.address.Address.FIELD_STREET;

import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.Status;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.Layers;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.ui.ColumnLayoutFactory;

import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.app.FormContainer;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldLabel;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.JsonPropertyAdapter;
import org.polymap.rhei.field.Validators;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.fulltext.FullTextIndex;
import org.polymap.rhei.fulltext.address.AddressFinder;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.ui.NotEmptyValidator;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class AddressForm
        extends FormContainer {

    static Log log = LogFactory.getLog( AddressForm.class );

    public static final IMessages       i18n = Messages.forPrefix( "AddressForm" ); //$NON-NLS-1$
    
    private IPanelSite                  site;
    
    private IFormFieldListener          fieldListener;

    private Composite                   body;

    private FullTextIndex               addressIndex = AzvPlugin.instance().addressIndex();

    private JSONObject                  address;

    private JSONObject                  search;


    public AddressForm( IPanelSite site ) {
        this.site = site;
        
        try {
            ILayer layer = ProjectRepository.instance().visit( Layers.finder( "adressen", "Adressen" ) ); //$NON-NLS-1$ //$NON-NLS-2$
            assert layer != null : i18n.get( "keineEbene" );
            search = new JSONObject();
            // FIXME Test only
            search.put( FIELD_STREET, "Markt" ); //$NON-NLS-1$
            search.put( FIELD_NUMBER, "1" ); //$NON-NLS-1$
            search.put( FIELD_POSTALCODE, "01234" ); //$NON-NLS-1$
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }

    
    protected abstract void showResults( Iterable<JSONObject> addresses );
    
    
    @Override
    public void createFormContent( final IFormEditorPageSite formSite ) {
        body = formSite.getPageBody();
        body.setLayout( ColumnLayoutFactory.defaults().spacing( 5 ).margins( 10, 10 ).columns( 1, 1 ).create() );

        Composite street = site.toolkit().createComposite( body );
        new FormFieldBuilder( street, new JsonPropertyAdapter( search, FIELD_STREET ) )
                .setLabel( i18n.get( "strasseHNr" ) ).setValidator( validator( FIELD_STREET ) ).create().setFocus();

        new FormFieldBuilder( street, new JsonPropertyAdapter( search, FIELD_NUMBER ) )
                .setLabel( IFormFieldLabel.NO_LABEL ).setValidator( validator( FIELD_NUMBER ) ).create();

        Composite city = site.toolkit().createComposite( body );
        new FormFieldBuilder( city, new JsonPropertyAdapter( search, FIELD_POSTALCODE ) )
                .setLabel( i18n.get( "plzOrt" ) ).setValidator( validator( FIELD_POSTALCODE ) ).create();

        new FormFieldBuilder( city, new JsonPropertyAdapter( search, FIELD_CITY ) )
                .setLabel( IFormFieldLabel.NO_LABEL ).setValidator( validator( FIELD_CITY ) ).create();

        // field listener
        formSite.addFieldListener( fieldListener = new IFormFieldListener() {
            public void fieldChange( FormFieldEvent ev ) {
                if (ev.getEventCode() == VALUE_CHANGE) {
                    if (formSite.isValid()) {
                        try {
                            formSite.submitEditor();
                            log.info( "VALID: " + search ); //$NON-NLS-1$
                            site.setStatus( Status.OK_STATUS );
                            
                            showResults( new AddressFinder( addressIndex ).maxResults( 1 ).find( search ) );
                        }
                        catch (Exception e) {
                            throw new RuntimeException( e );
                        }
                    }
                }
            }
        });
        //    activateStatusAdapter( site.getPanelSite() );
    }

    
    protected IFormFieldValidator validator( String propName ) {
        return Validators.AND( new NotEmptyValidator(), new AddressValidator( propName ) );
    }
    
    
//    protected Iterable<JSONObject> findAddress() {
//        StringBuilder query = new StringBuilder( 256 );
//        for (String propName : new String[] {FIELD_CITY, FIELD_NUMBER, FIELD_POSTALCODE, FIELD_STREET}) {
//            Object value = search.opt( propName );
//            if (value != null) {
//                query.append( query.length() > 0 ? " AND " : "" );
//                query.append( propName ).append( ":" ).append( value.toString() );
//            }
//        }
//        try {
//            return addressIndex.search( query.toString(), 1 );
//        }
//        catch (Exception e) {
//            throw new RuntimeException( e );
//        }
//    }

}
