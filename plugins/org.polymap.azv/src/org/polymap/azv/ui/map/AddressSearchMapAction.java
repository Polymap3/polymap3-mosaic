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
import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.UIJob;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.app.BatikApplication;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.fulltext.FullTextIndex;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;

/**
 * Address search field using {@link AzvPlugin#addressIndex()}.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AddressSearchMapAction
        extends ContributionItem {

    private static Log log = LogFactory.getLog( AddressSearchMapAction.class );

    public static final IMessages   i18n = Messages.forPrefix( "AddressSuche" ); //$NON-NLS-1$

    private MapViewer               viewer;

    private Text                    searchTxt;
    
    private Label                   resultCountLbl;
    
    private ContentProposalAdapter  proposal;

    private SimpleContentProposalProvider proposalProvider;
    
    
    public AddressSearchMapAction( MapViewer viewer ) {
        this.viewer = viewer;
    }


    public String getSearchText() {
        return !searchTxt.getText().startsWith( i18n.get( "hint" ) ) ? searchTxt.getText() : null;
    }
    
    
    @Override
    public void fill( Composite parent ) {
        IPanelToolkit tk = viewer.getPanelSite().toolkit();
        
        Composite result = tk.createComposite( parent );
        result.setLayoutData( RowDataFactory.swtDefaults().hint( 280, SWT.DEFAULT ).create() );
        result.setLayout( FormLayoutFactory.defaults().create() );
        
        searchTxt = tk.createText( result, i18n.get( "hint" ), SWT.SEARCH, SWT.CANCEL );
        searchTxt.setLayoutData( FormDataFactory.filled().create() );

        resultCountLbl = tk.createLabel( result, "" ); //$NON-NLS-1$
        resultCountLbl.moveAbove( searchTxt );
        resultCountLbl.setToolTipText( i18n.get( "keineTreffer" ) );
        resultCountLbl.setLayoutData( FormDataFactory.filled().top( 0, 7 ).clearLeft().width( 25 ).create() );
        resultCountLbl.setForeground( AzvPlugin.instance().discardColor.get() );
        resultCountLbl.setFont( JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT ) ); 
        
        searchTxt.setToolTipText( i18n.get( "suchenTip" ) );
        searchTxt.setForeground( Graphics.getColor( 0xa0, 0xa0, 0xa0 ) );
        searchTxt.addFocusListener( new FocusListener() {
            @Override
            public void focusLost( FocusEvent ev ) {
                if (searchTxt.getText().length() == 0) {
                    searchTxt.setText( i18n.get( "hint" ) );
                    searchTxt.setForeground( Graphics.getColor( 0xa0, 0xa0, 0xa0 ) );
                    //clearBtn.setEnabled( false );
                }
            }
            @Override
            public void focusGained( FocusEvent ev ) {
                if (searchTxt.getText().startsWith( i18n.get( "hint" ) )) {
                    searchTxt.setText( "" ); //$NON-NLS-1$
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
                if (txt.length() < 1) {
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
                    String txt = searchTxt.getText();
                    if (txt.length() == 0) {
                        return;
                    }
                    FullTextIndex addressIndex = AzvPlugin.instance().addressIndex();
                    Iterable<JSONObject> results = addressIndex.search( txt, 100 );

                    zoomResults( results );
                }
                catch (Exception e) {
                    log.warn( "", e ); //$NON-NLS-1$
                    BatikApplication.handleError( "", e ); //$NON-NLS-1$
                }
            }
        });
    }

    
    protected void zoomResults( Iterable<JSONObject> results ) throws Exception {
        ReferencedEnvelope bbox = new ReferencedEnvelope();
        for (JSONObject feature : results) {
            Geometry geom = (Geometry)feature.get( FullTextIndex.FIELD_GEOM );
            bbox.expandToInclude( geom.getEnvelopeInternal() );
        }
        
        if (!bbox.isNull()) {
            bbox.expandBy( 100 );
            log.info( "BBox: " + bbox ); //$NON-NLS-1$
            viewer.zoomToExtent( bbox );
        }
    }
    
    
    /**
     * Updates the {@link AddressSearchMapAction#proposalProvider}. 
     */
    class ProposalJob
            extends UIJob {

        private String      searchTextValue = searchTxt.getText();
        
        public ProposalJob() {
            super( i18n.get( "proposalsJobTitle" ) );
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
            super( i18n.get( "resultsJobTitle" ) );
        }

        @Override
        protected void runWithException( IProgressMonitor monitor ) throws Exception {
            // search
            FullTextIndex addressIndex = AzvPlugin.instance().addressIndex();
            final Iterable<JSONObject> results = addressIndex.search( searchTextValue, 100 );
            final int resultCount = FluentIterable.from( results ).size();
            
            // display results
            searchTxt.getDisplay().asyncExec( new Runnable() {
                public void run() {
                    String text = resultCount < 100 ? String.valueOf( resultCount ) : ">100"; //$NON-NLS-1$
                    resultCountLbl.setText( text );
                    resultCountLbl.setToolTipText( i18n.get( "ergebnisTip", text ) ); 
                    
                    if (resultCount == 0 || resultCount > 100) {
                        resultCountLbl.setForeground( AzvPlugin.instance().discardColor.get() );
                    }
                    else {
                        resultCountLbl.setForeground( AzvPlugin.instance().okColor.get() );

                        try {
                            zoomResults( results );
                        }
                        catch (Exception e) {
                            log.warn( "", e ); //$NON-NLS-1$
                            BatikApplication.handleError( "", e ); //$NON-NLS-1$
                        }
                    }
                }
            });
        }

    }

}
