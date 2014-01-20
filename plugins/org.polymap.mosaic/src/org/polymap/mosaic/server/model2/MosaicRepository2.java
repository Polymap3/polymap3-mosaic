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
package org.polymap.mosaic.server.model2;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.geotools.data.DataAccess;
import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.NameImpl;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;

import com.google.common.collect.Iterables;

import org.eclipse.jface.util.PropertyChangeEvent;

import org.polymap.core.data.feature.recordstore.LuceneQueryDialect;
import org.polymap.core.data.feature.recordstore.RDataStore;
import org.polymap.core.model2.Entity;
import org.polymap.core.model2.runtime.EntityRepository;
import org.polymap.core.model2.runtime.Query;
import org.polymap.core.model2.runtime.UnitOfWork;
import org.polymap.core.model2.runtime.ValueInitializer;
import org.polymap.core.model2.store.feature.FeatureStoreAdapter;
import org.polymap.core.runtime.ConcurrentReferenceHashMap;
import org.polymap.core.runtime.ConcurrentReferenceHashMap.ReferenceType;
import org.polymap.core.runtime.Polymap;
import org.polymap.core.runtime.SessionSingleton;
import org.polymap.core.runtime.event.EventManager;
import org.polymap.core.runtime.recordstore.lucene.LuceneRecordStore;

