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
package org.polymap.azv;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Supplier;

import org.eclipse.swt.graphics.Color;
import org.eclipse.rwt.graphics.Graphics;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.LockedLazyInit;

import org.polymap.rhei.batik.toolkit.MinWidthConstraint;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AzvPlugin
        extends AbstractUIPlugin {

    private static Log log = LogFactory.getLog( AzvPlugin.class );

    public static final String      ID = "org.polymap.azv";

    public static final FastDateFormat df = FastDateFormat.getInstance( "dd.MM.yyyy" );
    
    /* The property scope of the AZV plugin. */
    public static final String      PROPERTY_SCOPE = ID; 

    public static final String      ROLE_SCHACHTSCHEIN = "Schachtschein beantragen";
    public static final String      ROLE_LEITUNGSAUSKUNFT = "Leitungsauskunft";
    public static final String      ROLE_LEITUNGSAUSKUNFT2 = "Leitungsauskunft (vertrauenswürdig)";
    public static final String      ROLE_DIENSTBARKEITEN = "Dienstbarkeiten";
    public static final String      ROLE_HYDRANTEN = "Hydranten";
    public static final String      ROLE_WASSERQUALITAET = "Wasserqualität";
    public static final String      ROLE_ENTSORGUNG = "Entsorgung";
    public static final String      ROLE_MA = "Interner Sachbearbeiter";
    public static final String      ROLE_BL = "Betriebsstellenleiter";

    public static final String      CASE_SCHACHTSCHEIN = "Schachtschein";
    public static final String      CASE_LEITUNGSAUSKUNFT = "Leitungsauskunft";
    public static final String      CASE_DIENSTBARKEITEN = "Dienstbarkeiten";
    public static final String      CASE_HYDRANTEN = "Hydranten";
    public static final String      CASE_WASSERQUALITAET = "Wasserqualität";
    public static final String      CASE_ENTSORGUNG = "Entsorgung";
    public static final String      CASE_NUTZER = "Neuer Nutzer";

    public static final String      EVENT_TYPE_BEANTRAGT = "Antrag";
//    public static final String      EVENT_TYPE_TERMINIERT = "Terminiert";
    public static final String      EVENT_TYPE_STORNIERT = "Storno";
    public static final String      EVENT_TYPE_ABGEBROCHEN = "Abbruch";
    public static final String      EVENT_TYPE_ANFREIGABE = "An Freigabe";
    public static final String      EVENT_TYPE_ANBEARBEITUNG = "An Bearbeitung";
    public static final String      EVENT_TYPE_FREIGABE = "Freigabe";

    public static final MinWidthConstraint MIN_COLUMN_WIDTH = new MinWidthConstraint( 420, 1 );



//    /**
//     * Produces {@link StringFormField} instances only.
//     */
//    public static final IFormFieldFactory LABEL_FIELD_FACTORY = new IFormFieldFactory() {
//        public IFormField createField( Property prop ) {
//            return new LabelFormField() {
//                public Control createControl( Composite parent, IFormEditorToolkit toolkit ) {
//                    // prevent default form labeler style (FormContainer)
//                    Control result = super.createControl( parent, toolkit );
//                    result.setData( WidgetUtil.CUSTOM_VARIANT, "azv-formfield" );
//                    return result;
//                }
//            };
//        }
//    };

    private static AzvPlugin        instance;

    public static AzvPlugin instance() {
        assert instance != null;
        return instance;
    }


    // instance *******************************************

    private ServiceTracker          httpServiceTracker;
    
    public Lazy<Color>              discardColor = new LockedLazyInit( new Supplier<Color>() {
        public Color get() {
            return Graphics.getColor( 0xd3, 0x25, 0x16 );
        }
    });

    public Lazy<Color>              okColor = new LockedLazyInit( new Supplier<Color>() {
        public Color get() {
            return Graphics.getColor( 0x6e, 0xb0, 0x2e );
        }
    });
    
    public Lazy<Color>              openColor = new LockedLazyInit( new Supplier<Color>() {
        public Color get() {
            return Graphics.getColor( 0xff, 0xcc, 0x00 );
        }
    });


    @Override
    public void start( BundleContext context ) throws Exception {
        super.start( context );
        instance = this;
        
        // register HTTP resource
        httpServiceTracker = new ServiceTracker( context, HttpService.class.getName(), null ) {
            public Object addingService( ServiceReference reference ) {
                HttpService httpService = (HttpService)super.addingService( reference );                
                if (httpService != null) {
                    try {
                        httpService.registerResources( "/azvres", "/resources", null );
                    }
                    catch (NamespaceException e) {
                        throw new RuntimeException( e );
                    }
                }
                return httpService;
            }
        };
        httpServiceTracker.open();

    }

    @Override
    public void stop( BundleContext context ) throws Exception {
        super.stop( context );
        instance = null;
    }

}
