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
package org.polymap.mosaic.ui.casestable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.data.ui.featuretable.SimpleFeatureTableElement;

import org.polymap.mosaic.server.model.IMosaicCase;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class CasesViewerFilter
        extends ViewerFilter {

    private static Log log = LogFactory.getLog( CasesViewerFilter.class );

    protected abstract boolean apply( CasesTableViewer viewer, IMosaicCase mcase );
    
    @Override
    public final boolean select( Viewer viewer, Object parentElm, Object elm ) {
        CasesTableViewer casesViewer = (CasesTableViewer)viewer;
        IFeatureTableElement ftelm = (IFeatureTableElement)elm;
        return apply( casesViewer, casesViewer.entity( ((SimpleFeatureTableElement)ftelm).feature() ) );
    }
    
}
