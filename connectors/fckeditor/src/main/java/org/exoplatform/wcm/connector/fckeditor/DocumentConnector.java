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
import org.w3c.dom.Document;

//TODO: Auto-generated Javadoc
/*
 * Created by The eXo Platform SAS
 * Author : Anh Do Ngoc
 *          anh.do@exoplatform.com
 * Jun 24, 2008  
 */

/**
 * The Class DocumentConnector.
 */
@URITemplate("/document/")
public class DocumentConnector extends BaseConnector implements ResourceContainer {

  /**
   * Instantiates a new document connector.
   * 
   * @param container the container
   */
  public DocumentConnector(ExoContainer container) { super(container); }

  /* (non-Javadoc)
   * @see org.exoplatform.wcm.connector.fckeditor.BaseConnector#getFoldesAndFiles(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getFoldersAndFiles/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getFoldesAndFiles(@QueryParam("CurrentPortal") String currentPortalName, @QueryParam("CurrentFolder") String currentFolder, @QueryParam("Type") String type) throws Exception {
    Document document = buildFoldersAndFilesXMLOutput(currentPortalName, currentFolder, DOCUMENT_STORAGE);    
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(document).cacheControl(cacheControl).build();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.wcm.connector.fckeditor.BaseConnector#createFolder()
   */
  @Override  
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/createFolder/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response createFolder(@QueryParam("CurrentPortal") String currentPortalName, @QueryParam("CurrentFolder") String currentFolder, @QueryParam("Type") String type, @QueryParam("NewFolderName") String newFolderName) {
    
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.wcm.connector.fckeditor.BaseConnector#uploadFile()
   */
  @Override
  public void uploadFile() { }

}
