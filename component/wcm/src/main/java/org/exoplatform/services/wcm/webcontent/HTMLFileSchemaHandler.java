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

/*
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * Jun 23, 2008  
 */

public class HTMLFileSchemaHandler extends BaseWebSchemaHandler {

  protected String getHandlerNodeType() {   return "nt:file"; }
  protected String getParentNodeType() { return "exo:webFolder"; }

  public void onCreateNode(final Node file) throws Exception {
    Session session = file.getSession();    
    Node webFolder = file.getParent();
    String fileName = file.getName();
    //create temp folder
    addMixin(file, "exo:htmlFile");
    file.setProperty("exo:presentationType","exo:htmlFile");    
    String tempFolderName = fileName + this.hashCode();
    Node tempFolder = webFolder.addNode(tempFolderName, NT_UNSTRUCTURED);    
    String tempPath = tempFolder.getPath() + "/" +file.getName();        
    session.move(file.getPath(),tempPath);
    session.save();
    //rename the folder        
    Node webContent = webFolder.addNode(fileName, "exo:webContent");
    addMixin(webContent,"exo:privilegeable");
    addMixin(webContent,"exo:owneable");
    // need check why WebContentSchemaHandler doesn't run for this case
    createSchema(webContent);
    session.save();
    //the htmlFile become default.html file for the web content
    String htmlFilePath = webContent.getPath() + "/default.html";    
    session.move(tempPath, htmlFilePath);
    tempFolder.remove();    
    session.save();
  }
  
  private void createSchema(final Node webContent) throws Exception {
    if (!webContent.hasNode("js")) {
      Node js = webContent.addNode("js","exo:jsFolder"); 
      addMixin(js,"exo:owneable");
    }   
    if (!webContent.hasNode("css")) {
      Node css = webContent.addNode("css","exo:cssFolder");
      addMixin(css,"exo:owneable");
    }       
    if (!webContent.hasNode("medias")) {
      Node multimedia = webContent.addNode("medias",NT_FOLDER);      
      addMixin(multimedia, "exo:multimediaFolder");    
      addMixin(multimedia,"exo:owneable");
      Node images = multimedia.addNode("images",NT_FOLDER);
      addMixin(images, "exo:pictureFolder");
      addMixin(images,"exo:owneable");
      Node video = multimedia.addNode("videos",NT_FOLDER);        
      addMixin(video, "exo:videoFolder");    
      addMixin(video,"exo:owneable");
      Node audio = multimedia.addNode("audio",NT_FOLDER);    
      addMixin(audio, "exo:musicFolder");
      addMixin(audio,"exo:owneable");
    }                
    if (!webContent.hasNode("documents")) {
      Node document = webContent.addNode("documents",NT_UNSTRUCTURED);           
      addMixin(document, "exo:documentFolder");
      addMixin(document,"exo:owneable");
    }          
  }
}
