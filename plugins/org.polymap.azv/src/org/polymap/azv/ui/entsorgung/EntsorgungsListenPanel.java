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
package org.polymap.azv.ui.entsorgung;

import static org.polymap.mosaic.ui.MosaicUiPlugin.ff;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.opengis.filter.Filter;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;

import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.rwt.widgets.ExternalBrowser;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.window.Window;

import org.polymap.core.data.operation.DownloadServiceHandler;
import org.polymap.core.data.operation.DownloadServiceHandler.ContentProvider;
import org.polymap.core.qi4j.QiModule.EntityCreator;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
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
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.MinWidthConstraint;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.um.ui.LoginPanel;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.model.AzvRepository;
import org.polymap.azv.model.Entsorgungsliste;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicCaseEvent;
import org.polymap.mosaic.server.model2.MosaicCase2;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.CasePanel;
import org.polymap.mosaic.ui.casestable.CasesTableViewer;
import org.polymap.mosaic.ui.casestable.CasesViewerFilter;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class EntsorgungsListenPanel
        extends DefaultPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( EntsorgungsListenPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "azv", "entsorgungslisten" );
    
    /** Set by the {@link LoginPanel}. */
    @Context(scope="org.polymap.azv.ui")
    private ContextProperty<UserPrincipal>      user;
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>        mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2>  mosaicRepo;
    
    private AzvRepository                       azvRepo = AzvRepository.instance();

    private Composite                           contents;

    private List<CasesTableViewer>              casesViewers = new ArrayList();

    private Object panelListener;
    
    
    @Override
    public PanelIdentifier id() {
        return ID;
    }


    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        if (site.getPath().size() == 1) {
            
            // wait for user to log in, then check permission
            site.setTitle( "" );
            user.addListener( this, new EventFilter<PropertyAccessEvent>() {
                public boolean apply( PropertyAccessEvent input ) {
                    return input.getType() == PropertyAccessEvent.TYPE.SET;
                }
            });

            site.addToolbarAction( new Action( "Neue Liste" ) {
                public void run() {
                    openNeueListeDialog();
                }
            });
            return true;
        }
        return false;
    }

    
    @Override
    public void dispose() {
        super.dispose();
        mcase.set( null );
        if (panelListener != null) {
            getContext().removeEventHandler( panelListener );
            panelListener = null;
        }
    }


    @EventHandler(display=true)
    protected void userLoggedIn( PropertyAccessEvent ev ) {
        if (SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA )
                && SecurityUtils.isUserInGroup( AzvPlugin.ROLE_ENTSORGUNG )) {
            getSite().setTitle( "Entsorgungslisten" );
            getSite().setIcon( BatikPlugin.instance().imageForName( "resources/icons/truck-filter.png" ) );
        }
    }

    
    protected void openNeueListeDialog() {
        final InputDialog dialog = new InputDialog( BatikApplication.shellToParentOn(), 
                "Eine neue Liste anlegen", "Name der Entsorgungsliste", "...", null );
        dialog.setBlockOnOpen( true );
        if (dialog.open() == Window.OK) {
            try {
                Entsorgungsliste liste = azvRepo.newEntity( Entsorgungsliste.class, null, new EntityCreator<Entsorgungsliste>() {
                    public void create( Entsorgungsliste prototype ) throws Exception {
                        prototype.name().set( dialog.getValue() );
                        prototype.angelegtAm().set( new Date() );
                    }
                });
                azvRepo.commitChanges();
                createListenSection( contents, liste );
                contents.layout();
                getSite().layout( true );
            }
            catch (Exception e) {
                BatikApplication.handleError( "Die neue Liste konnte nicht angelegt werden.", e );
            }
        }
    }
    
    
    @Override
    public void createContents( Composite parent ) {
        contents = parent;
        Query<Entsorgungsliste> listen = azvRepo.findEntities( Entsorgungsliste.class, null, 0, 100 );
        for (Entsorgungsliste liste : listen) {
            createListenSection( contents, liste );
        }
        
        if (panelListener == null) {
            panelListener = new Object() {
                @EventHandler(display=true)
                protected void panelActivated( PanelChangeEvent ev ) {
                    for (CasesTableViewer viewer : casesViewers) {
                        if (!viewer.getControl().isDisposed()) {
                            viewer.refresh();
                        }
                    }
                }
            };
            getContext().addEventHandler( panelListener, new EventFilter<PanelChangeEvent>() {
                public boolean apply( PanelChangeEvent input ) {
                    return input.getSource() == EntsorgungsListenPanel.this
                            && input.getType() == PanelChangeEvent.TYPE.ACTIVATING;
                }
            });
        }
    }
    
    
    protected void createListenSection( final Composite parent, final Entsorgungsliste liste ) {
        final IPanelSection section = getSite().toolkit().createPanelSection( parent, "Liste: " + liste.name().get() );
        int prio = (int)liste.angelegtAm().get().getTime();
        section.addConstraint( new PriorityConstraint( prio ), new MinWidthConstraint( 500, 1 ) );
        section.getBody().setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
        
        // toolbar
        Composite toolbar = getSite().toolkit().createComposite( section.getBody() );
        toolbar.setLayout( RowLayoutFactory.fillDefaults().spacing( 5 ).create() );
        
        Button btn = getSite().toolkit().createButton( toolbar, "Drucken und abschließen" );
        btn.setToolTipText( "Diese Liste als Excel/CSV exportieren und archivieren" );
        btn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                exportListe( liste, section );
                getSite().layout( true );
            }
        });

        btn = getSite().toolkit().createButton( toolbar, "Geschlossen", SWT.CHECK );
        btn.setToolTipText( "Diese Liste ist geschlossen und kann nicht bebucht werden" );
        btn.setSelection( BooleanUtils.isTrue( liste.geschlossen().get() ) );
        btn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent ev ) {
                try {
                    liste.geschlossen().set( ((Button)ev.getSource()).getSelection() );
                    azvRepo.commitChanges();
                }
                catch (Exception e) {
                    //azvRepo.revertChanges();
                    BatikApplication.handleError( "Liste konnte nicht geändert werden.", e );
                }
            }
        });
        
