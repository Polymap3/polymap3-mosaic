/* 
 * polymap.org
 * Copyright (C) 2014, Falko Bräutigam. All rights reserved.
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
package org.polymap.azv.ui.i18n;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import org.eclipse.core.runtime.IProgressMonitor;

import org.polymap.core.operation.IOperationSaveListener;
import org.polymap.core.operation.OperationSupport;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.i18n.ResourceBundleTreeViewer;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;
import org.polymap.core.ui.SelectionAdapter;
import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class I18nEditor
        extends EditorPart
        implements IOperationSaveListener {

    private static Log log = LogFactory.getLog( I18nEditor.class );

    public static final String          EDITOR_ID = "org.polymap.azv.I18nEditor";
    
    public static final String          MESSAGES_DE_PROPERTIES = "org.polymap.azv.messages_de.properties";

    private Text                        editor;

    private ResourceBundleTreeViewer    tree;

    protected String                    selectedKey;


    @Override
    public void init( IEditorSite _site, IEditorInput _input ) throws PartInitException {
        setSite( _site );
        setInput( _input );
        
        OperationSupport.instance().addOperationSaveListener( this );
    }


    @Override
    public void dispose() {
        OperationSupport.instance().addOperationSaveListener( this );
    }


    @Override
    public void createPartControl( Composite parent ) {
        parent.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
        
        // tree
        tree = new ResourceBundleTreeViewer( parent, SWT.NONE );
        tree.setInput( Messages.resourceBundle() /*new File( Polymap.getWorkspacePath().toFile(), MESSAGES_DE_PROPERTIES )*/ );
        FormDataFactory.filled().right( 30 ).applyTo( tree.getControl() );
        
        tree.addSelectionChangedListener( new ISelectionChangedListener() {
            public void selectionChanged( SelectionChangedEvent ev ) {
                String key = SelectionAdapter.on( ev.getSelection() ).first( String.class );
                log.info( "Selected: " + key );
                String value = tree.getEntryValue( key );
                selectedKey = value != null ? key : null;
                if (value != null) {
                    editor.setText( value );
                    editor.setVisible( true );
                }
                else {
                    tree.expandToLevel( key, 1 );
                    editor.setVisible( false );
                    //editor.setText( "Dieser Eintrag enthält mehrere Untereinträge." );
                }
            }
        });

        editor = new Text( parent, SWT.MULTI | SWT.WRAP | SWT.BORDER );
        editor.setVisible( false );
//        editor.setText( "Wählen Sie einen Eintrag links aus dem Baum,\num den entsprechenden Text hier zu bearbeiten\n" );
        FormDataFactory.filled().left( tree.getControl() ).applyTo( editor );
        
        editor.addModifyListener( new ModifyListener() {
            public void modifyText( ModifyEvent event ) {
                log.info( "Changed: " + editor.getText() );
                tree.setEntryValue( selectedKey, editor.getText() );

                tree.getControl().getDisplay().asyncExec( new Runnable() {
                    public void run() {
                        firePropertyChange( PROP_DIRTY );
                    }
                });
            }
        });
        
//        // editor
//        editor = new CodeMirror( parent, SWT.NONE );
//        editor.setText( "Wählen Sie einen Eintrag links aus dem Baum,\num den entsprechenden Text hier zu bearbeiten\n" );
//        FormDataFactory.filled().left( tree.getControl() ).applyTo( editor );
//        
//        editor.addPropertyChangeListener( new PropertyChangeListener() {
//            public void propertyChange( PropertyChangeEvent ev ) {
//                if (ev.getPropertyName().equals( CodeMirror.PROP_TEXT ) && selectedKey != null) {
//                    log.info( "Changed: " + editor.getText() );
//                    tree.setEntryValue( selectedKey, editor.getText() );
//
//                    tree.getControl().getDisplay().asyncExec( new Runnable() {
//                        public void run() {
//                            firePropertyChange( PROP_DIRTY );
//                        }
//                    });
//                }
//            }
//        });
    }


    @Override
    public void doSave( IProgressMonitor monitor ) {
        try {
            tree.getContentProvider().save( 
                    new File( Polymap.getWorkspacePath().toFile(), MESSAGES_DE_PROPERTIES ), false );
            
            tree.getControl().getDisplay().asyncExec( new Runnable() {
                public void run() {
                    firePropertyChange( PROP_DIRTY );
                }
            });
        }
        catch (IOException e) {
            PolymapWorkbench.handleError( AzvPlugin.ID, this, "Die Datei konnte nicht korrekt gespeichert werden.", e );
        }
    }


    @Override
    public void doSaveAs() {
        // XXX Auto-generated method stub
        throw new RuntimeException( "not yet implemented." );
    }


    @Override
    public boolean isDirty() {
        return tree != null && tree.getContentProvider().isDirty();
    }


    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }


    @Override
    public void setFocus() {
        editor.setFocus();
    }

    
    // IOperationSaveListener *****************************
    
    @Override
    public void prepareSave( OperationSupport os, IProgressMonitor monitor ) throws Exception {
    }

    @Override
    public void save( OperationSupport os, IProgressMonitor monitor ) {
        doSave( monitor );
    }

    @Override
    public void rollback( OperationSupport os, IProgressMonitor monitor ) {
    }

    @Override
    public void revert( OperationSupport os, IProgressMonitor monitor ) {
    }
        
    
    // Input **********************************************
    
    public static class Input
            implements IEditorInput {

        @Override
        public Object getAdapter( Class adapter ) {
            return null;
        }

        @Override
        public boolean exists() {
            return true;
        }

        @Override
        public ImageDescriptor getImageDescriptor() {
            return null;
        }

        @Override
        public String getName() {
            return "i18n";
        }

        @Override
        public String getToolTipText() {
            return "i18n";
        }

        @Override
        public IPersistableElement getPersistable() {
            return null;
        }
        
    }

}
