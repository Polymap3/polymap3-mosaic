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

import static org.polymap.mosaic.ui.MosaicUiPlugin.ff;

import java.util.ArrayList;
import java.util.List;

import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;

import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.data.ui.featuretable.FeatureTableFilterBar;
import org.polymap.core.data.ui.featuretable.FeatureTableSearchField;
import org.polymap.core.model.Entity;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.entity.IEntityStateListener;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.security.UserPrincipal;
import org.polymap.core.ui.ColumnLayoutFactory;
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
import org.polymap.rhei.batik.PropertyAccessEvent;
import org.polymap.rhei.batik.app.BatikApplication;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.IPanelToolkit;
import org.polymap.rhei.batik.toolkit.NeighborhoodConstraint;
import org.polymap.rhei.batik.toolkit.NeighborhoodConstraint.Neighborhood;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.UserRepository;
import org.polymap.rhei.um.ui.LoginPanel;
import org.polymap.rhei.um.ui.LoginPanel.LoginForm;
import org.polymap.rhei.um.ui.UserSettingsPanel;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.ui.entsorgung.EntsorgungCasesDecorator;
import org.polymap.azv.ui.leitungsauskunft.LeitungsauskunftCasesDecorator;
import org.polymap.azv.ui.leitungsauskunft.LeitungsauskunftPanel;
import org.polymap.azv.ui.nutzerregistrierung.NutzerCasesDecorator;
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

    public static final PanelIdentifier ID = new PanelIdentifier( "start" );

    public static final IMessages       i18n = Messages.forPrefix( "StartPanel" );

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
    
    private List<ICasesViewerDecorator>     casesViewerDecorators = new ArrayList();
    
    private IEntityStateListener            casesListener;


    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        if (site.getPath().size() == 1) {
            this.tk = site.toolkit();
            this.repo.set( MosaicRepository2.instance() );
            site.setTitle( i18n.get( "title" ) );
            site.setIcon( BatikPlugin.instance().imageForName( "resources/icons/house.png" ) );

            casesViewerDecorators.add( context.propagate( new MitarbeiterCasesDecorator() ) );
            casesViewerDecorators.add( context.propagate( new NutzerCasesDecorator() ) );
            casesViewerDecorators.add( context.propagate( new SchachtscheinCasesDecorator() ) );
            casesViewerDecorators.add( context.propagate( new LeitungsauskunftCasesDecorator() ) );
            casesViewerDecorators.add( context.propagate( new EntsorgungCasesDecorator() ) );
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

        createLoginSection( contents )
                .addConstraint( new PriorityConstraint( 10 ), AzvPlugin.MIN_COLUMN_WIDTH );

        createActionsSection( contents )
                .addConstraint( new PriorityConstraint( 0 ) );
        
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
            String role = (String)btn.getData( "role" );
            btn.setEnabled( role == null || SecurityUtils.isUserInGroup( role ) );
        }
    }
    
    
    protected IPanelSection createWelcomeSection( Composite parent ) {
        welcomeSection = tk.createPanelSection( parent, i18n.get( "welcomeTitle" ) );
        welcomeSection.getControl().setData( "title", "welcome" );
        welcomeSection.getBody().setLayout( new FillLayout() );
        tk.createFlowText( welcomeSection.getBody(), i18n.get( "welcomeText" ) );
        return welcomeSection;
    }

    
    protected IPanelSection createLoginSection( Composite parent ) {
        loginSection = tk.createPanelSection( parent, i18n.get( "loginTitle" ) );
        loginSection.getControl().setData( "title", "login" );
        loginSection.getBody().setLayout( new FillLayout() );
        
        LoginForm loginForm = new LoginPanel.LoginForm( getContext(), getSite(), user ) {
            @Override
            protected boolean login( String name, String passwd ) {
                if (super.login( name, passwd )) {
                    loginSection.dispose();
                    createCasesSection( contents );
                    
                    // user -> show closed cases
                    if (!(SecurityUtils.isAdmin() || SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA ))) {
                        createClosedCasesSection( contents );
                    }
                    
                    contents.layout( true );
                    getSite().layout( true );
                
                    // adjust context: username and preferences
                    getSite().setTitle( i18n.get( "titleLoggedIn" ) );
                    getSite().setIcon( BatikPlugin.instance().imageForName( "resources/icons/house.png" ) );
                    
                    User umuser = UserRepository.instance().findUser( name );
                    getContext().setUserName( umuser != null ? umuser.name().get() : name );
                    if (!SecurityUtils.isAdmin()) {
                        getContext().addPreferencesAction( new Action( "Persönliche Daten" ) {
                            public void run() {
                                getContext().openPanel( UserSettingsPanel.ID );
                            }
                        });
                    }
                    getSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, "Sie haben sich erfolgreich angemeldet." ) );
                    return true;
                }
                else {
                    getSite().setStatus( new Status( IStatus.WARNING, AzvPlugin.ID, "Nutzername oder Passwort sind nicht korrekt." ) );
                    return false;
                }
            }
        };
        loginForm.setShowRegisterLink( true );
        loginForm.setShowStoreCheck( true );
        loginForm.setShowLostLink( true );
        
        Composite form = new Composite( loginSection.getBody(), SWT.BORDER ); 
        loginForm.createContents( form );
        
