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
package org.polymap.azv.ui.adresse;

import static com.google.common.collect.Iterables.toArray;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.IControlContentAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.runtime.UIJob;

import org.polymap.rhei.fulltext.FullTextIndex;
import org.polymap.rhei.fulltext.address.Address;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.ui.map.AddressSearchMapAction;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AddressProposal {

    private static Log log = LogFactory.getLog( AddressProposal.class );
    
    private FullTextIndex           addressIndex = AzvPlugin.instance().addressIndex();

    /** One of the {@link Address} field constants. */
    private String                  addressField;
    
    private Text                    control;
    
    private XContentProposalAdapter proposal;

    private SimpleContentProposalProvider proposalProvider;

    private String                  currentValue = "";
    

    /**
     * 
     * @param control
     * @param addressField One of the {@link Address} field constants.
     */
    public AddressProposal( Text control, String addressField ) {
        this.control = control;
        this.addressField = addressField;
        assert ArrayUtils.contains( Address.ALL_FIELDS, addressField );

        proposalProvider = new SimpleContentProposalProvider( new String[0] );
        TextContentAdapter controlAdapter = new TextContentAdapter() {
            public void insertControlContents( Control _control, String text, int cursorPosition ) {
                ((Text)_control).setText( text );
                ((Text)_control).setSelection( text.length() );
            }
        };

        proposal = new XContentProposalAdapter( control, controlAdapter, proposalProvider, null, null );
        proposal.setAutoActivationDelay( -1 );
        control.addKeyListener( new KeyAdapter() {
            public void keyReleased( KeyEvent ev ) {
                // just close popup, prevent re-open
                if (ev.keyCode == SWT.ESC) {
                }
                // allow to select entry in popup
                else if (ev.keyCode == SWT.ARROW_UP || ev.keyCode == SWT.ARROW_DOWN) {
                    proposal.setProposalPopupFocus();
                }
                else {
                    // close popup: visual feedback for user that key has been recognized;
                    // also, otherwise proposal would check in the current entries
                    proposalProvider.setProposals( new String[0] );
                    proposal.closeProposalPopup();

                    currentValue = AddressProposal.this.control.getText();
                    if (currentValue.length() == 0) {
                        proposalProvider.setProposals( new String[0] );
                    }
                    else {
                        new ProposalJob().schedule( 1750 );
                    }                    
                }
            }
        });
    }


    /**
     * Updates the {@link AddressSearchMapAction#proposalProvider}. 
     */
    class ProposalJob
            extends UIJob {

        private String      value = control.getText();
        
        public ProposalJob() {
            super( "Adressfeld suchen" );
        }

        @Override
        protected void runWithException( IProgressMonitor monitor ) throws Exception {
            // skip if control is disposed or no longer focused
            if (control == null || control.isDisposed()) {
                log.info( "Control is disposed." );
                return;
            }
            // skip if search text has changed
            if (value != currentValue) {
                log.info( "Search text has changed: " + value + " -> " + currentValue );
                return;
            }

            // find proposals
            final String[] results = toArray( 
                    addressIndex.propose( value, 10, addressField ), String.class );

            // display
            control.getDisplay().asyncExec( new Runnable() {
                public void run() {
                    if (!control.isFocusControl()) {
                        log.info( "Control is no longer focused." );
                        return;
                    }
                    proposalProvider.setProposals( results );
                    if (results.length > 0 && !results[0].equals( value )) {
                        proposal.openProposalPopup();
                        //proposal.setProposalPopupFocus();
                    }
                    else {
                        proposal.closeProposalPopup();
                    }
                }
            });
        }        
    }

    
    /**
     * Expose some protected methods.
     */
    class XContentProposalAdapter
            extends ContentProposalAdapter {

        public XContentProposalAdapter( Control control, IControlContentAdapter controlContentAdapter,
                IContentProposalProvider proposalProvider, KeyStroke keyStroke, char[] autoActivationCharacters ) {
            super( control, controlContentAdapter, proposalProvider, keyStroke, autoActivationCharacters );
        }

        @Override
        protected void closeProposalPopup() {
            super.closeProposalPopup();
        }

        @Override
        protected void openProposalPopup() {
            super.openProposalPopup();
        }
        
    }
    
}
