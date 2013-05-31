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
package org.polymap.mosaic.api;

/**
 * Signals an exception that occured while commuticating with the Mosaic server or
 * any other Mosaic related exception.
 * 
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicRemoteException
        extends RuntimeException {

    public MosaicRemoteException() {
        super();
    }

    public MosaicRemoteException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace ) {
        super( message, cause, enableSuppression, writableStackTrace );
    }

    public MosaicRemoteException( String message, Throwable cause ) {
        super( message, cause );
    }

    public MosaicRemoteException( String message ) {
        super( message );
    }

    public MosaicRemoteException( Throwable cause ) {
        super( cause );
    }

}
