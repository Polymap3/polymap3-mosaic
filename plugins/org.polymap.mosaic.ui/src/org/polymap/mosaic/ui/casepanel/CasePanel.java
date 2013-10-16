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
package org.polymap.mosaic.ui.casepanel;

import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.jface.action.Action;

import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.toolkit.ConstraintLayout;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.CaseStatus.Entry;

/**
 * The general case panel. Actually business logic is provided via the
 * {@value CaseActionExtension#POINT_ID} extension point and the {@link ICaseAction}
 * interface.
 * <p/>
 * The case object must be set as {@link ContextProperty} with scope
 * {@link MosaicUiPlugin#CONTEXT_PROPERTY_SCOPE} before opening this panel.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class CasePanel
        extends DefaultPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( CasePanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "mosaic", "case" );

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>    mcase;

    private IPanelToolkit                   tk;
    
    /** Sorted by priority of the action. */
    private TreeSet<CaseActionHolder>       caseActions = new TreeSet();

    private Composite                       actionSection;

    private Composite                       toolbarSection;

    private Button                          submitBtn;

    private Button                          discardBtn;

    /** The action that currently has an open action section. */ 
    protected CaseActionHolder              activeAction;
    
    
    /**
     * 
     */
    class CaseActionHolder
            implements Comparable<CaseActionHolder> {
        
        public CaseActionExtension      ext;
        public ICaseAction              caseAction;
        public Action                   action;
        public Button                   btn;
        
        @Override
        public int compareTo( CaseActionHolder rhs ) {
            int result = ext.getPriority() - rhs.ext.getPriority();
            return result != 0 ? result : hashCode() - rhs.hashCode();
        }
    }
    
    
    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        log.info( "CASE: " + mcase.get() );
        return false;
    }


    @Override
    public PanelIdentifier id() {
        return ID;
    }


    @Override
    public void createContents( Composite panelBody ) {
        getSite().setTitle( "Vorgang: ???" );
        this.tk = getSite().toolkit();
        int margins = getSite().getLayoutPreference( LAYOUT_MARGINS_KEY );
        int spacing = getSite().getLayoutPreference( LAYOUT_SPACING_KEY );
        panelBody.setLayout( FormLayoutFactory.defaults().margins( margins ).spacing( spacing ).create() );

        // init caseActions
        for (CaseActionExtension ext : CaseActionExtension.all()) {
            try {
                ICaseAction caseAction = ext.newInstance();
                getContext().propagate( caseAction );
                if (caseAction.init( getSite(), getContext() )) {
                    CaseActionHolder holder = new CaseActionHolder();
                    holder.ext = ext;
                    holder.caseAction = caseAction;
                    caseActions.add( holder );
                }
            }
            catch (Exception e) {
                log.warn( "Exception while initializing ICaseAction: " + ext, e );
            }
        }
        
        // status area
        Composite statusSection = tk.createComposite( panelBody );
        statusSection.setLayoutData( FormDataFactory.filled().bottom( -1 ).create() );
        createStatusSection( statusSection );

        // toolbar
        toolbarSection = tk.createComposite( panelBody );
        toolbarSection.setLayoutData( FormDataFactory.filled().top( statusSection ).bottom( statusSection, 105 ).create() );
        createToolbarSection( toolbarSection );
        
        // action area
        actionSection = tk.createComposite( panelBody );
        actionSection.setLayoutData( FormDataFactory.filled().top( toolbarSection ).bottom( toolbarSection, 30 ).create() );
        
        // content area
        Composite contentSection = tk.createComposite( panelBody );
        contentSection.setLayoutData( FormDataFactory.filled().top( actionSection ).bottom( 100 ).create() );
        createContentSection( contentSection );
    }


    protected void updateActionSection( CaseActionHolder holder ) {
        for (Control child : actionSection.getChildren()) {
            child.dispose();
        }
        // create action area
        actionSection.setLayout( new FillLayout() );
        if (holder != null) {
            try {
                ((FormData)actionSection.getLayoutData()).bottom = new FormAttachment( toolbarSection, 300 );
                holder.caseAction.createContents( actionSection );
            }
            catch (Throwable e) {
                log.warn( "", e );
            }
        }
        // remove action area
        else {
            ((FormData)actionSection.getLayoutData()).bottom = new FormAttachment( toolbarSection, 30 );
        }
        actionSection.getParent().layout( true );
    }
    
    
    protected void createStatusSection( Composite body ) {
        FillLayout layout = new FillLayout();
        layout.marginWidth = getSite().getLayoutPreference( LAYOUT_MARGINS_KEY );
        //layout.marginHeight = getSite().getLayoutPreference( LAYOUT_MARGINS_KEY );
        body.setLayout( layout );
        body.setData( WidgetUtil.CUSTOM_VARIANT, "atlas-panel-form" );

        CaseStatus status = new CaseStatus();
        for (CaseActionHolder holder: caseActions) {
            try {
                holder.caseAction.fillStatus( status );
            }
            catch (Throwable e) {
                log.warn( "", e );
            }
        }
        StringBuilder buf = new StringBuilder( 1024 );
        for (Entry entry : status.entries()) {
            buf.append( buf.length() > 0 ? " | " : "" );
            buf.append( entry.getKey() ).append( ": " ).append( "<strong>" ).append( entry.getValue() ).append( "</strong>" );
        }
        tk.createFlowText( body, buf.toString() );
    }
    
    
    protected void createToolbarSection( Composite body ) {
        body.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
        
        submitBtn = tk.createButton( body, "OK", SWT.PUSH );
        submitBtn.setLayoutData( FormDataFactory.filled().right( -1 ).create() );
        submitBtn.setEnabled( false );
        submitBtn.setToolTipText( "Aktion abschließen und Änderungen übernehmen" );
        submitBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                activeAction.caseAction.submit();
                updateActionSection( null );
                submitBtn.setEnabled( false );
                discardBtn.setEnabled( false );
            }
        });
        discardBtn = tk.createButton( body, "...", SWT.PUSH );
        discardBtn.setLayoutData( FormDataFactory.filled().left( submitBtn ).right( -1 ).create() );
        discardBtn.setEnabled( false );
        discardBtn.setToolTipText( "Änderungen verwerfen" );
        discardBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                activeAction.caseAction.discard();
                updateActionSection( null );
                submitBtn.setEnabled( false );
                discardBtn.setEnabled( false );
            }
        });
        
        Button prev = discardBtn;
        for (final CaseActionHolder holder: caseActions) {
            final Action action = new Action() {};
            action.setText( holder.ext.getName() );
            action.setToolTipText( holder.ext.getDescription() );
            action.setImageDescriptor( holder.ext.getIcon() );
            
            holder.caseAction.fillAction( action );
            
            holder.btn = tk.createButton( body, action.getText(), SWT.PUSH );
            holder.btn.setToolTipText( action.getToolTipText() );
            holder.btn.setEnabled( action.isEnabled() );
            
            FormDataFactory layoutData = FormDataFactory.filled().right( -1 );
            if (prev != null) {
                layoutData.left( prev );
            }
            holder.btn.setLayoutData( layoutData.create() );
            holder.btn.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent e ) {
                    log.info( " ---> " + action.getText() );
                    activeAction = holder;
                    holder.btn.setSelection( true );
                    
                    submitBtn.setEnabled( true );
                    discardBtn.setEnabled( true );
                    
                    updateActionSection( holder );
                }
            });
            prev = holder.btn;
        }
    }
    
    
    protected void createContentSection( Composite body ) {
        body.setLayout( new ConstraintLayout() );
        for (CaseActionHolder holder: caseActions) {
            try {
                holder.caseAction.fillContentArea( body );
            }
            catch (Throwable e) {
                log.warn( "Exception while fillContentArea()", e );
            }
        }
    }
    
}
