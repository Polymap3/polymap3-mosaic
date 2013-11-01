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

import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.rwt.widgets.ExternalBrowser;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;

import org.polymap.core.data.operation.DownloadServiceHandler;
import org.polymap.core.data.operation.DownloadServiceHandler.ContentProvider;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.SelectionAdapter;
import org.polymap.core.ui.upload.IUploadHandler;
import org.polymap.core.ui.upload.Upload;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicDocument;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.DefaultCaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseActionSite;

/**
 * Dokumente hochladen an Vorgang.
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DokumenteCaseAction
        extends DefaultCaseAction
        implements ICaseAction, IUploadHandler {

    private static Log log = LogFactory.getLog( DokumenteCaseAction.class );

    private static final IMessages      i18n = Messages.forPrefix( "Dokumente" );
    
    private static final FastDateFormat df = FastDateFormat.getInstance( "dd.MM.yyyy" );

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>    mcase;
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2> repo;

    private ICaseActionSite                 site;

    private IPanelSection                   personSection;

    private TableViewer                     viewer;

//    private SetModel                        model;

    private Display                         display;

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        return mcase.get() != null && repo.get() != null
                && !mcase.get().getNatures().contains( AzvPlugin.CASE_NUTZER );
    }


    @Override
    public void createContents( Composite parent ) {
        site.setShowSubmitDiscardButtons( false );
        FillLayout playout = (FillLayout)parent.getLayout();
        playout.marginWidth *= 2;      
        playout.spacing *= 2;      

        site.toolkit().createFlowText( parent , i18n.get( "welcomeText" ) );

        Composite formContainer = site.toolkit().createComposite( parent );
        formContainer.setLayout( FormLayoutFactory.defaults().margins( 40 ).create() );
        
        Upload upload = new Upload( formContainer, SWT.NONE );
        upload.setLayoutData( FormDataFactory.filled().bottom( -1 ).create() );
        upload.setHandler( this );
        display = Polymap.getSessionDisplay();
    }

    
    @Override
    public void uploadStarted( String name, String contentType, InputStream in )
            throws Exception {
        log.info( "received: " + name );
        IMosaicDocument doc = repo.get().newDocument( mcase.get(), name );
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

//        repo.get().newCaseEvent( (MosaicCase2)mcase.get(), doc.getName(), "Name: " + doc.getName() + 
//                ", Typ: " + doc.getContentType() +
//                ", Größe: " + doc.getSize(), "Dokument angelegt"  );
        repo.get().commitChanges();
        
        display.asyncExec( new Runnable() {
            public void run() {
                //model.addAll( new IMosaicDocument[] { doc } );

                viewer.refresh();
                // close action area
            }
        });
    }


    @Override
    public void fillContentArea( Composite parent ) {
        IPanelSection section = site.toolkit().createPanelSection( parent, "Dokumente" );
        section.addConstraint( new PriorityConstraint( 1 ) );
        section.getBody().setLayout( FormLayoutFactory.defaults().create() );
        
        // viewer
        viewer = new TableViewer( section.getBody(), SWT.NONE /*SWT.VIRTUAL | SWT.V_SCROLL | SWT.FULL_SELECTION |*/ );
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

        // date column
        vcolumn = new TableViewerColumn( viewer, SWT.CENTER );
        vcolumn.getColumn().setResizable( true );
        vcolumn.getColumn().setText( "Änderung" );
        vcolumn.setLabelProvider( new ColumnLabelProvider() {
            public String getText( Object elm ) {
                return df.format( ((IMosaicDocument)elm).getLastModified() );
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

        viewer.setContentProvider( new ArrayContentProvider() {
            public Object[] getElements( Object input ) {
                return Iterables.toArray( repo.get().documents( mcase.get() ), Object.class );
            }
        });
        viewer.setInput( mcase.get() );
        
        viewer.addDoubleClickListener( new IDoubleClickListener() {
            public void doubleClick( DoubleClickEvent ev ) {
                final IMosaicDocument selected = new SelectionAdapter( ev.getSelection() ).first( IMosaicDocument.class );
                String url = DownloadServiceHandler.registerContent( new ContentProvider() {
                    @Override
                    public InputStream getInputStream() throws Exception {
                        return selected.getInputStream();
                    }
                    @Override
                    public String getFilename() {
                        return selected.getName();
                    }
                    @Override
                    public String getContentType() {
                        return selected.getContentType();
                    }
                    @Override
                    public boolean done( boolean success ) {
                        return true;
                    }
                });
                ExternalBrowser.open( "download_window", url,
                        ExternalBrowser.NAVIGATION_BAR | ExternalBrowser.STATUS );
            }
        });

//        viewer.setContentProvider( new DeferredContentProvider( new Comparator<User>() {
//            public int compare( User o1, User o2 ) {
//                return 0;
//            }
//        }));
//        List<IMosaicDocument> documents = ImmutableList.copyOf( repo.documents( mcase.get() ) );
//        viewer.setInput( model = new SetModel() );
//        model.addAll( documents );
    }
    
}
