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
package org.polymap.azv.ui;

import java.util.Comparator;

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.ImmutableList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.deferred.DeferredContentProvider;
import org.eclipse.jface.viewers.deferred.SetModel;

import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.upload.Upload;
import org.polymap.core.ui.upload.IUploadHandler;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;
import org.polymap.rhei.um.User;

import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicDocument;
import org.polymap.mosaic.server.model2.MosaicCase2;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.DefaultCaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseAction;

/**
 * Dokumente hochladen an Vorgang.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DokumenteCaseAction
        extends DefaultCaseAction
        implements ICaseAction, IUploadHandler {

    private static Log log = LogFactory.getLog( DokumenteCaseAction.class );

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>    mcase;
    
    private MosaicRepository2               repo;

    private IPanelSite                      site;

    private IAppContext                     context;

    private IPanelSection                   personSection;

    private TableViewer                     viewer;

    private SetModel                        model;

    private Display display;

    
    @Override
    public boolean init( IPanelSite _site, IAppContext _context ) {
        this.site = _site;
        this.context = _context;
        this.repo = MosaicRepository2.instance();
        return true;
    }


    @Override
    public void createContents( Composite parent ) {
        Composite area = new Composite( parent, SWT.NONE );
        area.setLayout( FormLayoutFactory.defaults().margins( 40 ).create() );
        
        Upload upload = new Upload( area, SWT.NONE );
        upload.setLayoutData( FormDataFactory.filled().bottom( -1 ).right( 50 ).create() );
        upload.setHandler( this );
        display = Polymap.getSessionDisplay();
    }

    
    @Override
    public void uploadStarted( String name, String contentType, InputStream in )
            throws Exception {
        log.info( "received: " + name );
        IMosaicDocument doc = repo.newDocument( mcase.get(), name );
        OutputStream out = null;
        try {
            IOUtils.copy( in, out = doc.getOutputStream() );
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
        finally {
            IOUtils.closeQuietly( in );
            IOUtils.closeQuietly( out );
        }

        model.addAll( new IMosaicDocument[] { doc } );

        repo.newCaseEvent( (MosaicCase2)mcase.get(), doc.getName(), "Name: " + doc.getName() + 
                ", Typ: " + doc.getContentType() +
                ", Größe: " + doc.getSize(), "Dokument angelegt"  );
        repo.commitChanges();
        
        display.asyncExec( new Runnable() {
            public void run() {
                // close action area
            }
        });
    }


    @Override
    public void fillContentArea( Composite parent ) {
        IPanelSection section = site.toolkit().createPanelSection( parent, "Dokumente" );
        section.addConstraint( new PriorityConstraint( 10, 10 ) );
        section.getBody().setLayout( FormLayoutFactory.defaults().create() );
        
        // viewer
        viewer = new TableViewer( section.getBody(), SWT.VIRTUAL /*| SWT.V_SCROLL | SWT.FULL_SELECTION |*/ );
        viewer.getTable().setLayoutData( FormDataFactory.filled().height( 200 ).width( 400 ).create() );

        viewer.getTable().setLinesVisible( true );
        viewer.getTable().setHeaderVisible( true );
        TableLayout layout = new TableLayout();
        viewer.getTable().setLayout( layout );

        // name column
        TableViewerColumn vcolumn = new TableViewerColumn( viewer, SWT.LEFT );
        vcolumn.getColumn().setResizable( true );
        vcolumn.getColumn().setText( "Name" );
        vcolumn.setLabelProvider( new ColumnLabelProvider() {
            public String getText( Object elm ) {
                return ((IMosaicDocument)elm).getName();
            }
        });
        layout.addColumnData( new ColumnWeightData( 2, 100, true ) );            

        // size column
        vcolumn = new TableViewerColumn( viewer, SWT.RIGHT );
        vcolumn.getColumn().setResizable( true );
        vcolumn.getColumn().setText( "Größe" );
        vcolumn.setLabelProvider( new ColumnLabelProvider() {
            public String getText( Object elm ) {
                return FileUtils.byteCountToDisplaySize( ((IMosaicDocument)elm).getSize() );
            }
        });
        layout.addColumnData( new ColumnWeightData( 1, 60, true ) );            

        viewer.setContentProvider( new DeferredContentProvider( new Comparator<User>() {
            public int compare( User o1, User o2 ) {
                return 0;
            }
        }));
        viewer.setInput( model = new SetModel() );
        model.addAll( ImmutableList.copyOf( repo.documents( mcase.get() ) ) );
    }
    
}
