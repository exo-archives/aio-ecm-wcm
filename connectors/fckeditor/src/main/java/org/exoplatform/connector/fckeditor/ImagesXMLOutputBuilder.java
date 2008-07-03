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

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 9, 2008  
 */
public class ImagesXMLOutputBuilder extends FCKConnectorXMLOutputBuilder {

  private static final String[] IMAGE_MIMETYPE = {"image/gif", "image/jpeg", "image/bmp", "image/png", "image/tiff"};

  public ImagesXMLOutputBuilder(ExoContainer container) { super(container); }

  public Document buildFilesXMLOutput(String repository, String workspace, String currentFolder) throws Exception {
    Node currentNode = getNode(repository, workspace, currentFolder);    
    Element root = createRootElement(GET_FILES, currentNode);    
    Document document = root.getOwnerDocument();
    Element filesElement = document.createElement("Files");
    root.appendChild(filesElement);    
    for (NodeIterator iter = currentNode.getNodes(); iter.hasNext();) {
      Node child = iter.nextNode();      
      if (child.isNodeType(EXO_HIDDENABLE)) continue;
      String fileType = getFileType(child);
      if (fileType != null) {
        Element file = createFileElement(document, child, fileType);
        filesElement.appendChild(file);
      }          
    }    
    return document;
  }    


  public Document buildFoldersAndFilesXMLOutput(String repository, String workspace, String currentFolder) throws Exception {
    Node currentNode = getNode(repository, workspace, currentFolder);    
    Element root = createRootElement(GET_ALL, currentNode);
    Document document = root.getOwnerDocument();
    Element foldersElement = document.createElement("Folders");
    Element filesElement = document.createElement("Files");
    root.appendChild(foldersElement);
    root.appendChild(filesElement);
    for (NodeIterator iter = currentNode.getNodes(); iter.hasNext();) {
      Node child = iter.nextNode();
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

  protected String createFileLink(Node node) throws Exception {        
    String mimeType = node.getNode("jcr:content").getProperty("jcr:mimeType").getString();
    //TODO should use mimetype registry to check
    boolean isImageType = false;
    for (String s : IMAGE_MIMETYPE) {
      if (s.endsWith(mimeType)) {
        isImageType = true;
        break;
      }
    }        
    if (isImageType)  {
      String repository = ((ManageableRepository) node.getSession().getRepository()).getConfiguration().getName();
      String workspace = node.getSession().getWorkspace().getName();
      String currentPath = node.getPath();
      return "portal/rest/lnkproducer/openit.lnk?path=/" + repository + "/" + workspace + currentPath;
    }    
    return createCommonWebdavURL(node); 
  }
    
  protected String getFileType(Node node) throws Exception {
    //get mimetype by resource resolver        
    if (node.isNodeType(NT_FILE)) {      
      String mimeType = node.getNode("jcr:content").getProperty("jcr:mimeType").getString();
      if(isImageMimeType(mimeType))
        return node.getPrimaryNodeType().getName();      
    }     
    return null;
  }  
  
  private boolean isImageMimeType(String mimeType){   
    for (String s : IMAGE_MIMETYPE) {
      if (s.endsWith(mimeType)) {
        return true;        
      }
    }        
    return false;
  }

}
