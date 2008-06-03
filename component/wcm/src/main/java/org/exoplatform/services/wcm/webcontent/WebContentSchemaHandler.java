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
package org.exoplatform.services.wcm.webcontent;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.wcm.core.BaseWebSchemaHandler;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 28, 2008  
 */
public class WebContentSchemaHandler extends BaseWebSchemaHandler {

  protected String getHandlerNodeType() { return "nt:file"; }  
  protected String getParentNodeType() { return "exo:webFolder"; }
  
  public void process(Node file) throws Exception {
    Session session = file.getSession();    
    Node webFolder = file.getParent();
    String fileName = file.getName();
    //create temp folder
    addMixin(file, "exo:htmlFile") ;
    file.setProperty("exo:presentationType","exo:htmlFile");    
    String tempFolderName = fileName + this.hashCode() ;
    Node tempFolder = webFolder.addNode(tempFolderName, NT_UNSTRUCTURED) ;    
    String newPath = tempFolder.getPath() + "/" +file.getName();        
    session.move(file.getPath(),newPath);    
    //rename the folder
    String realWebContentPath = webFolder.getPath() + "/" + fileName ;
    session.move(tempFolder.getPath(),realWebContentPath) ;
    Node webContent = (Node)session.getItem(realWebContentPath) ;
    addMixin(webContent, "exo:webContent") ; 
    addMixin(webContent, EXO_OWNABLE) ;
    webContent.setProperty("exo:presentationType","exo:webContent") ;       
    createSchema(webContent) ;
    session.save();
  }
  
  private void createSchema(Node webContent) throws Exception {
    Node jsFolder = webContent.addNode("js","exo:jsFolder") ;
    addMixin(jsFolder, EXO_OWNABLE) ;    
    Node cssFolder = webContent.addNode("css","exo:cssFolder") ;
    addMixin(cssFolder,EXO_OWNABLE);
    Node multimedia = webContent.addNode("multimedia",NT_FOLDER);
    addMixin(multimedia, EXO_OWNABLE) ;
    addMixin(multimedia, "exo:multimediaFolder") ;    
    Node images = multimedia.addNode("images",NT_FOLDER);
    addMixin(images, EXO_OWNABLE) ;
    addMixin(images, "exo:pictureFolder") ;        
    Node video = multimedia.addNode("videos",NT_FOLDER);    
    addMixin(video, EXO_OWNABLE) ;
    addMixin(video, "exo:videoFolder") ;    
    Node audio = multimedia.addNode("audio",NT_FOLDER);
    addMixin(audio, EXO_OWNABLE) ;
    addMixin(audio, "exo:musicFolder") ;    
    Node document = webContent.addNode("documents",NT_UNSTRUCTURED) ;       
    addMixin(document, EXO_OWNABLE) ;
    addMixin(document, "exo:documentFolder") ;
  }      

}