//        Deque<Control> deque = new LinkedList();
//        deque.push( form );
//        while (!deque.isEmpty()) {
//            Control control = deque.pop();
////            if (control instanceof Label) {
////                control.setFont( font );
////            }
//            control.setData( WidgetUtil.CUSTOM_VARIANT, null );
//            if (control instanceof Composite) {
//                for (Control child : ((Composite)control).getChildren()) {
//                    deque.push( child );
//                }
//            }
//        }
        return loginSection;
    }

    
    protected void createCasesSection( Composite parent ) {
        casesSection = tk.createPanelSection( parent, "Aktuelle Vorgänge" );
        casesSection.addConstraint( new PriorityConstraint( 5 ), AzvPlugin.MIN_COLUMN_WIDTH );
        casesSection.getBody().setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
        
        Filter filter = ff.equals( ff.property( "status" ), ff.literal( IMosaicCaseEvent.TYPE_OPEN ) );
        casesViewer = new CasesTableViewer( casesSection.getBody(), repo.get(), filter, SWT.NONE );
        casesViewer.addColumn( new AzvStatusCaseTableColumn( repo.get() ) );
        casesViewer.getTable().setLayoutData( FormDataFactory.filled().top( -1 ).height( 400 ).create() );
        casesViewer.getColumn( "created" ).sort( SWT.UP );
        casesViewer.addDoubleClickListener( new IDoubleClickListener() {
            public void doubleClick( DoubleClickEvent ev ) {
                IMosaicCase sel = Iterables.getOnlyElement( casesViewer.getSelected() );
                log.info( "CASE: " + sel );
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
        
        // searchField
        FeatureTableSearchField searchField = new FeatureTableSearchField( casesViewer, casesSection.getBody(), casesViewer.propertyNames() );
        Composite searchCtrl = searchField.getControl();
        searchCtrl.setLayoutData( FormDataFactory.filled()
                .height( 27 ).bottom( casesViewer.getTable() ).left( filterBar.getControl() ).create() );
        
//        for (Control child : searchCtrl.getChildren()) {
//            if (child instanceof Button) {
//                ((Button)child).setImage( BatikPlugin.instance().imageForName( "resources/icons/close.png" ) );
//                ((Button)child).setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_DISCARD );                
//            }
//        }
    }

    
    protected void createClosedCasesSection( Composite parent ) {
        // normal user -> propable just a few open cases
        casesViewer.getTable().setLayoutData( FormDataFactory.filled().top( -1 ).height( 150 ).create() );

        IPanelSection closedCasesSection = tk.createPanelSection( parent, "Bearbeitete Vorgänge" );
        closedCasesSection.addConstraint( AzvPlugin.MIN_COLUMN_WIDTH,
                new PriorityConstraint( 0 ),
                new NeighborhoodConstraint( casesSection.getControl(), Neighborhood.BOTTOM, 1 ) ); 
        closedCasesSection.getBody().setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
        
        Filter filter = ff.equals( ff.property( "status" ), ff.literal( IMosaicCaseEvent.TYPE_CLOSED ) );
        final CasesTableViewer closedCasesViewer = new CasesTableViewer( closedCasesSection.getBody(), repo.get(), filter, SWT.NONE );
        closedCasesViewer.addColumn( new AzvStatusCaseTableColumn( repo.get() ) );
        closedCasesViewer.getTable().setLayoutData( FormDataFactory.filled().top( -1 ).height( 150 ).create() );
        closedCasesViewer.addDoubleClickListener( new IDoubleClickListener() {
            public void doubleClick( DoubleClickEvent ev ) {
                IMosaicCase sel = Iterables.getOnlyElement( closedCasesViewer.getSelected() );
                log.info( "CASE: " + sel );
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
        closedCasesViewer.getColumn( "created" ).sort( SWT.UP );
    }
    
    
    protected IPanelSection createActionsSection( Composite parent ) {
        IPanelSection section = tk.createPanelSection( parent, "Anträge und Auskünfte", Section.TITLE_BAR );
        section.getControl().setData( "title", "actions" );
        Composite body = section.getBody();
        body.setLayout( ColumnLayoutFactory.defaults().columns( 1, 2 ).spacing( 10 ).create() );

        actionBtns.add( createActionButton( body, "Wasserqualität", 
                "Auskunftsersuchen zu Wasserhärten und Wasserqualitäten",
                BatikPlugin.instance().imageForName( "resources/icons/waterdrop.png" ),
                null,
                new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                getContext().openPanel( WasserQualiPanel.ID );
            }
        }));
        actionBtns.add( createActionButton( body, "Entsorgung", 
                "Antrag auf Entsorgung von dezentralen Abwasserbeseitigungsanlagen",
                BatikPlugin.instance().imageForName( "resources/icons/truck.png" ),
                null, //AzvPlugin.ROLE_ENTSORGUNG,
                new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                IMosaicCase newCase = repo.get().newCase( "", "" );
                newCase.addNature( AzvPlugin.CASE_ENTSORGUNG );
                mcase.set( newCase );
                getContext().openPanel( CasePanel.ID );
            }
        }));
        actionBtns.add( createActionButton( body, "Schachtschein", 
                "Antrag für einen Schachtschein",
                BatikPlugin.instance().imageForName( "resources/icons/letter.png" ),
                AzvPlugin.ROLE_SCHACHTSCHEIN,
                new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                try {
                    // create new case; commit/rollback inside CaseAction
                    IMosaicCase newCase = repo.get().newCase( "", "" );
                    newCase.addNature( AzvPlugin.CASE_SCHACHTSCHEIN );
                    //newCase.put( KEY_USER, user.get().username().get() );
                    mcase.set( newCase );
                    getContext().openPanel( CasePanel.ID );
                }
                catch (Exception e) {
                    BatikApplication.handleError( "Schachtschein konnte nicht angelegt werden.", e );
                }
            }
        }));
        actionBtns.add( createActionButton( body, "Hydranten", "Hydrantentpläne",
                BatikPlugin.instance().imageForName( "resources/icons/fire.png" ),
                AzvPlugin.ROLE_HYDRANTEN,
                new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                try {
                    IMosaicCase newCase = repo.get().newCase( "Hydrantenauskunft", "" );
                    newCase.addNature( AzvPlugin.CASE_HYDRANTEN );
                    mcase.set( newCase );
                    getContext().openPanel( CasePanel.ID );
                }
                catch (Exception e) {
                    BatikApplication.handleError( "Schachtschein konnte nicht angelegt werden.", e );
                }
            }
        }));
        actionBtns.add( createActionButton( body, "Dienstbarkeiten", 
                "Auskunftsersuchen zu dinglichen Rechten auf privaten und öffentlichen Grundstücken (Leitungsrechte, beschränkte persönliche Dienstbarkeiten).",
                BatikPlugin.instance().imageForName( "resources/icons/letters.png" ),
                null,  //AzvPlugin.ROLE_DIENSTBARKEITEN,
                new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                getSite().setStatus( new Status( IStatus.INFO, AzvPlugin.ID, "Noch keine Dienstbarkeiten verfügbar." ) );
            }
        }));
        actionBtns.add( createActionButton( body, "Leitungsauskunft", 
                "Auskunftsersuchen zum Bestand von technischen Anlagen der Wasserver- und Abwasserentsorgung (Leitungen, WW, KA, PW, usw.)",
                BatikPlugin.instance().imageForName( "resources/icons/pipelines.png" ),
                AzvPlugin.ROLE_LEITUNGSAUSKUNFT,
                new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                if (SecurityUtils.isUserInGroup( AzvPlugin.ROLE_LEITUNGSAUSKUNFT2 )) {
                    getContext().openPanel( LeitungsauskunftPanel.ID );
                }
                else {
                    try {
                        // create new case; commit/rollback inside CaseAction
                        IMosaicCase newCase = repo.get().newCase( "", "" );
                        newCase.addNature( AzvPlugin.CASE_LEITUNGSAUSKUNFT );
                        //newCase.put( KEY_USER, user.get().username().get() );
                        mcase.set( newCase );
                        getContext().openPanel( CasePanel.ID );
                    }
                    catch (Exception e) {
                        BatikApplication.handleError( "Schachtschein konnte nicht angelegt werden.", e );
                    }
                }
            }
        }));
        return section;
    }

    
    private Control createActionButton( Composite client, String title, String tooltip, Image image, String role, final SelectionListener l ) {
        Button result = tk.createButton( client, title, SWT.PUSH, SWT.LEFT );
        result.setToolTipText( tooltip );
        result.setImage( image );
        //result.setLayoutData( RowDataFactory.swtDefaults().create() );
        result.addMouseListener( new MouseListener() {
            public void mouseUp( MouseEvent e ) {
                l.widgetSelected( null );
            }
            public void mouseDown( MouseEvent e ) {
            }
            public void mouseDoubleClick( MouseEvent e ) {
                l.widgetSelected( null );
            }
        });
        if (role != null) {
            result.setEnabled( SecurityUtils.isUserInGroup( role ) );
            result.setData( "role", role );
        }
        return result;
    }
}
