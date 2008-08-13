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
import org.exoplatform.ecm.connector.fckeditor.FCKUtils;
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

//TODO: Auto-generated Javadoc
/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Jun 24, 2008
 */

/**
 * The Class LinkConnector.
 */
@URITemplate("/wcmLink/")
public class LinkConnector extends BaseConnector implements ResourceContainer {

  /**
   * Instantiates a new link connector.
   * 
   * @param container the container
   */
  public LinkConnector(ExoContainer container) {
    super(container);
  }

  /**
   * Gets the folders and files.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param jcrPath the jcr path
   * @param currentFolder the current folder
   * @param command the command
   * @param type the type
   * 
   * @return the folders and files
   * 
   * @throws Exception the exception
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/getFoldersAndFiles/")
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getFoldersAndFiles(@QueryParam("repositoryName")
      String repositoryName, @QueryParam("workspaceName")
      String workspaceName, @QueryParam("jcrPath")
      String jcrPath, @QueryParam("currentFolder")
      String currentFolder, @QueryParam("command")
      String command, @QueryParam("type")
      String type) throws Exception {
    Response response = buildXMLDocumentOutput(currentFolder, workspaceName, repositoryName,
        jcrPath, command);
    if (response == null)
      return Response.Builder.ok().build();
    else
      return response;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.wcm.connector.fckeditor.BaseConnector#buildXMLDocumentOutput(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  protected Response buildXMLDocumentOutput(String currentFolder, String workspaceName,
      String repositoryName, String jcrPath, String command) throws Exception {
    Document document = null;
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    Node sharedPortalNode = getSharedPortalNode(repositoryName);
    Node currentPortalNode = getCurrentPortalNode(repositoryName, workspaceName, jcrPath);
    if (currentPortalNode == null)
      return null;
    Node rootNode = currentPortalNode.getParent();
    String currentFolderFullpath = null;
    Node currentNode = null;
    if (currentFolder.length() == 0 || "/".equals(currentFolder)) {
      document = createDocumentForRoot(rootNode, sharedPortalNode, currentPortalNode, command);
    } else {
      currentFolderFullpath = getCurrentFolderFullPath(currentPortalNode, sharedPortalNode,
          currentFolder, jcrPath);
      currentNode = getCurrentNode(repositoryName, workspaceName, currentFolderFullpath);
      Node webContentNode = getWebContentNode(repositoryName, workspaceName, jcrPath);
      if (currentFolderFullpath.equals(currentPortalNode.getPath())
          || currentFolderFullpath.equals(sharedPortalNode.getPath())) {
        document = createDocumentForPortal(currentNode, null, command);
      } else if (currentFolderFullpath.equals(jcrPath)) {
        if (webContentNode != null) {
          document = createDocumentForPortal(webContentNode, null, command);
        } else {
          document = createDocumentForContentStorage(currentNode, command);
        }
      } else {
        document = createDocumentForContentStorage(currentNode, command);
      }
    }
    return Response.Builder.ok(document).mediaType("text/xml").cacheControl(cacheControl).build();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.wcm.connector.fckeditor.BaseConnector#getRootStorageOfPortal(javax.jcr.Node)
   */
  @Override
  protected Node getRootStorageOfPortal(Node portal) throws Exception {
    PortalFolderSchemaHandler folderSchemaHandler = webSchemaConfigService
    .getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    return folderSchemaHandler.getLinkFolder(portal);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.wcm.connector.fckeditor.BaseConnector#getRootStorageOfWebContent(javax.jcr.Node)
   */
  @Override
  protected Node getRootStorageOfWebContent(Node webContent) throws Exception {
    throw new Exception("Unsupported.");
  }

  /* (non-Javadoc)
   * @see org.exoplatform.wcm.connector.fckeditor.BaseConnector#getStorageType()
   */
  @Override
  protected String getStorageType() throws Exception {
    return FCKUtils.LINK_TYPE;
  }

}
