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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import org.polymap.core.workbench.PolymapWorkbench;

import org.polymap.azv.AzvPlugin;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class OpenI18nEditorAction
        implements IWorkbenchWindowActionDelegate {

    private static Log log = LogFactory.getLog( OpenI18nEditorAction.class );


    @Override
    public void init( IWorkbenchWindow window ) {
    }


    @Override
    public void run( IAction action ) {
        try {
            I18nEditor.Input input = new I18nEditor.Input();

            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            
            // check current editors
            I18nEditor result = null;
            for (IEditorReference reference : page.getEditorReferences()) {
                IEditorInput candidate = reference.getEditorInput();
                if (candidate.equals( input )) {
                    result = (I18nEditor)reference.getPart( true );
                    page.activate( result );
                }
            }

            // not found -> open new editor
            if (result == null /*&& createIfAbsent*/) {
                page.openEditor( input, I18nEditor.EDITOR_ID, true, IWorkbenchPage.MATCH_NONE );
            }
        }
        catch (Exception e) {
            PolymapWorkbench.handleError( AzvPlugin.ID, this, "", e );
        }
    }


    @Override
    public void selectionChanged( IAction action, ISelection selection ) {
    }


    @Override
    public void dispose() {
    }
    
}
