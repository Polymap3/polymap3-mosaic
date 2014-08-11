/*
 * polymap.org
 * Copyright (C) 2013, Polymap GmbH. All rights reserved.
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

import static org.polymap.azv.AzvPlugin.CASE_DIENSTBARKEITEN;
import static org.polymap.azv.AzvPlugin.CASE_ENTSORGUNG;
import static org.polymap.azv.AzvPlugin.CASE_LEITUNGSAUSKUNFT;
import static org.polymap.azv.AzvPlugin.CASE_SCHACHTSCHEIN;
import static org.polymap.mosaic.ui.MosaicUiPlugin.ff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import java.beans.PropertyChangeEvent;

import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import org.eclipse.rwt.lifecycle.WidgetUtil;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.data.ui.featuretable.FeatureTableFilterBar;
import org.polymap.core.data.ui.featuretable.FeatureTableSearchField;
import org.polymap.core.model.Entity;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.security.UserPrincipal;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelChangeEvent;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PropertyAccessEvent;
import org.polymap.rhei.batik.app.BatikApplication;
import org.polymap.rhei.batik.layout.desktop.DesktopToolkit;
import org.polymap.rhei.batik.toolkit.ConstraintData;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.MaxWidthConstraint;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;
import org.polymap.rhei.batik.toolkit.NeighborhoodConstraint;
import org.polymap.rhei.batik.toolkit.NeighborhoodConstraint.Neighborhood;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.ui.LoginPanel;
import org.polymap.rhei.um.ui.RegisterPanel;
import org.polymap.rhei.um.ui.UserSettingsPanel;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.model.AzvVorgang;
import org.polymap.azv.ui.dienstbarkeiten.DienstbarkeitenCasesDecorator;
import org.polymap.azv.ui.entsorgung.EntsorgungCasesDecorator;
import org.polymap.azv.ui.hydranten.HydrantenPanel;
import org.polymap.azv.ui.leitungsauskunft.LeitungsauskunftCasesDecorator;
import org.polymap.azv.ui.leitungsauskunft.LeitungsauskunftPanel;
import org.polymap.azv.ui.nutzerregistrierung.NutzerCasesDecorator;
import org.polymap.azv.ui.nutzerregistrierung.UserPermissionsSection;
import org.polymap.azv.ui.schachtschein.SchachtscheinCasesDecorator;
import org.polymap.azv.ui.wasserquali.WasserQualiPanel;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicCaseEvent;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.CasePanel;
import org.polymap.mosaic.ui.casestable.CasesTableViewer;
import org.polymap.mosaic.ui.casestable.ICasesViewerDecorator;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class StartPanel
        extends DefaultPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( StartPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "start" ); //$NON-NLS-1$

    public static final IMessages       i18n = Messages.forPrefix( "StartPanel" ); //$NON-NLS-1$

    /** Just for convenience, same as <code>getSite().toolkit()</code>. */
    private IPanelToolkit                   tk;
    
    /** Set by the {@link LoginPanel}. */
    private ContextProperty<UserPrincipal>  user;
    
    //@Context(scope=AzvPlugin.PROPERTY_SCOPE)
    private ContextProperty<Entity>         entity;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>    mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2> repo;

    private List<Control>                   actionBtns = new ArrayList();

    private Composite                       contents;

    private IPanelSection                   loginSection, casesSection, welcomeSection;

    private CasesTableViewer                casesViewer;
    
    private CasesTableViewer                closedCasesViewer;
    
    private List<ICasesViewerDecorator>     casesViewerDecorators = new ArrayList();
    
    private Object                          panelListener;


    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        if (site.getPath().size() == 1) {
            this.tk = site.toolkit();
            this.repo.set( MosaicRepository2.instance() );
            site.setTitle( i18n.get( "title" ) ); //i18n.get( "title" ) );
            site.setIcon( BatikPlugin.instance().imageForName( "resources/icons/house.png" ) ); //$NON-NLS-1$

            casesViewerDecorators.add( context.propagate( new MitarbeiterCasesDecorator() ) );
            casesViewerDecorators.add( context.propagate( new NutzerCasesDecorator() ) );
            casesViewerDecorators.add( context.propagate( new SchachtscheinCasesDecorator() ) );
            casesViewerDecorators.add( context.propagate( new LeitungsauskunftCasesDecorator() ) );
            casesViewerDecorators.add( context.propagate( new DienstbarkeitenCasesDecorator() ) );
            casesViewerDecorators.add( context.propagate( new EntsorgungCasesDecorator() ) );
            
            user.addListener( this, new EventFilter<PropertyAccessEvent>() {
                public boolean apply( PropertyAccessEvent input ) {
                    return input.getType() == PropertyAccessEvent.TYPE.SET;
                }
            });

            // Impressum
            getContext().addPreferencesAction( new Action( Messages.get( "Impressum_titel" ) ) {
                public void run() {
                    ArticlePanel panel = (ArticlePanel)getContext().openPanel( ArticlePanel.ID );
                    panel.setContent( Messages.get( "Impressum_titel" ), Messages.get( "Impressum_inhalt" ) );
                }
            });

            
            // RegisterPanel erweitern für Beantragte Rechte
            context.addListener( panelListener = new Object() {
                @EventHandler(display=true)
                public void panelChanged( PanelChangeEvent ev ) {
                    if (ev.getSource() instanceof RegisterPanel && ev.getType() == PanelChangeEvent.TYPE.ACTIVATED) {
                        log.info( "PANEL changed: " + ev );
                        RegisterPanel panel = ev.getPanel();
                        Composite parent = panel.getPanelContainer();
                        
                        IPanelSection section = panel.getSite().toolkit().createPanelSection( parent, "Beantragte Rechte" );
                        section.addConstraint( new PriorityConstraint( 5 ), AzvPlugin.MIN_COLUMN_WIDTH,
                                new NeighborhoodConstraint( panel.getPersonSection(), Neighborhood.TOP, 100 ) );
                        new UserPermissionsSection( getSite(), Collections.EMPTY_SET ).createContent( section.getBody() );
                        
                        panel.getPersonSection().setTitle( "Angaben zur Person/Firma" );
                        
                        panel.getSite().layout( true );
                    }
                }
            });
            return true;
        }
        return false;
    }


    @Override
    public PanelIdentifier id() {
        return ID;
    }

    
    protected boolean isAuthenticatedUser() {
        return user.get() != null; // ||  nutzer.get() != null && nutzer.get().authentifiziert().get();
    }

    
    @Override
    public void createContents( Composite parent ) {
        contents = parent;
        
        createWelcomeSection( contents )
                .addConstraint( new PriorityConstraint( 100 ), AzvPlugin.MIN_COLUMN_WIDTH );

//        createLoginSection( contents )
//                .addConstraint( new PriorityConstraint( 10 ), AzvPlugin.MIN_COLUMN_WIDTH );

        createActionsSection( contents );
        
        // listen to PropertyAccessEvent
        EventManager.instance().subscribe( this, new EventFilter<PropertyAccessEvent>() {
            public boolean apply( PropertyAccessEvent ev ) {
                return ev.getType() == PropertyAccessEvent.TYPE.SET
                        && ev.getSource().getDeclaredType().equals( UserPrincipal.class );
            }
        });
    }

    
    @EventHandler(display=true)
    protected void handleEvent( PropertyAccessEvent ev ) {
        for (Control btn : actionBtns) {
            String role = (String)btn.getData( "role" ); //$NON-NLS-1$
            btn.setEnabled( role == null || SecurityUtils.isUserInGroup( role ) );
        }
    }
    
    
    protected IPanelSection createWelcomeSection( Composite parent ) {
        welcomeSection = tk.createPanelSection( parent, i18n.get( "welcomeTitle" ) ); //$NON-NLS-1$
        welcomeSection.getControl().setData( "title", "welcome" ); //$NON-NLS-1$ //$NON-NLS-2$
        welcomeSection.getBody().setLayout( new FillLayout() );
        tk.createFlowText( welcomeSection.getBody(), i18n.get( "welcomeText" ) ); //$NON-NLS-1$
        return welcomeSection;
    }

    
    protected void createCasesSection( Composite parent ) {
        casesSection = tk.createPanelSection( parent, i18n.get( "aktuelleVorgaengeTitle" ) );
        casesSection.addConstraint( 
                new PriorityConstraint( 100 ), AzvPlugin.MIN_COLUMN_WIDTH, new MaxWidthConstraint( 1000, 0 ) );
        casesSection.getBody().setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
        
        Filter filter = ff.equals( ff.property( "status" ), ff.literal( IMosaicCaseEvent.TYPE_OPEN ) ); //$NON-NLS-1$
        casesViewer = new CasesTableViewer( casesSection.getBody(), repo.get(), filter, SWT.NONE );
        casesViewer.addColumn( new AzvStatusCaseTableColumn( repo.get() ) );
        casesViewer.getColumn( "created" ).sort( SWT.UP ); //$NON-NLS-1$

        // table layout
        int displayHeight = BatikApplication.sessionDisplay().getBounds().height;
        int tableHeight = (displayHeight - (2*65) - (2*75));  // margins, titles+icons
        FormDataFactory.filled().top( -1 ).height( tableHeight ).width( 420 ).applyTo( casesViewer.getTable() );

        casesViewer.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent ev ) {
                IMosaicCase sel = Iterables.getOnlyElement( casesViewer.getSelected() );
                mcase.set( sel );
                getContext().openPanel( CasePanel.ID );            
            }
        });

        // filterBar
        FeatureTableFilterBar filterBar = new FeatureTableFilterBar( casesViewer, casesSection.getBody() );
        filterBar.getControl().setLayoutData( FormDataFactory.filled().bottom( casesViewer.getTable() ).right( 50 ).create() );
        
        // decorate viewer
        for (ICasesViewerDecorator deco : casesViewerDecorators) {
            deco.fill( casesViewer, filterBar );
        }
        
        // global update listener
        Control updateBtn = new UpdateHandler( tk ) {
            protected void doUpdate( List<PropertyChangeEvent> events ) {
                repo.get().rollbackChanges();
                if (casesViewer != null) {
                    casesViewer.refresh( true );
                }
                if (closedCasesViewer != null) {
                    closedCasesViewer.refresh( true );
                }
            }
            
        }.createControl( casesSection.getBody() );
        updateBtn.setLayoutData( FormDataFactory.filled()
                .width( 30 ).clearRight().bottom( casesViewer.getTable() ).left( filterBar.getControl() ).create() );
        
        // searchField
        FeatureTableSearchField searchField = new FeatureTableSearchField( 
                casesViewer, casesSection.getBody(), Arrays.asList( "name", "created" ) ); //$NON-NLS-1$ //$NON-NLS-2$
        Composite searchCtrl = searchField.getControl();
        searchCtrl.setLayoutData( FormDataFactory.filled()
                .height( 27 ).bottom( casesViewer.getTable() ).left( updateBtn ).create() );
        
