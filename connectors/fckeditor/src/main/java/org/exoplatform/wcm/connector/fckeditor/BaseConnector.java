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
import javax.jcr.Session;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ecm.connector.fckeditor.FCKFileHandler;
import org.exoplatform.ecm.connector.fckeditor.FCKFolderHandler;
import org.exoplatform.ecm.connector.fckeditor.FCKMessage;
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

  /** The file upload handler. */
  private FileUploadHandler                   fileUploadHandler;

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
    fileUploadHandler = new FileUploadHandler(container);    
    localSessionProvider = (ThreadLocalSessionProviderService) container
    .getComponentInstanceOfType(ThreadLocalSessionProviderService.class);
    repositoryService = (RepositoryService) container
    .getComponentInstanceOfType(RepositoryService.class);
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
   * @return the response
   * 
   * @throws Exception the exception
   */
  protected Response buildXMLDocumentOutput(String newFolderName, String currentFolder,
      String jcrPath, String repositoryName, String workspaceName, String command, String language)
  throws Exception {    
    Node currentPortalNode = getCurrentPortalNode(repositoryName, workspaceName, jcrPath);
    Node sharedPortalNode = getSharedPortalNode(repositoryName);
    String fullPath = getCurrentFolderFullPath(currentPortalNode, sharedPortalNode, currentFolder,
        jcrPath);
    Node currentNode = getCurrentNode(repositoryName, workspaceName, fullPath);
    if (currentFolder.length() != 0 && !currentFolder.equals("/") && !fullPath.equals(jcrPath)
        && !fullPath.equals(currentPortalNode.getPath())) {
      return folderHandler.createNewFolder(currentNode, newFolderName, language);
    }
    return null;
  }

  /**
   * Builds the xml document output.
   * 
   * @param currentFolder the current folder
   * @param workspaceName the workspace name
   * @param repositoryName the repository name
   * @param jcrPath the jcr path
   * @param command the command
   * 
   * @return the response
   * 
   * @throws Exception the exception
   */
  protected Response buildXMLDocumentOutput(String currentFolder, String workspaceName,
      String repositoryName, String jcrPath, String command) throws Exception {
    Document document = null;
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    Node sharedPortalNode = getSharedPortalNode(repositoryName);
    Node currentPortalNode = getCurrentPortalNode(repositoryName, workspaceName, jcrPath);
    Node webContentNode = null;
    Node rootNode = currentPortalNode.getParent();
    String currentFolderFullpath = null;
    Node currentNode = null;
    if (currentFolder.length() == 0 || "/".equals(currentFolder)) {
      document = createDocumentForRoot(rootNode, sharedPortalNode, currentPortalNode, command);
    } else {
      currentFolderFullpath = getCurrentFolderFullPath(currentPortalNode, sharedPortalNode,
          currentFolder, jcrPath);
      currentNode = getCurrentNode(repositoryName, workspaceName, currentFolderFullpath);
      webContentNode = getWebContentNode(currentNode, jcrPath);
      if (currentFolderFullpath.equals(currentPortalNode.getPath())
          || currentFolderFullpath.equals(sharedPortalNode.getPath())) {
        document = createDocumentForPortal(currentNode, webContentNode, command);
      } else if (currentFolderFullpath.equals(jcrPath)) {
        document = createDocumentForPortal(webContentNode, null, command);
      } else {
        document = createDocumentForContentStorage(currentNode, command);
      }
    }
    return Response.Builder.ok(document).mediaType("text/xml").cacheControl(cacheControl).build();
  }

  /**
   * Creates the document for root.
   * 
   * @param rootNode the root node
   * @param sharedPortalNode the shared portal node
   * @param currentPortalNode the current portal node
   * @param command the command
   * 
   * @return the document
   * 
   * @throws Exception the exception
   */
  private Document createDocumentForRoot(Node rootNode, Node sharedPortalNode,
      Node currentPortalNode, String command) throws Exception {
    Document document = null;
    Element rootElement = FCKUtils.createRootElement(command, rootNode, rootNode
        .getPrimaryNodeType().getName());
    document = rootElement.getOwnerDocument();
    Element folders = document.createElement("Folders");
    Element files = document.createElement("Files");
    Element sharedPortalElement = null;
    Element currentPortalElement = null;
    if (sharedPortalNode != null) {
      sharedPortalElement = folderHandler.createFolderElement(document, sharedPortalNode,
          sharedPortalNode.getPrimaryNodeType().getName());
      folders.appendChild(sharedPortalElement);
    }
    if (currentPortalNode != null
        && !currentPortalNode.getPath().equals(sharedPortalNode.getPath())) {
      currentPortalElement = folderHandler.createFolderElement(document, currentPortalNode,
          currentPortalNode.getPrimaryNodeType().getName());
      folders.appendChild(currentPortalElement);
    }
    rootElement.appendChild(folders);
    rootElement.appendChild(files);
    return document;
  }

  /**
   * Creates the document for portal.
   * 
   * @param rootNode the root node
   * @param webContentNode the web content node
   * @param command the command
   * 
   * @return the document
   * 
   * @throws Exception the exception
   */
  private Document createDocumentForPortal(Node rootNode, Node webContentNode, String command)
  throws Exception {
    Node storageNode = null;
    try {
      storageNode = getRootStorageOfPortal(rootNode);
    } catch (Exception e) {
      storageNode = getRootStorageOfWebContent(rootNode);
    }
    Element rootElement = FCKUtils.createRootElement(command, rootNode, folderHandler
        .getFolderType(rootNode));
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
   * Gets the current folder full path.
   * 
   * @param currentPortalNode the current portal node
   * @param sharedPortalNode the shared portal node
   * @param currentFolder the current folder
   * @param jcrPath the jcr path
   * 
   * @return the current folder full path
   * 
   * @throws Exception the exception
   */
  private String getCurrentFolderFullPath(Node currentPortalNode, Node sharedPortalNode,
      String currentFolder, String jcrPath) throws Exception {
    String currentFolderRelativePath = null;
    String currentPortalRelativePath = "/" + currentPortalNode.getName() + "/";
    String sharedPortalRelativePath = "/" + sharedPortalNode.getName() + "/";
    String webContentRelativePath = null;
    String storageRelativePath = null;
    Node rootPortalNode = null;
    Node webContentNode = null;
    Node rootStorageNode = null;
    if (currentPortalNode.getPath().equals(sharedPortalNode.getPath())
        || currentFolder.startsWith(sharedPortalRelativePath)) {
      rootPortalNode = sharedPortalNode;
      currentPortalRelativePath = sharedPortalRelativePath;
    } else
      rootPortalNode = currentPortalNode;
    webContentNode = getWebContentNode(currentPortalNode, jcrPath);
    rootStorageNode = getRootStorageOfPortal(rootPortalNode);
    webContentRelativePath = currentPortalRelativePath + webContentNode.getName() + "/";
    storageRelativePath = currentPortalRelativePath + rootStorageNode.getName() + "/";
    if (currentFolder.equals(currentPortalRelativePath)) {
      return rootPortalNode.getPath();
    } else if (currentFolder.equals(webContentRelativePath)) {
      return webContentNode.getPath();
    } else if (currentFolder.equals(storageRelativePath)) {
      return rootStorageNode.getPath();
    } else if (currentFolder.startsWith(webContentRelativePath)) {
      rootStorageNode = getRootStorageOfWebContent(webContentNode);
      storageRelativePath = webContentRelativePath + rootStorageNode.getName() + "/";
      if (currentFolder.equals(storageRelativePath)) {
        return rootStorageNode.getPath();
      } else {
        currentFolderRelativePath = currentFolder.replaceFirst(storageRelativePath, "");
        return rootStorageNode.getPath() + "/" + currentFolderRelativePath;
      }
    } else if (currentFolder.startsWith(storageRelativePath)) {
      currentFolderRelativePath = currentFolder.replaceFirst(storageRelativePath, "");
      return rootStorageNode.getPath().concat("/").concat(currentFolderRelativePath);
    }
    return null;
  }

  /**
   * Gets the current node.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param fullPath the full path
   * 
   * @return the current node
   * 
   * @throws Exception the exception
   */
  private Node getCurrentNode(String repositoryName, String workspaceName, String fullPath)
  throws Exception {
    Session session = getSession(repositoryName, workspaceName);
    if (fullPath != null && fullPath.length() != 0)
      return (Node) session.getItem(fullPath);
    return null;
  }

  /**
   * Gets the current portal node.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param jcrPath the jcr path
   * 
   * @return the current portal node
   * 
   * @throws Exception the exception
   */
  private Node getCurrentPortalNode(String repositoryName, String workspaceName, String jcrPath)
  throws Exception {
    Node sharedPortalNode = getSharedPortalNode(repositoryName);
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
   * Gets the shared portal node.
   * 
   * @param repositoryName the repository name
   * 
   * @return the shared portal node
   * 
   * @throws Exception the exception
   */
  private Node getSharedPortalNode(String repositoryName) throws Exception {
    try {
      Node sharedPortal = livePortalManagerService.getLiveSharedPortal(repositoryName,
          localSessionProvider.getSessionProvider(null));
      return sharedPortal;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Gets the web content node.
   * 
   * @param superNode the super node
   * @param jcrPath the jcr path
   * 
   * @return the web content node
   * 
   * @throws Exception the exception
   */
  private Node getWebContentNode(Node superNode, String jcrPath) throws Exception {
    String superNodePath = superNode.getPath() + "/";
    String jcrTemp = jcrPath + "/";
    if (superNode != null && jcrTemp.startsWith(superNodePath)) {
      String relativePath = jcrTemp.replaceFirst(superNodePath, "");
      if (relativePath == null || relativePath.length() == 0) {
        return superNode;
      } else {
        Node webContentNode = superNode.getNode(relativePath);
        if ("exo:webContent".equals(webContentNode.getPrimaryNodeType().getName()))
          return webContentNode;
      }
    }
    return null;
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
   * Creates the upload file response.
   * 
   * @param inputStream the input stream
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param currentFolder the current folder
   * @param jcrPath the jcr path
   * @param uploadId the upload id
   * @param language the language
   * @param contentType the content type
   * @param contentLength the content length
   * 
   * @return the response
   * 
   * @throws Exception the exception
   */
  protected Response createUploadFileResponse(InputStream inputStream, String repositoryName,
      String workspaceName, String currentFolder, String jcrPath, String uploadId, String language,
      String contentType, String contentLength) throws Exception {
    Node currentPortalNode = getCurrentPortalNode(repositoryName, workspaceName, jcrPath);
    Node sharedPortalNode = getSharedPortalNode(repositoryName);
    String fullPath = getCurrentFolderFullPath(currentPortalNode, sharedPortalNode, currentFolder,
        jcrPath);
    Session session = getSession(repositoryName, workspaceName);
    Node currentNode = (Node) session.getItem(fullPath);
    if (currentFolder.length() != 0 && !currentFolder.equals("/") && !fullPath.equals(jcrPath)
        && !fullPath.equals(currentPortalNode.getPath())) {
      return fileUploadHandler.upload(uploadId, contentType, Double.parseDouble(contentLength),
          inputStream, currentNode, language);  
    }
    return null;
  }

  /**
   * Creates the process upload response.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param currentFolder the current folder
   * @param jcrPath the jcr path
   * @param action the action
   * @param language the language
   * @param fileName the file name
   * @param uploadId the upload id
   * 
   * @return the response
   * 
   * @throws Exception the exception
   */
  protected Response createProcessUploadResponse(String repositoryName, String workspaceName,
      String currentFolder, String jcrPath, String action, String language, String fileName,
      String uploadId) throws Exception {
    if (FileUploadHandler.SAVE_ACTION.equals(action)) {
      Node currentPortalNode = getCurrentPortalNode(repositoryName, workspaceName, jcrPath);
      Node sharedPortalNode = getSharedPortalNode(repositoryName);
      String fullPath = getCurrentFolderFullPath(currentPortalNode, sharedPortalNode,
          currentFolder, jcrPath);
      Session session = getSession(repositoryName, workspaceName);
      Node currentNode = (Node) session.getItem(fullPath);
      return fileUploadHandler.saveAsNTFile(currentNode, uploadId, fileName, language);
    }
    return fileUploadHandler.control(uploadId, action);
  }

}
