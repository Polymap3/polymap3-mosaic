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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.app.BatikApplication;
import org.polymap.rhei.batik.toolkit.ConstraintLayout;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;

import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.ui.MosaicUiPlugin;

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

    /** The action that currently has an open action section. */ 
    protected CaseActionHolder              activeAction;

    private Composite                       contentSection;

    private CaseStatusViewer                statusViewer;
    
    
    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        return false;
    }


    @Override
    public void dispose() {
        if (activeAction != null) {
            discardActiveAction();
        }
        for (CaseActionHolder holder : caseActions) {
            holder.caseAction.dispose();
        }
        caseActions.clear();
        if (statusViewer != null) {
            statusViewer.dispose();
            statusViewer = null;
        }
    }


    @Override
    public PanelIdentifier id() {
        return ID;
    }


    @Override
    public void createContents( Composite panelBody ) {
        getSite().setTitle( "Vorgang: " + mcase.get() != null ? mcase.get().getName() : "" );
        this.tk = getSite().toolkit();
        int margins = getSite().getLayoutPreference( LAYOUT_MARGINS_KEY );
        int spacing = getSite().getLayoutPreference( LAYOUT_SPACING_KEY );
        panelBody.setLayout( FormLayoutFactory.defaults().margins( margins+10, 0 ).create() );

        // init caseActions
        CaseActionHolder autoActivate = null;
        for (CaseActionExtension ext : CaseActionExtension.all()) {
            try {
                ICaseAction caseAction = ext.newInstance();
                getContext().propagate( caseAction );

                CaseActionHolder holder = new CaseActionHolder( ext, caseAction );
                CaseActionSite actionSite = new CaseActionSite( holder );
                
                if (caseAction.init( actionSite )) {
                    caseActions.add( holder );
                    if (ext.isAutoActivate()) {
                        autoActivate = autoActivate == null || autoActivate.ext.getPriority() < ext.getPriority()
                                ? holder : autoActivate;
                    }
                }
            }
            catch (Exception e) {
                log.warn( "Exception while initializing ICaseAction: " + ext, e );
            }
        }
        // auto activate CaseAction after init has completed
        if (autoActivate != null) {
            final CaseActionHolder finalAutoActivate = autoActivate;
            panelBody.getDisplay().asyncExec( new Runnable() {
                public void run() {
                    activateAction( finalAutoActivate );
                }
            });
        }

        // status area
        Composite statusSection = tk.createComposite( panelBody );
        statusSection.setLayoutData( FormDataFactory.filled().bottom( -1 ).create() );
        createStatusSection( statusSection );

        // toolbar
        toolbarSection = tk.createComposite( panelBody );
        createToolbarSection( toolbarSection );
        toolbarSection.setLayoutData( toolbarSection.getChildren().length > 0
                ? FormDataFactory.filled().top( statusSection, spacing ).height( 30 ).bottom( -1 ).create()
                : FormDataFactory.filled().top( statusSection ).height( 0 ).bottom( -1 ).create() );
//        if (toolbarSection.getChildren().length == 0) {
//            toolbarSection.setVisible( false );
//            toolbarSection.setBackground( Graphics.getColor( 30, 30, 30 ) );
//        }
        
        // action area
        actionSection = tk.createComposite( panelBody );
        actionSection.setLayoutData( FormDataFactory.filled().top( toolbarSection ).bottom( toolbarSection, 30 ).create() );
        actionSection.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_ACTION_SECTION_DEACTIVE );
        
        // content area
        contentSection = tk.createComposite( panelBody );
        contentSection.setLayoutData( FormDataFactory.filled().top( actionSection ).bottom( 100 ).create() );
        createContentSection( contentSection );
    }


    protected void updateActionSection( CaseActionHolder holder ) {
        for (Control child : actionSection.getChildren()) {
            child.dispose();
        }
        // action area
        int margins = getSite().getLayoutPreference( LAYOUT_MARGINS_KEY );
        int spacing = getSite().getLayoutPreference( LAYOUT_SPACING_KEY );
        actionSection.setLayout( FormLayoutFactory.defaults().create() );
        
        // closeBtn
        // XXX CSS currently for TOGGLE buttons only
        Button closeBtn = getSite().toolkit().createButton( actionSection, null, SWT.TOGGLE );
        closeBtn.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_TOOLBAR_SECTION );
        closeBtn.setSelection( true );
        closeBtn.setToolTipText( "Aktion abbrechen" );
        closeBtn.setImage( BatikPlugin.instance().imageForName( "resources/icons/cancel-circle.png" ) );
        closeBtn.setLayoutData( FormDataFactory.filled().left( -1 ).bottom( -1 ).top( 0, 3 ).width( 27 ).create() );
        closeBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                discardActiveAction();
            }
        });
        
        // contents Composite
        Composite contents = getSite().toolkit().createComposite( actionSection );
        contents.setLayoutData( FormDataFactory.filled().create() );
        ConstraintLayout layout = new ConstraintLayout( margins, margins, spacing );
        contents.setLayout( layout );
        
        if (holder != null) {
            try {
                holder.caseAction.createContents( contents );

                // XXX get margins from panel settings
                int panelWidth = actionSection.getParent().getSize().x - 40;
                int actionSectionHeight = contents.computeSize( panelWidth, -1, true ).y + 80;
                ((FormData)actionSection.getLayoutData()).bottom = new FormAttachment( toolbarSection, actionSectionHeight );

                actionSection.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_ACTION_SECTION_ACTIVE );
                if (contents.getChildren().length > 0) {
                    contentSection.setEnabled( false );
                }
                // no children -> submit immediatelly
                else {
                    actionSection.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_ACTION_SECTION_DEACTIVE );
                    ((FormData)actionSection.getLayoutData()).bottom = new FormAttachment( toolbarSection, 30 );
                    
                    submitActiveAction();
                }
            }
            catch (Throwable e) {
                log.warn( "", e );
            }
        }
        // remove action area
        else {
            actionSection.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_ACTION_SECTION_DEACTIVE );
            ((FormData)actionSection.getLayoutData()).bottom = new FormAttachment( toolbarSection, 30 );
            contentSection.setEnabled( true );
        }
        actionSection.getParent().layout( true );
        getSite().layout( true );
    }
    
    
    protected void createStatusSection( Composite body ) {
        FillLayout layout = new FillLayout();
        body.setLayout( layout );
        body.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_STATUS_SECTION );

        statusViewer = new CaseStatusViewer( getSite() );
        for (CaseActionHolder holder: caseActions) {
            try {
                holder.caseAction.fillStatus( statusViewer.status );
            }
            catch (Throwable e) {
                log.warn( "", e );
            }
        }
        statusViewer.createContents( body );
    }
    
    
    protected void createToolbarSection( Composite body ) {
        body.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
        body.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_TOOLBAR_SECTION );

        Button prev = null;
        for (final CaseActionHolder holder: caseActions) {
            final Action action = new Action() {};
            action.setText( holder.ext.getName() );
            action.setToolTipText( holder.ext.getDescription() );
            action.setImageDescriptor( holder.ext.getIcon() );
            
            holder.caseAction.fillAction( action );
         
            // listen to changes the caseAction may perform
            action.addPropertyChangeListener( new IPropertyChangeListener() {
                public void propertyChange( PropertyChangeEvent event ) {
                    fillToolbarBtn( holder, action );                    
                }
            });
            
            if (action.getText() != null || action.getImageDescriptor() != null) {
                holder.btn = tk.createButton( body, null, SWT.TOGGLE );
                fillToolbarBtn( holder, action );

                FormDataFactory layoutData = FormDataFactory.filled().right( -1 );
                if (prev != null) {
                    layoutData.left( prev );
                }
                holder.btn.setLayoutData( layoutData.create() );
                holder.btn.addSelectionListener( new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent e ) {
                        if (holder.btn.getSelection()) {
                            activateAction( holder );
                        }
                        else {
                            discardActiveAction();
                        }
                    }
                });
                prev = holder.btn;
            }
        }
    }

    
    private void fillToolbarBtn( CaseActionHolder holder, IAction action ) {
        holder.btn.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_TOOLBAR_SECTION );
        holder.btn.setToolTipText( action.getToolTipText() != null ? action.getToolTipText() : action.getText() );
        holder.btn.setEnabled( action.isEnabled() );
        
        ImageDescriptor icon = action.getImageDescriptor();        
        if (icon != null) {
            holder.btn.setImage( MosaicUiPlugin.getDefault().images().image( icon, icon.toString() ) );
        }
        if (holder.ext.isCaseChangeAction()) {
            holder.btn.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_SUBMIT );
            if (icon == null) {
                holder.btn.setImage( BatikPlugin.instance().imageForName( "resources/icons/ok.png" ) );
            }
        }
        Image btnIcon = holder.btn.getImage();
        int displayWidth = BatikApplication.sessionDisplay().getClientArea().width;
        if ((displayWidth > 720 && action.getText() != null) || btnIcon == null) {
            holder.btn.setText( action.getText().toUpperCase() );
        }
    }

    private void activateAction( CaseActionHolder holder ) {
        if (activeAction != null) {
            discardActiveAction();
        }
        activeAction = holder;
        if (holder.btn != null) {
            holder.btn.setSelection( true );
        }
        updateActionSection( holder );
        holder.updateEnabled();
    }
    

    private void submitActiveAction() {
        try {
            activeAction.caseAction.submit();
            
            if (activeAction.btn != null) {
                activeAction.btn.setSelection( false );
            }
            activeAction = null;
            updateActionSection( null );
        }
        catch (Exception e) {
            BatikApplication.handleError( "Die Änderungen konnten nicht korrekt übernommen werden.", e );
        }
    }
    
    
    private void discardActiveAction() {
        activeAction.caseAction.discard();

        if (activeAction.btn != null && !activeAction.btn.isDisposed()) {
            activeAction.btn.setSelection( false );
        }
        activeAction = null;
        updateActionSection( null );
    }
    
    
    protected void createContentSection( Composite body ) {
        body.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_CONTENT_SECTION );
        // XXX adopt computed margins/spacing from panel
        ConstraintLayout layout = new ConstraintLayout( 0, 20, 40 );
        body.setLayout( layout );
        for (CaseActionHolder holder: caseActions) {
            try {
                holder.caseAction.fillContentArea( body );
            }
            catch (Throwable e) {
                log.warn( "Exception while fillContentArea()", e );
            }
        }
    }
    
    
    /**
     * 
     */
    class CaseActionHolder
            implements Comparable<CaseActionHolder> {
        
        public CaseActionExtension  ext;
        public ICaseAction          caseAction;
        public Action               action;
        public Button               btn;
        protected boolean           submitEnabled = true;
        protected boolean           showSubmitButton = true;        

        protected CaseActionHolder( CaseActionExtension ext, ICaseAction caseAction ) {
            this.ext = ext;
            this.caseAction = caseAction;
        }

        protected void updateEnabled() {
            if (submitBtn != null && !submitBtn.isDisposed()) {
                submitBtn.setEnabled( submitEnabled );            
            }
        }
        
        @Override
        public int compareTo( CaseActionHolder rhs ) {
            int result = ext.getPriority() - rhs.ext.getPriority();
            return result != 0 ? result : ext.getId().compareTo( rhs.ext.getId() );
        }
    }


    /**
     * 
     */
    class CaseActionSite
            implements ICaseActionSite {

        protected CaseActionHolder  holder;
                
        public CaseActionSite( CaseActionHolder holder ) {
            assert holder != null;
            this.holder = holder;
        }

        @Override
        public IPanelSite getPanelSite() {
            return CasePanel.this.getSite();
        }

        @Override
        public IAppContext getContext() {
            return CasePanel.this.getContext();
        }

        @Override
        public void setShowSubmitButton( boolean show ) {
            holder.showSubmitButton = show;
//            if (submitBtn != null && !submitBtn.isDisposed()) {
//                submitBtn.dispose();
//                submitBtn = null;
//                discardBtn.dispose();
//                discardBtn = null;
//            }
        }

        @Override
        public void setSubmitEnabled( boolean enabled ) {
            holder.submitEnabled = enabled;
            holder.updateEnabled();
        }

        @Override
        public String getActionId() {
            return holder.ext.getId();
        }

        @Override
        public IPanelToolkit toolkit() {
            return getSite().toolkit();
        }

        @Override
        public void activateCaseAction( String actionId ) {
            assert actionId != null;
            for (final CaseActionHolder elm: caseActions) {
                if (elm.ext.getId().equals( actionId )) {
                    activateAction( elm );
                    return;
                }
            }
        }

        @Override
        public void submit() {
            submitActiveAction();
        }

        @Override
        public void discard() {
            discardActiveAction();
        }

        @Override
        public void addListener( Object annotated ) {
            EventManager.instance().subscribe( annotated, new EventFilter<CaseActionEvent>() {
                public boolean apply( CaseActionEvent input ) {
                    // XXX Auto-generated method stub
                    throw new RuntimeException( "not yet implemented." );
                }
            });
        }
        
        @Override
        public boolean removeListener( Object annotated ) {
            return EventManager.instance().unsubscribe( annotated );
        }

        @Override
        public Button createSubmit( Composite parent, String title ) {
            submitBtn = toolkit().createButton( parent, title != null ? title : null );
            submitBtn.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_SUBMIT );
            submitBtn.addSelectionListener( new SelectionAdapter() {
                public void widgetSelected( SelectionEvent ev ) {
                    submitActiveAction();
                }
            });
            submitBtn.setEnabled( holder.submitEnabled );
            return submitBtn;
        }
        
    }
    
}
