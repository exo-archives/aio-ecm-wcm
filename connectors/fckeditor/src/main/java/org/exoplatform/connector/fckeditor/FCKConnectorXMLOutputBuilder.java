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

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 4, 2008  
 */
public class FCKConnectorXMLOutputBuilder {    

  private final String EXO_HIDDENABLE = "exo:hiddenable".intern();
  private final String NT_FILE = "nt:file".intern();
  private final String NT_FOLDER = "nt:folder".intern();
  private final String NT_UNSTRUCTURED = "nt:unstructured".intern();

  private final String GET_FILES = "GetFiles".intern();  
  private final String GET_FOLDERS = "FetFolders".intern();  
  private final String GET_ALL = "GetFoldersAndFiles".intern();

  private final String[] MSOFFICE_MIMETYPE = {"application/ppt","application/msword","application/xls"} ; 

  private RepositoryService repositoryService_ ;
  private TemplateService templateService_ ;  
  private ThreadLocalSessionProviderService sessionProviderService_ ;

  public FCKConnectorXMLOutputBuilder(ExoContainer container) {    
    this.repositoryService_ = (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class) ;
    this.templateService_ = (TemplateService) container.getComponentInstanceOfType(TemplateService.class) ;
    this.sessionProviderService_ = (ThreadLocalSessionProviderService)container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class) ;
  }

  public Document buildFilesXMLOutput(String repository,String workspace,String currentFolder) throws Exception {
    Node currentNode = getNode(repository, workspace, currentFolder) ;    
    Element root = createRootElement(GET_FILES, currentNode) ;
    Document document = root.getOwnerDocument();
    Element filesElement = document.createElement("Files") ;
    root.appendChild(filesElement) ;    
    for(NodeIterator iter = currentNode.getNodes();iter.hasNext();) {
      Node child = iter.nextNode() ;
      if(child.isNodeType(EXO_HIDDENABLE)) continue ;
      Element file = createFileElement(document, child) ;
      if(file == null) continue ;
      filesElement.appendChild(file) ;           
    }    
    return document ;
  }    

  public Document buildFoldersXMLOutput(String repository,String workspace,String currentFolder) throws Exception {    
    Node currentNode = getNode(repository, workspace, currentFolder) ;    
    Element root = createRootElement(GET_FOLDERS, currentNode) ;    
    Document document = root.getOwnerDocument();
    Element foldersElement = document.createElement("Folders") ;    
    root.appendChild(foldersElement) ;
    for(NodeIterator iter = currentNode.getNodes();iter.hasNext();) {
      Node child = iter.nextNode();
      if(child.isNodeType(EXO_HIDDENABLE)) continue ;
      Element folder = createFolderElement(document, child) ;
      if(folder == null) continue ;
      foldersElement.appendChild(folder) ;
    }
    return document ;
  }  

  public Document buildFoldersAndFilesXMLOutput(String repository,String workspace,String currentFolder) throws Exception {
    Node currentNode = getNode(repository, workspace, currentFolder) ;    
    Element root = createRootElement(GET_ALL, currentNode) ;
    Document document = root.getOwnerDocument();
    Element foldersElement = document.createElement("Folders") ;
    Element filesElement = document.createElement("Files") ;
    root.appendChild(foldersElement);
    root.appendChild(filesElement) ;
    for(NodeIterator iter = currentNode.getNodes();iter.hasNext();) {
      Node child = iter.nextNode();
      if(child.isNodeType(EXO_HIDDENABLE)) continue ;
      Element folder = createFolderElement(document, child) ;
      if(folder != null){
        foldersElement.appendChild(folder) ;
      }else {
        Element file = createFileElement(document, child) ;
        if(file != null) 
          filesElement.appendChild(file) ;
      }      
    }
    return document ;
  }

  public Document createFolder(String repository,String workspace,String currentFolder,String newFolder) {
    return null ;
  }

  public Document uploadFile(String repository,String workspace,String currentFolder,String fileName,String mimeType,InputStream fileData) {
    return null ;
  }        

  private Element createFileElement(Document document,Node child) throws Exception {
    String fileType = getFileType(child) ;
    if(fileType == null) return null ;           
    Element file = document.createElement("File") ;
    file.setAttribute("name", child.getName()) ;    
    file.setAttribute("dateCreated", child.getProperty("exo:dateCreated").getString()) ;
    file.setAttribute("dateModified", child.getProperty("exo:dateModified").getString()) ;
    file.setAttribute("creator", child.getProperty("exo:owner").getString()) ;
    file.setAttribute("nodeType", fileType) ;
    if(child.isNodeType(NT_FILE)) {         
      long size = child.getNode("jcr:content").getProperty("jcr:data").getLength() ;      
      file.setAttribute("size", "" + size/1000) ;      
      file.setAttribute("url", createFileURL(child)) ;
    }else {
      file.setAttribute("size", "") ;
      String url = createWebdavURL(child) ;
      file.setAttribute("url", url) ;
    }    
    return file ;
  }

  private Element createFolderElement(Document document, Node child) throws Exception {
    String folderType = getFolderType(child) ;
    if(folderType == null) return null ;
    String url = createWebdavURL(child) ;
    Element folder = document.createElement("Folder") ;
    folder.setAttribute("name", child.getName()) ;
    folder.setAttribute("url", url) ;
    folder.setAttribute("nodeType", folderType) ;
    return folder;
  }

  private String getFolderType(Node node) throws Exception {
    //need use a service to get extended folder type for the node
    NodeType nodeType = node.getPrimaryNodeType() ;    
    String primaryType = nodeType.getName();
    String repository = ((ManageableRepository)node.getSession().getRepository()).getConfiguration().getName() ;
    if(templateService_.getDocumentTemplates(repository).contains(primaryType)) return null ;    
    if(NT_UNSTRUCTURED.equals(primaryType) || NT_FOLDER.equals(primaryType)) 
      return primaryType ;    
    if(nodeType.isNodeType(NT_UNSTRUCTURED) || nodeType.isNodeType(NT_FOLDER)) {
      //check if the nodetype is exo:videoFolder...
      return primaryType ;
    }    
    return primaryType ;
  }

  private String getFileType(Node node) throws Exception {    
    if(node.isNodeType(NT_FILE)) {
      if(node.isNodeType("exo:presentationable"))
        return node.getProperty("exo:presentationType").getString() ;
      return NT_FILE ;
    }else {
      String primaryType = node.getPrimaryNodeType().getName() ;
      String repository = ((ManageableRepository)node.getSession().getRepository()).getConfiguration().getName() ;
      if(templateService_.getDocumentTemplates(repository).contains(primaryType)) 
        return primaryType ;
    }
    return null ;
  }


  private String createWebdavURL(Node node) throws Exception {
    String repository = ((ManageableRepository)node.getSession().getRepository()).getConfiguration().getName();
    String workspace = node.getSession().getWorkspace().getName();
    String currentPath = node.getPath() ;
    String url = "portal/rest/jcr/"+repository + "/" + workspace + currentPath ;
    return url ;
  }

  private String createFileURL(Node node) throws Exception {
    String mimeType = node.getNode("jcr:content").getProperty("jcr:mimeType").getString() ;
    boolean isMSOfficeType = false ;
    for(String s:MSOFFICE_MIMETYPE) {
      if(s.endsWith(mimeType)) {
        isMSOfficeType = true;
        break;
      }
    }        
    if(isMSOfficeType)  {
      String repository = ((ManageableRepository)node.getSession().getRepository()).getConfiguration().getName();
      String workspace = node.getSession().getWorkspace().getName();
      String currentPath = node.getPath() ;
      return "portal/rest/lnkproducer/openit.lnk?path=/"+repository + "/"+workspace + currentPath ;
    }    
    return getFolderType(node) ;
  }

  private Node getNode(String repository,String workspace,String path) throws Exception {
    SessionProvider sessionProvider = sessionProviderService_.getSessionProvider(null) ;    
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;    
    Session session = sessionProvider.getSession(workspace, manageableRepository) ;
    return (Node) session.getItem(path) ;
  }      

  private Element createRootElement (String command, Node node) throws Exception {
    Document doc = null;
    DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    doc=builder.newDocument();
    String currentPath = node.getPath() ;
    if(!currentPath.endsWith("/")) {
      currentPath = currentPath + "/" ;
    }
    String nodeType = getFolderType(node) ;
    String url = createWebdavURL(node) ;
    Element rootElement = doc.createElement("Connector");
    doc.appendChild(rootElement);
    rootElement.setAttribute("command",command);
    rootElement.setAttribute("resourceType","Node");    
    Element currentFolderElement = doc.createElement("CurrentFolder");
    currentFolderElement.setAttribute("name",node.getName());
    currentFolderElement.setAttribute("nodeType",nodeType);
    currentFolderElement.setAttribute("path",currentPath);
    currentFolderElement.setAttribute("url", url);
    rootElement.appendChild(currentFolderElement);
    return rootElement ;
  }
}
