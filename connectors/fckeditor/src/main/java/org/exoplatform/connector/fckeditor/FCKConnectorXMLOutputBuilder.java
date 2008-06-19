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
import org.exoplatform.container.component.BaseComponentPlugin;
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
public abstract class FCKConnectorXMLOutputBuilder extends BaseComponentPlugin{    

  protected final String EXO_HIDDENABLE = "exo:hiddenable".intern();
  protected final String NT_FILE = "nt:file".intern();
  protected final String NT_FOLDER = "nt:folder".intern();
  protected final String NT_UNSTRUCTURED = "nt:unstructured".intern();

  protected final String GET_FILES = "GetFiles".intern();  
  protected final String GET_FOLDERS = "FetFolders".intern();  
  protected final String GET_ALL = "GetFoldersAndFiles".intern();  
  
  protected RepositoryService repositoryService_ ;
  protected TemplateService templateService_ ;  
  protected ThreadLocalSessionProviderService sessionProviderService_ ;

  public FCKConnectorXMLOutputBuilder(ExoContainer container) {    
    this.repositoryService_ = (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class) ;
    this.templateService_ = (TemplateService) container.getComponentInstanceOfType(TemplateService.class) ;
    this.sessionProviderService_ = (ThreadLocalSessionProviderService)container.getComponentInstanceOfType(ThreadLocalSessionProviderService.class) ;
  }
  
  public abstract Document buildFilesXMLOutput(String repository,String workspace,String currentFolder) throws Exception ;  
  public abstract Document buildFoldersAndFilesXMLOutput(String repository,String workspace,String currentFolder) throws Exception ;  
  protected abstract String createFileLink(Node node) throws Exception ;  
  protected abstract String getFileType(Node node) throws Exception ;    

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
  
  public Document createFolder(String repository,String workspace,String currentFolder,String newFolder) {
    return null ;
  }

  public Document uploadFile(String repository,String workspace,String currentFolder,String fileName,String mimeType,InputStream fileData) {
    return null ;
  }        

  protected Element createFileElement(Document document,Node child) throws Exception {
    String fileType = getFileType(child) ;
    Element file = document.createElement("File") ;
    file.setAttribute("name", child.getName()) ;    
    file.setAttribute("dateCreated", child.getProperty("exo:dateCreated").getString()) ;
    file.setAttribute("dateModified", child.getProperty("exo:dateModified").getString()) ;
    file.setAttribute("creator", child.getProperty("exo:owner").getString()) ;
    file.setAttribute("fileType", fileType) ;
    if(child.isNodeType(NT_FILE)) {         
      long size = child.getNode("jcr:content").getProperty("jcr:data").getLength() ;      
      file.setAttribute("size", "" + size/1000) ;      
      file.setAttribute("url",createFileLink(child)) ;
    }else {
      file.setAttribute("size", "") ;
      String url = createCommonWebdavURL(child) ;
      file.setAttribute("url", url) ;
    }    
    return file ;
  }
  
  protected Element createImageElement(Document document, Node child) throws Exception{
    String fileType = getFileType(child );
    Element image = document.createElement("Image");
    image.setAttribute("name", child.getName());
    image.setAttribute("dateCreated", child.getProperty("exo:dateCreated").getString());
    image.setAttribute("dateModified", child.getProperty("exo:dateModified").getString());
    image.setAttribute("creator", child.getProperty("exo:owner").getString());
    image.setAttribute("fileType", fileType);
    if(child.isNodeType(NT_FILE)) {         
      long size = child.getNode("jcr:content").getProperty("jcr:data").getLength() ;      
      image.setAttribute("size", "" + size/1000) ;      
      image.setAttribute("url",createFileLink(child)) ;
    }else {
      image.setAttribute("size", "") ;
      String url = createCommonWebdavURL(child) ;
      image.setAttribute("url", url) ;
    }    
    return image;
  }

  protected Element createFolderElement(Document document, Node child) throws Exception {
    String folderType = getFolderType(child) ;
    if(folderType == null) return null ;
    String url = createCommonWebdavURL(child) ;
    Element folder = document.createElement("Folder") ;
    folder.setAttribute("name", child.getName()) ;
    folder.setAttribute("url", url) ;
    folder.setAttribute("nodeType", folderType) ;
    return folder;
  }

  protected String getFolderType(Node node) throws Exception {
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
  
  protected String createCommonWebdavURL(Node node) throws Exception {
    String repository = ((ManageableRepository)node.getSession().getRepository()).getConfiguration().getName();
    String workspace = node.getSession().getWorkspace().getName();
    String currentPath = node.getPath() ;
    String url = "portal/rest/jcr/"+repository + "/" + workspace + currentPath ;
    return url ;
  }  
    
  protected Node getNode(String repository,String workspace,String path) throws Exception {
    SessionProvider sessionProvider = sessionProviderService_.getSessionProvider(null) ;    
    ManageableRepository manageableRepository = repositoryService_.getRepository(repository) ;    
    Session session = sessionProvider.getSession(workspace, manageableRepository) ;
    return (Node) session.getItem(path) ;
  }      

  protected Element createRootElement (String command, Node node) throws Exception {
    Document doc = null;
    DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    doc=builder.newDocument();
    String currentPath = node.getPath() ;
    if(!currentPath.endsWith("/")) {
      currentPath = currentPath + "/" ;
    }
    String nodeType = getFolderType(node) ;
    String url = createCommonWebdavURL(node) ;
    Element rootElement = doc.createElement("Connector");
    doc.appendChild(rootElement);
    rootElement.setAttribute("command",command);
    rootElement.setAttribute("resourceType","Node");    
    Element currentFolderElement = doc.createElement("CurrentFolder");
    currentFolderElement.setAttribute("name",node.getName());
    currentFolderElement.setAttribute("folderType",nodeType);
    currentFolderElement.setAttribute("path",currentPath);
    currentFolderElement.setAttribute("url", url);
    rootElement.appendChild(currentFolderElement);
    return rootElement ;
  }
}
