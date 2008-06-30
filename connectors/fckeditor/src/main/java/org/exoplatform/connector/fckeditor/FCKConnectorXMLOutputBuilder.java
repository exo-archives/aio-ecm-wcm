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
package org.exoplatform.connector.fckeditor;

import java.io.InputStream;
import java.security.AccessControlException;
import java.text.SimpleDateFormat;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 4, 2008  
 */
public abstract class FCKConnectorXMLOutputBuilder extends BaseComponentPlugin {    

  protected static final String EXO_HIDDENABLE = "exo:hiddenable".intern();
  protected static final String NT_FILE = "nt:file".intern();
  protected static final String NT_FOLDER = "nt:folder".intern();
  protected static final String NT_UNSTRUCTURED = "nt:unstructured".intern();  

  protected static final String GET_FILES = "GetFiles".intern();  
  protected static final String GET_FOLDERS = "GetFolders".intern();  
  protected static final String GET_ALL = "GetFoldersAndFiles".intern();
  protected static final String CREATE_FOLDER = "CreateFolder".intern();  

  protected RepositoryService repositoryService;
  protected TemplateService templateService;  
  protected ThreadLocalSessionProviderService sessionProviderService;

  protected static final int FOLDER_CREATED = 0;
  protected static final int FOLDER_EXISTED = 101;    
  protected static final int FOLDER_PERMISSION_CREATING = 103;
  protected static final int UNKNOWN_ERROR = 110;    


