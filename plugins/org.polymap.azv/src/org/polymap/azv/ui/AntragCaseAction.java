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

import java.beans.PropertyChangeEvent;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;

import org.eclipse.jface.action.IAction;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.runtime.event.EventManager;

import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.app.BatikApplication;
import org.polymap.rhei.um.User;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.model.AzvVorgang;
import org.polymap.azv.model.OrtMixin;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicDocument;
import org.polymap.mosaic.server.model.MosaicCaseEvents;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.DefaultCaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseAction;
import org.polymap.mosaic.ui.casepanel.ICaseActionSite;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public abstract class AntragCaseAction
        extends DefaultCaseAction
        implements ICaseAction {

    private static Log log = LogFactory.getLog( AntragCaseAction.class );

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    protected ContextProperty<IMosaicCase>      mcase;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    protected ContextProperty<MosaicRepository2> repo;

    protected ICaseActionSite                   site;

    protected IAction                           action;

    
    protected abstract IMessages i18n();
    

    protected abstract String docsTemplateDir();

    
    @Override
    public boolean init( ICaseActionSite _site ) {
        this.site = _site;

        // updateAction() wenn mcase geändert (von StartCaseAction.submit())
        EventManager.instance().subscribe( this, new EventFilter<PropertyChangeEvent>() {
            public boolean apply( PropertyChangeEvent ev ) {
                return action != null && mcase.get() == ev.getSource();  //DrawFeatureMapAction.EVENT_NAME.equals( ev.getPropertyName() );
            }
        });
        return true;
    }

    
    @Override
    public void dispose() {
        EventManager.instance().unsubscribe( this );
        action = null;
    }

    
    @Override
    public void fillAction( @SuppressWarnings("hiding") IAction action ) {
        this.action = action;
        if (MosaicCaseEvents.contains( mcase.get(), AzvPlugin.EVENT_TYPE_BEANTRAGT )) {
            action.setText( null );
            action.setImageDescriptor( null );
            action.setEnabled( false );
        }
        else {
            updateAction( null );        
        }
    }
    
    
    @Override
    public void submit() throws Exception {
        // dokumente
//        File dir = new File( Polymap.getWorkspacePath().toFile(), __"Dokumente/Schachtschein" ); //$NON-NLS-1$
        FileObject dir = AzvPlugin.docsRoot().resolveFile( docsTemplateDir() );
        dir.createFolder();
        for (FileObject f : dir.getChildren()) {
            IMosaicDocument doc = repo.get().newDocument( mcase.get(), DokumenteCaseAction.OUTGOING_PREFIX + f.getName().getBaseName() );
            OutputStream out = null; 
            InputStream in = null;
            try {
                IOUtils.copy( in = f.getContent().getInputStream(), out = doc.getOutputStream() );
            }
            finally {
                IOUtils.closeQuietly( out );
                IOUtils.closeQuietly( in );
            }
        }
        // events
        repo.get().newCaseEvent( mcase.get(), "Beantragt", "", AzvPlugin.EVENT_TYPE_BEANTRAGT ); //$NON-NLS-1$ //$NON-NLS-2$
        repo.get().commitChanges();

        // email
        User user = mcase.get().as( AzvVorgang.class ).user();        
        AzvPlugin.sendEmail( user, i18n() );

        // status
        site.getPanelSite().setStatus( new Status( IStatus.OK, AzvPlugin.ID, i18n().get( "statusOk" ) ) );

        BatikApplication.sessionDisplay().asyncExec( new Runnable() {
            public void run() {
                site.getContext().closePanel( site.getPanelSite().getPath() );
            }
        });
        
//      fillAction( action );
    }


    /**
     * 
     * 
     */
    @EventHandler(display=true)
    protected void updateAction( PropertyChangeEvent ev ) {
        action.setEnabled( true );
        action.setText( i18n().get( "abschicken" ) );
        action.setToolTipText( i18n().get( "abschickenTip" ) );

        if (mcase.get().getName().length() == 0) {
            action.setToolTipText( i18n().get( "keineBezeichnung" ) );
            action.setEnabled( false );
        }            
        else if (mcase.get().get( OrtMixin.KEY_POINT ) == null) {
            action.setToolTipText( i18n().get( "keinOrt" ) );
            action.setEnabled( false );
        }
    }

}
