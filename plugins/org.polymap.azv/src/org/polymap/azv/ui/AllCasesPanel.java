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
package org.polymap.azv.ui;

import static org.apache.commons.lang.StringUtils.defaultString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opengis.filter.Filter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.collect.Iterables;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;

import org.polymap.core.data.ui.featuretable.DefaultFeatureTableColumn;
import org.polymap.core.data.ui.featuretable.FeatureTableFilterBar;
import org.polymap.core.data.ui.featuretable.FeatureTableSearchField;
import org.polymap.core.data.ui.featuretable.IFeatureTableElement;
import org.polymap.core.runtime.IMessages;
import org.polymap.core.runtime.event.EventFilter;
import org.polymap.core.runtime.event.EventHandler;
import org.polymap.core.security.SecurityUtils;
import org.polymap.core.security.UserPrincipal;
import org.polymap.core.ui.FormDataFactory;
import org.polymap.core.ui.FormLayoutFactory;

import org.polymap.rhei.batik.BatikPlugin;
import org.polymap.rhei.batik.Context;
import org.polymap.rhei.batik.ContextProperty;
import org.polymap.rhei.batik.DefaultPanel;
import org.polymap.rhei.batik.IAppContext;
import org.polymap.rhei.batik.IPanel;
import org.polymap.rhei.batik.IPanelSite;
import org.polymap.rhei.batik.PanelIdentifier;
import org.polymap.rhei.batik.PropertyAccessEvent;
import org.polymap.rhei.batik.toolkit.ConstraintLayout;
import org.polymap.rhei.um.ui.LoginPanel;

import org.polymap.azv.AzvPlugin;
import org.polymap.azv.Messages;
import org.polymap.azv.model.NutzerMixin;
import org.polymap.azv.ui.entsorgung.EntsorgungCasesDecorator;
import org.polymap.azv.ui.leitungsauskunft.LeitungsauskunftCasesDecorator;
import org.polymap.azv.ui.nutzerregistrierung.NutzerCasesDecorator;
import org.polymap.azv.ui.schachtschein.SchachtscheinCasesDecorator;
import org.polymap.mosaic.server.model.IMosaicCase;
import org.polymap.mosaic.server.model2.MosaicRepository2;
import org.polymap.mosaic.ui.MosaicUiPlugin;
import org.polymap.mosaic.ui.casepanel.CasePanel;
import org.polymap.mosaic.ui.casestable.CasesTableViewer;
import org.polymap.mosaic.ui.casestable.ICasesViewerDecorator;


