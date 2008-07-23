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

  protected String getHandlerNodeType() { return "exo:webContent"; }  
  protected String getParentNodeType() { return "nt:base"; }

  public void process(final Node webContent) throws Exception {
    Session session = webContent.getSession();
    createSchema(webContent);
    session.save();
  }

  public Node getCSSFolder(final Node webContent) throws Exception {
    return webContent.getNode("css");
  }  

  public Node getJSFolder(final Node webContent) throws Exception {
    return webContent.getNode("js");
  }

  public Node getImagesFolders(final Node webContent) throws Exception {
    return webContent.getNode("multimedia/images");
  }

  public Node getVideoFolder(final Node webContent) throws Exception {
    return webContent.getNode("multimedia/videos");
  }

  public Node getLinkFolder(final Node webContent) throws Exception {
    return webContent.getNode("links");
  }
  
  public Node getDocumentFolder (final Node webContent) throws Exception {
    return webContent.getNode("documents");
  }

  private void createSchema(final Node webContent) throws Exception {
    if (!webContent.hasNode("js")) {
      webContent.addNode("js","exo:jsFolder"); 
    }   
    if (!webContent.hasNode("css")) {
      webContent.addNode("css","exo:cssFolder"); 
    }       
    if (!webContent.hasNode("multimedia")) {
      Node multimedia = webContent.addNode("multimedia",NT_FOLDER);
      addMixin(multimedia, "exo:multimediaFolder");    
      Node images = multimedia.addNode("images",NT_FOLDER);
      addMixin(images, "exo:pictureFolder");        
      Node video = multimedia.addNode("videos",NT_FOLDER);        
      addMixin(video, "exo:videoFolder");    
      Node audio = multimedia.addNode("audio",NT_FOLDER);    
      addMixin(audio, "exo:musicFolder");
    }                
    if (!webContent.hasNode("documents")) {
      Node document = webContent.addNode("documents",NT_UNSTRUCTURED);           
      addMixin(document, "exo:documentFolder"); 
    }    
    if (!webContent.hasNode("links")) {
      webContent.addNode("links",NT_UNSTRUCTURED); 
    }    
  }

}
