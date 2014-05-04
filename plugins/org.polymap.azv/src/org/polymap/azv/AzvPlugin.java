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
package org.polymap.azv;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;
import java.util.ResourceBundle;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.util.tracker.ServiceTracker;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.apache.commons.vfs2.FileObject;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

import org.eclipse.rwt.graphics.Graphics;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.polymap.core.project.ILayer;
import org.polymap.core.project.LayerFinder;
import org.polymap.core.project.ProjectRepository;
import org.polymap.core.runtime.CachedLazyInit;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.Lazy;
import org.polymap.core.runtime.LockedLazyInit;
import org.polymap.core.runtime.Polymap;

import org.polymap.rhei.batik.toolkit.MinWidthConstraint;
import org.polymap.rhei.fulltext.FullQueryProposalDecorator;
import org.polymap.rhei.fulltext.FullTextIndex;
import org.polymap.rhei.fulltext.LogQueryDecorator;
import org.polymap.rhei.fulltext.SessionHolder;
import org.polymap.rhei.fulltext.address.AddressFeatureTransformer;
import org.polymap.rhei.fulltext.address.AddressTokenFilter;
import org.polymap.rhei.fulltext.indexing.Feature2JsonTransformer;
import org.polymap.rhei.fulltext.indexing.LowerCaseTokenFilter;
import org.polymap.rhei.fulltext.indexing.ToStringTransformer;
import org.polymap.rhei.fulltext.lucene.LuceneFullTextIndex;
import org.polymap.rhei.fulltext.update.LayerModificationWatcher;
import org.polymap.rhei.fulltext.update.UpdateableFullTextIndex;
import org.polymap.rhei.um.User;
import org.polymap.rhei.um.email.EmailService;

import org.polymap.mosaic.server.model2.MosaicRepository2;

/**
 *
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AzvPlugin
        extends AbstractUIPlugin {

    private static Log log = LogFactory.getLog( AzvPlugin.class );

    public static final String      ID = "org.polymap.azv";

    public static final FastDateFormat df = FastDateFormat.getInstance( "dd.MM.yyyy" );
    
    /* The property scope of the AZV plugin. */
    public static final String      PROPERTY_SCOPE = ID; 

    public static final String      ROLE_SCHACHTSCHEIN = "Schachtschein beantragen";
    public static final String      ROLE_LEITUNGSAUSKUNFT = "Leitungsauskunft";
    public static final String      ROLE_LEITUNGSAUSKUNFT2 = "Leitungsauskunft (vertrauenswürdig)";
    public static final String      ROLE_DIENSTBARKEITEN = "Dienstbarkeiten";
    public static final String      ROLE_HYDRANTEN = "Hydranten";
    public static final String      ROLE_WASSERQUALITAET = "Wasserqualität";
    public static final String      ROLE_ENTSORGUNG = "Entsorgung";
    public static final String      ROLE_MA = "Interner Sachbearbeiter";
    public static final String      ROLE_BL = "Betriebsstellenleiter";

    public static final String      CASE_SCHACHTSCHEIN = "Schachtschein";
    public static final String      CASE_LEITUNGSAUSKUNFT = "Leitungsauskunft";
    public static final String      CASE_DIENSTBARKEITEN = "Dienstbarkeiten";
    public static final String      CASE_HYDRANTEN = "Hydranten";
    public static final String      CASE_WASSERQUALITAET = "Wasserqualität";
    public static final String      CASE_ENTSORGUNG = "Entsorgung";
    public static final String      CASE_NUTZER = "Neuer Nutzer";

    public static final String      EVENT_TYPE_BEANTRAGT = "Antrag";
//    public static final String      EVENT_TYPE_TERMINIERT = "Terminiert";
    public static final String      EVENT_TYPE_STORNIERT = "Storno";
    public static final String      EVENT_TYPE_ABGEBROCHEN = "Abbruch";
    public static final String      EVENT_TYPE_ANFREIGABE = "An Freigabe";
    public static final String      EVENT_TYPE_ANBEARBEITUNG = "An Bearbeitung";
    public static final String      EVENT_TYPE_FREIGABE = "Freigabe";
    public static final String      EVENT_TYPE_ERLEDIGT = "Erledigt";

    public static final MinWidthConstraint MIN_COLUMN_WIDTH = new MinWidthConstraint( 420, 1 );



//    /**
//     * Produces {@link StringFormField} instances only.
//     */
//    public static final IFormFieldFactory LABEL_FIELD_FACTORY = new IFormFieldFactory() {
//        public IFormField createField( Property prop ) {
//            return new LabelFormField() {
//                public Control createControl( Composite parent, IFormEditorToolkit toolkit ) {
//                    // prevent default form labeler style (FormContainer)
//                    Control result = super.createControl( parent, toolkit );
//                    result.setData( WidgetUtil.CUSTOM_VARIANT, "azv-formfield" );
//                    return result;
//                }
//            };
//        }
//    };

    private static AzvPlugin        instance;

    public static AzvPlugin instance() {
        assert instance != null;
        return instance;
    }


    // instance *******************************************

    private ServiceTracker          httpServiceTracker;
    
