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

import static org.polymap.azv.ui.adresse.NumberxValidator.NUMBERX_PATTERN;
import static org.polymap.rhei.fulltext.address.Address.FIELD_CITY;
import static org.polymap.rhei.fulltext.address.Address.FIELD_NUMBER;
import static org.polymap.rhei.fulltext.address.Address.FIELD_NUMBER_X;
import static org.polymap.rhei.fulltext.address.Address.FIELD_POSTALCODE;
import static org.polymap.rhei.fulltext.address.Address.FIELD_STREET;

import java.util.List;
import java.util.regex.Matcher;
import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.Status;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.Layers;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

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

    private JSONObject                  search;


    public AddressForm( IPanelSite site ) {
        this.site = site;
        
        try {
            ILayer layer = ProjectRepository.instance().visit( Layers.finder( "adressen", "Adressen" ) ); //$NON-NLS-1$ //$NON-NLS-2$
            assert layer != null : i18n.get( "keineEbene" );
            search = new JSONObject();
            // FIXME Test only
            search.put( FIELD_STREET, "Lindenstraße" ); //$NON-NLS-1$
            search.put( FIELD_CITY, "Anklam" ); //$NON-NLS-1$
            search.put( FIELD_POSTALCODE, "17389" ); //$NON-NLS-1$
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
        street.setLayout( FormLayoutFactory.defaults().create() );

        Composite field = createField( street, new JsonPropertyAdapter( search, FIELD_STREET ) )
                .setLabel( i18n.get( "strasseHNr" ) )
                .setValidator( validator( FIELD_STREET ) )
                .create();
        field.setLayoutData( FormDataFactory.filled().right( 75 ).create() );
        field.setFocus();

        createField( street, new JsonPropertyAdapter( search, FIELD_NUMBER ) )
                .setLabel( IFormFieldLabel.NO_LABEL )
                .setValidator( new NumberxValidator( FIELD_NUMBER ) )
                .create()
                .setLayoutData( FormDataFactory.filled().left( 75 ).create() );

        Composite city = site.toolkit().createComposite( body );
        createField( city, new JsonPropertyAdapter( search, FIELD_POSTALCODE ) )
                .setLabel( i18n.get( "plzOrt" ) ).setValidator( validator( FIELD_POSTALCODE ) ).create();

        createField( city, new JsonPropertyAdapter( search, FIELD_CITY ) )
                .setLabel( IFormFieldLabel.NO_LABEL ).setValidator( validator( FIELD_CITY ) ).create();

        // field listener
        formSite.addFieldListener( fieldListener = new IFormFieldListener() {
            
            @Override
            public void fieldChange( FormFieldEvent ev ) {
                log.info( "ev: " + ev ); //$NON-NLS-1$
            }

            @EventHandler(display=true,delay=750)
            public void fieldChanges( List<FormFieldEvent> ev ) {
                if (formSite.isValid()) {
                    try {
                        formSite.submitEditor();
                        log.info( "VALID: " + search ); //$NON-NLS-1$
                        site.setStatus( Status.OK_STATUS );

                        String number = search.getString( FIELD_NUMBER );
                        Matcher match = NUMBERX_PATTERN.matcher( number );
                        match.find();  // already checked by validator

                        JSONObject searchx = new JSONObject( search, new String[] {FIELD_CITY, FIELD_POSTALCODE, FIELD_STREET} );
                        searchx.put( FIELD_NUMBER, match.group( 1 ) );
                        String x = match.group( 2 );
                        if (x != null && x.length() > 0) {
                            searchx.put( FIELD_NUMBER_X, x );
                        }
                        showResults( new AddressFinder( addressIndex ).maxResults( 2 ).find( searchx ) );
                    }
                    catch (RuntimeException e) {
                        throw e;
                    }
                    catch (Exception e) {
                        throw new RuntimeException( e );
                    }
                }
            }
        });
        //    activateStatusAdapter( site.getPanelSite() );
    }

    
    protected IFormFieldValidator validator( String propName ) {
        return Validators.AND( new NotEmptyValidator(), new AddressValidator( propName ) );
    }
    
}
