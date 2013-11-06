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
package org.polymap.azv.ui.leitungsauskunft;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.core.data.ui.featuretable.FeatureTableFilterBar;
import org.polymap.core.security.SecurityUtils;
import org.polymap.rhei.batik.BatikPlugin;

import org.polymap.azv.AzvPlugin;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.ui.casestable.CasesTableViewer;
import org.polymap.mosaic.ui.casestable.CasesViewerFilter;
import org.polymap.mosaic.ui.casestable.ICasesViewerDecorator;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class LeitungsauskunftCasesDecorator
        extends CasesViewerFilter
        implements ICasesViewerDecorator {

    private static Log log = LogFactory.getLog( LeitungsauskunftCasesDecorator.class );


    @Override
    public void fill( CasesTableViewer viewer, FeatureTableFilterBar filterBar ) {
        if (SecurityUtils.isUserInGroup( AzvPlugin.ROLE_LEITUNGSAUSKUNFT )) {
            filterBar.add( this )
                    .setIcon( BatikPlugin.instance().imageForName( "resources/icons/pipelines-filter.png" ) )
                    .setTooltip( "Leitungsauskünfte anzeigen" )
                    .setGroup( "azv" );
        }
        else {
            viewer.addFilter( new CasesViewerFilter() {
                protected boolean apply( CasesTableViewer _viewer, IMosaicCase mcase ) {
                    return !apply( _viewer, mcase );
                }
            });
        }
    }

    
    @Override
    protected boolean apply( CasesTableViewer viewer, IMosaicCase mcase ) {
        return mcase.getNatures().contains( AzvPlugin.CASE_LEITUNGSAUSKUNFT );
    }
    
}
