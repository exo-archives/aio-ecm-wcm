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
public class ImagesXMLOutputBuilder extends FCKConnectorXMLOutputBuilder{

  public ImagesXMLOutputBuilder(ExoContainer container) {
    super(container);
  }

  public Document buildFilesXMLOutput(String repository, String workspace, String currentFolder)
  throws Exception {
    Node currNode = getNode(repository, workspace, currentFolder);
    Element root = createRootElement(GET_FILES, currNode);
    Document document = root.getOwnerDocument();
    Element imagesElement = document.createElement("Images");
    for(NodeIterator iterator = currNode.getNodes(); iterator.hasNext(); ){
      Node child = iterator.nextNode();
      if(child.isNodeType(EXO_HIDDENABLE)) continue;
      Element image = createImageElement(document, child);
      if(image == null) continue;
      imagesElement.appendChild(image);
    }
    return document;
  }

  public Document buildFoldersAndFilesXMLOutput(String repository, String workspace,
      String currentFolder) throws Exception {
    Node currentNode = getNode(repository, workspace, currentFolder) ;    
    Element root = createRootElement(GET_ALL, currentNode) ;
    Document document = root.getOwnerDocument();
    Element foldersElement = document.createElement("Folders") ;
    Element filesElement = document.createElement("Images") ;
    root.appendChild(foldersElement);
    root.appendChild(filesElement) ;
    for(NodeIterator iter = currentNode.getNodes();iter.hasNext();) {
      Node child = iter.nextNode();
      if(child.isNodeType(EXO_HIDDENABLE)) continue ;
      Element folder = createFolderElement(document, child) ;
      if(folder != null){
        foldersElement.appendChild(folder) ;
      }else {
        Element image = createImageElement(document, child) ;
        if(image != null) 
          filesElement.appendChild(image) ;
      }      
    }
    return document ;
  }

  protected String createFileLink(Node node) throws Exception {
    
    return null;
  }

  protected String getFileType(Node node) throws Exception {
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

}
