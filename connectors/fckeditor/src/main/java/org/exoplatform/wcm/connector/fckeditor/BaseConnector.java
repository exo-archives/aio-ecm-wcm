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
import javax.jcr.NodeIterator;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.ecm.connector.fckeditor.FCKFileHandler;
import org.exoplatform.ecm.connector.fckeditor.FCKFolderHandler;
import org.exoplatform.ecm.connector.fckeditor.FCKUtils;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Jun 26, 2008
 */

public abstract class BaseConnector {

  protected LivePortalManagerService livePortalManagerService;

  protected WebSchemaConfigService   webSchemaConfigService;

  protected FCKFileHandler           fileHandler;

  protected FCKFolderHandler         folderHandler;

  ThreadLocalSessionProviderService  sessionProvider;

  protected abstract Node getStorage(Node node) throws Exception;

  protected abstract String getStorageType() throws Exception;

  public BaseConnector(ExoContainer container) {
    livePortalManagerService = (LivePortalManagerService) container
    .getComponentInstanceOfType(LivePortalManagerService.class);
    webSchemaConfigService = (WebSchemaConfigService) container
    .getComponentInstanceOfType(WebSchemaConfigService.class);
    fileHandler = new FCKFileHandler(container);
    folderHandler = new FCKFolderHandler(container);
    sessionProvider = (ThreadLocalSessionProviderService) container
    .getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
  }

  protected Document buildXMLDocumentOuput(String repositoryName, String workspaceName,
      String jcrPath, String currentFolder, String command, String type) throws Exception {
    Node currentPortalNode = getCurrentPortal(repositoryName, workspaceName, jcrPath);
    Node sharePortalNode = livePortalManagerService.getLiveSharedPortal(repositoryName,
        sessionProvider.getSessionProvider(null));
    Node webContentNode = getWebContentFolder(repositoryName, workspaceName, jcrPath);
    Document document = null;
    if (currentFolder.equals("/")) {
      document = createDocumentForRoot(currentPortalNode, sharePortalNode, webContentNode);
    } else {
      String tempPath = currentFolder.replace("/", "");
      String relPath = null;
      String rootPath = null;
      Node storage = null;
      Node currentNode = null;
      if (tempPath.equals(currentPortalNode.getName())) {
        document = createStorageDocument(currentPortalNode);
      } else if (tempPath.equals(sharePortalNode.getName())) {
        document = createStorageDocument(sharePortalNode);
      } else if (tempPath.equals(webContentNode.getName())) {
        document = createStorageDocument(webContentNode);
      } else if (tempPath.startsWith(currentPortalNode.getName())) {
        storage = getStorage(currentPortalNode);
        rootPath = getRootStoragePath(currentPortalNode.getName(), storage.getName());
      } else if (tempPath.startsWith(sharePortalNode.getName())) {
        storage = getStorage(sharePortalNode);
        rootPath = getRootStoragePath(sharePortalNode.getName(), storage.getName());
      } else if (tempPath.startsWith(webContentNode.getName())) {
        storage = getStorage(webContentNode);
        rootPath = getRootStoragePath(webContentNode.getName(), storage.getName());
      }
      if (storage != null) {
        relPath = currentFolder.replace(rootPath, "");
        if (relPath == null || relPath.length() == 0)
          currentNode = storage;
        else
          currentNode = storage.getNode(relPath);
        document = createDocumentForContentStorage(currentNode);
      }
    }
    return document;
  }

