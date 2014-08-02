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
package org.polymap.azv.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.app.BatikApplication;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class ArticlePanel
        extends DefaultPanel {

    private static Log log = LogFactory.getLog( ArticlePanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "article" );

    private String          title;
    
    private String          content;
    
    
    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        return false;
    }


    @Override
    public PanelIdentifier id() {
        return ID;
    }


    public void setContent( String title, String content ) {
        this.title = title;
        this.content = content;
    }


    @Override
    public void createContents( final Composite parent ) {
        // delay after ArticleLinkRenderer has called setArticle
        BatikApplication.sessionDisplay().asyncExec( new Runnable() {
            public void run() {
                IPanelToolkit tk = getSite().toolkit();
                getSite().setTitle( title );
                
//                String title = null;
//                if (content.startsWith( "#" )) {
//                      title = StringUtils.substringBefore( content, "\n" ).substring( 1 );
//                      content = content.substring( title.length() + 2 );
//                }
                
                IPanelSection section = tk.createPanelSection( parent, title );
                section.addConstraint( new PriorityConstraint( 10 ) );
                section.getBody().setLayout( new FillLayout() );
                tk.createFlowText( section.getBody(), content );
            }
        });
    }
    
}
