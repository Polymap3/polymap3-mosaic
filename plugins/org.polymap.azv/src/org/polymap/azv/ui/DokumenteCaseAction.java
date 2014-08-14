/* 
 * polymap.org
 * Copyright (C) 2013-2014, Falko Bräutigam. All rights reserved.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import java.beans.PropertyChangeEvent;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.data.operation.DownloadServiceHandler;
import org.polymap.core.data.operation.DownloadServiceHandler.ContentProvider;
import org.polymap.core.data.operation.DownloadServiceHandler.DownloadDialog;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.ui.ColumnLayoutFactory;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.SelectionAdapter;
import org.polymap.core.ui.upload.IUploadHandler;
import org.polymap.core.ui.upload.Upload;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.app.BatikApplication;
import org.polymap.rhei.batik.toolkit.ConstraintData;
import org.polymap.rhei.batik.toolkit.IPanelSection;
import org.polymap.rhei.batik.toolkit.NeighborhoodConstraint;
import org.polymap.rhei.batik.toolkit.NeighborhoodConstraint.Neighborhood;
import org.polymap.rhei.batik.toolkit.PriorityConstraint;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicCaseEvent;
import org.polymap.mosaic.server.model.IMosaicDocument;
import org.polymap.mosaic.server.model.MosaicCaseEvents;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.DefaultCaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseActionSite;

/**
 * Dokumente am Vorgang: "Beigebrachte" und "von GKU"
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class DokumenteCaseAction
        extends DefaultCaseAction
        implements ICaseAction, IUploadHandler {

    private static Log log = LogFactory.getLog( DokumenteCaseAction.class );

    private static final IMessages      i18n = Messages.forPrefix( "Dokumente" ); //$NON-NLS-1$
    
    private static final FastDateFormat df = FastDateFormat.getInstance( i18n.get( "dateFormat" ) );
    
    /** Präfix für Namen der Dokumente "von GKU". */
    public static final String          OUTGOING_PREFIX = "__";

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>    mcase;
    
    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2> repo;

    private ICaseActionSite                 site;

    /** "Beigebrachte" Dokumente */
    private TableViewer                     viewer1;

    /** "Von GKU" Dokumente */
    private TableViewer                     viewer2;

    private Display                         display;

    /** Home of {@link #viewer1} */
    private IPanelSection                   section1;

    /** Home of {@link #viewer2} */
    private IPanelSection                   section2;

    private Composite                       formContainer;

    private IAction                         caseAction;

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;
        if (mcase.get() != null && repo.get() != null
                && (mcase.get().getNatures().contains( AzvPlugin.CASE_SCHACHTSCHEIN )
                 || mcase.get().getNatures().contains( AzvPlugin.CASE_LEITUNGSAUSKUNFT )
                 || mcase.get().getNatures().contains( AzvPlugin.CASE_DIENSTBARKEITEN ))) {

            // listen to changes of the mcase
            EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {
                public boolean apply( PropertyChangeEvent input ) {
                    return input.getSource() == mcase.get();
                }
            });
            return true;
        }
        return false;
    }


    @Override
    public void dispose() {
        caseAction = null;
        EventManager.instance().unsubscribe( this );
    }


    @Override
    public void fillAction( IAction action ) {
        this.caseAction = action;
        String caseStatus = mcase.get().getStatus();
        if (caseStatus.equals( IMosaicCaseEvent.TYPE_CLOSED )) {
            action.setText( null );
            action.setImageDescriptor( null );
            action.setEnabled( false );
        }
    }

    
    @Override
    public void createContents( Composite parent ) {
        site.setShowSubmitButton( false );

        Label txt = site.toolkit().createFlowText( parent , i18n.get( "welcomeText" ) ); //$NON-NLS-1$
        txt.setLayoutData( new ConstraintData( AzvPlugin.MIN_COLUMN_WIDTH, new PriorityConstraint( 100 ) ) );

        formContainer = site.toolkit().createComposite( parent );
        formContainer.setLayout( ColumnLayoutFactory.defaults().margins( 40, 20 ).spacing( 10 ).create() );
        
        Upload upload = new Upload( formContainer, SWT.NONE );
        upload.setHandler( this );
        display = Polymap.getSessionDisplay();
    }

    
    @Override
    public void uploadStarted( String name, String contentType, int contentLength, InputStream in ) throws Exception {
        log.info( "received: " + name ); //$NON-NLS-1$
        
        final boolean isOutgoing = SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA );
        name = isOutgoing ? OUTGOING_PREFIX + name : name;
        
        OutputStream out = null;
        try {
            // make upload modal: disable entire UI
            display.asyncExec( new Runnable() {
                public void run() {
                    BatikApplication.shellToParentOn().setEnabled( false );
                }
            });

            
            IMosaicDocument doc = repo.get().newDocument( mcase.get(), name );
            IOUtils.copy( in, out = doc.getOutputStream() );
            
//          repo.get().newCaseEvent( (MosaicCase2)mcase.get(), doc.getName(), "Name: " + doc.getName() + 
//          ", Typ: " + doc.getContentType() +
//          ", Größe: " + doc.getSize(), "Dokument angelegt"  );
            repo.get().commitChanges();

            display.asyncExec( new Runnable() {
                public void run() {
                    // success message
//                    site.toolkit().createFlowText( formContainer, i18n.get( "successText" ) );
//                    site.toolkit().createButton( formContainer, i18n.get( "closeBtn") )
//                        .addSelectionListener( new org.eclipse.swt.events.SelectionAdapter() {
//                            public void widgetSelected( SelectionEvent e ) {
//                                site.discard();
//                            }
//                        });
//                    formContainer.layout();
                    site.getPanelSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, i18n.get( "successText" ) ) ); //$NON-NLS-1$
                    site.discard();

                    // update/create viewer
                    TableViewer viewer = isOutgoing ? viewer2 : viewer1; 
                    if (viewer != null) {
                        viewer.refresh();
                    }
                    else if (isOutgoing) {
                        createViewer2();
                    }
                    else {
                        createViewer1();
                    }
                }
            });
        }
        catch (Exception e) {
            BatikApplication.handleError( i18n.get( "fehlerBeimHochladen" ), e );
        }
        finally {
            IOUtils.closeQuietly( in );
            IOUtils.closeQuietly( out );

            // re-enable entire UI
            display.asyncExec( new Runnable() {
                public void run() {
                    BatikApplication.shellToParentOn().setEnabled( true );
                }
            });
        }        
    }


    @Override
    public void fillContentArea( Composite parent ) {
        // "Beigebrachte"
        section1 = site.toolkit().createPanelSection( parent, i18n.get( "title1" ) );
        section1.addConstraint( new PriorityConstraint( 5 ) );
        section1.getBody().setLayout( FormLayoutFactory.defaults().create() );
        
        if (Iterables.isEmpty( repo.get().documents( mcase.get() ) )) {
            site.toolkit().createLabel( section1.getBody(), i18n.get( "keinDokument" ) );
        }
        else {
            createViewer1();
        }

        // "von GKU"
        section2 = site.toolkit().createPanelSection( parent, i18n.get( "title2" ) );
        section2.addConstraint( 
                new PriorityConstraint( 5 ), 
                new NeighborhoodConstraint( section1, Neighborhood.BOTTOM, 100 ) );
        section2.getBody().setLayout( FormLayoutFactory.defaults().create() );
        
        if (Iterables.isEmpty( repo.get().documents( mcase.get() ) )) {
            site.toolkit().createLabel( section2.getBody(), i18n.get( "keinDokument" ) );
        }
        else {
            createViewer2();
        }
        
//        EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {
//            public boolean apply( PropertyChangeEvent input ) {
//                return caseAction != null && input.getSource() == mcase.get();
//            }
//        });
    }

    
    protected void deleteDocument( IMosaicDocument doc ) {
        try {
            repo.get().removeDocument( doc, mcase.get() );
            repo.get().commitChanges();
        }
        catch (Exception e) {
            BatikApplication.handleError( i18n.get( "fehlerBeimLöschen" ), e );
        }
    }

    
    @EventHandler(display=true,delay=500)
    protected void mcaseChanged( List<PropertyChangeEvent> evs ) {
        if (viewer1 != null && !viewer1.getControl().isDisposed()) {
            viewer1.setInput( mcase.get() );
        }
        if (viewer2 != null && !viewer2.getControl().isDisposed()) {
            viewer2.setInput( mcase.get() );
        }
    }


    protected void createViewer1() {
        viewer1 = createViewer( section1, new ViewerFilter() {
            public boolean select( Viewer _viewer, Object _parentElm, Object _elm ) {
                return !((IMosaicDocument)_elm).getName().startsWith( OUTGOING_PREFIX );
            }
        });
    }


    protected void createViewer2() {
        viewer2 = createViewer( section2, new ViewerFilter() {
            public boolean select( Viewer _viewer, Object _parentElm, Object _elm ) {
                return ((IMosaicDocument)_elm).getName().startsWith( OUTGOING_PREFIX );
            }
        });
    }


    protected TableViewer createViewer( IPanelSection _section, ViewerFilter filter ) {
        for (Control child : _section.getBody().getChildren()) {
            child.dispose();
        }
        
        // viewer
        final TableViewer viewer = new TableViewer( _section.getBody(), SWT.NONE /*SWT.VIRTUAL | SWT.V_SCROLL | SWT.FULL_SELECTION |*/ );
        viewer.getTable().setLayoutData( FormDataFactory.filled().height( 200 ).width( 400 ).create() );

        viewer.getTable().setLinesVisible( true );
        viewer.getTable().setHeaderVisible( true );
        TableLayout layout = new TableLayout();
        viewer.getTable().setLayout( layout );

        // name column
        TableViewerColumn vcolumn = new TableViewerColumn( viewer, SWT.LEFT );
        vcolumn.getColumn().setResizable( true );
        vcolumn.getColumn().setText( i18n.get( "columnName" ) );
        vcolumn.setLabelProvider( new ColumnLabelProvider() {
            public String getText( Object elm ) {
                return StringUtils.removeStart( ((IMosaicDocument)elm).getName(), OUTGOING_PREFIX );
            }
        });
        layout.addColumnData( new ColumnWeightData( 4, 120, true ) );            

        // date column
        vcolumn = new TableViewerColumn( viewer, SWT.CENTER );
        vcolumn.getColumn().setResizable( true );
        vcolumn.getColumn().setText( i18n.get( "columnAenderung" ) );
        vcolumn.setLabelProvider( new ColumnLabelProvider() {
            public String getText( Object elm ) {
                return df.format( ((IMosaicDocument)elm).getLastModified() );
            }
        });
        layout.addColumnData( new ColumnWeightData( 1, 90, true ) );            

        // size column
        vcolumn = new TableViewerColumn( viewer, SWT.RIGHT );
        vcolumn.getColumn().setResizable( true );
        vcolumn.getColumn().setText( i18n.get( "columnGroesse" ) );
        vcolumn.setLabelProvider( new ColumnLabelProvider() {
            public String getText( Object elm ) {
                return FileUtils.byteCountToDisplaySize( ((IMosaicDocument)elm).getSize() );
            }
        });
        layout.addColumnData( new ColumnWeightData( 1, 90, true ) );            

        // delete action column
        boolean deletePermitted = 
                !(MosaicCaseEvents.caseStatus( mcase.get() ) == IMosaicCaseEvent.TYPE_CLOSED)
                && (SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA ) || _section == section1);
        
        if (deletePermitted) {
            vcolumn = new TableViewerColumn( viewer, SWT.LEFT );
            vcolumn.getColumn().setResizable( false );
            vcolumn.setLabelProvider( new ColumnLabelProvider() {
                // make sure you dispose these buttons when viewer input changes
                // http://stackoverflow.com/questions/12480402/swt-tableviewer-adding-a-remove-button-to-a-column-in-the-table
                Map<Object,Label> buttons = new HashMap();

                public void update( final ViewerCell cell ) {
                    TableItem item = (TableItem)cell.getItem();
                    final IMosaicDocument doc = (IMosaicDocument)cell.getElement();
                    Label button;
                    if (buttons.containsKey( doc )) {
                        button = buttons.get( doc );
                    }
                    else {
                        button = new Label( (Composite)cell.getControl(), SWT.NONE );
                        button.setImage( AzvPlugin.instance().imageForName( "resources/icons/errorstate.gif" ) );
                        button.setToolTipText( "Dokument löschen" );
                        button.addMouseListener( new MouseAdapter() {
                            public void mouseUp( MouseEvent ev ) {
                                log.info( "Dokument: " + doc.getName() );
                                deleteDocument( doc );

                                for (Label btn : buttons.values()) {
                                    btn.dispose();
                                }
                                buttons.clear();
                                viewer.setInput( mcase.get() );
                            }
                        });
                        buttons.put( cell.getElement(), button );
                    }
                    TableEditor editor = new TableEditor( item.getParent() );
                    editor.grabHorizontal = true;
                    editor.grabVertical = true;
                    editor.setEditor( button, item, cell.getColumnIndex() );
                    editor.layout();
                }
            });
            layout.addColumnData( new ColumnWeightData( 1, 25, false ) );
        }

        viewer.addFilter( filter );
        viewer.setContentProvider( new ArrayContentProvider() {
            public Object[] getElements( Object input ) {
                return Iterables.toArray( repo.get().documents( mcase.get() ), Object.class );
            }
        });
        viewer.setInput( mcase.get() );
        
        // download on click
        viewer.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent ev ) {
                final IMosaicDocument selected = SelectionAdapter.on( ev.getSelection() ).first( IMosaicDocument.class );
                if (selected != null) {
                    final AtomicReference<DownloadDialog> dialogRef = new AtomicReference();
                    
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
                            final DownloadDialog dialog = dialogRef.get();
                            if (dialog != null && !dialog.getShell().isDisposed()) {
                                dialog.getShell().getDisplay().asyncExec( new Runnable() {
                                    public void run() {
                                        dialog.close();
                                    }
                                });
                            }
                            return true;
                        }
                    });
                    
//                    url = "http://localhost:8080/" + url;
                    dialogRef.set( new DownloadDialog( BatikApplication.shellToParentOn(), url )
                            .setMessage( i18n.get( "downloadText", url ) ) );
//                            .openBrowserWindow( "download_window", ExternalBrowser.NAVIGATION_BAR | ExternalBrowser.STATUS )
                    dialogRef.get().setBlockOnOpen( true );
                    dialogRef.get().open();
                            
//                    ExternalBrowser.open( "download_window", url, //$NON-NLS-1$
//                            ExternalBrowser.NAVIGATION_BAR | ExternalBrowser.STATUS );
                }
            }
        });
        viewer.addDoubleClickListener( new IDoubleClickListener() {
            public void doubleClick( DoubleClickEvent ev ) {
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
        return viewer;
    }
    
}
