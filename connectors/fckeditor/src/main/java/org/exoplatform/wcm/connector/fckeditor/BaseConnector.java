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

import java.io.InputStream;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.ecm.connector.fckeditor.FCKFileHandler;
import org.exoplatform.ecm.connector.fckeditor.FCKFolderHandler;
import org.exoplatform.ecm.connector.fckeditor.FCKUtils;
import org.exoplatform.ecm.connector.fckeditor.FileUploadHandler;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Sep 10, 2008
 */
public abstract class BaseConnector {

  private FCKFolderHandler                  folderHandler;

  private FCKFileHandler                    fileHandler;

  private FileUploadHandler                 fileUploadHandler;

  private RepositoryService                 repositoryService;
  
  protected ThreadLocalSessionProviderService localSessionProvider;

  protected LivePortalManagerService          livePortalManagerService;

  protected WebSchemaConfigService          webSchemaConfigService;

  protected abstract Node getRootContentStorage(Node node) throws Exception;

  protected abstract String getContentStorageType() throws Exception;

  public BaseConnector(ExoContainer container) {
    livePortalManagerService = (LivePortalManagerService) container
        .getComponentInstanceOfType(LivePortalManagerService.class);
    localSessionProvider = (ThreadLocalSessionProviderService) container
        .getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    repositoryService = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);
    webSchemaConfigService = (WebSchemaConfigService) container
        .getComponentInstanceOfType(WebSchemaConfigService.class);
    folderHandler = new FCKFolderHandler(container);
    fileHandler = new FCKFileHandler(container);
    fileUploadHandler = new FileUploadHandler(container);
  }

  protected Response buildXMLResponseOnExpand(String currentFolder, String workspaceName,
      String repositoryName, String jcrPath, String command) throws Exception {
    Node sharedPortal = livePortalManagerService.getLiveSharedPortal(repositoryName,
        localSessionProvider.getSessionProvider(null));
    Node currentPortal = getCurrentPortalNode(sharedPortal, repositoryName, workspaceName, jcrPath);
    if (currentFolder.length() == 0 || "/".equals(currentFolder))
      return buildXMLResponseForRoot(currentPortal, sharedPortal, command);
    String currentPortalRelPath = "/" + currentPortal.getName() + "/";
    String sharePortalRelPath = "/" + sharedPortal.getName() + "/";
    Node webContent = getWebContent(repositoryName, workspaceName, jcrPath);
    if (!currentPortal.getPath().equals(sharedPortal.getPath())
        && currentFolder.startsWith(sharePortalRelPath)) {
      if (currentFolder.equals(sharePortalRelPath)) {
        return buildXMLResponseForPortal(sharedPortal, null, command);
      } else {
        Node currentContentStorageNode = getCorrectContentStorage(sharedPortal, currentFolder);
        return buildXMLResponseForContentStorage(currentContentStorageNode, command);
      }
    } else if (!currentPortal.getPath().equals(sharedPortal.getPath())
        && currentFolder.startsWith(currentPortalRelPath)) {
      return buildXMLResponseCommon(currentPortal, webContent, currentFolder, command);
    } else {
      return buildXMLResponseCommon(sharedPortal, webContent, currentFolder, command);
    }
  }

  protected Response buildXMLDocumentOnCreateFolder(String newFolderName, String currentFolder,
      String jcrPath, String repositoryName, String workspaceName, String command, String language)
      throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    Node currentNode = null;
    Node sharedPortal = livePortalManagerService.getLiveSharedPortal(repositoryName,
        localSessionProvider.getSessionProvider(null));
    Node currentPortal = getCurrentPortalNode(sharedPortal, repositoryName, workspaceName, jcrPath);
    Node webContent = getWebContent(repositoryName, workspaceName, jcrPath);
    currentNode = getActiveFolder(currentFolder, currentPortal, sharedPortal, webContent);
    return folderHandler.createNewFolder(currentNode, newFolderName, language);
  }

  protected Response buildXMLResponseCommon(Node activePortal, Node webContent, String currentFolder,
      String command) throws Exception {
    String activePortalRelPath = "/" + activePortal.getName() + "/";
    if (currentFolder.equals(activePortalRelPath))
      return buildXMLResponseForPortal(activePortal, webContent, command);
    if (webContent != null) {
      String webContentRelPath = activePortalRelPath + webContent.getName() + "/";
      if (currentFolder.startsWith(webContentRelPath)) {
        if (currentFolder.equals(webContentRelPath))
          return buildXMLResponseForPortal(webContent, null, command);
        Node contentStorageOfWebContent = getCorrectContentStorage(webContent, currentFolder);
        return buildXMLResponseForContentStorage(contentStorageOfWebContent, command);
      }
    }
    Node correctContentStorage = getCorrectContentStorage(activePortal, currentFolder);
    return buildXMLResponseForContentStorage(correctContentStorage, command);
  }

  protected Response buildXMLResponseForRoot(Node currentPortal, Node sharedPortal, String command)
      throws Exception {
    Document document = null;
    Node rootNode = currentPortal.getSession().getRootNode();
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
    return getResponse(document);
  }

  protected Response buildXMLResponseForPortal(Node node, Node webContent, String command)
      throws Exception {
    Node storageNode = getRootContentStorage(node);
    Element rootElement = FCKUtils.createRootElement(command, node, folderHandler
        .getFolderType(node));
    Document document = rootElement.getOwnerDocument();
    Element folders = document.createElement("Folders");
    Element files = document.createElement("Files");
    Element storageElement = folderHandler.createFolderElement(document, storageNode, storageNode
        .getPrimaryNodeType().getName());
    folders.appendChild(storageElement);
    Element webContentElement = null;
    if (webContent != null) {
      webContentElement = folderHandler.createFolderElement(document, webContent, webContent
          .getPrimaryNodeType().getName());
      folders.appendChild(webContentElement);
    }
    rootElement.appendChild(folders);
    rootElement.appendChild(files);
    return getResponse(document);
  }

  protected Response buildXMLResponseForContentStorage(Node node, String command) throws Exception {
    Element rootElement = FCKUtils.createRootElement(command, node, folderHandler
        .getFolderType(node));
    Document document = rootElement.getOwnerDocument();
    Element folders = document.createElement("Foders");
    Element files = document.createElement("Files");
    for (NodeIterator iterator = node.getNodes(); iterator.hasNext();) {
      Node child = iterator.nextNode();
      if (child.isNodeType(FCKUtils.EXO_HIDDENABLE))
        continue;
      String folderType = folderHandler.getFolderType(child);
      if (folderType != null) {
        Element folder = folderHandler.createFolderElement(document, child, folderType);
        folders.appendChild(folder);
      }
      String sourceType = getContentStorageType();
      String fileType = fileHandler.getFileType(child, sourceType);
      if (fileType != null) {
        Element file = fileHandler.createFileElement(document, child, fileType);
        files.appendChild(file);
      }
    }
    rootElement.appendChild(folders);
    rootElement.appendChild(files);
    return getResponse(document);
  }

  private Node getActiveFolder(String currentFolder, Node currentPortal, Node sharedPortal,
      Node webContent) throws Exception {
    String sharedPortalRelPath = "/" + sharedPortal.getName() + "/";
    String currentPortalRelPath = "/" + currentPortal.getName() + "/";
    Node currentNode = null;
    Node activePortal = null;
    String webContentRelPath = null;
    if (webContent != null)
      webContentRelPath = currentPortalRelPath + webContent.getName() + "/";
    if (webContent != null && currentFolder.startsWith(webContentRelPath)
        && !currentFolder.equals(webContentRelPath)) {
      currentNode = getCorrectContentStorage(webContent, currentFolder);
    } else if ((webContent == null)
        || (webContent != null && !currentFolder.startsWith(webContentRelPath))) {
      if (currentFolder.startsWith(currentPortalRelPath)
          && !currentFolder.equals(currentPortalRelPath))
        activePortal = currentPortal;
      else if (currentFolder.startsWith(sharedPortalRelPath)
          && !currentFolder.equals(sharedPortalRelPath))
        activePortal = sharedPortal;
      currentNode = getCorrectContentStorage(activePortal, currentFolder);
    }
    return currentNode;
  }

  protected Node getCorrectContentStorage(Node node, String currentFolder) throws Exception {
    if (node == null)
      return null;
    Node rootContentStorage = getRootContentStorage(node);
    String rootContentStorageRelPath = null;
    if (!node.getPrimaryNodeType().getName().equals("exo:webContent")) {
      rootContentStorageRelPath = "/" + node.getName() + "/" + rootContentStorage.getName() + "/";
    } else {
      Node parent = node.getParent();
      if (parent.getPrimaryNodeType().isNodeType("exo:webFolder"))
        rootContentStorageRelPath = "/" + parent.getParent().getName() + "/" + node.getName() + "/"
            + rootContentStorage.getName() + "/";
      else
        rootContentStorageRelPath = "/" + parent.getName() + "/" + node.getName() + "/"
            + rootContentStorage.getName() + "/";
    }
    if (currentFolder.equals(rootContentStorageRelPath))
      return rootContentStorage;
    String correctStorageRelPath = currentFolder.replace(rootContentStorageRelPath, "");
    correctStorageRelPath = correctStorageRelPath.substring(0, correctStorageRelPath.length() - 1);
    return rootContentStorage.getNode(correctStorageRelPath);
  }

  private Response getResponse(Document document) {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(document).mediaType("text/xml").cacheControl(cacheControl).build();
  }

  protected Node getWebContent(String repositoryName, String workspaceName, String jcrPath)
      throws Exception {
    Session session = getSession(repositoryName, workspaceName);
    Node webContent = null;
    try {
      webContent = (Node) session.getItem(jcrPath);
      if ("exo:webContent".equals((webContent.getPrimaryNodeType().getName())))
        return webContent;
      else
        return null;
    } catch (PathNotFoundException exception) {
    }
    return null;
  }

  protected Node getCurrentPortalNode(Node sharedPortal, String repositoryName,
      String workspaceName, String jcrPath) throws Exception {
    List<Node> portaNodes = livePortalManagerService.getLivePortals(repositoryName,
        localSessionProvider.getSessionProvider(null));
    for (Node portalNode : portaNodes) {
      String portalPath = portalNode.getPath();
      if (jcrPath.startsWith(portalPath))
        return portalNode;
    }
    return null;
  }

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

  protected Response createUploadFileResponse(InputStream inputStream, String repositoryName,
      String workspaceName, String currentFolder, String jcrPath, String uploadId, String language,
      String contentType, String contentLength) throws Exception {
    Node sharedPortal = livePortalManagerService.getLiveSharedPortal(repositoryName,
        localSessionProvider.getSessionProvider(null));
    Node currentPortal = getCurrentPortalNode(sharedPortal, repositoryName, workspaceName, jcrPath);
    Node webContent = getWebContent(repositoryName, workspaceName, jcrPath);
    Node currentNode = getActiveFolder(currentFolder, currentPortal, sharedPortal, webContent);
    try {
      return fileUploadHandler.upload(uploadId, contentType, Double.parseDouble(contentLength),
          inputStream, currentNode, language);
    } catch (Exception e) {
      return null;
    }
  }

  protected Response createProcessUploadResponse(String repositoryName, String workspaceName,
      String currentFolder, String jcrPath, String action, String language, String fileName,
      String uploadId) throws Exception {
    if (FileUploadHandler.SAVE_ACTION.equals(action)) {
      CacheControl cacheControl = new CacheControl();
      cacheControl.setNoCache(true);
      Node sharedPortal = livePortalManagerService.getLiveSharedPortal(repositoryName,
          localSessionProvider.getSessionProvider(null));
      Node currentPortal = getCurrentPortalNode(sharedPortal, repositoryName, workspaceName,
          jcrPath);
      Node webContent = getWebContent(repositoryName, workspaceName, jcrPath);
      Node currentNode = getActiveFolder(currentFolder, currentPortal, sharedPortal, webContent);
      return fileUploadHandler.saveAsNTFile(currentNode, uploadId, fileName, language);
    }
    return fileUploadHandler.control(uploadId, action);
  }
}