//        btn = getSite().toolkit().createButton( toolbar, "Liste löschen" );
//        btn.setToolTipText( "Diese Liste komplett löschen" );
//        btn.setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_DISCARD );
//        btn.addSelectionListener( new SelectionAdapter() {
//            public void widgetSelected( SelectionEvent ev ) {
//                try {
//                    azvRepo.removeEntity( liste );
//                    azvRepo.commitChanges();
//                    
//                    section.dispose();
//                    parent.layout();
//                }
//                catch (Exception e) {
//                    BatikApplication.handleError( "Liste konnte nicht gelöscht werden.", e );
//                }
//            }
//        });
        
        // viewer
        Filter filter = ff.and( 
                ff.equals( ff.property( "status" ), ff.literal( IMosaicCaseEvent.TYPE_OPEN ) ),
                ff.equals( ff.property( "natures" ), ff.literal( AzvPlugin.CASE_ENTSORGUNG ) ) );
        
        final CasesTableViewer casesViewer = new CasesTableViewer( section.getBody(), mosaicRepo.get(), filter, SWT.NONE );
        casesViewer.getTable().setLayoutData( FormDataFactory.filled().top( toolbar ).height( 300 ).width( 400 ).create() );
        casesViewer.addDoubleClickListener( new IDoubleClickListener() {
            public void doubleClick( DoubleClickEvent ev ) {
                IMosaicCase sel = Iterables.getOnlyElement( casesViewer.getSelected() );
                log.info( "CASE: " + sel );
                mcase.set( sel );
                getContext().openPanel( CasePanel.ID );
            }
        });
        // filter liste
        casesViewer.addFilter( new CasesViewerFilter() {
            protected boolean apply( CasesTableViewer viewer, IMosaicCase mcase ) {
                // beantragte + in liste
                // no cache this may change as the liste changes
                return liste.mcaseIds().get().contains( mcase.getId() );
            }
        });
        casesViewers.add( casesViewer );
    }


    protected void exportListe( final Entsorgungsliste liste, final IPanelSection section ) {
        final String filename = liste.name().get() + ".csv";

        String url = DownloadServiceHandler.registerContent( new ContentProvider() {
            Display display = Polymap.getSessionDisplay();
            @Override
            public InputStream getInputStream() throws Exception {
                CsvPreference prefs = new CsvPreference( '"', ';', "\r\n" );
                ByteArrayOutputStream buf = new ByteArrayOutputStream( 32*1024 );
                Writer writer = new OutputStreamWriter( buf, "ISO-8859-1" );
                CsvListWriter csvWriter = new CsvListWriter( writer, prefs );
                
                csvWriter.writeHeader( "Ort", "Straße", "Nr.", "Name", "Bemerkung" );
                for (String id : liste.mcaseIds().get()) {
                    MosaicCase2 mcase = mosaicRepo.get().entity( MosaicCase2.class, id );
                    csvWriter.write( 
                            StringUtils.defaultString( mcase.get( EntsorgungCaseAction.KEY_CITY ) ), 
                            StringUtils.defaultString( mcase.get( EntsorgungCaseAction.KEY_STREET ) ), 
                            StringUtils.defaultString( mcase.get( EntsorgungCaseAction.KEY_NUMBER ) ), 
                            StringUtils.defaultString( mcase.get( EntsorgungCaseAction.KEY_NAME ) ), 
                            StringUtils.defaultString( mcase.get( EntsorgungCaseAction.KEY_BEMERKUNG ) ) );
                    
                    mosaicRepo.get().closeCase( mcase, "Gedruckt", "Gedruckt in Liste: " + liste.name().get() );
                }
                csvWriter.close();
                log.info( "CSV: " + buf.toString() );
                return new ByteArrayInputStream( buf.toByteArray() );
            }
            @Override
            public String getFilename() {
                return filename;
            }
            @Override
            public String getContentType() {
                return "application/csv";
            }
            @Override
            public boolean done( boolean success ) {
                if (success) {
                    try {
                        mosaicRepo.get().commitChanges();
                        
                        azvRepo.removeEntity( liste );
                        azvRepo.commitChanges();
                        
                        display.asyncExec( new Runnable() {
                            public void run() {
                                section.getBody().getParent().getParent().layout();
                                section.dispose();
                            }
                        });
                    }
                    catch (Exception e) {
                        mosaicRepo.get().rollbackChanges();
                        azvRepo.revertChanges();
                        BatikApplication.handleError( "Liste konnte nicht gelöscht werden.", e );
                    }                    
                }
                return true;
            }
        });
        ExternalBrowser.open( "download_window", url,
                ExternalBrowser.NAVIGATION_BAR | ExternalBrowser.STATUS );
    }

}
