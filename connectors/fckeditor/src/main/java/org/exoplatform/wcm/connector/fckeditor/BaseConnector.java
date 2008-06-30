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

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.connector.fckeditor.FCKConnectorXMLOutputBuilder;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

//TODO: Auto-generated Javadoc
/*
 * Created by The eXo Platform SAS
 * Author : Anh Do Ngoc
 *          anh.do@exoplatform.com
 * Jun 26, 2008  
 */

/**
 * The Class BaseConnector.
 */
public abstract class BaseConnector extends FCKConnectorXMLOutputBuilder implements ResourceContainer {

  /** The live portal manager service. */
  protected LivePortalManagerService livePortalManagerService;

  /** The web schema config service. */
  protected WebSchemaConfigService webSchemaConfigService;

  /** The Constant IMAGE_STORAGE. */
  protected static final String IMAGE_STORAGE = "Image";

  /** The Constant DOCUMENT_STORAGE. */
  protected static final String DOCUMENT_STORAGE = "Document";

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
   * Gets the foldes and files.
   * 
   * @param currentPortalName the current portal name
   * @param currentFolder the current folder
   * @param type the type
   * 
   * @return the foldes and files
   * 
   * @throws Exception the exception
   */
  public abstract Response getFoldesAndFiles(
      @QueryParam("CurrentPortal") String currentPortalName, 
      @QueryParam("CurrentFolder") String currentFolder, 
      @QueryParam("Type") String type) throws Exception;

  /**
   * Upload file.
   */
  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/upload/")
  @OutputTransformer(XMLOutputTransformer.class)
  public abstract void uploadFile(); 

  /**
   * Creates the folder.
   * @return TODO
   */
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/createFolder/")
  @OutputTransformer(XMLOutputTransformer.class)  
  public abstract Response createFolder(@QueryParam("CurrentPortal") String currentPortalName, @QueryParam("CurrentFolder") String currentFolder, @QueryParam("Type") String type, @QueryParam("NewFolderName") String newFolderName);

  /* (non-Javadoc)
   * @see org.exoplatform.connector.fckeditor.FCKConnectorXMLOutputBuilder#buildFoldersAndFilesXMLOutput(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Document buildFoldersAndFilesXMLOutput(String currentPortalName, String currentFolder, String type) throws Exception {
    Node currentPortal = livePortalManagerService.getLivePortal(currentPortalName, sessionProviderService.getSessionProvider(null));
    Node sharePortal = livePortalManagerService.getLiveSharedPortal(sessionProviderService.getSessionProvider(null));          
    Document document = null;
    if (currentFolder == null || "/".equalsIgnoreCase(currentFolder)) {          
      document = createDocumentForRoot(currentPortal, sharePortal);
    } else if (currentFolder.replace("/", "").equals(currentPortalName)) {
      document = createDocumentForPortal(currentPortal, type);
    } else if (currentFolder.replace("/", "").equals(sharePortal.getName())) {
      document = createDocumentForPortal(sharePortal, type);
    } else if (currentFolder.startsWith("/" + currentPortalName)) {      
      document = createDocumentForStorage(currentPortal, currentFolder, type);    
    } else {
      document = createDocumentForStorage(sharePortal, currentFolder, type);
    }       
    return document;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.connector.fckeditor.FCKConnectorXMLOutputBuilder#buildFilesXMLOutput(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Document buildFilesXMLOutput(String repository, String workspace, String currentFolder) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.connector.fckeditor.FCKConnectorXMLOutputBuilder#createFileLink(javax.jcr.Node)
   */
  @Override
  protected String createFileLink(Node node) throws Exception {
    return null;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.connector.fckeditor.FCKConnectorXMLOutputBuilder#getFileType(javax.jcr.Node)
   */
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
   * @param type the type
   * 
   * @return the document
   * 
   * @throws Exception the exception
   */
  private Document createDocumentForPortal(Node portal, String type) throws Exception {
    Element root = createRootElement(GET_ALL, portal);
    Document document = root.getOwnerDocument();
    Element foldersElement = document.createElement("Folders");
    Element filesElement = document.createElement("Files");    
    root.appendChild(foldersElement);
    root.appendChild(filesElement);
    Node storage = getStorage(portal, type);    
    Element storageElement = createFolderElement(document, storage, storage.getPrimaryNodeType().getName());
    foldersElement.appendChild(storageElement);    
    return document;
  }

  /**
   * Creates the document for storage.
   * 
   * @param portal the current portal
   * @param currentFolder the current folder
   * @param type the type
   * 
   * @return the document
   * 
   * @throws Exception the exception
   */
  private Document createDocumentForStorage(Node portal, String currentFolder, String type) throws Exception {   
    Node currentNode = getCurrentFolder(portal, currentFolder, type);   
    Element root = createRootElement(GET_ALL, currentNode);
    Document document = root.getOwnerDocument();
    Element foldersElement = document.createElement("Folders");
    Element filesElement = document.createElement("Files");
    root.appendChild(foldersElement);
    root.appendChild(filesElement);
    for (NodeIterator nodeIterator = currentNode.getNodes(); nodeIterator.hasNext();) {
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
   * Gets the storage.
   * 
   * @param portal the portal
   * @param type the type
   * 
   * @return the storage
   * 
   * @throws Exception the exception
   */
  private Node getStorage(Node portal, String type) throws Exception {
    PortalFolderSchemaHandler portalFolderSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    if (DOCUMENT_STORAGE.equals(type)) {
      return portalFolderSchemaHandler.getDocumentStorage(portal);
    } else if (IMAGE_STORAGE.equals(type)) {
      return portalFolderSchemaHandler.getImagesFolder(portal);
    }
    return null;
  }

  private Node getCurrentFolder(Node portal, String currentFolder, String type) throws Exception {
    String portalPath = portal.getPath() + "/";        
    Node storage = getStorage(portal, type);
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