import org.polymap.mosaic.server.document.SimpleFilesystemMapper;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model.IMosaicCaseEvent;
import org.polymap.mosaic.server.model.IMosaicDocument;
import org.polymap.mosaic.server.project.MosaicProjectRepository;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class MosaicRepository2
        extends SessionSingleton {

    private static Log log = LogFactory.getLog( MosaicRepository2.class );
    
    public static final String              NAMESPACE = "http://polymap.org/mosaic";

    public static final FilterFactory2      ff = CommonFactoryFinder.getFilterFactory2( null );

    private static final Object             initlock = new Object();
    
    /** The backend store for features of data and metadata. */
    private static LuceneRecordStore        lucenestore;

    /** {@link DataAccess} based on the local {@link #lucenestore}. */
    private static RDataStore               datastore;
    
    /** The repo for {@link Entity Entities} modelling the metadata of cases. */
    private static EntityRepository         repo;
    
    /**
     * Allows entities to find the associated {@link MosaicRepository2} from their
     * UnitOfWork. 
     */
    private static Map<UnitOfWork,MosaicRepository2> sessions = new ConcurrentReferenceHashMap( 32, ReferenceType.WEAK, ReferenceType.WEAK );

    private static StandardFileSystemManager    fsManager;
    
    private static FileObject               documentsRoot;

    private static SimpleFilesystemMapper   documentsNameMapper;

//    /** {@link IService} based on the local {@link #datastore}. */
//    private static RServiceImpl         service;
//
//    private static Map<Class<? extends Entity>,RGeoResource> geores;
//    
//    private static MosaicProjectRepositoryAssembler projectAssempler;
    
    
    static MosaicRepository2 session( UnitOfWork uow ) {
        return sessions.get( uow );
    }
    
    
    /**
     * Initialize global, underlying store and EntityRepository.
     */
    public static void init( File dataDir, boolean clean ) {
        try {
            // persistence: workspace / Lucene
            if (dataDir != null) {
                File luceneDir = new File( dataDir, "org.polymap.mosaic.data" );
                luceneDir.mkdir();
                lucenestore = new LuceneRecordStore( luceneDir, clean );
            }
            else {
                lucenestore = new LuceneRecordStore();
            }
            // Cache<Object,Document> documentCache = CacheConfig.DEFAULT.initSize( 10000 ).create();
            // store.setDocumentCache( documentCache );

            // Feature DataStore
            datastore = new RDataStore( lucenestore, new LuceneQueryDialect() );

            // Entity repo of MosaicCase and MosaicCaseEvent;
            // create FeatrueTypes of necessary
            repo = EntityRepository.newConfiguration()
                    .setStore( new FeatureStoreAdapter( datastore ) )
                    .setEntities( new Class[] {MosaicCase2.class, MosaicCaseEvent2.class, MosaicCaseKeyValue.class} )
                    .create();
            
            // Documents store
            fsManager = new StandardFileSystemManager();
            URL config = MosaicRepository2.class.getResource( "vfs_config.xml" );
            fsManager.setConfiguration( config );
            fsManager.init();

            String rootUri = dataDir != null
                    ? "file://" + dataDir.getAbsolutePath() + "/org.polymap.mosaic.documents"
                    : "file:///tmp/org.polymap.mosaic.documents";
//            rootUri = System.getProperty( PROP_ROOT_URI );
//            if (rootUri == null) {
//                throw new IllegalStateException( "System property is missing: " + PROP_ROOT_URI + " (allows to set the WebDAV URL of the remote server, i.e. webdav://admin:login@localhost:10080/webdav/Mosaic)" );
//            }
            documentsRoot = fsManager.resolveFile( rootUri );
            documentsRoot.createFolder();
            documentsNameMapper = new SimpleFilesystemMapper();
//            for (FileObject child : documentsRoot.getChildren()) {
//                log.info( "DOCUMENTS: " + child );
//            }

//            // IService and IGeoResource for MosaicCase/Event
//            service = new RServiceImpl( RServiceExtension.toURL( "Mosaic" ), null ) {
//                protected RDataStore getDS() throws IOException {
//                    return datastore;
//                }
//            };
//            geores = new HashMap();
//            for (IGeoResource res : service.resources( null )) {
//                String typename = res.getInfo( null ).getName();
//                if (typename.equals( MosaicCase2.FEATURETYPE_NAME )) {
//                    geores.put( MosaicCase2.class, (RGeoResource)res );                    
//                }
//                else if (typename.equals( MosaicCaseEvent2.FEATURETYPE_NAME )) {
//                    geores.put( MosaicCaseEvent2.class, (RGeoResource)res );                    
//                }
//                else {
//                    throw new IllegalStateException( "Unknown geores: " + typename );
//                }
//            }
//
//            // Qi4j project repo **************************
//            Energy4Java qi4j = Qi4jPlugin.qi4j.get();
//            projectAssempler = new MosaicProjectRepositoryAssembler();
//            
//            ApplicationSPI application = qi4j.newApplication( new ApplicationAssembler() {
//                public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory ) throws AssemblyException {
//                    ApplicationAssembly app = applicationFactory.newApplicationAssembly();
//                    try {
//                        projectAssempler.assemble( app );
//                    }
//                    catch (Exception e) {
//                        throw new RuntimeException( e );
//                    }
//                    return app;
//                }
//            } );
//            projectAssempler.setApp( application );
//            application.activate();
            
//            // OGC/WFS service
//            MosaicProjectRepository r = new MosaicProjectRepository( projectAssempler );
//            final IMap metaDataMap = r.newEntity( IMap.class, "Root_MetaData" );
//            metaDataMap.setLabel( "MetaData" );
//            
//            ILayer caseLayer = r.newEntity( ILayer.class, "Root_Cases" );
//            caseLayer.setLabel( "Cases" );
//            caseLayer.setGeoResource( geores.get( MosaicCase2.class ) );
//            metaDataMap.addLayer( caseLayer );
//            
//            ILayer eventsLayer = r.newEntity( ILayer.class, "Root_Events" );
//            eventsLayer.setLabel( "Events" );
//            eventsLayer.setGeoResource( geores.get( MosaicCaseEvent2.class ) );
//            metaDataMap.addLayer( eventsLayer );
//            r.commitChanges();
//
//            ServiceContext sc = new ServiceContext( "Mosaic.MetaData" ) {
//                private MapHttpServer servlet;
//                @Override
//                protected void start() throws Exception {
//                    servlet = MapHttpServerFactory.createWMS( metaDataMap, "/Mosaic", false );
//                }
//                @Override
//                protected void stop() throws Exception {
//                    MapHttpServerFactory.destroyServer( servlet );
//                }
//                @Override
//                protected boolean needsRestart( EntityStateEvent ev ) throws Exception {
//                    // XXX Auto-generated method stub
//                    throw new RuntimeException( "not yet implemented." );
//                }
//            };
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }        
    }
    
    
    /**
     * Finds data dir from in Workspace or from environment variable and call
     * {@link #init(File, boolean)}.
     */
    public static void init() {
        if (repo == null) {
            synchronized (initlock) {
                if (repo == null) {
                    File dataDir = null;
                    try {
                        dataDir = Polymap.getDataDir();
                    }
                    catch (Exception e) {
                        log.warn( "No Eclipse workbench -> trying env variable: MOSAIC_WORKSPACE_HOME" );
                        String env = System.getenv( "MOSAIC_WORKSPACE_HOME" );
                        if (env != null) {
                            dataDir = new File( env );
                        }
                        
                    }
                    if (dataDir != null) {
                        init( dataDir, false );
                    }
                    else {
                        throw new RuntimeException( "No Workbench and no environment variable specified for Mosaic worspace." );
                    }
                }
            }
        }
    }

    /**
     * Creates a new session associated to an {@link UnitOfWork}.
     * @return Newly created session instance;
     */
    public static MosaicRepository2 newInstance() {
        init();
        return new MosaicRepository2();
    }
    
    /**
     * The instance of the current user SessionContext.
     */
    public static MosaicRepository2 instance() {
        init();
        return instance( MosaicRepository2.class );
    }
    
    
    // instance *******************************************
    
    private FeatureSource           cases;

    private UnitOfWork              uow;
    
    private String                  user = "defaultUser";
    
//    private MosaicProjectRepository projectRepo;
    
    
    protected MosaicRepository2() {        
        uow = repo.newUnitOfWork();
        sessions.put( uow, this );
//        projectRepo = new MosaicProjectRepository( projectAssempler );
        
//        // test/default entries
//        long storeSize = lucenestore.storeSizeInByte();
//        newCase( "FirstCase", null );
//        commitChanges();
    }

    
    /**
     * 
     * @deprecated Exposed for tests and local impl. Remove later. 
     * @param type
     * @throws IOException
     */
    public FeatureSource featureSource( Class<?> type ) {
        try {
            if (IMosaicCase.class.isAssignableFrom( type )) {
                return datastore.getFeatureSource( new NameImpl( MosaicCase2.FEATURETYPE_NAME ) );
            }
            else if (IMosaicCaseEvent.class.isAssignableFrom( type )) {
                return datastore.getFeatureSource( new NameImpl( MosaicCaseEvent2.FEATURETYPE_NAME ) );
            }
            else if (MosaicCaseKeyValue.class.isAssignableFrom( type )) {
                return datastore.getFeatureSource( new NameImpl( MosaicCaseKeyValue.FEATURETYPE_NAME ) );
            }
            else {
                throw new IllegalArgumentException();
            }
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    
    
    public MosaicProjectRepository projectRepo() {
        throw new RuntimeException( "Project support commented out." );
        //return projectRepo;
    }
    
    
    public <T extends Entity> T entity( Class<T> type, String id ) {
        return uow.entity( type, id );
    }

    
    public <T extends Entity> T entityForState( Class<T> type, Object state ) {
        return uow.entityForState( type, state );
    }

    
    public <T extends Entity> Query<T> query( Class<T> type, Filter filter ) {
        return uow.query( type, filter );
    }


    public IMosaicCaseEvent newCaseEvent( final IMosaicCase mcase, final String name, final String description, final String eventType ) {
        assert mcase != null;
        assert name != null : "Event name must not be null."; 
        assert eventType != null : "Event type must not be null."; 
        
        MosaicCaseEvent2 result = newEntity( MosaicCaseEvent2.class, null, new EntityCreator<MosaicCaseEvent2>() {
            public void create( MosaicCaseEvent2 prototype ) throws Exception {
                prototype.name.set( name );
                prototype.description.set( description );
                prototype.type.set( eventType );
                prototype.timestamp.set( new Date() );
                prototype.user.set( user );
                mcase.addEvent( prototype );
            }
        });
        
        PropertyChangeEvent ev = new PropertyChangeEvent( mcase, "events", null, null );
        EventManager.instance().publish( ev );
        
        return result;
    }

    
    public IMosaicCase newCase( final String name, final String description, String... natures ) {
        //assert name != null && name.length() > 0;
        return newEntity( MosaicCase2.class, null, new EntityCreator<MosaicCase2>() {
            public void create( MosaicCase2 prototype ) throws Exception {
                prototype.name.set( name );
                if (description != null) {
                    prototype.description.set( description );
                }

                Date now = new Date();
                prototype.created.set( now );
                prototype.lastModified.set( now );
                prototype.status.set( IMosaicCaseEvent.TYPE_OPEN );
                IMosaicCaseEvent created = newCaseEvent( prototype, "Angelegt", "Der Vorgang wurde angelegt.", IMosaicCaseEvent.TYPE_NEW );
                prototype.addEvent( created );

//                // metaData project
//                IMap metaDataMap = projectRepo().newEntity( IMap.class, null );
//                metaDataMap.setLabel( "MetaData" );
//                
//                ILayer caseLayer = projectRepo().newEntity( ILayer.class, null );
//                caseLayer.setLabel( "Case" );
//                caseLayer.setGeoResource( geores.get( MosaicCase2.class ) );
//                metaDataMap.addLayer( caseLayer );
//                
//                ILayer eventsLayer = projectRepo().newEntity( ILayer.class, null );
//                eventsLayer.setLabel( "Events" );
//                eventsLayer.setGeoResource( geores.get( MosaicCaseEvent2.class ) );
//                metaDataMap.addLayer( eventsLayer );
//                prototype.metaDataMapId.set( metaDataMap.id() );
//
//                // data project
//                IMap dataMap = projectRepo().newEntity( IMap.class, null );
//                dataMap.setLabel( "Data" );
//                prototype.dataMapId.set( dataMap.id() );
            }
        });
    }

    
    public void closeCase( IMosaicCase mcase, String eventName, String eventDescription ) {
        IMosaicCaseEvent closed = newCaseEvent( mcase, eventName, eventDescription, IMosaicCaseEvent.TYPE_CLOSED );
        mcase.addEvent( closed );
        
        ((MosaicCase2)mcase).lastModified.set( new Date() );
        ((MosaicCase2)mcase).status.set( IMosaicCaseEvent.TYPE_CLOSED );
    }
    

    public IMosaicDocument newDocument( IMosaicCase mcase, String name ) {
        try {
            String path = documentsNameMapper.documentPath( mcase, name );
            FileObject file = documentsRoot.resolveFile( path );
            if (file.exists()) {
                throw new IllegalArgumentException( "Dokument existiert bereits: " + path );
            }
            MosaicDocument doc = new MosaicDocument( file );
            
            ((MosaicCase2)mcase).lastModified.set( new Date() );
            newCaseEvent( mcase, doc.getName(), "Ein neues Dokument wurde angelegt: " + doc.getName() 
                    /*+ ", Typ: " + doc.getContentType()
                    + ", Größe: " + doc.getSize()*/, "Dokument angelegt"  );
            
            return doc;
        }
        catch (FileSystemException e) {
            throw new RuntimeException( e );
        }
    }

    
    public Iterable<IMosaicDocument> documents( IMosaicCase mcase ) {
        try {
            String path = documentsNameMapper.documentPath( mcase, null );
            FileObject dir = documentsRoot.resolveFile( path );
            dir.createFolder();
            return Iterables.transform( Arrays.asList( dir.getChildren() ), MosaicDocument.toDocument );
        }
        catch (FileSystemException e) {
            throw new RuntimeException( e );
        }
    }


    public <T extends Entity> T newEntity( Class<T> type, String id, final EntityCreator creator ) {
        return uow.createEntity( type, id, new ValueInitializer<T>() {
            public T initialize( T value ) throws Exception {
                creator.create( value );
                return value;
            }
        });
    }


    public void commitChanges() {
        try {
            //projectRepo().commitChanges();
            uow.commit();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    
    
    public void rollbackChanges() {
        try {
            //projectRepo().rollbackChanges();
            uow.rollback();
        }
        catch (Exception e) {
            throw new RuntimeException( e );
        }
    }
    
    
    /**
     * Functor for the
     * {@link MosaicRepository2#newEntity(Class, String, EntityCreator)} method.
     */
    public static interface EntityCreator<T> {

        public void create( T prototype ) throws Exception;

    }

}
