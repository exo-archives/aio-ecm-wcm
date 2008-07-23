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
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ecm.connector.fckeditor.FCKFileHandler;
import org.exoplatform.ecm.connector.fckeditor.FCKFolderHandler;
import org.exoplatform.ecm.connector.fckeditor.FCKMessage;
import org.exoplatform.ecm.connector.fckeditor.FCKUtils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

//TODO: Auto-generated Javadoc
/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Jun 26, 2008
 */

/**
 * The Class BaseConnector.
 */
public abstract class BaseConnector {

  /** The live portal manager service. */
  protected LivePortalManagerService          livePortalManagerService;

  /** The web schema config service. */
  protected WebSchemaConfigService            webSchemaConfigService;

  /** The file handler. */
  protected FCKFileHandler                    fileHandler;

  /** The folder handler. */
  protected FCKFolderHandler                  folderHandler;

  /** The fck message. */
  private FCKMessage                          fckMessage;

  /** The local session provider. */
  protected ThreadLocalSessionProviderService localSessionProvider;

  /** The repository service. */
  protected RepositoryService                 repositoryService;

  /**
   * Gets the root storage of portal.
   * 
   * @param node the node
   * 
   * @return the root storage of portal
   * 
   * @throws Exception the exception
   */
  protected abstract Node getRootStorageOfPortal(Node node) throws Exception;

  /**
   * Gets the root storage of web content.
   * 
   * @param node the node
   * 
   * @return the root storage of web content
   * 
   * @throws Exception the exception
   */
  protected abstract Node getRootStorageOfWebContent(Node node) throws Exception;

