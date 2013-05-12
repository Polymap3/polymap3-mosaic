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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.rwt.RWT;

import org.eclipse.jface.layout.RowLayoutFactory;

import org.eclipse.ui.forms.widgets.Section;

import org.polymap.core.model.Entity;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.security.UserPrincipal;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.atlas.AtlasPlugin;
import org.polymap.atlas.ContextProperty;
import org.polymap.atlas.DefaultPanel;
import org.polymap.atlas.IAppContext;
import org.polymap.atlas.IAtlasToolkit;
import org.polymap.atlas.IPanel;
import org.polymap.atlas.IPanelSite;
import org.polymap.atlas.PanelIdentifier;
import org.polymap.atlas.PropertyAccessEvent;
import org.polymap.atlas.app.AtlasApplication;
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
    private IAtlasToolkit                   tk;
    
    private ContextProperty<UserPrincipal>  user;
    
    private ContextProperty<Nutzer>         nutzer;
    
    //@Context(scope=AZVPlugin.PROPERTY_SCOPE)
    private ContextProperty<Entity>         entity;

    private Section                         mosaicSection;

    
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


    @Override
    public void createContents( Composite parent ) {
        getSite().setTitle( "AZV" );
        parent.setLayout( FormLayoutFactory.defaults().margins( DEFAULTS_SPACING ).create() );

        Composite contents = tk.createComposite( parent );
        contents.setLayoutData( FormDataFactory.offset( 0 ).left( 25 ).right( 75 ).width( 500 ).create() );
        contents.setLayout( FormLayoutFactory.defaults().spacing( DEFAULTS_SPACING*2 ).create() );
        
        Composite welcome = createWelcomeSection( contents );
        welcome.setLayoutData( FormDataFactory.filled().bottom( -1 ).create() );

        mosaicSection = getSite().toolkit().createSection( contents, "Laufende Vorgänge", Section.TITLE_BAR );
        mosaicSection.setLayoutData( FormDataFactory.filled().bottom( -1 ).top( welcome ).create() );
        Composite client = (Composite)mosaicSection.getClient();
        if (user.get() == null) {
            fillLoginSection( client );
        }
        else {
            fillCaseSection( client );
        }
        // listen to PropertyAccessEvent
        EventManager.instance().subscribe( this, new EventFilter<PropertyAccessEvent>() {
            public boolean apply( PropertyAccessEvent input ) {
                return input.getType() == PropertyAccessEvent.TYPE.SET
                        && input.getSource().getDeclaredType().equals( UserPrincipal.class );
            }
        });

        Composite actions = createActionsSection( contents );
        actions.setLayoutData( FormDataFactory.filled().top( mosaicSection ).create() );
    }

    
    @EventHandler(display=true)
    protected void handleEvent( PropertyAccessEvent ev ) {
        Composite client = (Composite)mosaicSection.getClient();
        for (Control child : client.getChildren()) {
            child.dispose();
        }
        if (user.get() == null) {
            fillLoginSection( client );
        }
        else {
            fillCaseSection( client );
        }
        client.layout( true );
    }
    
    
    protected Composite createWelcomeSection( Composite parent ) {
        Composite section = tk.createComposite( parent );
        section.setLayout( new FillLayout() );
        String msg = "<b>Willkommen im Web-Portal der GKU</b><br/><br/>"
                + "Sie können verschiedene Vorgänge auslösen und Anträge stellen. Sie werden dann durch weitere Eingaben geführt. "
                + "Außerdem können Sie den Stand von bereits ausgelösten Vorgängen hier überprüfen.";
        tk.createLabel( section, msg, SWT.CENTER, SWT.SHADOW_IN, SWT.WRAP )
                .setData( RWT.MARKUP_ENABLED, Boolean.TRUE );
        return section;
    }

    
    protected void fillLoginSection( Composite client ) {
        client.setLayout( RowLayoutFactory.fillDefaults().fill( true ).justify( true ).create() );
        
        Button loginBtn = tk.createButton( client, "Anmelden", SWT.PUSH );
        loginBtn.setToolTipText( "Als bereits registrierter Nutzer im Web-Portal der GKU anmelden" );
        loginBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                getContext().openPanel( LoginPanel.ID );
            }
        });
        Button registerBtn = tk.createButton( client, "Registrieren", SWT.PUSH );
        registerBtn.setToolTipText( "Registrieren Sie sich als Nutzer des GKU Web-Portals" );
        registerBtn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                nutzer.set( AzvRepository.instance().newNutzer() );
                getContext().openPanel( NutzerPanel.ID );
            }
        });
    }

    
    protected void fillCaseSection( Composite client ) {
        client.setLayout( RowLayoutFactory.fillDefaults().fill( true ).type( SWT.VERTICAL ).create() );
        tk.createLabel( client, "Keine Vorgänge." );
    }
    
    
    protected Section createActionsSection( Composite parent ) {
        Section section = getSite().toolkit().createSection( parent, "Anträge und Auskünfte", Section.TITLE_BAR );
        Composite client = (Composite)section.getClient();

        client.setLayout( ColumnLayoutFactory.defaults().columns( 1, 1 ).margins( 5, 5 ).spacing( 5 ).create() );

        createActionButton( client, "Auskunft Wasserhärten und Qualitäten", "Auskunftsersuchen zu Wasserhärten und Wasserqualitäten",
                new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent e ) {
                        // XXX Auto-generated method stub
                        throw new RuntimeException( "not yet implemented." );
                    }
                }
        );
        createActionButton( client, "Entsorgung von Abwasserbeseitigungsanlagen", "Verwaltung und Organisation der bedarfsgerechten Entsorgung von dezentralen Abwasserbeseitigungsanlagen",
                new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent e ) {
                        // XXX Auto-generated method stub
                        throw new RuntimeException( "not yet implemented." );
                    }
                }
        );
        createActionButton( client, "Hydrantentmanagement", "Hydrantentpläne",
                new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent e ) {
                        // XXX Auto-generated method stub
                        throw new RuntimeException( "not yet implemented." );
                    }
                }
        );
        createActionButton( client, "Auskunft zum technischen Anlagen", "Auskunftsersuchen zum Bestand von technischen Anlagen der Wasserver- und Abwasserentsorgung (Leitungen, WW, KA, PW, usw.)",
                new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent e ) {
                        // XXX Auto-generated method stub
                        throw new RuntimeException( "not yet implemented." );
                    }
                }
        );
        createActionButton( client, "Antrag für Schachtscheine", "Antrag für Schachtscheine",
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
                }
        );
        createActionButton( client, "Auskunft zu dinglichen Rechten", "Auskunftsersuchen zu dinglichen Rechten auf privaten und öffentlichen Grundstücken (Leitungsrechte, beschränkte persönliche Dienstbarkeiten).",
                new SelectionAdapter() {
                    public void widgetSelected( SelectionEvent e ) {
                        // XXX Auto-generated method stub
                        throw new RuntimeException( "not yet implemented." );
                    }
                }
        );
        return section;
    }

    
    protected Control createActionButton( Composite client, String title, String tooltip, final SelectionListener l ) {
        Button result = tk.createButton( client, title+"...", SWT.PUSH, SWT.LEFT, SWT.FLAT );
        result.setToolTipText( tooltip );
        result.setImage( AtlasPlugin.instance().imageForName( "icons/run.gif" ) );
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
