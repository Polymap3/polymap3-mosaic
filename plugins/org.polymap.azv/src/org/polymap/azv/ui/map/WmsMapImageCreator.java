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
package org.polymap.azv.ui.map;

import java.util.ArrayList;
import java.util.List;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

import org.geotools.data.ows.Specification;
import org.geotools.data.wms.WMS1_0_0;
import org.geotools.data.wms.WMS1_1_0;
import org.geotools.data.wms.WMS1_1_1;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.response.GetMapResponse;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.ows.ServiceException;
import org.xml.sax.SAXException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.base.Joiner;
import com.vividsolutions.jts.geom.Coordinate;

import org.eclipse.rwt.RWT;

import org.polymap.core.data.util.Geometries;

import org.polymap.openlayers.rap.widget.layers.WMSLayer;

/**
 * 
 *
 * @author <a href="http://www.polymap.de">Falko Bräutigam</a>
 */
public class WmsMapImageCreator {

    private static Log log = LogFactory.getLog( WmsMapImageCreator.class );
    
    private List<Descriptor>            descriptors = new ArrayList();
    
    /**
     * 
     */
    class Descriptor {
        protected URL           wmsUrl;
        protected String[]      layerNames; 
    }
    
    
    public WmsMapImageCreator( Iterable<WMSLayer> layers ) {
        for (WMSLayer layer : layers) {
            try {
                Descriptor descriptor = new Descriptor();
                String wmsUrl = layer.getWmsUrl();
                if (wmsUrl.startsWith( ".." )) {
                    HttpServletRequest request = RWT.getRequest();
                    log.info( "Services port: " + request.getLocalPort() );
                    wmsUrl = "http://localhost:" + request.getLocalPort() + wmsUrl.substring( 2 );
                }
                descriptor.wmsUrl = new URL( wmsUrl );
                descriptor.layerNames = StringUtils.split( layer.getWmsLayers(), ", " );
                descriptors.add( descriptor );
            }
            catch (MalformedURLException e) {
                throw new RuntimeException( e );
            }
        }        
    }
    
    
    public Image createImage( ReferencedEnvelope bbox, int w, int h ) {
        // adjust ratio
        double targetWidth = bbox.getWidth();
        double targetHeight = (bbox.getWidth() / w) * h;
        Coordinate center = bbox.centre();
        double minX = center.x - (targetWidth / 2);
        double maxX = center.x + (targetWidth / 2);
        double minY = center.y - (targetHeight / 2);
        double maxY = center.y + (targetHeight / 2);
        
        // request images
        List<Image> images = new ArrayList();
        for (Descriptor descriptor : descriptors) {
            InputStream in = null;
            try {
                WebMapServer wms = new CustomWMS( descriptor.wmsUrl );
                GetMapRequest getMap = wms.createGetMapRequest();
                getMap.setFormat( "image/png" );
                getMap.setDimensions( w, h );
                getMap.setTransparent( true );

                getMap.setBBox( Joiner.on( "," ).join( minX, minY, maxX, maxY ) );
                getMap.setSRS( Geometries.srs( bbox.getCoordinateReferenceSystem() ) );
                for (String layerName : descriptor.layerNames) {
                    // XXX Polygis(alt) möchte unbedingt mit Windows verbandelt blieben und nutzt Backslashes :(
                    layerName = StringUtils.replace( layerName, "\\\\", "\\" );
                    getMap.addLayer( layerName, (String)null );
                }

                log.info( "WMS URL: " + getMap.getFinalURL() );
                GetMapResponse wmsResponse = wms.issueRequest( getMap );
                byte[] bytes = IOUtils.toByteArray( in = wmsResponse.getInputStream() );
                log.info( "::: " + bytes.length );

                if (bytes.length > 1024) {
                    images.add( ImageIO.read( new ByteArrayInputStream( bytes ) ) );
                }
                else {
                    log.info( "::: " + new String( bytes ) );                        
                }
            }
            catch (Exception e) {
                log.warn( "", e );
            }
            finally {
                IOUtils.closeQuietly( in );
            }
        }
        // combine images
        return images.size() == 1 ? images.get( 0 ) : combineImages( images, w, h );
    }
    

    protected Image combineImages( List<Image> images, int w, int h ) {
        // put images together (MapContext order)
        Graphics2D g = null;
        try {
            // create image
            BufferedImage result = new BufferedImage( w, h, BufferedImage.TYPE_4BYTE_ABGR );
            g = result.createGraphics();

            // rendering hints
            RenderingHints hints = new RenderingHints( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
            hints.add( new RenderingHints( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON ) );
            hints.add( new RenderingHints( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) );
            g.setRenderingHints( hints );

            for (Image image : images) {
                int rule = AlphaComposite.SRC_OVER;
                float alpha = 1f; //((float)entry.getKey().getOpacity()) / 100;
                g.setComposite( AlphaComposite.getInstance( rule, alpha ) );
                g.drawImage( image, 0, 0, null );
            }
            return result;
        }
        finally {
            if (g != null) { g.dispose(); }
        }
    }

    
    /**
     * 
     */
    public static class CustomWMS extends WebMapServer {

        public CustomWMS(URL serverURL) throws IOException, ServiceException, SAXException {
            super(serverURL);
            if (getCapabilities() == null) {
                throw new IOException("Unable to parse capabilities document."); //$NON-NLS-1$
            }
        }
        
        protected void setupSpecifications() {
            specs = new Specification[3];
            specs[0] = new WMS1_0_0();
            specs[1] = new WMS1_1_0();
            specs[2] = new WMS1_1_1();
        }
    }

}
