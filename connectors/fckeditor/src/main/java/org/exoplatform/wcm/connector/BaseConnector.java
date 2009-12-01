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
package org.exoplatform.wcm.connector;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.ecm.connector.fckeditor.FCKFileHandler;
import org.exoplatform.ecm.connector.fckeditor.FCKFolderHandler;
import org.exoplatform.ecm.connector.fckeditor.FCKUtils;
import org.exoplatform.services.cms.link.LinkManager;
import org.exoplatform.services.cms.voting.VotingService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Sep 10, 2008
 */
/**
 * The Class BaseConnector.
 */
public abstract class BaseConnector {

  /** The folder handler. */
  protected FCKFolderHandler                  folderHandler;

  /** The file handler. */
  protected FCKFileHandler                    fileHandler;

  /** The file upload handler. */
  protected FileUploadHandler                 fileUploadHandler;

  /** The repository service. */
  protected RepositoryService                 repositoryService;

  /** The log. */
  private static Log log = ExoLogger.getLogger(BaseConnector.class);

  /** The voting service. */
  protected VotingService votingService;  
  
  /** The link manager. */
  protected LinkManager linkManager;  
  
  /** The live portal manager service. */
  protected LivePortalManagerService          livePortalManagerService;

  /** The web schema config service. */
  protected WebSchemaConfigService            webSchemaConfigService;

  /**
   * Gets the root content storage.
   * 
   * @param node the node
   * @return the root content storage
   * @throws Exception the exception
   */
  protected abstract Node getRootContentStorage(Node node) throws Exception;

  /**
   * Gets the content storage type.
   * 
   * @return the content storage type
   * @throws Exception the exception
   */
  protected abstract String getContentStorageType() throws Exception;

  /**
   * Instantiates a new base connector.
   * 
   * @param container the container
   */
  public BaseConnector(ExoContainer container) {
    livePortalManagerService = (LivePortalManagerService) container.getComponentInstanceOfType(LivePortalManagerService.class);
    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    webSchemaConfigService = (WebSchemaConfigService) container.getComponentInstanceOfType(WebSchemaConfigService.class);
    votingService = (VotingService) container.getComponentInstanceOfType(VotingService.class);
    linkManager = (LinkManager) container.getComponentInstanceOfType(LinkManager.class);
    folderHandler = new FCKFolderHandler(container);
    fileHandler = new FCKFileHandler(container);
    fileUploadHandler = new FileUploadHandler(container);
  }

  /**
   * Builds the xml response on expand.
   * 
   * @param currentFolder the current folder
   * @param runningPortal
   * @param workspaceName the workspace name
   * @param repositoryName the repository name
   * @param jcrPath the jcr path
   * @param command the command
   * @return the response
   * @throws Exception the exception
   */
  protected Response buildXMLResponseOnExpand(String currentFolder,
                                              String runningPortal,
                                              String workspaceName,
                                              String repositoryName,
                                              String jcrPath,
                                              String command) throws Exception {
    SessionProvider sessionProvider = WCMCoreUtils.getSessionProvider();
    Node sharedPortalNode = livePortalManagerService.getLiveSharedPortal(sessionProvider, repositoryName);
    sessionProvider.close();
    Node activePortalNode = getCurrentPortalNode(repositoryName,
                                                 jcrPath,
                                                 runningPortal,
                                                 sharedPortalNode);
    if (currentFolder.length() == 0 || "/".equals(currentFolder))
      return buildXMLResponseForRoot(activePortalNode, sharedPortalNode, command);
    String currentPortalRelPath = "/" + activePortalNode.getName() + "/";
    String sharePortalRelPath = "/" + sharedPortalNode.getName() + "/";
    Node webContent = getWebContent(repositoryName, workspaceName, jcrPath);
    if (!activePortalNode.getPath().equals(sharedPortalNode.getPath())
        && currentFolder.startsWith(sharePortalRelPath)) {
      if (currentFolder.equals(sharePortalRelPath)) {
        return buildXMLResponseForPortal(sharedPortalNode, null, command);
      } else {
        Node currentContentStorageNode = getCorrectContentStorage(sharedPortalNode,
                                                                  null,
                                                                  currentFolder);
        return buildXMLResponseForContentStorage(currentContentStorageNode, command);
      }
    } else if (!activePortalNode.getPath().equals(sharedPortalNode.getPath())
        && currentFolder.startsWith(currentPortalRelPath)) {
      return buildXMLResponseCommon(activePortalNode, webContent, currentFolder, command);
    } else {
      return buildXMLResponseCommon(sharedPortalNode, webContent, currentFolder, command);
    }
  }

