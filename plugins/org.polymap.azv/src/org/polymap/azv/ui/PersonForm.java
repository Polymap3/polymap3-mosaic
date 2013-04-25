/* 
 * polymap.org
 * Copyright 2013, Polymap GmbH. ALl rigths reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.azv.ui;

import java.util.List;
import java.util.regex.Matcher;

import org.geotools.feature.NameImpl;
import org.opengis.feature.type.Name;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.common.QualifiedName;
import org.qi4j.api.property.StateHolder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueComposite;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.rhei.data.entityfeature.PropertyAdapter;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NullValidator;
import org.polymap.rhei.field.PicklistFormField;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPageSite;
import org.polymap.rhei.model.ConstantWithSynonyms;

import org.polymap.azv.model.AzvRepository;
import org.polymap.azv.model.Person;
import org.polymap.azv.model.PersonValue;

/**
 * (Sub)form for {@link PersonValue}. Handles creation of a new
 * {@link ValueComposite} if properties have been changed.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class PersonForm {

    private static Log log = LogFactory.getLog( PersonForm.class );
    
    static final int                    FIELD_OFFSET_H = 5;
    static final int                    FIELD_OFFSET_V = 1;

    private PersonValue                 personValue;
    
    /** Prototype of a {@link PersonValue} with properties changed by this form. */
    private PersonValue                 newPersonValue;
    
    /** The value builder of {@link #newPersonValue}. */
    private ValueBuilder<PersonValue>   vbuilder;

    private PersonSearcher              searcher;
    
    private String                      propertyNS;


    public PersonForm( PersonValue personValue, String propertyNS ) {
        super();
        this.personValue = personValue;
        this.propertyNS = propertyNS;
        
        if (this.personValue == null) {
            ValueBuilder<PersonValue> _vbuilder = AzvRepository.instance().newValueBuilder( PersonValue.class );
            PersonValue prototype = _vbuilder.prototype();
            prototype.name().set( "" );
            this.personValue = _vbuilder.newInstance();
        }
    }


    public void dispose() {
        if (searcher != null) {
            searcher.dispose();
            searcher = null;
        }
    }
    
    
    /**
     * Submits changes and creates new instancen of a {@link PersonValue} with
     * changed properties, or null if no changes were made.
     * 
     * @param checkCreatePersonEntity True indicates that the global person
     *        store is checked and a new {@link PersonComposite} is created if
     *        no matching entry exists.
     */
    public PersonValue doSubmit( boolean checkCreatePersonEntity, IProgressMonitor monitor ) {
        final PersonValue result = vbuilder != null ? vbuilder.newInstance() : null;
        
        // check/create PersonComposite
        if (result != null && checkCreatePersonEntity) {
            List<Person> searcherResults = searcher.getResults();
            log.debug( "Checking searcher entities: " + searcherResults );
            
            if (searcherResults != null && searcherResults.isEmpty()) {
                log.debug( "    searcher entities: " + searcherResults.size() );
                AzvRepository repo = AzvRepository.instance();
            
                log.debug( "Creating new Person entity..." );
                Person entity = repo.newPerson( null );
                entity.person().set( result );
            }
        }
        return result;
    }


    /**
     * 
     *
     * @param previous
     * @return
     */
    protected abstract FormData createFieldLayoutData( Composite previous );

    
    public void createConent( Composite parent, IFormEditorPageSite site ) {
        final StringFormField nameFormField = new StringFormField();
        final StringFormField strasseFormField = new StringFormField();
        IFormFieldValidator strasseValidator = new NullValidator() {
            public String validate( Object value ) {
                Matcher matcher = ExportServiceHandler.hnrPattern.matcher( value.toString() );
                if (matcher.find()) {
                    log.debug( "Strasse: '" + value.toString().substring( 0, matcher.start() ) + "'" );
                    log.debug( "Hausnummer: '" + matcher.group( 0 ) + "'" );
                    log.debug( "    Nummer: " + matcher.group( 1 ) );
                    log.debug( "    Zusatz: " + matcher.group( 2 ) );
                    return null;
                }
                else {
                    return "Ungültige Strasse/Hausnummer. (Format für Hausnummern: 20, 20a oder 2-4)";
                }
            }
        };
        final StringFormField ortFormField = new StringFormField();
        final StringFormField plzFormField = new StringFormField();
        final StringFormField telFormField = new StringFormField();
        final StringFormField faxFormField = new StringFormField();
        final StringFormField emailFormField = new StringFormField();
        final PicklistFormField gruppeFormField = new PicklistFormField( Kundengruppe.all );
        final StringFormField nummerFormField = new StringFormField();
        final PicklistFormField anredeFormField = new PicklistFormField( new String[] {"Herr", "Frau", "Firma"} );
        anredeFormField.setTextEditable( true );
        anredeFormField.setForceTextMatch( false );
        final StringFormField zusatzFormField = new StringFormField();
        final PicklistFormField landFormField = new PicklistFormField( Land.all.labelsAndNull() );
        landFormField.setTextEditable( true );
        landFormField.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent ev ) {
                String kuerzel = ((Combo)ev.widget).getText();
                ConstantWithSynonyms land = Land.all.forLabelOrSynonym( kuerzel );
                if (land != null) {
                    landFormField.removeModifyListener( this );
                    landFormField.setValue( land.label );
                    landFormField.addModifyListener( this );
                }
            }
        });
        
        // searcher
        searcher = new PersonSearcher( site, personValue, propertyNS ) {
            protected void valueSelected( PersonValue value ) {
                nameFormField.setValue( value.name().get() );
                strasseFormField.setValue( value.strasse().get() );
                ortFormField.setValue( value.ort().get() );
                plzFormField.setValue( value.plz().get() );
                landFormField.setValue( value.land().get() );
                emailFormField.setValue( value.email().get() );
                faxFormField.setValue( value.fax().get() );
                telFormField.setValue( value.tel().get() );
                gruppeFormField.setValue( value.gruppe().get() );
                nummerFormField.setValue( value.nummer().get() );
                anredeFormField.setValue( value.anrede().get() );
                zusatzFormField.setValue( value.zusatz().get() );
            }
        };
        Button searchBtn = searcher.createButton( parent );
        
        // kundengruppe
        Composite gruppeField = site.newFormField( parent, 
                new PersonPropertyAdapter( personValue.gruppe() ), 
                gruppeFormField, null, "Kundengruppe" );
        gruppeField.setLayoutData( createFieldLayoutData( null ) );

        // searchBtn
        FormData layoutData = new FormData();
        layoutData.left = new FormAttachment( 100, -90 );
        layoutData.right = new FormAttachment( 100, -FIELD_OFFSET_H );
        layoutData.height = 18;
        layoutData.top = new FormAttachment( 0 );
        searchBtn.setLayoutData( layoutData );
        ((FormData)gruppeField.getLayoutData()).right = new FormAttachment( searchBtn, FIELD_OFFSET_H-20 );

        // nummer
        Composite nummerField = site.newFormField( parent, 
                new PersonPropertyAdapter( personValue.nummer() ), 
                nummerFormField, null, "Kundennummer" );
        nummerField.setLayoutData( createFieldLayoutData( gruppeField ) );

        // anrede
        Composite anredeField = site.newFormField( parent, 
                new PersonPropertyAdapter( personValue.anrede() ), 
                anredeFormField, null, "Anrede" );
        anredeField.setLayoutData( createFieldLayoutData( nummerField ) );

        // name
        Composite nameField = site.newFormField( parent, 
                new PersonPropertyAdapter( personValue.name() ), 
                nameFormField, null, "Name,Vorname,Ttl.", "Name der Person oder Institution in der Form: Name, Vorname, Titel" );
        nameField.setLayoutData( createFieldLayoutData( anredeField ) );

        // zusatz
        Composite zusatzField = site.newFormField( parent, 
                new PersonPropertyAdapter( personValue.zusatz() ), 
                zusatzFormField, null, "Zusatz" );
        zusatzField.setLayoutData( createFieldLayoutData( nameField ) );

        // strasse
        Composite strasseField = site.newFormField( parent,
                new PersonPropertyAdapter( personValue.strasse() ), 
                strasseFormField, strasseValidator, "Stra�e/Haus-Nr." );
        strasseField.setLayoutData( createFieldLayoutData( zusatzField ) );

        Composite plzField = site.newFormField( parent,
                new PersonPropertyAdapter( personValue.plz() ), 
                plzFormField, null, "PLZ, Ort" );
        plzField.setLayoutData( createFieldLayoutData( strasseField ) );
        ((FormData)plzField.getLayoutData()).right = new FormAttachment( 0, 200 );

        Composite ortField = site.newFormField( parent,
                new PersonPropertyAdapter( personValue.ort() ), 
                ortFormField, null, "_nolabel_" );
        ortField.setLayoutData( createFieldLayoutData( strasseField ) );
        ((FormData)ortField.getLayoutData()).left = new FormAttachment( plzField, FIELD_OFFSET_H );

        // land
        Composite landField = site.newFormField( parent,
                new PersonPropertyAdapter( personValue.land() ), 
                landFormField, null, "Land" );
        landField.setLayoutData( createFieldLayoutData( plzField ) );

        // tel / fax
        Composite telField = site.newFormField( parent,
                new PersonPropertyAdapter( personValue.tel() ), 
                telFormField, null, "Telefon, Fax" );
        telField.setLayoutData( createFieldLayoutData( landField ) );
        ((FormData)telField.getLayoutData()).right = new FormAttachment( 50, 50 );

        Composite faxField = site.newFormField( parent,
                new PersonPropertyAdapter( personValue.fax() ), 
                faxFormField, null, "_nolabel_" );
        faxField.setLayoutData( createFieldLayoutData( landField ) );
        ((FormData)faxField.getLayoutData()).left = new FormAttachment( telField, FIELD_OFFSET_H );

        Composite emailField = site.newFormField( parent,
                new PersonPropertyAdapter( personValue.email() ), 
                emailFormField, null, "EMail" );
        layoutData = createFieldLayoutData( telField );
        layoutData.bottom = new FormAttachment( 100, -2 );
        emailField.setLayoutData( layoutData );
    }
    

    /**
     * 
     */
    class PersonPropertyAdapter
            extends PropertyAdapter {
        
        public PersonPropertyAdapter( org.qi4j.api.property.Property delegate ) {
            super( delegate );
        }

        public Name getName() {
            // FIXME Hack to distinguish between the several person forms in one parent form
            // see PersonSearcher when fixing
            Name name = super.getName();
            return new NameImpl( name.getNamespaceURI(), propertyNS + "." + name.getLocalPart() );
        }

        public void setValue( Object value ) {
            log.debug( "setValue(): value=" + value );
            if (newPersonValue == null) {
                vbuilder = AzvRepository.instance().newValueBuilder( PersonValue.class );
                newPersonValue = vbuilder.prototype();
                
                // copy properties
                personValue.state().visitProperties( new StateHolder.StateVisitor() {
                    public void visitProperty( QualifiedName name, Object propValue ) {
                        newPersonValue.state().getProperty( name )
                                .set( personValue.state().getProperty( name ).get() );
                    }
                });
            }
            org.qi4j.api.property.Property<Object> valueProp = 
                    newPersonValue.state().getProperty( delegate().qualifiedName() );
            valueProp.set( value );
        }

        public Object getValue() {
            if (newPersonValue != null) {
                return newPersonValue.state().getProperty( delegate().qualifiedName() ).get();
            }
            else {
                return super.getValue();
            }
        }

    }
    
}
