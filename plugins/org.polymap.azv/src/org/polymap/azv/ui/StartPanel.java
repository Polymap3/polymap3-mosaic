/*
 * polymap.org
 * Copyright 2013, Polymap GmbH. All rights reserved.
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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

import org.eclipse.rwt.RWT;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.model.Entity;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.security.UserPrincipal;

import org.polymap.atlas.AtlasPlugin;
import org.polymap.atlas.ContextProperty;
import org.polymap.atlas.DefaultPanel;
import org.polymap.atlas.IAppContext;
import org.polymap.atlas.IPanel;
import org.polymap.atlas.IPanelSite;
import org.polymap.atlas.PanelIdentifier;
import org.polymap.atlas.PropertyAccessEvent;
import org.polymap.atlas.app.AtlasApplication;
import org.polymap.atlas.toolkit.ConstraintData;
import org.polymap.atlas.toolkit.ILayoutContainer;
import org.polymap.atlas.toolkit.IPanelSection;
import org.polymap.atlas.toolkit.IPanelToolkit;
import org.polymap.atlas.toolkit.MaxWidthConstraint;
import org.polymap.atlas.toolkit.MinWidthConstraint;
import org.polymap.atlas.toolkit.PriorityConstraint;
import org.polymap.azv.model.AzvRepository;
import org.polymap.azv.model.Nutzer;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class StartPanel
        extends DefaultPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( StartPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "azvstart" );

    /** Just for convenience, same as <code>getSite().toolkit()</code>. */
    private IPanelToolkit                   tk;
    
    /** Set by the {@link LoginPanel}. */
    private ContextProperty<UserPrincipal>  user;
    
    /** Set by the {@link LoginPanel}. */
    private ContextProperty<Nutzer>         nutzer;
    
    //@Context(scope=AZVPlugin.PROPERTY_SCOPE)
    private ContextProperty<Entity>         entity;

    private List<Control>                   actionBtns = new ArrayList();

    private IPanelSection                   contents;

    private IPanelSection                   loginSection, casesSection, welcomeSection;

    
    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        this.tk = site.toolkit();
        return site.getPath().size() == 1;
    }


    @Override
    public PanelIdentifier id() {
        return ID;
    }

    
    protected boolean isAuthenticatedUser() {
        return user.get() != null ||
                nutzer.get() != null && nutzer.get().authentifiziert().get();
    }

    
    @Override
    public void createContents( Composite parent ) {
        getSite().setTitle( "AZV" );
        parent.setLayout( new FillLayout() );

        contents = tk.createPanelSection( parent, null );
        contents.addConstraint( new MinWidthConstraint( 500, 1 ) )
                .addConstraint( new MaxWidthConstraint( 800, 1 ) );
        
        Button btn1 = tk.createButton( contents.getBody(), "1", SWT.PUSH, SWT.WRAP );
        btn1.setLayoutData( new ConstraintData( 
                new PriorityConstraint( 1, 1 )/*, new MaxWidthConstraint( 300, 1 )*/ ) );
        Button btn2 = tk.createButton( contents.getBody(), "2 (xxxxxxxxxxxxxxxxxxxxx)", SWT.PUSH  );
        btn2.setLayoutData( new ConstraintData( new PriorityConstraint( 2, 1 ) ) );
        Button btn3 = tk.createButton( contents.getBody(), "3", SWT.PUSH  );
        
//        createWelcomeSection( contents );
//        createLoginSection( contents );
//        createCasesSection( contents );
//        createActionsSection( contents );
        
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
        welcomeSection.dispose();
        
        for (Control btn : actionBtns) {
            btn.setEnabled( isAuthenticatedUser() );
        }
    }
    
    
    protected void createWelcomeSection( ILayoutContainer parent ) {
        welcomeSection = tk.createPanelSection( parent, "Willkommen im Web-Portal der GKU" );
        String msg = "Sie können verschiedene Vorgänge auslösen und Anträge stellen. Sie werden dann durch weitere Eingaben geführt. "
                + "Außerdem können Sie den Stand von bereits ausgelösten Vorgängen hier überprüfen.";
        tk.createLabel( welcomeSection.getBody(), msg, SWT.CENTER, SWT.SHADOW_IN, SWT.WRAP )
                .setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
    }

    
    protected void createLoginSection( ILayoutContainer parent ) {
        loginSection = tk.createPanelSection( parent, "Anmelden" );
        loginSection.addConstraint( new MinWidthConstraint( 300, 0 ) );
        
        new LoginPanel.LoginForm( nutzer, user ).createContents( loginSection );
    }

    
    protected void createCasesSection( IPanelSection parent ) {
        casesSection = tk.createPanelSection( parent, "Aktuelle Vorgänge" );
        tk.createLabel( casesSection.getBody(), "Keine Vorgänge." );
    }
    
    
    protected IPanelSection createActionsSection( ILayoutContainer parent ) {
        IPanelSection section = tk.createPanelSection( parent, "Anträge und Auskünfte", Section.TITLE_BAR );
        Composite body = section.getBody();

        createActionButton( body, "Wasserqualität", 
                "Auskunftsersuchen zu Wasserhärten und Wasserqualitäten",
                AtlasPlugin.instance().imageForName( "icons/waterdrop.png" ),
                new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
        });
        actionBtns.add( createActionButton( body, "Entsorgung", 
                "Verwaltung und Organisation der bedarfsgerechten Entsorgung von dezentralen Abwasserbeseitigungsanlagen",
                AtlasPlugin.instance().imageForName( "icons/truck.png" ),
                new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
        }));
        actionBtns.add( createActionButton( body, "Hydranten", "Hydrantentpläne",
                AtlasPlugin.instance().imageForName( "icons/fire.png" ),
                new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
        }));
        actionBtns.add( createActionButton( body, "Leitungsauskunft", 
                "Auskunftsersuchen zum Bestand von technischen Anlagen der Wasserver- und Abwasserentsorgung (Leitungen, WW, KA, PW, usw.)",
                AtlasPlugin.instance().imageForName( "icons/pipelines.png" ),
                new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
        }));
        actionBtns.add( createActionButton( body, "Schachtscheine", 
                "Antrag für Schachtscheine",
                AtlasPlugin.instance().imageForName( "icons/letter.png" ),
                new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                try {
                    log.info( "Schachtschein!" );
                    entity.set( AzvRepository.instance().newSchachtschein() );
                    getContext().openPanel( SchachtscheinPanel.ID );
                }
                catch (Exception e) {
                    AtlasApplication.handleError( "Schachtschein konnte nicht angelegt werden.", e );
                }
            }
        }));
        actionBtns.add( createActionButton( body, "Dienstbarkeiten", 
                "Auskunftsersuchen zu dinglichen Rechten auf privaten und öffentlichen Grundstücken (Leitungsrechte, beschränkte persönliche Dienstbarkeiten).",
                AtlasPlugin.instance().imageForName( "icons/letters.png" ),
                new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                // XXX Auto-generated method stub
                throw new RuntimeException( "not yet implemented." );
            }
        }));
        for (Control btn : actionBtns) {
            btn.setEnabled( isAuthenticatedUser() );
        }
        return section;
    }

    protected Control createActionButton( Composite client, String title, String tooltip, Image image, final SelectionListener l ) {
        Button result = tk.createButton( client, title, SWT.PUSH, SWT.LEFT, SWT.FLAT );
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
        return result;
    }
}
