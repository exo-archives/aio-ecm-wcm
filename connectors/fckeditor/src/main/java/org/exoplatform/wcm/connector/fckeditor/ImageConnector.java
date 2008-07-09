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

/*
 * Created by The eXo Platform SAS
 * Author : Anh Do Ngoc
 *          anh.do@exoplatform.com
 * Jun 24, 2008  
 */

@URITemplate("/image/")
public class ImageConnector extends BaseConnector implements ResourceContainer {

  public ImageConnector(ExoContainer container) {
    super(container);
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getFoldersAndFiles/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getFoldersAndFiles(@QueryParam("repositoryName")
      String repositoryName, @QueryParam("workspaceName")
      String workspaceName, @QueryParam("currentFolder")
      String currentFolder, @QueryParam("command")
      String command, @QueryParam("type")
      String type) throws Exception {
    Document document = null;
    document = buildXMLDocumentOuput(repositoryName, workspaceName, currentFolder, command, type);
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(document).mediaType("text/xml").cacheControl(cacheControl).build();
  }
  
}
