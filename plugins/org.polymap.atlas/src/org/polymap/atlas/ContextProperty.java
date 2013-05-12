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
package org.polymap.atlas;

/**
 * This interface allows to access an {@link IAppContext} property. Instances are
 * automatically injected into objects of type {@link IPanel} when initialized.
 * <p/>
 * By default the <b>scope</b> of the property is the <b>package</b> of the
 * {@link IPanel} class. The {@link Context} annotation can be used to explicitly set
 * the scope.
 * 
 * @see Context
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public interface ContextProperty<T> {

    public T get();
    
    public T set( T value );
    
    public Class<T> getDeclaredType();
    
    public String getScope();

}
