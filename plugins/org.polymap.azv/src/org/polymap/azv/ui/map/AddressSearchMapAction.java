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
package org.polymap.azv.ui.map;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.json.JSONObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.vividsolutions.jts.geom.Geometry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import org.eclipse.rwt.graphics.Graphics;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.RowDataFactory;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.UIJob;

import org.polymap.rhei.batik.app.BatikApplication;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.fulltext.FullTextIndex;

import org.polymap.azv.AzvPlugin;
import org.polymap.openlayers.rap.widget.base_types.Bounds;

/**
 * Address search field using {@link AzvPlugin#addressIndex()}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AddressSearchMapAction
        extends ContributionItem {

    private static Log log = LogFactory.getLog( AddressSearchMapAction.class );

    private MapViewer               viewer;

    private Text                    searchTxt;
    
    private Label                   resultCountLbl;
    
    private ContentProposalAdapter  proposal;

    private SimpleContentProposalProvider proposalProvider;
    
    
    public AddressSearchMapAction( MapViewer viewer ) {
        this.viewer = viewer;
    }


    public String getSearchText() {
        return !searchTxt.getText().startsWith( "Suchen" ) ? searchTxt.getText() : null;
    }
    
    
    @Override
    public void fill( Composite parent ) {
        IPanelToolkit tk = viewer.getPanelSite().toolkit();
        searchTxt = tk.createText( parent, "Suchen: Ort, PLZ, Straße", SWT.SEARCH, SWT.CANCEL );
        searchTxt.setLayoutData( RowDataFactory.swtDefaults().hint( 280, SWT.DEFAULT ).create() );
        //searchTxt.setLayoutData( FormDataFactory.filled().right( clearBtn ).create() );

        resultCountLbl = tk.createLabel( parent, "-" );
        resultCountLbl.setToolTipText( "Noch keine Treffer\nGeben Sie zuerst eine Suche ein" );
        resultCountLbl.setLayoutData( RowDataFactory.swtDefaults().hint( 20, SWT.DEFAULT ).create() );
        
        searchTxt.setToolTipText( "Mit <Enter> das Ergebnisse in der Karte anzeigen\nBei mehreren Treffern wird das gesamte Gebiet angezeigt" );
        searchTxt.setForeground( Graphics.getColor( 0xa0, 0xa0, 0xa0 ) );
        searchTxt.addFocusListener( new FocusListener() {
            @Override
            public void focusLost( FocusEvent ev ) {
                if (searchTxt.getText().length() == 0) {
                    searchTxt.setText( "Suchen..." );
                    searchTxt.setForeground( Graphics.getColor( 0xa0, 0xa0, 0xa0 ) );
                    //clearBtn.setEnabled( false );
                }
            }
            @Override
            public void focusGained( FocusEvent ev ) {
                if (searchTxt.getText().startsWith( "Suchen" )) {
                    searchTxt.setText( "" );
                    searchTxt.setForeground( Graphics.getColor( 0x00, 0x00, 0x00 ) );
                }
            }
        });
        
        // proposal
        proposalProvider = new SimpleContentProposalProvider( new String[0] );
        TextContentAdapter controlAdapter = new TextContentAdapter() {
            public void insertControlContents( Control control, String text, int cursorPosition ) {
                ((Text)control).setText( text );
                ((Text)control).setSelection( text.length() );
            }
        };
        proposal = new ContentProposalAdapter( searchTxt, controlAdapter, proposalProvider, null, null );
        proposal.setAutoActivationDelay( 750 );
        searchTxt.addKeyListener( new KeyAdapter() {
            public void keyReleased( KeyEvent ev ) {
                if (ev.keyCode == SWT.ARROW_DOWN) {
                    proposal.setProposalPopupFocus();
                }
            }
        });
        
        // modification listener
        searchTxt.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent ev ) {
                String txt = searchTxt.getText();
                if (txt.length() <= 2) {
                    proposalProvider.setProposals( new String[0] );
                }
                else {
                    new ProposalJob().schedule();
                    new ResultCountJob().schedule();
                }
            }
        });
        searchTxt.addListener( SWT.DefaultSelection, new Listener() {
            public void handleEvent( Event ev ) {
                try {
                    zoomResults();
                }
                catch (Exception e) {
                    log.warn( "", e );
                    BatikApplication.handleError( "", e );
                }
            }
        });
    }

    
    protected void zoomResults() throws Exception {
        String txt = searchTxt.getText();
        if (txt.length() == 0) {
            return;
        }
        FullTextIndex addressIndex = AzvPlugin.instance().addressIndex();
        Iterable<JSONObject> results = addressIndex.search( txt, 300 );

        ReferencedEnvelope bbox = new ReferencedEnvelope();
        for (JSONObject feature : results) {
            Geometry geom = (Geometry)feature.get( FullTextIndex.FIELD_GEOM );
            bbox.expandToInclude( geom.getEnvelopeInternal() );
        }
        
        if (!bbox.isNull()) {
            bbox.expandBy( 100 );
            log.info( "BBox: " + bbox );
            viewer.getMap().zoomToExtent( new Bounds( bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY() ), true );
        }
    }
    
    
    /**
     * Updates the {@link AddressSearchMapAction#proposalProvider}. 
     */
    class ProposalJob
            extends UIJob {

        private String      searchTextValue = searchTxt.getText();
        
        public ProposalJob() {
            super( "Proposals" );
        }

        @Override
        protected void runWithException( IProgressMonitor monitor ) throws Exception {
            // proposals
            FullTextIndex addressIndex = AzvPlugin.instance().addressIndex();
            final Iterable<String> results = addressIndex.propose( searchTextValue, 10 );

            // display
            searchTxt.getDisplay().asyncExec( new Runnable() {
                public void run() {
                    proposalProvider.setProposals( Iterables.toArray( results, String.class ) );
                }
            });
        }        
    }

    
    /**
     * Updates the {@link AddressSearchMapAction#resultCountLbl} label. 
     */
    class ResultCountJob
            extends UIJob {

        private String      searchTextValue = searchTxt.getText();

        public ResultCountJob() {
            super( "Count results" );
        }

        @Override
        protected void runWithException( IProgressMonitor monitor ) throws Exception {
            // search
            FullTextIndex addressIndex = AzvPlugin.instance().addressIndex();
            Iterable<JSONObject> results = addressIndex.search( searchTextValue, 100 );
            final int resultCount = FluentIterable.from( results ).size();
            
            // display
            searchTxt.getDisplay().asyncExec( new Runnable() {
                public void run() {
                    String text = resultCount < 100 ? String.valueOf( resultCount ) : ">100";
                    resultCountLbl.setText( text );
                    resultCountLbl.setToolTipText( "Die aktuelle Suche ergibt " + text + " Treffer" );
                }
            });
        }

    }

}
