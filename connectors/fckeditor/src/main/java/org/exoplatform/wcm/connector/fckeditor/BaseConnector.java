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
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.exoplatform.connector.fckeditor.ErrorMessage;
import org.exoplatform.connector.fckeditor.FCKConnectorXMLOutputBuilder;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/*
 * Created by The eXo Platform SAS
 * Author : Anh Do Ngoc
 *          anh.do@exoplatform.com
 * Jun 26, 2008  
 */

/**
 * The Class BaseConnector.
 */
public abstract class BaseConnector extends FCKConnectorXMLOutputBuilder {

  /** The live portal manager service. */
  protected LivePortalManagerService livePortalManagerService;

  /** The web schema config service. */
  protected WebSchemaConfigService webSchemaConfigService;

  /**
   * Gets the storage.
   * 
   * @param portal the portal
   * 
   * @return the storage
   * 
   * @throws Exception the exception
   */
  protected abstract Node getStorage(Node portal) throws Exception;

  /**
   * Instantiates a new base connector.
   * 
   * @param container the container
   */
  public BaseConnector(ExoContainer container) {
    super(container);    
    livePortalManagerService = (LivePortalManagerService) container.getComponentInstanceOfType(LivePortalManagerService.class);
    webSchemaConfigService = (WebSchemaConfigService) container.getComponentInstanceOfType(WebSchemaConfigService.class);
  }

  /**
   * Builds the folders and files xml output.
   * 
   * @param currentPortalName the current portal name
   * @param currentFolder the current folder
   * 
   * @return the document
   * 
   * @throws Exception the exception
   */
  protected Document buildFoldersAndFilesXMLOutput(String currentPortalName, String currentFolder) throws Exception {
    Node currentPortal = livePortalManagerService.getLivePortal(currentPortalName, sessionProviderService.getSessionProvider(null));
    Node sharePortal = livePortalManagerService.getLiveSharedPortal(sessionProviderService.getSessionProvider(null));        
    Node currentNode = null;
    if (currentFolder == null || currentFolder.equals("/")) {      
      return createDocumentForRoot(currentPortal, sharePortal);
    } else {
      try {
        currentNode = getCurrentFolder(currentPortal, currentFolder);
        if (currentNode == currentPortal) 
          return createDocumentForPortal(currentNode);        
      } catch (Exception e) {        
        currentNode = getCurrentFolder(sharePortal, currentFolder);
        if (currentNode == sharePortal) 
          return createDocumentForPortal(currentNode);        
      }      
    }  
    return createDocumentForStorage(currentNode);
  }

  /**
   * Builds the folders and files xml output.
   * 
   * @param currentPortalName the current portal name
   * @param currentFolder the current folder
   * @param newFolderName the new folder name
   * @param newFolderType the new folder type
   * 
   * @return the document
   * 
   * @throws Exception the exception
   */
  protected Document buildFoldersAndFilesXMLOutput(String currentPortalName, String currentFolder, String newFolderName, String newFolderType) throws Exception {
    Node currentPortal = livePortalManagerService.getLivePortal(currentPortalName, sessionProviderService.getSessionProvider(null));
    Node sharePortal = livePortalManagerService.getLiveSharedPortal(sessionProviderService.getSessionProvider(null));
    Node currentNode = null;    
    Session session = currentPortal.getSession();
    try {
      currentNode = getCurrentFolder(currentPortal, currentFolder);
    } catch (Exception e) {
      currentNode = getCurrentFolder(sharePortal, currentFolder);
    }

    Element root = createRootElement(CREATE_FOLDER, currentNode);    
    Document document = root.getOwnerDocument();    
    Element error = createErrorElement(document, UNKNOWN_ERROR);  

    if(hasAddNodePermission(currentNode)) {
      try {
        currentNode.getNode(newFolderName);
        error = createErrorElement(document, ErrorMessage.FOLDER_EXISTED);
      } catch (Exception e2) {
        currentNode.addNode(newFolderName, newFolderType);
        error = createErrorElement(document, ErrorMessage.FOLDER_CREATED);
        session.save();        
      }  
    } else {
      error = createErrorElement(document, ErrorMessage.FOLDER_PERMISSION_CREATING);
    }
    root.appendChild(error);
    return document;
  }

  @Override
  public Document buildFilesXMLOutput(String repository, String workspace, String currentFolder) throws Exception {
    return null;
  }