  /**
   * Builds the xml response common.
   * 
   * @param activePortal the active portal
   * @param webContent the web content
   * @param currentFolder the current folder
   * @param command the command
   * @return the response
   * @throws Exception the exception
   */
  protected Response buildXMLResponseCommon(Node activePortal,
                                            Node webContent,
                                            String currentFolder,
                                            String command) throws Exception {
    String activePortalRelPath = "/" + activePortal.getName() + "/";
    if (currentFolder.equals(activePortalRelPath))
      return buildXMLResponseForPortal(activePortal, webContent, command);
    if (webContent != null) {
      String webContentRelPath = activePortalRelPath + webContent.getName() + "/";
      if (currentFolder.startsWith(webContentRelPath)) {
        if (currentFolder.equals(webContentRelPath))
          return buildXMLResponseForPortal(webContent, null, command);
        Node contentStorageOfWebContent = getCorrectContentStorage(activePortal,
                                                                   webContent,
                                                                   currentFolder);
        return buildXMLResponseForContentStorage(contentStorageOfWebContent, command);
      }
    }
    Node correctContentStorage = getCorrectContentStorage(activePortal, null, currentFolder);
    return buildXMLResponseForContentStorage(correctContentStorage, command);
  }

  /**
   * Builds the xml response for root.
   * 
   * @param currentPortal the current portal
   * @param sharedPortal the shared portal
   * @param command the command
   * @return the response
   * @throws Exception the exception
   */
  protected Response buildXMLResponseForRoot(Node currentPortal, Node sharedPortal, String command) throws Exception {
    Document document = null;
    Node rootNode = currentPortal.getSession().getRootNode();
    Element rootElement = FCKUtils.createRootElement(command,
                                                     rootNode,
                                                     rootNode.getPrimaryNodeType().getName());
    document = rootElement.getOwnerDocument();
    Element folders = document.createElement("Folders");
    Element files = document.createElement("Files");
    Element sharedPortalElement = null;
    Element currentPortalElement = null;
    if (sharedPortal != null) {
      sharedPortalElement = folderHandler.createFolderElement(document,
                                                              sharedPortal,
                                                              sharedPortal.getPrimaryNodeType()
                                                                          .getName());
      folders.appendChild(sharedPortalElement);
    }
    if (currentPortal != null && !currentPortal.getPath().equals(sharedPortal.getPath())) {
      currentPortalElement = folderHandler.createFolderElement(document,
                                                               currentPortal,
                                                               currentPortal.getPrimaryNodeType()
                                                                            .getName());
      folders.appendChild(currentPortalElement);
    }
    rootElement.appendChild(folders);
    rootElement.appendChild(files);
    return getResponse(document);
  }

  /**
   * Builds the xml response for portal.
   * 
   * @param node the node
   * @param webContent the web content
   * @param command the command
   * @return the response
   * @throws Exception the exception
   */
  protected Response buildXMLResponseForPortal(Node node, Node webContent, String command) throws Exception {
    Node storageNode = getRootContentStorage(node);
    Element rootElement = FCKUtils.createRootElement(command,
                                                     node,
                                                     folderHandler.getFolderType(node));
    Document document = rootElement.getOwnerDocument();
    Element folders = document.createElement("Folders");
    Element files = document.createElement("Files");
    Element storageElement = folderHandler.createFolderElement(document,
                                                               storageNode,
                                                               storageNode.getPrimaryNodeType()
                                                                          .getName());
    folders.appendChild(storageElement);
    Element webContentElement = null;
    if (webContent != null) {
      webContentElement = folderHandler.createFolderElement(document,
                                                            webContent,
                                                            webContent.getPrimaryNodeType()
                                                                      .getName());
      folders.appendChild(webContentElement);
    }
    rootElement.appendChild(folders);
    rootElement.appendChild(files);
    return getResponse(document);
  }

