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
package org.polymap.atlas.app;

import java.util.Date;

import org.opengis.feature.Feature;
import org.opengis.feature.Property;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.Action;

import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.NullProgressMonitor;

import org.polymap.core.runtime.Polymap;
import org.polymap.rhei.field.CheckboxFormField;
import org.polymap.rhei.field.DateTimeFormField;
import org.polymap.rhei.field.IFormField;
import org.polymap.rhei.field.IFormFieldValidator;
import org.polymap.rhei.field.NumberValidator;
import org.polymap.rhei.field.StringFormField;
import org.polymap.rhei.form.IFormEditorPage;
import org.polymap.rhei.form.IFormEditorToolkit;
import org.polymap.rhei.internal.form.AbstractFormEditorPageContainer;
import org.polymap.rhei.internal.form.FormEditorToolkit;

import org.polymap.atlas.AtlasPlugin;
import org.polymap.atlas.toolkit.ILayoutContainer;

/**
 * A container for Rhei forms. Sub-classes can use the Rhei form API the
 * create forms that are connected to features or entities.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class FormContainer
        implements IFormEditorPage {

    private static Log log = LogFactory.getLog( FormContainer.class );

    private FormEditorToolkit   toolkit;

    private Composite           pageBody;

    private PageContainer       pageSite;


    public final void createContents( ILayoutContainer parent ) {
        toolkit = new FormEditorToolkit( new FormToolkit( Polymap.getSessionDisplay() ) );
        pageBody = parent.getBody();
        pageSite = new PageContainer( this );

        try {
            createFormContent( pageSite );
            pageSite.doLoad( new NullProgressMonitor() );
        }
        catch (Exception e) {
            AtlasApplication.handleError( AtlasPlugin.PLUGIN_ID, "An error occured while creating the new page.", e );
        }
    }

    
    // default implementation of IFormEditorPage **********

    @Override
    public final String getTitle() {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public Action[] getEditorActions() {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public String getId() {
        throw new RuntimeException( "not yet implemented." );
    }

    @Override
    public byte getPriority() {
        throw new RuntimeException( "not yet implemented." );
    }


    /**
     *
     */
    private class PageContainer
            extends AbstractFormEditorPageContainer {

        public PageContainer( IFormEditorPage page ) {
            super( FormContainer.this, page, "_id_", "_title_" );
        }

        public void createContent() {
            page.createFormContent( this );
        }

        public Composite getPageBody() {
            return pageBody;
        }

        public IFormEditorToolkit getToolkit() {
            return toolkit;
        }

        public void setFormTitle( String title ) {
            throw new RuntimeException( "not yet implemented." );
        }

        public void setEditorTitle( String title ) {
            throw new RuntimeException( "not yet implemented." );
        }

        public void setActivePage( String pageId ) {
            log.warn( "setActivePage() not supported." );
        }
    }

    
    /**
     * This field builder allows to create a new form field. It provides a simple,
     * chainable API that allows to set several aspects of the result. If an aspect
     * is not set then a default is computed.
     */
    public class FormFieldBuilder {
        
        private String              propName;
        
        private Composite           parent;
        
        private String              label;
        
        private String              tooltip;
        
        private Feature             builderFeature;
        
        private Property            prop;
        
        private IFormField          field;
        
        private IFormFieldValidator validator;
        
        private boolean             enabled = true;

        private Object              layoutData;

        
        public FormFieldBuilder( Property prop ) {
            this.prop = prop;
        }

        public FormFieldBuilder( Composite parent, Property prop ) {
            this.parent = parent;
            this.prop = prop;
        }

        public FormFieldBuilder( Feature feature, String propName ) {
            this.propName = propName;
//            this.label = propName;
            this.builderFeature = feature;
        }

        public FormFieldBuilder( Feature feature, String propName, Composite parent) {
            this( feature, propName );
            this.parent = parent;
        }
        
        public FormFieldBuilder setParent( Composite parent ) {
            this.parent = parent instanceof Section 
                    ? (Composite)((Section)parent).getClient() : parent;
            return this;
        }

        public FormFieldBuilder setProperty( Property prop ) {
            this.prop = prop;
            return this;
        }

        public FormFieldBuilder setFeature( Feature feature ) {
            this.builderFeature = feature;
            return this;
        }
        
        public FormFieldBuilder setLabel( String label ) {
            this.label = label;
            return this;
        }
        
        public FormFieldBuilder setToolTipText( String tooltip ) {
            this.tooltip = tooltip;
            return this;
        }

        public FormFieldBuilder setField( IFormField field ) {
            this.field = field;
            return this;
        }

        public FormFieldBuilder setValidator( IFormFieldValidator validator ) {
            this.validator = validator;
            return this;
        }

        public FormFieldBuilder setEnabled( boolean enabled ) {
            this.enabled = enabled;
            return this;
        }
        
        public FormFieldBuilder setLayoutData( Object data ) {
            this.layoutData = data;
            return this;
        }
        
        public Composite create() {
            if (parent == null) {
                parent = pageSite.getPageBody();
            }
            if (prop == null) {
                prop = builderFeature.getProperty( propName );
                if (prop == null) {
                    throw new IllegalStateException( "No such property: " + propName );
                }
            }
            if (field == null) {
                Class binding = prop.getType().getBinding();
                // Number
                if (Number.class.isAssignableFrom( binding )) {
                    field = new StringFormField();
                    validator = new NumberValidator( binding, Polymap.getSessionLocale() );
                }
                // Date
                else if (Date.class.isAssignableFrom( binding )) {
                    field = new DateTimeFormField();
                }
                // Boolean
                else if (Date.class.isAssignableFrom( binding )) {
                    field = new CheckboxFormField();
                }
                // default: String
                else {
                    field = new StringFormField();
                }
            }
            Composite result = pageSite.newFormField( parent, prop, field, validator, label );
            // layoutData
            if (layoutData != null) {
                result.setLayoutData( layoutData );
            }
//            else {
//                applyLayout( result );
//            }
            // tooltip
            if (tooltip != null) {
                result.setToolTipText( tooltip );
            }
            // editable
            if (!enabled) {
                pageSite.setFieldEnabled( prop.getName().getLocalPart(), enabled );
            }
            return result;
        }
    }

}