/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class AllCasesPanel
        extends DefaultPanel
        implements IPanel {

    private static Log log = LogFactory.getLog( AllCasesPanel.class );

    public static final PanelIdentifier ID = new PanelIdentifier( "allcases" );

    public static final IMessages       i18n = Messages.forPrefix( "AllCasesPanel" );

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<MosaicRepository2>  repo;

    @Context(scope=MosaicUiPlugin.CONTEXT_PROPERTY_SCOPE)
    private ContextProperty<IMosaicCase>        mcase;

    /** Set by the {@link LoginPanel}. */
    @Context(scope="org.polymap.azv.ui")
    private ContextProperty<UserPrincipal>      user;
    
    private Composite                           contents;

    private CasesTableViewer                    casesViewer;

    private List<ICasesViewerDecorator>         casesViewerDecorators = new ArrayList();

    
    @Override
    public boolean init( IPanelSite site, IAppContext context ) {
        super.init( site, context );
        if (site.getPath().size() == 1) {
            // wait for user to log in, then check permission
            site.setTitle( null );
            user.addListener( this, new EventFilter<PropertyAccessEvent>() {
                public boolean apply( PropertyAccessEvent input ) {
                    return input.getType() == PropertyAccessEvent.TYPE.SET;
                }
            });

            //casesViewerDecorators.add( context.propagate( new MitarbeiterCasesDecorator() ) );
            casesViewerDecorators.add( context.propagate( new NutzerCasesDecorator() ) );
            casesViewerDecorators.add( context.propagate( new SchachtscheinCasesDecorator() ) );
            casesViewerDecorators.add( context.propagate( new LeitungsauskunftCasesDecorator() ) );
            casesViewerDecorators.add( context.propagate( new EntsorgungCasesDecorator() ) );
            return true;
        }
        return false;
    }

    
    @EventHandler(display=true)
    protected void userLoggedIn( PropertyAccessEvent ev ) {
        if (SecurityUtils.isUserInGroup( AzvPlugin.ROLE_MA )) {
            getSite().setTitle( i18n.get( "title" ) );
            getSite().setIcon( BatikPlugin.instance().imageForName( "resources/icons/drawer.png" ) );
        }
    }

    
    @Override
    public void createContents( Composite panelBody ) {
        ((ConstraintLayout)panelBody.getLayout()).marginHeight /= 2;
        int panelHeight = panelBody.getParent().getSize().y;
        log.info( "panelHeight: " + panelHeight );
        contents = getSite().toolkit().createComposite( panelBody );
        contents.setLayout( FormLayoutFactory.defaults().spacing( 5 ).create() );
        
        //Filter filter = ff.equals( ff.property( "status" ), ff.literal( IMosaicCaseEvent.TYPE_OPEN ) );
        casesViewer = new CasesTableViewer( contents, repo.get(), Filter.INCLUDE, SWT.NONE );
        casesViewer.getTable().setLayoutData( FormDataFactory.filled().top( -1 ).height( panelHeight-80 )/*.width( 300 )*/.create() );
        
        casesViewer.addColumn( new UserColumn( casesViewer ) );
        casesViewer.addColumn( new AzvStatusCaseTableColumn( repo.get() ) );
        casesViewer.getColumn( "created" ).sort( SWT.UP );

        casesViewer.addDoubleClickListener( new IDoubleClickListener() {
            public void doubleClick( DoubleClickEvent ev ) {
                IMosaicCase sel = Iterables.getOnlyElement( casesViewer.getSelected() );
                log.info( "CASE: " + sel );
                mcase.set( sel );
                getContext().openPanel( CasePanel.ID );
            }
        });

        // filterBar
        FeatureTableFilterBar filterBar = new FeatureTableFilterBar( casesViewer, contents );
        filterBar.getControl().setLayoutData( FormDataFactory.filled().bottom( casesViewer.getTable() ).right( 50 ).create() );
        
        // decorate viewer
        for (ICasesViewerDecorator deco : casesViewerDecorators) {
            deco.fill( casesViewer, filterBar );
        }
        
        // searchField
        FeatureTableSearchField searchField = new FeatureTableSearchField( 
                casesViewer, contents, Arrays.asList( "name", "created", "user" ) );
        Composite searchCtrl = searchField.getControl();
        searchCtrl.setLayoutData( FormDataFactory.filled()
                .height( 27 ).bottom( casesViewer.getTable() ).left( filterBar.getControl() ).create() );
        
//        for (Control child : searchCtrl.getChildren()) {
//            if (child instanceof Button) {
//                ((Button)child).setImage( BatikPlugin.instance().imageForName( "resources/icons/close.png" ) );
//                ((Button)child).setData( WidgetUtil.CUSTOM_VARIANT, MosaicUiPlugin.CSS_DISCARD );                
//            }
//        }

    }

    
    /**
     * 
     */
    class UserColumn
            extends DefaultFeatureTableColumn {

        public UserColumn( final CasesTableViewer viewer ) {
            super( CasesTableViewer.createDescriptor( "user", String.class ) );
            // viewer.getSchema().getDescriptor( new NameImpl( "user" ) ) );
            setWeight( 2, 140 );
            setHeader( "Kunde/Nutzer" );
            setSortable( false );
            
            setLabelProvider( new ColumnLabelProvider() {
                @Override
                public String getText( Object elm ) {
                    IMosaicCase mc = viewer.entity( ((IFeatureTableElement)elm).fid() );
                    String username = mc.get( NutzerMixin.KEY_USER );
                    //String username = mc.as( NutzerMixin.class ).username.get();
                    return defaultString( username, "-" );
                }
            });
        }
        
    }

    
    @Override
    public PanelIdentifier id() {
        return ID;
    }
    
}