  /**
   * Builds the xml response for content storage.
   * 
   * @param node the node
   * @param command the command
   * @return the response
   * @throws Exception the exception
   */
  protected Response buildXMLResponseForContentStorage(Node node, String command) throws Exception {
    Element rootElement = FCKUtils.createRootElement(command,
                                                     node,
                                                     folderHandler.getFolderType(node));
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

  protected Node getCorrectContentStorage(Node activePortal, Node webContent, String currentFolder) throws Exception {
    if (currentFolder == null || currentFolder.trim().length() == 0)
      return null;
    Node rootContentStorage = null;
    String rootContentStorageRelPath = null;
    if (activePortal != null && webContent == null) {
      rootContentStorage = getRootContentStorage(activePortal);
      rootContentStorageRelPath = "/" + activePortal.getName() + "/" + rootContentStorage.getName()
          + "/";
    } else if (activePortal != null && webContent != null) {
      rootContentStorage = getRootContentStorage(webContent);
      rootContentStorageRelPath = "/" + activePortal.getName() + "/" + webContent.getName() + "/"
          + rootContentStorage.getName() + "/";
    }
    if (currentFolder.equals(rootContentStorageRelPath))
      return rootContentStorage;
    try {
      String correctStorageRelPath = currentFolder.replace(rootContentStorageRelPath, "");
      correctStorageRelPath = correctStorageRelPath.substring(0, correctStorageRelPath.length() - 1);
      return rootContentStorage.getNode(correctStorageRelPath);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Gets the response.
   * 
   * @param document the document
   * @return the response
   */
  protected Response getResponse(Document document) {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    return Response.Builder.ok(document).mediaType("text/xml").cacheControl(cacheControl).build();
  }

  /**
   * Gets the jcr content.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param jcrPath the jcr path
   * @return the jcr content
   * @throws Exception the exception
   */
  protected Node getContent(String repositoryName, String workspaceName, String jcrPath, String NodeTypeFilter, boolean isSystemSession) throws Exception {
    if (jcrPath == null || jcrPath.trim().length() == 0)
      return null;
    SessionProvider sessionProvider = isSystemSession?WCMCoreUtils.getSessionProvider():WCMCoreUtils.getUserSessionProvider();
    Session session = null;
    try {
      ManageableRepository repository = repositoryService.getRepository(repositoryName);
      session = sessionProvider.getSession(workspaceName, repository);
      Node content = (Node) session.getItem(jcrPath);
      if (content.isNodeType("exo:taxonomyLink")) {
    	  content = linkManager.getTarget(content);
      }
      
      if (NodeTypeFilter==null || (NodeTypeFilter!=null && content.isNodeType(NodeTypeFilter)) ) 
    	  return content;
    } catch (Exception e) {
      log.error("Error when perform getContent: ", e.fillInStackTrace());
    } finally {
      if (session != null) session.logout();
      sessionProvider.close();
    }
    return null;
  }

  /**
   * Gets the jcr content.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param jcrPath the jcr path
   * @return the jcr content
   * @throws Exception the exception
   */
  protected Node getContent(String repositoryName, String workspaceName, String jcrPath) throws Exception {
	  return getContent(repositoryName, workspaceName, jcrPath, null, true);
  }
  
  /**
   * Gets the web content.
   * 
   * @param repositoryName the repository name
   * @param workspaceName the workspace name
   * @param jcrPath the jcr path
   * @return the web content
   * @throws Exception the exception
   */
  protected Node getWebContent(String repositoryName, String workspaceName, String jcrPath) throws Exception {
	  return getContent(repositoryName, workspaceName, jcrPath, "exo:webContent", true);
  }
  
  protected Node getCurrentPortalNode(String repositoryName,
                                      String jcrPath,
                                      String runningPortal,
                                      Node sharedPortal) throws Exception {
    if (jcrPath == null || jcrPath.length() == 0)
      return null;
    Node currentPortal = null;
    List<Node> livePortaNodes = new ArrayList<Node>();
    SessionProvider sessionProvider = WCMCoreUtils.getSessionProvider();
    try {
      livePortaNodes = livePortalManagerService.getLivePortals(sessionProvider, repositoryName);
      if (sharedPortal != null)
        livePortaNodes.add(sharedPortal);
      for (Node portalNode : livePortaNodes) {
        String portalPath = portalNode.getPath();
        if (jcrPath.startsWith(portalPath))
          currentPortal = portalNode;
      }
      if (currentPortal == null)
        currentPortal = livePortalManagerService.getLivePortal(sessionProvider, repositoryName, runningPortal);
      
      sessionProvider.close();
      return currentPortal;
    } catch (Exception e) {
      return null;
    }
  }
  
}
