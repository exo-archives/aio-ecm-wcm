/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wcm.connector.fckeditor;

import javax.jcr.Node;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.w3c.dom.Document;

// TODO: Auto-generated Javadoc
/*
 * Created by The eXo Platform SAS
 * Author : Anh Do Ngoc
 *          anh.do@exoplatform.com
 * Jun 24, 2008  
 */

/**
 * The Class ImageConnector.
 */
@URITemplate("/image/")
public class ImageConnector extends BaseConnector implements ResourceContainer {

  /**
   * Instantiates a new image connector.
   * 
   * @param container the container
   */
  public ImageConnector(ExoContainer container) { super(container); }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getFoldersAndFiles/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getFoldesAndFiles(@QueryParam("CurrentPortal") String currentPortalName, @QueryParam("CurrentFolder") String currentFolder, @QueryParam("Type") String type) throws Exception {
    Document document = buildFoldersAndFilesXMLOutput(currentPortalName, currentFolder);    
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(document).cacheControl(cacheControl).build();
  }


  @Override
  protected Node getStorage(Node portal) throws Exception {
    PortalFolderSchemaHandler portalFolderSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    return portalFolderSchemaHandler.getImagesFolder(portal);
  }

  @Override
  public Document buildFoldersAndFilesXMLOutput(String repository, String workspace, String currentFolder) throws Exception {
    return null;
  }
}