  @Override
  protected String createFileLink(Node node) throws Exception {
    return null;
  }

  @Override
  protected String getFileType(Node node) throws Exception {
    if (node.isNodeType(NT_FILE)) {
      if (node.isNodeType("exo:presentationable"))
        return node.getProperty("exo:presentationType").getString();
      return NT_FILE;
    } else {
      String primaryType = node.getPrimaryNodeType().getName();
      String repository = ((ManageableRepository) node.getSession().getRepository()).getConfiguration().getName();
      if (templateService.getDocumentTemplates(repository).contains(primaryType)) 
        return primaryType;
    }
    return null;
  }

  /**
   * Creates the document for root.
   * 
   * @param currentPortal the current portal
   * @param sharePortal the share portal
   * 
   * @return the document
   * 
   * @throws Exception the exception
   */
  private Document createDocumentForRoot(Node currentPortal, Node sharePortal) throws Exception {
    Document document = null;      
    Node rootNode = currentPortal.getParent();
    Element root = createRootElement(GET_ALL, rootNode);
    document = root.getOwnerDocument();
    Element foldersElement = document.createElement("Folders");
    root.appendChild(foldersElement);
    Element livePortalFolderElement = createFolderElement(document, currentPortal, currentPortal.getPrimaryNodeType().getName());
    Element sharedPortalFolderElement = createFolderElement(document, sharePortal, sharePortal.getPrimaryNodeType().getName());
    foldersElement.appendChild(livePortalFolderElement);
    foldersElement.appendChild(sharedPortalFolderElement);
    Element filesElement = document.createElement("Files");
    root.appendChild(filesElement);
    return document;
  }

  /**
   * Creates the document for portal.
   * 
   * @param portal the portal
   * 
   * @return the document
   * 
   * @throws Exception the exception
   */
  private Document createDocumentForPortal(Node portal) throws Exception {
    Element root = createRootElement(GET_ALL, portal);
    Document document = root.getOwnerDocument();
    Element foldersElement = document.createElement("Folders");
    Element filesElement = document.createElement("Files");    
    root.appendChild(foldersElement);
    root.appendChild(filesElement);
    Node storage = getStorage(portal);    
    Element storageElement = createFolderElement(document, storage, storage.getPrimaryNodeType().getName());
    foldersElement.appendChild(storageElement);    
    return document;
  }

  /**
   * Creates the document for storage.
   * 
   * @param portal the current portal
   * @param currentFolder the current folder
   * 
   * @return the document
   * 
   * @throws Exception the exception
   */
  private Document createDocumentForStorage(Node currentFolder) throws Exception {         
    Element root = createRootElement(GET_ALL, currentFolder);
    Document document = root.getOwnerDocument();
    Element foldersElement = document.createElement("Folders");
    Element filesElement = document.createElement("Files");
    root.appendChild(foldersElement);
    root.appendChild(filesElement);
    for (NodeIterator nodeIterator = currentFolder.getNodes(); nodeIterator.hasNext();) {
      Node child = nodeIterator.nextNode();
      if (child.isNodeType(EXO_HIDDENABLE)) continue;
      String folderType = getFolderType(child);
      if (folderType != null) {
        Element folder = createFolderElement(document, child, folderType);
        foldersElement.appendChild(folder);
      } else {
        String fileType = getFileType(child);
        if (fileType != null) {
          Element file = createFileElement(document, child, fileType);
          filesElement.appendChild(file);
        }
      }
    }       
    return document;
  } 

  /**
   * Gets the current folder.
   * 
   * @param portal the portal
   * @param currentFolder the current folder
   * 
   * @return the current folder
   * 
   * @throws Exception the exception
   */
  protected Node getCurrentFolder(Node portal, String currentFolder) throws Exception {
    String portalPath = portal.getPath() + "/";        
    Node storage = getStorage(portal);
    String currentFolderFullPath = null;    
    if (! portalPath.endsWith(currentFolder)) {
      String rootStorageFolder = "/" + portal.getName() + "/" + storage.getName() + "/";
      if (! rootStorageFolder.equals(currentFolder)) {
        currentFolderFullPath = currentFolder.replace(rootStorageFolder, "").substring(0, currentFolder.length() - rootStorageFolder.length() - 1);
        return storage.getNode(currentFolderFullPath);
      }
      return storage;
    }          
    return portal;
  }

}

