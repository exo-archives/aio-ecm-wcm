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

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.ecm.connector.fckeditor.FCKFileHandler;
import org.exoplatform.ecm.connector.fckeditor.FCKFolderHandler;
import org.exoplatform.ecm.connector.fckeditor.FCKUtils;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

// TODO: Auto-generated Javadoc
/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Jun 26, 2008
 */

/**
 * The Class BaseConnector.
 */
public abstract class BaseConnector {
  
  /** The live portal manager service. */
  private LivePortalManagerService livePortalManagerService;

  /** The web schema config service. */
  private WebSchemaConfigService   webSchemaConfigService;

  /** The file handler. */
  private FCKFileHandler           fileHandler;

  /** The folder handler. */
  private FCKFolderHandler         folderHandler;

  /** The session provider. */
  SessionProvider                  sessionProvider;

  /**
   * Instantiates a new base connector.
   * 
   * @param container the container
   */
  public BaseConnector(ExoContainer container) {
    livePortalManagerService = (LivePortalManagerService) container
        .getComponentInstanceOfType(LivePortalManagerService.class);
    webSchemaConfigService = (WebSchemaConfigService) container
        .getComponentInstanceOfType(WebSchemaConfigService.class);
    fileHandler = new FCKFileHandler(container);
    folderHandler = new FCKFolderHandler(container);
    sessionProvider = SessionProvider.createSystemProvider();
  }

  /**
   * Builds the xml document ouput.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param currentFolder the current folder
   * @param command the command
   * @param type the type
   * 
   * @return the document
   * 
   * @throws Exception the exception
   */
  protected Document buildXMLDocumentOuput(String repositoryName, String workspaceName,
      String currentFolder, String command, String type) throws Exception {
    Node shareLivePortalNode = livePortalManagerService.getLiveSharedPortal(repositoryName,
        sessionProvider);
    Node currentPortalNode = getCurrentPortal(repositoryName, workspaceName, currentFolder);
    Node currentFolderNode = getCurrentFolder(repositoryName, workspaceName, currentFolder);
    Node rootNode = shareLivePortalNode.getParent();
    Element rootElement = FCKUtils.createRootElement(command, rootNode, folderHandler
        .getFolderType(rootNode));
    Document document = rootElement.getOwnerDocument();
    Element folders = document.createElement("Folders");
    Element files = document.createElement("Files");
    Element sharePortalElement = folderHandler.createFolderElement(document, shareLivePortalNode,
        shareLivePortalNode.getPrimaryNodeType().getName());
    rootElement.appendChild(folders);
    rootElement.appendChild(files);
    if (currentFolderNode != null) {
      Element currentFolderElement = folderHandler.createFolderElement(document, currentFolderNode,
          currentFolderNode.getPrimaryNodeType().getName());
      if (currentPortalNode != shareLivePortalNode) {
        Element currentPortalElement = folderHandler.createFolderElement(document,
            currentPortalNode, currentPortalNode.getPrimaryNodeType().getName());
        folders.appendChild(currentFolderElement);
        folders.appendChild(currentPortalElement);
        folders.appendChild(sharePortalElement);
      } else {
        folders.appendChild(currentFolderElement);
        folders.appendChild(sharePortalElement);
      }
    } else {
      if (currentPortalNode != null && currentPortalNode != shareLivePortalNode) {
        Element currentPortalElement = folderHandler.createFolderElement(document,
            currentPortalNode, currentPortalNode.getPrimaryNodeType().getName());
        folders.appendChild(currentPortalElement);
        folders.appendChild(sharePortalElement);
      } else if (currentPortalNode != null && currentPortalNode == shareLivePortalNode) {
        folders.appendChild(sharePortalElement);
      }
    }
    return document;
  }

  /**
   * Gets the current portal.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param currentFolder the current folder
   * 
   * @return the current portal
   * 
   * @throws Exception the exception
   */
  private Node getCurrentPortal(String repositoryName, String workspaceName, String currentFolder)
      throws Exception {
    Node shareLivePortalNode = livePortalManagerService.getLiveSharedPortal(repositoryName,
        sessionProvider);
    if (currentFolder.startsWith(shareLivePortalNode.getPath())) {
      return shareLivePortalNode;
    } else {
      List<Node> livePortaNodels = livePortalManagerService.getLivePortals(repositoryName,
          sessionProvider);
      for (Node livePortalNode : livePortaNodels) {
        if (currentFolder.startsWith(livePortalNode.getPath()))
          return livePortalNode;
      }
    }
    return null;
  }

  /**
   * Gets the current folder.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param currentFolder the current folder
   * 
   * @return the current folder
   * 
   * @throws Exception the exception
   */
  private Node getCurrentFolder(String repositoryName, String workspaceName, String currentFolder)
      throws Exception {
    Node currentPortalNode = getCurrentPortal(repositoryName, workspaceName, currentFolder);
    if (currentPortalNode != null) {
      if (!currentFolder.equals(currentPortalNode.getPath())) {
        String relativePath = currentFolder.replace(currentPortalNode.getPath(), "");
        Node currentFolderNode = currentPortalNode.getNode(relativePath);
        if ("exo:webContent".equals(currentFolderNode.getPrimaryNodeType()))
          return currentFolderNode;
      }
    }
    return null;
  }

}
