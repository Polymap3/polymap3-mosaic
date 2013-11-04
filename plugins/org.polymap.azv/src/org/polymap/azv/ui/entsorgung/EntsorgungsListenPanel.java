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

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.opengis.filter.Filter;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.qi4j.api.query.Query;

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

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PropertyAccessEvent;
import org.polymap.rhei.batik.app.BatikApplication;
import org.polymap.rhei.batik.toolkit.ConstraintData;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.MaxWidthConstraint;
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
    private ContextProperty<MosaicRepository2>  mosaicRepo;
    
    private AzvRepository                       azvRepo = AzvRepository.instance();

    private IPanelSection                       contents;
    
    
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
                    final InputDialog dialog = new InputDialog( BatikApplication.getShellToParentOn(), 
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
                            createListenSection( contents.getBody(), liste );
                            contents.getBody().layout();
                        }
                        catch (Exception e) {
                            BatikApplication.handleError( "Die neue Liste konnte nicht angelegt werden.", e );
                        }
                    }
                }
            });
            return true;
        }
        return false;
    }

    
    @EventHandler(display=true)
    protected void userLoggedIn( PropertyAccessEvent ev ) {
        if (SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA )
                && SecurityUtils.isUserInGroup( AzvPlugin.ROLE_ENTSORGUNG )) {
            getSite().setTitle( "Entsorgungslisten" );
        }
    }

    
    @Override
    public void createContents( Composite parent ) {
        contents = getSite().toolkit().createPanelSection( parent, null );
        contents.addConstraint( new MinWidthConstraint( 500, 1 ) )
                .addConstraint( new MaxWidthConstraint( 800, 1 ) );
        
        Query<Entsorgungsliste> listen = azvRepo.findEntities( Entsorgungsliste.class, null, 0, 100 );
        for (Entsorgungsliste liste : listen) {
            createListenSection( contents.getBody(), liste );
        }
    }
    
    
    protected void createListenSection( final Composite parent, final Entsorgungsliste liste ) {
        final IPanelSection section = getSite().toolkit().createPanelSection( parent, "Liste: " + liste.name().get() );
        section.getControl().setLayoutData( new ConstraintData( new PriorityConstraint( 5 ) ) );
        section.getBody().setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
        
        // toolbar
        Composite toolbar = getSite().toolkit().createComposite( section.getBody() );
        toolbar.setLayout( RowLayoutFactory.fillDefaults().spacing( 5 ).create() );
        Button btn = getSite().toolkit().createButton( toolbar, "Drucken und abschließen" );
        btn.setToolTipText( "Diese Liste als Excel/CSV exportieren und archivieren" );
        btn.addSelectionListener( new SelectionAdapter() {
            public void widgetSelected( SelectionEvent e ) {
                exportListe( liste, section );
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
        
        CasesTableViewer casesViewer = new CasesTableViewer( section.getBody(), mosaicRepo.get(), filter, SWT.NONE );
        casesViewer.getTable().setLayoutData( FormDataFactory.filled().top( toolbar ).height( 300 ).width( 400 ).create() );
//        casesViewer.addDoubleClickListener( new IDoubleClickListener() {
//            public void doubleClick( DoubleClickEvent ev ) {
//                IMosaicCase sel = Iterables.getOnlyElement( casesViewer.getSelected() );
//                log.info( "CASE: " + sel );
//                mcase.set( sel );
//                getContext().openPanel( CasePanel.ID );
//            }
//        });
        // filter liste
        final Set<String> ids = new HashSet( liste.mcaseIds().get() );
        casesViewer.addFilter( new CasesViewerFilter() {
            protected boolean apply( CasesTableViewer viewer, IMosaicCase mcase ) {
                // beantragte + in liste                 
                return ids.contains( mcase.getId() );
            }
        });
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
                            StringUtils.defaultString( mcase.get( "city" ) ), 
                            StringUtils.defaultString( mcase.get( "street" ) ), 
                            StringUtils.defaultString( mcase.get( "number" ) ), 
                            StringUtils.defaultString( mcase.get( "name" ) ), 
                            StringUtils.defaultString( mcase.get( "bemerkung" ) ) );
                    
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