//        for (Control child : searchCtrl.getChildren()) {
//            if (child instanceof Button) {
//                ((Button)child).setImage( BatikPlugin.instance().imageForName( "resources/icons/close.png" ) );
//                ((Button)child).setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_DISCARD );                
//            }
//        }
    }

    
    protected void createClosedCasesSection( Composite parent ) {
        int displayHeight = BatikApplication.sessionDisplay().getBounds().height;
        int tableHeight = (displayHeight - (3*65) - (2*75)) / 2;  // margins, titles+icons
        casesViewer.getTable().setLayoutData( FormDataFactory.filled().top( -1 ).height( tableHeight ).width( 420 ).create() );

        IPanelSection closedCasesSection = tk.createPanelSection( parent, i18n.get( "bearbeiteteVorgaengeTitle" ) );
        closedCasesSection.addConstraint( AzvPlugin.MIN_COLUMN_WIDTH,
                new PriorityConstraint( 100 ),
                new NeighborhoodConstraint( casesSection.getControl(), Neighborhood.BOTTOM, 1 ) );
        closedCasesSection.getBody().setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
        
        Filter filter = ff.equals( ff.property( "status" ), ff.literal( IMosaicCaseEvent.TYPE_CLOSED ) ); //$NON-NLS-1$
        closedCasesViewer = new CasesTableViewer( closedCasesSection.getBody(), repo.get(), filter, SWT.NONE );
        closedCasesViewer.addColumn( new AzvStatusCaseTableColumn( repo.get() ) );
        closedCasesViewer.getTable().setLayoutData( FormDataFactory.filled().top( -1 ).height( tableHeight ).width( 420 ).create() );
        closedCasesViewer.addDoubleClickListener( new IDoubleClickListener() {
            public void doubleClick( DoubleClickEvent ev ) {
                IMosaicCase sel = Iterables.getOnlyElement( closedCasesViewer.getSelected() );
                mcase.set( sel );
                getContext().openPanel( CasePanel.ID );
            }
        });
        closedCasesViewer.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent ev ) {
                IMosaicCase sel = Iterables.getOnlyElement( closedCasesViewer.getSelected() );
                mcase.set( sel );
                getContext().openPanel( CasePanel.ID );            
            }
        });

        // filterBar
        FeatureTableFilterBar filterBar = new FeatureTableFilterBar( closedCasesViewer, closedCasesSection.getBody() );
        filterBar.getControl().setLayoutData( FormDataFactory.filled().bottom( closedCasesViewer.getTable() ).right( 50 ).create() );
        
        // decorate viewer
        for (ICasesViewerDecorator deco : casesViewerDecorators) {
            deco.fill( closedCasesViewer, filterBar );
        }
        closedCasesViewer.getColumn( "created" ).sort( SWT.UP ); //$NON-NLS-1$
    }
    
    
    protected void createActionsSection( Composite parent ) {
        IPanelSection section1 = tk.createPanelSection( parent, i18n.get( "auskuenfteTitle" ), Section.TITLE_BAR );
        section1.addConstraint( new PriorityConstraint( 50 ) );
        section1.getControl().setData( "title", "actions" ); //$NON-NLS-1$ //$NON-NLS-2$
        Composite body = section1.getBody();
//        body.setLayout( RowLayoutFactory.fillDefaults().type( SWT.VERTICAL ).fill( true ).justify( true ).create() );
//        body.setLayout( ColumnLayoutFactory.defaults().columns( 1, 1 ).spacing( 10 ).create() );

        actionBtns.add( createActionButton( body, i18n.get( "wasserquali" ), i18n.get( "wasserqualiTip" ),
                BatikPlugin.instance().imageForName( "resources/icons/waterdrop.png" ), //$NON-NLS-1$
                null,
                false, new Runnable() {
                    public void run() {
                        getContext().openPanel( WasserQualiPanel.ID );
                    }
        }));
        actionBtns.add( createActionButton( body, i18n.get( "dienstbarkeiten" ), i18n.get( "dienstbarkeitenTip" ),
                BatikPlugin.instance().imageForName( "resources/icons/letters.png" ), //$NON-NLS-1$
                AzvPlugin.ROLE_DIENSTBARKEITEN,
                true, new Runnable() {
                    public void run() {
                        try {
                            // create new case; commit/rollback inside CaseAction
                            mcase.set( AzvVorgang.newCase( repo.get(), "", "", CASE_DIENSTBARKEITEN ) );
                            getContext().openPanel( CasePanel.ID );
                        }
                        catch (Exception e) {
                            BatikApplication.handleError( i18n.get( "dienstbarkeitenFehler" ), e );
                        }
                    }
        }));

        IPanelSection section2 = tk.createPanelSection( parent, i18n.get( "antraegeTitle" ), Section.TITLE_BAR );
        section2.addConstraint( new PriorityConstraint( 0 ), new NeighborhoodConstraint( section1, Neighborhood.BOTTOM, 100 ) );
        section2.getControl().setData( "title", "actions" ); //$NON-NLS-1$ //$NON-NLS-2$
        body = section2.getBody();
//        body.setLayout( RowLayoutFactory.fillDefaults().type( SWT.VERTICAL ).spacing( 10 ).margins( 0, 10 ).create() );
//        body.setLayout( ColumnLayoutFactory.defaults().columns( 1, 1 )..spacing( 15 ).create() );
        
        actionBtns.add( createActionButton( body, i18n.get( "entsorgung" ), i18n.get( "entsorgungTip" ),
                BatikPlugin.instance().imageForName( "resources/icons/truck.png" ), //$NON-NLS-1$
                AzvPlugin.ROLE_ENTSORGUNG,
                true, new Runnable() {
                    public void run() {
                        mcase.set( AzvVorgang.newCase( repo.get(), "", "", CASE_ENTSORGUNG ) );
                        getContext().openPanel( CasePanel.ID );
                    }
        }));
        actionBtns.add( createActionButton( body, i18n.get( "schachtschein" ), i18n.get( "schachtscheinTip" ),
                BatikPlugin.instance().imageForName( "resources/icons/letter.png" ), //$NON-NLS-1$
                AzvPlugin.ROLE_SCHACHTSCHEIN,
                true, new Runnable() {
                    public void run() {
                        try {
                            // create new case; commit/rollback inside CaseAction
                            mcase.set( AzvVorgang.newCase( repo.get(), "", "", CASE_SCHACHTSCHEIN ) );
                            //newCase.put( KEY_USER, user.get().username().get() );
                            getContext().openPanel( CasePanel.ID );
                        }
                        catch (Exception e) {
                            BatikApplication.handleError( i18n.get( "schachtscheinFehler" ), e );
                        }
                    }
        }));
        actionBtns.add( createActionButton( body, i18n.get( "hydranten" ), i18n.get( "hydrantenTip" ),
                BatikPlugin.instance().imageForName( "resources/icons/fire.png" ), //$NON-NLS-1$
                AzvPlugin.ROLE_HYDRANTEN,
                true, new Runnable() {
                    public void run() {
                        getContext().openPanel( HydrantenPanel.ID );
                    }
        }));
        actionBtns.add( createActionButton( body, i18n.get( "leitungsauskunft" ), i18n.get( "leitungsauskunftTip" ),
                BatikPlugin.instance().imageForName( "resources/icons/pipelines.png" ), //$NON-NLS-1$
                AzvPlugin.ROLE_LEITUNGSAUSKUNFT,
                true, new Runnable() {
                    public void run() {
                        if (SecurityUtils.isUserInGroup( AzvPlugin.ROLE_LEITUNGSAUSKUNFT2 )) {
                            getContext().openPanel( LeitungsauskunftPanel.ID );
                        }
                        else {
                            try {
                                // create new case; commit/rollback inside CaseAction
                                mcase.set( AzvVorgang.newCase( repo.get(), "", "", CASE_LEITUNGSAUSKUNFT ) );
                                getContext().openPanel( CasePanel.ID );
                            }
                            catch (Exception e) {
                                BatikApplication.handleError( i18n.get( "leitungsauskunftFehler" ), e );
                            }
                        }
                    }
        }));
    }

    
    private Runnable          afterLogginTask;
    
    private int               layoutPrio = 100;
    
    private Control createActionButton( Composite client, String title, String tooltip, Image image, 
                final String role, final boolean checkLogin, final Runnable task ) {
        Composite container = tk.createComposite( client, SWT.BORDER );
        container.setData( WidgetUtil.CUSTOM_VARIANT, DesktopToolkit.CSS_FORM  );
        container.setLayoutData( new ConstraintData( 
                new MinWidthConstraint( 300, 1 ), new PriorityConstraint( layoutPrio-- ) ) );
//        container.setLayout( ColumnLayoutFactory.defaults().columns( 1, 1 ).spacing( 5 ).margins( 10 ).create() );
        container.setLayout( FormLayoutFactory.defaults().spacing( 5 ).margins( 10 ).create() );
        
        Label msg = tk.createLabel( container, tooltip, SWT.WRAP );
        int displayWidth = BatikApplication.sessionDisplay().getBounds().width;
        FormDataFactory.filled().clearBottom()
                // Texte brauchen 3 Zeilen wenn 2 Spalten oder Display zu schmal
                .height( displayWidth < 1100 || displayWidth > 1300 ? 50 : 35 ).applyTo( msg );
        
        Button result = tk.createButton( container, title, SWT.PUSH );
        result.setLayoutData( FormDataFactory.filled().top( msg ).create() );
        result.setImage( image );
        result.addMouseListener( new MouseAdapter() {
            public void mouseUp( MouseEvent e ) {
                checkLogin( task, checkLogin, role );
            }
        });
        return result;
    }


    private void checkLogin( final Runnable task, boolean checkLogin, final String role ) {
        Runnable checkPermissionsTask = new Runnable() {
            public void run() {
                if (role == null || SecurityUtils.isUserInGroup( role ) ) {
                    task.run();
                }
                else {
                    getSite().setStatus( new Status( IStatus.WARNING, AzvPlugin.ID, i18n.get( "keineFreigabe" ) ) );
                }
            }
        };

        // logged in -> run task
        if (!checkLogin || user.get() != null) {
            checkPermissionsTask.run();
        }
        // open LoginPanel and wait for user property to by set
        else {
            afterLogginTask = checkPermissionsTask;
            getContext().openPanel( AzvLoginPanel.ID );
        }
    }

    
    @EventHandler(display=true)
    void userLoggedIn( PropertyAccessEvent ev ) {
        if (casesSection != null) {
            return;
        }
        user.removeListener( StartPanel.this );

        createCasesSection( contents );

        // user -> show closed cases
        if (!(SecurityUtils.isAdmin() || SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA ))) {
            createClosedCasesSection( contents );
        }

        welcomeSection.dispose();
        welcomeSection = null;
        
        contents.layout( true );
        getSite().layout( true );

        // adjust context: username and preferences
        getSite().setTitle( i18n.get( "titleLoggedIn" ) ); //$NON-NLS-1$
        getSite().setIcon( BatikPlugin.instance().imageForName( "resources/icons/house.png" ) ); //$NON-NLS-1$

        User umuser = UserRepository.instance().findUser( user.get().getName() );
        getContext().setUserName( umuser != null ? umuser.name().get() : user.get().getName() );
        if (!SecurityUtils.isAdmin()) {
            getContext().addPreferencesAction( new Action( i18n.get( "persoenlicheDaten" ) ) {
                public void run() {
                    getContext().openPanel( UserSettingsPanel.ID );
                }
            });
        }
        
        if (afterLogginTask != null) {
            afterLogginTask.run();
        }
    }
}
