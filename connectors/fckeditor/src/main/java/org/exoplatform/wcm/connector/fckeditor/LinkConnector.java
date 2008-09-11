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
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
 
// TODO: Auto-generated Javadoc
/*
 * Created by The eXo Platform SAS 
 * Author : Anh Do Ngoc 
 * anh.do@exoplatform.com
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
  String currentFolder, @QueryParam("currentPortal")
  String currentPortal, @QueryParam("command")
  String command, @QueryParam("type")
  String type) throws Exception {
    Response response = buildXMLResponseOnExpand(currentFolder, currentPortal, workspaceName,
        repositoryName, jcrPath, command);
    if (response == null)
      return Response.Builder.ok().build();
    else
      return response;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.wcm.connector.fckeditor.BaseConnector#buildXMLResponseOnExpand(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  protected Response buildXMLResponseOnExpand(String currentFolder, String currentPortal,
      String workspaceName, String repositoryName, String jcrPath, String command) throws Exception {
    Node sharedPortal = livePortalManagerService.getLiveSharedPortal(repositoryName,
        localSessionProvider.getSessionProvider(null));
    Node currentPortalNode = getCurrentPortalNode(sharedPortal, repositoryName, workspaceName, jcrPath);
    if (currentFolder.length() == 0 || "/".equals(currentFolder))
      return buildXMLResponseForRoot(currentPortalNode, sharedPortal, command);
    String currentPortalRelPath = "/" + currentPortalNode.getName() + "/";
    String sharePortalRelPath = "/" + sharedPortal.getName() + "/";
    if (!currentPortalNode.getPath().equals(sharedPortal.getPath())
        && currentFolder.startsWith(sharePortalRelPath)) {
      if (currentFolder.equals(sharePortalRelPath)) {
        return buildXMLResponseForPortal(sharedPortal, null, command);
      } else {
        Node currentContentStorageNode = getCorrectContentStorage(sharedPortal, currentFolder);
        return buildXMLResponseForContentStorage(currentContentStorageNode, command);
      }
    } else if (!currentPortalNode.getPath().equals(sharedPortal.getPath())
        && currentFolder.startsWith(currentPortalRelPath)) {
      return buildXMLResponseCommon(currentPortalNode, null, currentFolder, command);
    } else {
      return buildXMLResponseCommon(sharedPortal, null, currentFolder, command);
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.wcm.connector.fckeditor.BaseConnector#getRootContentStorage(javax.jcr.Node)
   */
  @Override
  protected Node getRootContentStorage(Node parentNode) throws Exception {
    try {
      PortalFolderSchemaHandler folderSchemaHandler = webSchemaConfigService
          .getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
      return folderSchemaHandler.getLinkFolder(parentNode);
    } catch (Exception e) {
      throw new Exception("Unsupported.");
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.wcm.connector.fckeditor.BaseConnector#getContentStorageType()
   */
  @Override
  protected String getContentStorageType() throws Exception {
    return FCKUtils.LINK_TYPE;
  }

}