  private Document createDocumentForRoot(Node currentPortalNode, Node shareLivePortalNode,
      Node webContentNode) throws Exception {
    Node rootNode = shareLivePortalNode.getParent();
    Element rootElement = FCKUtils.createRootElement("", rootNode, folderHandler
        .getFolderType(rootNode));
    Document document = rootElement.getOwnerDocument();
    Element folders = document.createElement("Folders");
    Element files = document.createElement("Files");
    Element sharePortalElement = folderHandler.createFolderElement(document, shareLivePortalNode,
        shareLivePortalNode.getPrimaryNodeType().getName());
    rootElement.appendChild(folders);
    rootElement.appendChild(files);
    if (webContentNode != null) {
      Element currentFolderElement = folderHandler.createFolderElement(document, webContentNode,
          webContentNode.getPrimaryNodeType().getName());
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
      if (currentPortalNode != null
          && !currentPortalNode.getPath().equals(shareLivePortalNode.getPath())) {
        Element currentPortalElement = folderHandler.createFolderElement(document,
            currentPortalNode, currentPortalNode.getPrimaryNodeType().getName());
        folders.appendChild(currentPortalElement);
        folders.appendChild(sharePortalElement);
      } else if (currentPortalNode != null
          && currentPortalNode.getPath().equals(shareLivePortalNode.getPath())) {
        folders.appendChild(sharePortalElement);
      }
    }
    return document;
  }

  private Document createStorageDocument(Node rootNode) throws Exception {
    Node storageNode = getStorage(rootNode);
    Element rootElement = FCKUtils.createRootElement("", rootNode, folderHandler
        .getFolderType(rootNode));
    Document document = rootElement.getOwnerDocument();
    Element folders = document.createElement("Folders");
    Element files = document.createElement("Files");
    Element storageElement = folderHandler.createFolderElement(document, storageNode, storageNode
        .getPrimaryNodeType().getName());
    folders.appendChild(storageElement);
    rootElement.appendChild(folders);
    rootElement.appendChild(files);
    return document;
  }

  protected Document createDocumentForContentStorage(Node currentNode) throws Exception {
    Element rootElement = FCKUtils.createRootElement("", currentNode, folderHandler
        .getFolderType(currentNode));
    Document document = rootElement.getOwnerDocument();
    Element folders = document.createElement("Foders");
    Element files = document.createElement("Files");
    for (NodeIterator iterator = currentNode.getNodes(); iterator.hasNext();) {
      Node child = iterator.nextNode();
      if (child.isNodeType(FCKUtils.EXO_HIDDENABLE))
        continue;
      String folderType = folderHandler.getFolderType(child);
      if (folderType != null) {
        Element folder = folderHandler.createFolderElement(document, child, folderType);
        folders.appendChild(folder);
      }
      String fileType = fileHandler.getFileType(child, getStorageType());
      if (fileType != null) {
        Element file = fileHandler.createFileElement(document, child, fileType);
        files.appendChild(file);
      }
    }
    rootElement.appendChild(folders);
    rootElement.appendChild(files);
    return document;
  }

  private Node getCurrentPortal(String repositoryName, String workspaceName, String jcrPath)
  throws Exception {
    Node shareLivePortalNode = livePortalManagerService.getLiveSharedPortal(repositoryName,
        sessionProvider.getSessionProvider(null));
    if (jcrPath.startsWith(shareLivePortalNode.getPath())) {
      return shareLivePortalNode;
    } else {
      List<Node> livePortaNodels = livePortalManagerService.getLivePortals(repositoryName,
          sessionProvider.getSessionProvider(null));
      for (Node livePortalNode : livePortaNodels) {
        String rootPath = livePortalNode.getPath();
        if (jcrPath.startsWith(rootPath))
          return livePortalNode;
      }
    }
    return null;
  }

  private Node getWebContentFolder(String repositoryName, String workspaceName, String jcrPath)
  throws Exception {
    Node currentPortalNode = getCurrentPortal(repositoryName, workspaceName, jcrPath);
    if (currentPortalNode != null) {
      if (!jcrPath.equals(currentPortalNode.getPath())) {
        String relativePath = jcrPath.substring(currentPortalNode.getPath().length() + 1);
        Node currentFolderNode = currentPortalNode.getNode(relativePath);
        if ("exo:webContent".equals(currentFolderNode.getPrimaryNodeType().getName()))
          ;
        return currentFolderNode;
      }
    }
    return null;
  }

  private String getRootStoragePath(String root, String storage) throws Exception {
    String rootPath = "/" + root + "/" + storage + "/";
    return rootPath;
  }

}
