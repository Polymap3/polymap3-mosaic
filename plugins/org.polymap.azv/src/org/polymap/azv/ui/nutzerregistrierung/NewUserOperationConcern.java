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
package org.polymap.azv.ui.nutzerregistrierung;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

import org.polymap.core.operation.IOperationConcernFactory;
import org.polymap.core.operation.OperationConcernAdapter;
import org.polymap.core.operation.OperationInfo;

import org.polymap.rhei.um.User;
import org.polymap.rhei.um.operations.NewUserOperation;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.model.AzvVorgang;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model2.MosaicRepository2;

/**
 * Intercepts {@link NewUserOperation} and creates a new Mosaic case for it. 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class NewUserOperationConcern
        extends IOperationConcernFactory {

    private static Log log = LogFactory.getLog( NewUserOperationConcern.class );


    @Override
    public IUndoableOperation newInstance( final IUndoableOperation op, final OperationInfo info ) {
        if (op instanceof NewUserOperation) {

            return new OperationConcernAdapter() {

                @Override
                public IStatus execute( IProgressMonitor monitor, IAdaptable _info ) throws ExecutionException {
                    // call upstream
                    IStatus result = info.next().execute( monitor, _info );
                    if (!result.isOK()) {
                        return result;
                    }
                    try {
                        User user = ((NewUserOperation)op).getUser();
                        
                        MosaicRepository2 repo = MosaicRepository2.instance();
                        IMosaicCase newCase = repo.newCase( user.name().get(),
                                "Ein neuer Nutzer wurde angelegt. Authentizität und Rechte müssen bestätigt werden." );
                        newCase.addNature( AzvPlugin.CASE_NUTZER );
                        newCase.as( AzvVorgang.class ).username.set( user.username().get() );
                        
                        String roles = Joiner.on( ',' ).join( UserPermissionsSection.instance().getRoles() );
                        newCase.put( "roles", roles );
                        repo.commitChanges();
                        
//                        MosaicRemoteServer mosaic = MosaicRemoteServer.instance();
//                      MosaicCase newCase = mosaic.createCase( "Neuer Nutzer: " + user.name().get(), "Ein neuer Nutzer wurde angelegt. Authentizität und Rechte müssen bestätigt werden." );
//                        // FIXME set default permissions 
//                        newCase.store();
                        
                        return result;
                    }
                    catch (Exception e) {
                        throw new ExecutionException( "", e );
                    }
                }

                @Override
                protected OperationInfo getInfo() {
                    return info;
                }
            };
        }
        return null;
    }
    
    
}