//    private MosaicRepository2       mosaicRepo;
    
    private Lazy<LuceneFullTextIndex> addressIndex;           

    private LayerModificationWatcher addressWatcher;
    
    private SessionHolder           addressSession;
    
    /** Color defined/used in batik.css. */
    public Lazy<Color>              discardColor = new LockedLazyInit( new Supplier<Color>() {
        public Color get() { return Graphics.getColor( 0xd3, 0x25, 0x16 ); }
    });

    /** Color defined/used in batik.css. */
    public Lazy<Color>              okColor = new LockedLazyInit( new Supplier<Color>() {
        public Color get() { return Graphics.getColor( 0x6e, 0xb0, 0x2e ); }
    });
    
    /** Color defined/used in batik.css. */
    public Lazy<Color>              openColor = new LockedLazyInit( new Supplier<Color>() {
        public Color get() { return Graphics.getColor( 0xff, 0xcc, 0x00 ); }
    });


    public FullTextIndex addressIndex() {
        FullTextIndex result = new LogQueryDecorator( addressIndex.get() );
        result = new LowerCaseTokenFilter( result );
        result = new FullQueryProposalDecorator( result );
        return result;
    }

    
    @Override
    public void start( BundleContext context ) throws Exception {
        super.start( context );
        instance = this;
        
        // helps to integrate i18n changes from workspace and classpath
//        saveMessages( new File( "/tmp/org.polymap.azv.messages_de.properties" ) );
        
//        mosaicRepo = MosaicRepository2.instance();
        MosaicRepository2.setDocumentNameMapper( new DocumentNameMapper() );
        
        // addressIndex
        addressSession = new SessionHolder( "Adressen" );
        final File addressStoreDir = new File( Polymap.getDataDir(), "org.polymap.azv.addresses" );
        addressStoreDir.mkdirs();
        addressIndex = new CachedLazyInit( 10000, new Supplier<LuceneFullTextIndex>() {
            public LuceneFullTextIndex get() {
                try {
                    LuceneFullTextIndex result = new LuceneFullTextIndex( addressStoreDir );
                    result.addTokenFilter( new AddressTokenFilter() );
                    result.addTokenFilter( new LowerCaseTokenFilter() );                    
                    return result;
                }
                catch (IOException e) {
                    log.error( "", e );
                    throw new RuntimeException( e );
                }
            }
        });
        // watch layer
        addressSession.execute( new Runnable() {
            public void run() {
                ILayer layer = ProjectRepository.instance().visit( new LayerFinder( "Adressen", "adressen", "addresses") );
                if (layer != null) {
                    addressWatcher = new LayerModificationWatcher( addressSession, null, layer ) {
                        protected UpdateableFullTextIndex index() { return addressIndex.get(); }
                    };
                    addressWatcher.addTransformer( new Feature2JsonTransformer( layer ) );
                    addressWatcher.addTransformer( new ToStringTransformer() );
                    addressWatcher.addTransformer( new AddressFeatureTransformer() );
                    addressWatcher.setCommitOnException( true );
                } 
                else {
                    log.warn( "Es existiert keine Ebene für Adressdaten! (Ebenenname: Adressen)" );
                }
            }
        });
        // check/initialize index
        if (addressStoreDir.list().length == 0 && addressWatcher != null) {
            addressWatcher.start();
        }
        
        // register HTTP resource
        httpServiceTracker = new ServiceTracker( context, HttpService.class.getName(), null ) {
            public Object addingService( ServiceReference reference ) {
                HttpService httpService = (HttpService)super.addingService( reference );                
                if (httpService != null) {
                    try {
                        httpService.registerResources( "/azvres", "/resources", null );
                    }
                    catch (NamespaceException e) {
                        throw new RuntimeException( e );
                    }
                }
                return httpService;
            }
        };
        httpServiceTracker.open();

    }

    @Override
    public void stop( BundleContext context ) throws Exception {
//        mosaicRepo.close();
        
        super.stop( context );
        instance = null;
    }


    public Image imageForName( String resName ) {
        ImageRegistry images = getImageRegistry();
        Image image = images.get( resName );
        if (image == null || image.isDisposed()) {
            URL res = getBundle().getResource( resName );
            assert res != null : "Image resource not found: " + resName;
            images.put( resName, ImageDescriptor.createFromURL( res ) );
            image = images.get( resName );
        }
        return image;
    }

    
    public void saveMessages( File file ) throws IOException {
        OutputStream out = null;
        try {
            Properties props = new Properties();
            ResourceBundle rb = Messages.resourceBundle();
            for (String key : Collections.list( rb.getKeys() ) ) {
                props.put( key, rb.getString( key ) );
            }
            out = new BufferedOutputStream( new FileOutputStream( file ) );
            props.store( out, " Generated by Polymap3 I18N Editor (http://polymap.org/polymap3)" );
        }
        finally {
            IOUtils.closeQuietly( out );
        }
    }


    public static void sendEmail( User user, IMessages i18n, String... args ) throws EmailException {
        String salu = user.salutation().get() != null ? user.salutation().get() : ""; //$NON-NLS-1$
        String header = "Sehr geehrte" + (salu.equalsIgnoreCase( "Herr" ) ? "r " : " ") + salu + " " + user.name().get(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        
        LinkedList<String> allargs = Lists.newLinkedList( Arrays.asList( args ) );
        allargs.add( 0, header );
        
        Email email = new SimpleEmail();
        email.setCharset( "ISO-8859-1" ); //$NON-NLS-1$
        email.addTo( user.email().get() )
                .setSubject( i18n.get( "emailSubject") ) //$NON-NLS-1$
                .setMsg( i18n.get( "email", allargs.toArray() ) ); //$NON-NLS-1$
        EmailService.instance().send( email );
    }

    
    public static FileObject docsRoot() {
        return MosaicRepository2.documentsRoot;
    }
    
}