  public FCKConnectorXMLOutputBuilder(ExoContainer container) {    
    this.repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    this.templateService = (TemplateService) container.getComponentInstanceOfType(TemplateService.class);
    this.sessionProviderService = (ThreadLocalSessionProviderService) container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class);   
  }

  public abstract Document buildFilesXMLOutput(String repository, String workspace, String currentFolder) throws Exception;  
  public abstract Document buildFoldersAndFilesXMLOutput(String repository, String workspace, String currentFolder) throws Exception;  
  protected abstract String createFileLink(Node node) throws Exception;  
  protected abstract String getFileType(Node node) throws Exception;    

  public Document buildFoldersXMLOutput(String repository, String workspace, String currentFolder) throws Exception {    
    Node currentNode = getNode(repository, workspace, currentFolder);   
    Element root = createRootElement(GET_FOLDERS, currentNode);    
    Document document = root.getOwnerDocument();
    Element foldersElement = document.createElement("Folders");    
    root.appendChild(foldersElement);
    for (NodeIterator iter = currentNode.getNodes(); iter.hasNext();) {      
      Node child = iter.nextNode();     
      if (child.isNodeType(EXO_HIDDENABLE)) continue;
      String folderType = getFolderType(child);      
      if(folderType != null) {        
        Element folder = createFolderElement(document, child, folderType);
        foldersElement.appendChild(folder);
      }            
    }
    return document;
  }   

  public Document buildFoldersXMLOutput(String repository, String workspace, String currentFolder, String newFolderName) throws Exception {    
    Node currentNode = getNode(repository, workspace, currentFolder);
    Element root = createRootElement(CREATE_FOLDER, currentNode);    
    Document document = root.getOwnerDocument();    
    Element error = createErrorElement(document, UNKNOWN_ERROR);    
    if(hasAddNodePermission(currentNode)){         
      try {
        currentNode.getNode(newFolderName);
        error = createErrorElement(document, ErrorMessage.FOLDER_EXISTED);    
      } catch (Exception e) {
        currentNode.addNode(newFolderName, "nt:unstructured");
        error = createErrorElement(document, FOLDER_CREATED);
      }                 
    } else {
      error = createErrorElement(document, FOLDER_PERMISSION_CREATING);
    }    
    currentNode.save();
    root.appendChild(error);
    return document; 
  }

  public Document uploadFile(String repository, String workspace, String currentFolder, String fileName, String mimeType, InputStream fileData) { return null; }        

  protected Element createFolderElement(Document document, Node child, String folderType) throws Exception {        
    String url = createCommonWebdavURL(child);
    Element folder = document.createElement("Folder");
    folder.setAttribute("name", child.getName());
    folder.setAttribute("url", url);
    folder.setAttribute("folderType", folderType);
    return folder;
  }

  protected Element createFileElement(Document document, Node child, String fileType) throws Exception {   
    Element file = document.createElement("File");
    file.setAttribute("name", child.getName());     
    SimpleDateFormat dateFormat = new SimpleDateFormat();
    dateFormat.applyPattern(ISO8601.SIMPLE_DATETIME_FORMAT);    
    file.setAttribute("dateCreated", dateFormat.format(child.getProperty("exo:dateCreated").getDate()));    
    file.setAttribute("dateModified", dateFormat.format(child.getProperty("exo:dateModified").getDate()));      
    file.setAttribute("creator", child.getProperty("exo:owner").getString());
    file.setAttribute("fileType", fileType);
    if (child.isNodeType(NT_FILE)) {         
      long size = child.getNode("jcr:content").getProperty("jcr:data").getLength();      
      file.setAttribute("size", "" + size / 1000);      
      file.setAttribute("url", createFileLink(child));
    } else {
      file.setAttribute("size", "");
      String url = createCommonWebdavURL(child);
      file.setAttribute("url", url);
    }    
    return file;
  }  

  protected Element createErrorElement(Document document, int errorNumber) throws Exception {
    ErrorMessage errorMessage = new ErrorMessage();
    Element error = document.createElement("Error");
    error.setAttribute("number", Integer.toString(errorNumber));
    error.setAttribute("text", errorMessage.getErrorMessage(errorNumber));
    return error;
  }

  protected String getFolderType(Node node) throws Exception {
    //need use a service to get extended folder type for the node
    NodeType nodeType = node.getPrimaryNodeType();    
    String primaryType = nodeType.getName();
    String repository = ((ManageableRepository) node.getSession().getRepository()).getConfiguration().getName();
    if (templateService.getDocumentTemplates(repository).contains(primaryType)) return null;    
    if (NT_UNSTRUCTURED.equals(primaryType) || NT_FOLDER.equals(primaryType)) return primaryType;    
    if (nodeType.isNodeType(NT_UNSTRUCTURED) || nodeType.isNodeType(NT_FOLDER)) {
      //check if the nodetype is exo:videoFolder...
      return primaryType;
    }    
    return primaryType;
  }  

  protected String createCommonWebdavURL(Node node) throws Exception {
    String repository = ((ManageableRepository) node.getSession().getRepository()).getConfiguration().getName();
    String workspace = node.getSession().getWorkspace().getName();
    String currentPath = node.getPath();
    String url = "portal/rest/jcr/" + repository + "/" + workspace + currentPath;
    return url;
  }  

  protected Node getNode(String repository, String workspace, String path) throws Exception {
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);    
    ManageableRepository manageableRepository = repositoryService.getRepository(repository);     
    Session session = sessionProvider.getSession(workspace, manageableRepository);
    return (Node) session.getItem(path);
  }      

  protected Element createRootElement(String command, Node node) throws Exception {
    Document doc = null;
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    doc = builder.newDocument();
    String currentPath = node.getPath();
    if (!currentPath.endsWith("/")) {
      currentPath = currentPath + "/";
    }
    String nodeType = getFolderType(node);
    String url = createCommonWebdavURL(node);
    Element rootElement = doc.createElement("Connector");
    doc.appendChild(rootElement);
    rootElement.setAttribute("command", command);
    rootElement.setAttribute("resourceType", "Node");    
    Element currentFolderElement = doc.createElement("CurrentFolder");
    currentFolderElement.setAttribute("name", node.getName());
    currentFolderElement.setAttribute("folderType", nodeType);
    currentFolderElement.setAttribute("path", currentPath);
    currentFolderElement.setAttribute("url", url);
    rootElement.appendChild(currentFolderElement);
    return rootElement;
  }  

  protected boolean hasAddNodePermission(Node node) throws Exception {
    try {
      ((ExtendedNode)node).checkPermission(PermissionType.ADD_NODE) ;
      return true ;
    } catch (AccessControlException e) { }
    return false ;
  }

}
