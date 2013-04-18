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
package org.polymap.atlas.internal;

import java.util.ArrayDeque;
import java.util.Queue;
import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.polymap.atlas.Context;
import org.polymap.atlas.IAppContext;
import org.polymap.atlas.IPanel;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class PanelContextInjector
        implements Runnable {

    private static Log log = LogFactory.getLog( PanelContextInjector.class );

    private IPanel          panel;

    private IAppContext     context;


    public PanelContextInjector( IPanel panel, IAppContext context ) {
        this.panel = panel;
        this.context = context;
    }

    @Override
    public void run() {
        Queue<Class> types = new ArrayDeque( 16 );
        types.add( panel.getClass() );

        while (!types.isEmpty()) {
            Class type = types.remove();
            if (type.getSuperclass() != null) {
                types.add( type.getSuperclass() );
            }

            for (Field f : type.getDeclaredFields()) {
                Context annotation = f.getAnnotation( Context.class );
                if (annotation != null) {
                    f.setAccessible( true );

                    String contextKey = annotation.key().length() > 0 ? annotation.key() : f.getName();
                    Object value = context.get( panel, contextKey );

                    try {
                        f.set( panel, value );
                        log.info( "injected: " + f.getName() + " <- " + value );
                    }
                    catch (Exception e) {
                        throw new RuntimeException( e );
                    }
                }
            }
        }
    }

}