  /**
   * Gets the storage type.
   * 
   * @return the storage type
   * 
   * @throws Exception the exception
   */
  protected abstract String getStorageType() throws Exception;

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
    fckMessage = new FCKMessage(ExoContainerContext.getCurrentContainer());
    localSessionProvider = (ThreadLocalSessionProviderService) container
    .getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    repositoryService = (RepositoryService) container
    .getComponentInstanceOfType(RepositoryService.class);
  }

  /**
   * Builds the xml document output.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param jcrPath the jcr path
   * @param currentFolder the current folder
   * @param command the command
   * @param type the type
   * 
   * @return the document
   * 
   * @throws Exception the exception
   */
  protected Document buildXMLDocumentOutput(String repositoryName, String workspaceName,
      String jcrPath, String currentFolder, String command, String type) throws Exception {
    Node currentPortalNode = getCurrentPortal(repositoryName, workspaceName, jcrPath);
    Node sharePortalNode = getSharedPortal(repositoryName);
    Node webContent = getWebContentFolder(repositoryName, workspaceName, jcrPath);
    Document document = null;
    if (currentPortalNode == null) {
      return null;
    } else if (currentFolder.equals("/")) {
      document = createDocumentForRoot(repositoryName, workspaceName, jcrPath, command);
    } else {
      String tempPath = currentFolder.replace("/", "");
      String relativePath = null;
      String rootStoragePath = null;
      Node rootStorageOfPortal = null;
      Node rootStorageOfWebContent = null;
      Node currentNode = null;
      if (tempPath.equals(currentPortalNode.getName())) {
        document = createStorageDocument(currentPortalNode, webContent, command);
      } else if (tempPath.equals(sharePortalNode.getName())) {
        document = createStorageDocument(sharePortalNode, null, command);
      } else if (tempPath.startsWith(currentPortalNode.getName())) {
        rootStorageOfPortal = getRootStorageOfPortal(currentPortalNode);
        rootStoragePath = getRootStoragePath(currentPortalNode.getName(), null, rootStorageOfPortal
            .getName());
        if (currentFolder.equals(rootStoragePath)) {
          document = createDocumentForContentStorage(rootStorageOfPortal, command);
        } else if (currentFolder.startsWith(rootStoragePath)) {
          relativePath = currentFolder.replace(rootStoragePath, "");
          currentNode = rootStorageOfPortal.getNode(relativePath);
          document = createDocumentForContentStorage(currentNode, command);
        } else if (currentFolder.equals("/" + currentPortalNode.getName() + "/"
            + webContent.getName() + "/")) {
          document = createStorageDocument(webContent, null, command);
        } else {
          rootStorageOfWebContent = getRootStorageOfWebContent(webContent);
          rootStoragePath = getRootStoragePath(currentPortalNode.getName(), webContent.getName(),
              rootStorageOfWebContent.getName());
          relativePath = currentFolder.replace(rootStoragePath, "");
          if (relativePath == null || relativePath.length() == 0)
            currentNode = rootStorageOfWebContent;
          else
            currentNode = rootStorageOfWebContent.getNode(relativePath);
          document = createDocumentForContentStorage(currentNode, command);
        }
      }
    }
    return document;
  }

  /**
   * Builds the xml document output.
   * 
   * @param newFolderName the new folder name
   * @param currentFolder the current folder
   * @param jcrPath the jcr path
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param command the command
   * @param language the language
   * 
   * @return the document
   * 
   * @throws Exception the exception
   */
  protected Document buildXMLDocumentOutput(String newFolderName, String currentFolder,
      String jcrPath, String repositoryName, String workspaceName, String command, String language)
  throws Exception {
    Node currentPortal = getCurrentPortal(repositoryName, workspaceName, jcrPath);
    Node sharedPortal = getSharedPortal(repositoryName);
    Node webContent = getWebContentFolder(repositoryName, workspaceName, jcrPath);
    Node currentNode = null;
    Node rootStorageOfPortal = null;
    String rootStoragePath = null;
    if (currentFolder.replace("/", "").startsWith(currentPortal.getName())) {
      rootStorageOfPortal = getRootStorageOfPortal(currentPortal);
      rootStoragePath = getRootStoragePath(currentPortal.getName(), null, rootStorageOfPortal
          .getName());
    } else {
      rootStorageOfPortal = getRootStorageOfPortal(sharedPortal);
      rootStoragePath = getRootStoragePath(sharedPortal.getName(), null, rootStorageOfPortal
          .getName());
    }
    Node rootStorageOfWebContent = getRootStorageOfWebContent(webContent);
    String relativePath = null;
    if (currentFolder.equals(rootStoragePath)) {
      currentNode = rootStorageOfPortal;
    } else if (currentFolder.startsWith(rootStoragePath)) {
      relativePath = currentFolder.replace(rootStoragePath, "");
      currentNode = rootStorageOfPortal.getNode(relativePath);
    } else {
      rootStoragePath = getRootStoragePath(currentPortal.getName(), webContent.getName(),
          rootStorageOfWebContent.getName());
      relativePath = currentFolder.replace(rootStoragePath, "");
      if (relativePath == null || relativePath.length() == 0)
        currentNode = rootStorageOfWebContent;
      else
        currentNode = rootStorageOfWebContent.getNode(relativePath);
    }
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    if (!FCKUtils.hasAddNodePermission(currentNode)) {
      Object[] args = { currentNode.getPath() };
      return fckMessage.createMessage(FCKMessage.FOLDER_PERMISSION_CREATING, FCKMessage.ERROR,
          language, args);
    }
    if (currentNode.hasNode(newFolderName)) {
      Object[] args = { currentNode.getPath(), newFolderName };
      return fckMessage.createMessage(FCKMessage.FOLDER_EXISTED, FCKMessage.ERROR, language, args);
    }
    currentNode.addNode(newFolderName, FCKUtils.NT_UNSTRUCTURED);
    currentNode.save();
    return createDocumentForContentStorage(currentNode, command);
  }

  /**
   * Creates the document for root.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param jcrPath the jcr path
   * @param command the command
   * 
   * @return the document
   * 
   * @throws Exception the exception
   */
  private Document createDocumentForRoot(String repositoryName, String workspaceName,
      String jcrPath, String command) throws Exception {
    Node sharedPortal = getSharedPortal(repositoryName);
    Node currentPortal = getCurrentPortal(repositoryName, workspaceName, jcrPath);
    Node rootNode = getSession(repositoryName, workspaceName).getRootNode();
    Document document = null;
    Element rootElement = FCKUtils.createRootElement(command, rootNode, rootNode
        .getPrimaryNodeType().getName());
    document = rootElement.getOwnerDocument();
    Element folders = document.createElement("Folders");
    Element files = document.createElement("Files");
    Element sharedPortalElement = null;
    Element currentPortalElement = null;
    if (sharedPortal != null) {
      sharedPortalElement = folderHandler.createFolderElement(document, sharedPortal, sharedPortal
          .getPrimaryNodeType().getName());
      folders.appendChild(sharedPortalElement);
    }
    if (currentPortal != null && !currentPortal.getPath().equals(sharedPortal.getPath())) {
      currentPortalElement = folderHandler.createFolderElement(document, currentPortal,
          currentPortal.getPrimaryNodeType().getName());
      folders.appendChild(currentPortalElement);
    }
    rootElement.appendChild(folders);
    rootElement.appendChild(files);
    return document;
  }

  /**
   * Creates the storage document.
   * 
   * @param node the node
   * @param webContentNode the web content node
   * @param command the command
   * 
   * @return the document
   * 
   * @throws Exception the exception
   */
  private Document createStorageDocument(Node node, Node webContentNode, String command)
  throws Exception {
    Node storageNode = null;
    try {
      storageNode = getRootStorageOfPortal(node);
    } catch (Exception e) {
      storageNode = getRootStorageOfWebContent(node);
    }
    Element rootElement = FCKUtils.createRootElement(command, node, folderHandler
        .getFolderType(node));
    Document document = rootElement.getOwnerDocument();
    Element folders = document.createElement("Folders");
    Element files = document.createElement("Files");
    Element storageElement = folderHandler.createFolderElement(document, storageNode, storageNode
        .getPrimaryNodeType().getName());
    folders.appendChild(storageElement);
    Element webContentElement = null;
    if (webContentNode != null) {
      webContentElement = folderHandler.createFolderElement(document, webContentNode,
          webContentNode.getPrimaryNodeType().getName());
      folders.appendChild(webContentElement);
    }
    rootElement.appendChild(folders);
    rootElement.appendChild(files);
    return document;
  }

  /**
   * Creates the document for content storage.
   * 
   * @param rootNode the root node
   * @param command the command
   * 
   * @return the document
   * 
   * @throws Exception the exception
   */
  private Document createDocumentForContentStorage(Node rootNode, String command) throws Exception {
    Element rootElement = FCKUtils.createRootElement(command, rootNode, folderHandler
        .getFolderType(rootNode));
    Document document = rootElement.getOwnerDocument();
    Element folders = document.createElement("Foders");
    Element files = document.createElement("Files");
    for (NodeIterator iterator = rootNode.getNodes(); iterator.hasNext();) {
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

  /**
   * Gets the current portal.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param jcrPath the jcr path
   * 
   * @return the current portal
   * 
   * @throws Exception the exception
   */
  private Node getCurrentPortal(String repositoryName, String workspaceName, String jcrPath)
  throws Exception {
    Node sharedPortalNode = getSharedPortal(repositoryName);
    if (sharedPortalNode != null && jcrPath.startsWith(sharedPortalNode.getPath()))
      return sharedPortalNode;
    List<Node> portaNodes = livePortalManagerService.getLivePortals(repositoryName,
        localSessionProvider.getSessionProvider(null));
    for (Node portalNode : portaNodes) {
      String portalPath = portalNode.getPath();
      if (jcrPath.startsWith(portalPath))
        return portalNode;
    }
    return null;
  }

  /**
   * Gets the shared portal.
   * 
   * @param repositoryName the repository name
   * 
   * @return the shared portal
   * 
   * @throws Exception the exception
   */
  private Node getSharedPortal(String repositoryName) throws Exception {
    try {
      Node sharedPortal = livePortalManagerService.getLiveSharedPortal(repositoryName,
          localSessionProvider.getSessionProvider(null));
      return sharedPortal;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Gets the web content folder.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param jcrPath the jcr path
   * 
   * @return the web content folder
   * 
   * @throws Exception the exception
   */
  private Node getWebContentFolder(String repositoryName, String workspaceName, String jcrPath)
  throws Exception {
    Session session = getSession(repositoryName, workspaceName);
    try {
      Node webContent = (Node) session.getItem(jcrPath);
      if ("exo:webContent".equals(webContent.getPrimaryNodeType().getName()))
        return webContent;
      return null;
    } catch (PathNotFoundException e) {
      return null;
    }
  }

  /**
   * Gets the session.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * 
   * @return the session
   * 
   * @throws Exception the exception
   */
  private Session getSession(String repositoryName, String workspaceName) throws Exception {
    ManageableRepository manageableRepository = null;
    if (repositoryName == null) {
      manageableRepository = repositoryService.getCurrentRepository();
    } else {
      manageableRepository = repositoryService.getRepository(repositoryName);
    }
    if (workspaceName == null || workspaceName.length() == 0) {
      workspaceName = manageableRepository.getConfiguration().getDefaultWorkspaceName();
    }
    SessionProvider sessionProvider = localSessionProvider.getSessionProvider(null);
    return sessionProvider.getSession(workspaceName, manageableRepository);
  }

  /**
   * Gets the root storage path.
   * 
   * @param portalName the portal name
   * @param webContent the web content
   * @param storage the storage
   * 
   * @return the root storage path
   * 
   * @throws Exception the exception
   */
  private String getRootStoragePath(String portalName, String webContent, String storage)
  throws Exception {
    if (webContent == null)
      return "/" + portalName + "/" + storage + "/";
    else
      return "/" + portalName + "/" + webContent + "/" + storage + "/";
  }

}
