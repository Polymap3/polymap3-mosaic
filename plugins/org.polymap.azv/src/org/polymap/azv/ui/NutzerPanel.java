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
package org.polymap.azv.ui;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.property.Property;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.app.DefaultFormPanel;
import org.polymap.rhei.data.entityfeature.PlainValuePropertyAdapter;
import org.polymap.rhei.data.entityfeature.ValuePropertyAdapter;
import org.polymap.rhei.field.FormFieldEvent;
import org.polymap.rhei.field.IFormFieldListener;
import org.polymap.rhei.field.NullValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.field.StringFormField.Style;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.form.IFormEditorToolkit;

import org.polymap.azv.model.Nutzer;
import org.polymap.azv.model.PersonValue;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NutzerPanel
        extends DefaultFormPanel {

    private static Log log = LogFactory.getLog( NutzerPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "nutzerdaten" );
    
    private static final Pattern        EMAIL_PATTERN = Pattern.compile( "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$" );
    
    private IFormEditorToolkit          tk;

    private ContextProperty<Nutzer>     nutzer;

    private Button                      okBtn;
    
    
    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        // open only if directly called
        return false;
    }

    
    @Override
    public PanelIdentifier id() {
        return ID;
    }

    
    @Override
    public void createFormContent( final IFormEditorPageSite pageSite ) {
        getSite().setTitle( "Nutzerdaten" );
        tk = pageSite.getToolkit();
        Composite parent = pageSite.getPageBody();
        parent.setLayout( FormLayoutFactory.defaults().margins( DEFAULTS_SPACING ).create() );

        Composite client = tk.createComposite( parent );
        client.setLayoutData( FormDataFactory.offset( 0 ).top( 0, 75 ).left( 25 ).right( 75 ).width( 500 ).create() );
        client.setLayout( ColumnLayoutFactory.defaults().columns( 1, 1 ).spacing( 5 ).create() );
        
        Nutzer entity = nutzer.get();
        Property<PersonValue> person = entity.person();
        
        new FormFieldBuilder( client, new ValuePropertyAdapter( person.get().email(), person ) )
                .setLabel( "Login/EMail*" ).setToolTipText( "Die EMail dient auch als Login-Name" )
                .setField( new StringFormField() )
                .setValidator( new NullValidator() {
                    public String validate( Object value ) {
                        if (value == null || ((String)value).length() == 0) {
                            return "Bitte geben Sie ihre korrekte EMail-Adresse an";
                        }
                        else if (!EMAIL_PATTERN.matcher( (String)value ).matches() ) {
                            return "Eine EMail-Adresse muss folgende Form haben: name@domain.de";
                        }
                        return null;
                    }
                })
                .create();
        
        PlainValuePropertyAdapter<String> pwdAdaptor = new PlainValuePropertyAdapter<String>( "password", "" );
        new FormFieldBuilder( client, pwdAdaptor )
                .setLabel( "Passwort*" )
                .setField( new StringFormField( Style.PASSWORD ) )
                .setValidator( new NullValidator() {
                    public String validate( Object value ) {
                        if (value == null || ((String)value).length() < 6) {
                            return "Das Passwort ist zu kurz";
                        }
                        else if (!StringUtils.containsAny( (String)value, "0123456789!§$%&/()=?`*~'#-_.:,;@^°" )) {
                            return "Das Passwort muss wenigstens ein Sonderzeichen enthalten";
                        }
                        return null;
                    }
                })
                .create();

        new FormFieldBuilder( client, new ValuePropertyAdapter( person.get().name(), person ) )
                .setLabel( "Name*" ).setToolTipText( "Vollständiger Name" ).create();

        Composite str = tk.createComposite( client );
        str.setLayout( FormLayoutFactory.defaults().spacing( DEFAULTS_SPACING ).create() );
        new FormFieldBuilder( str, new ValuePropertyAdapter( person.get().strasse(), person ) )
                .create().setLayoutData( FormDataFactory.filled().right( 50 ).create() );
        new FormFieldBuilder( str, new ValuePropertyAdapter( person.get().nummer(), person ) )
                .setLabel( "Hausnummer" ).create().setLayoutData( FormDataFactory.filled().left( 50, DEFAULTS_SPACING ).create() );

        Composite ort = tk.createComposite( client );
        ort.setLayout( FormLayoutFactory.defaults().spacing( DEFAULTS_SPACING ).create() );
        new FormFieldBuilder( ort, new ValuePropertyAdapter( person.get().plz(), person ) )
                .setLabel( "PLZ" ).create().setLayoutData( FormDataFactory.filled().right( 50 ).create() );
        new FormFieldBuilder( ort, new ValuePropertyAdapter( person.get().ort(), person ) )
                .setLabel( "Ort" ).create().setLayoutData( FormDataFactory.filled().left( 50, DEFAULTS_SPACING ).create() );

        okBtn = tk.createButton( client, "Registrieren", SWT.NONE );
        okBtn.setEnabled( false );
        okBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                throw new RuntimeException( "not yet implemented" );
            }
        });
        
        pageSite.addFieldListener( new IFormFieldListener() {
            public void fieldChange( FormFieldEvent ev ) {
                okBtn.setEnabled( pageSite.isValid() );
            }
        });
        
        pageSite.setFieldValue( "email", "falko@polymap.de" );
        pageSite.setFieldValue( "password", "falko@polymap.de" );
    }
    
}
