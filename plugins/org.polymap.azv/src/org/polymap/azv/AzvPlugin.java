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

import org.eclipse.ui.plugin.AbstractUIPlugin;

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
    public static final String      ROLE_DIENSTBARKEITEN = "Dienstbarkeiten";
    public static final String      ROLE_HYDRANTEN = "Hydranten";
    public static final String      ROLE_WASSERQUALITAET = "Wasserqualität";
    public static final String      ROLE_ENTSORGUNG = "Entsorgung";
    public static final String      ROLE_MA = "Interner Sachbearbeiter";

    public static final String      CASE_SCHACHTSCHEIN = "Schachtschein";
    public static final String      CASE_LEITUNGSAUSKUNFT = "Leitungsauskunft";
    public static final String      CASE_DIENSTBARKEITEN = "Dienstbarkeiten";
    public static final String      CASE_HYDRANTEN = "Hydranten";
    public static final String      CASE_WASSERQUALITAET = "Wasserqualität";
    public static final String      CASE_ENTSORGUNG = "Entsorgung";
    public static final String      CASE_NUTZER = "Neuer Nutzer";

    public static final String      EVENT_TYPE_BEANTRAGT = "Beantragt";
    public static final String      EVENT_TYPE_TERMINIERT = "Terminiert";
    

    private static AzvPlugin        instance;

    public static AzvPlugin instance() {
        assert instance != null;
        return instance;
    }


    // instance *******************************************

    private ServiceTracker          httpServiceTracker;
    

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
