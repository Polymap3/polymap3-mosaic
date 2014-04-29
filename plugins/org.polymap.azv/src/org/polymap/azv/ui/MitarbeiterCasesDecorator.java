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

import static org.polymap.azv.AzvPlugin.EVENT_TYPE_ANBEARBEITUNG;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_ANFREIGABE;
import static org.polymap.azv.AzvPlugin.EVENT_TYPE_BEANTRAGT;
import static org.polymap.azv.AzvPlugin.ROLE_BL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.ui.featuretable.FeatureTableFilterBar;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.security.SecurityUtils;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.model.AzvVorgang;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casestable.CasesTableViewer;
import org.polymap.mosaic.ui.casestable.CasesViewerFilter;
import org.polymap.mosaic.ui.casestable.ICasesViewerDecorator;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MitarbeiterCasesDecorator
        implements ICasesViewerDecorator {

    private static Log log = LogFactory.getLog( MitarbeiterCasesDecorator.class );

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2> repo;

    
    @Override
    public void fill( CasesTableViewer viewer, FeatureTableFilterBar filterBar ) {
        // filter permissions
        viewer.addFilter( new CasesViewerFilter() {
            private String username = Polymap.instance().getUser().getName();
            
            protected boolean apply( CasesTableViewer _viewer, IMosaicCase mcase ) {
                // Admin: alle
                if (SecurityUtils.isAdmin()) {
                    return true;
                }
                // Mitarbeiter: beantragt 
                String azvStatus = AzvVorgang.azvStatusOf( mcase );
                if (SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA ) 
                        && (EVENT_TYPE_BEANTRAGT.equals( azvStatus ) 
                        || EVENT_TYPE_ANBEARBEITUNG.equals( azvStatus ))) {
                    return true;
                }
                // Betriebstellenleiter: An Freigabe
                if (SecurityUtils.isUserInGroup( ROLE_BL )
                        && EVENT_TYPE_ANFREIGABE.equals( azvStatus )) {
                    return true;
                }
                // Kunde: meine Vorgänge
                else {
                    String caseuser = mcase.get( AzvVorgang.KEY_USER );
                    return caseuser != null && caseuser.equals( username );
                }
            }
        });

//        // filterBar
//        if (SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA )) {
//            
//            // Registrierungen
//            filterBar.add( new CasesViewerFilter() {
//                protected boolean apply( CasesTableViewer _viewer, IMosaicCase mcase ) {
//                    Set<String> natures = mcase.getNatures();
//                    return natures.contains( AzvPlugin.CASE_NUTZER ); 
//                }
//            })
//            .setIcon( BatikPlugin.instance().imageForName( "resources/icons/users-filter.png" ) )
//            .setTooltip( "Kundenregistrierungen anzeigen" );

//            // Beantragt
//            filterBar.add( new CasesViewerFilter() {
//                protected boolean apply( CasesTableViewer _viewer, IMosaicCase mcase ) {
//                    return Iterables.find( mcase.getEvents(), MosaicCaseEvents.contains( "Beantragt" ), null ) != null;
//                }
//            })
//            .setIcon( BatikPlugin.instance().imageForName( "resources/icons/filter.png" ) )
//            .setTooltip( "Vollständige Anträge anzeigen" );
//        }
    }
    
}
