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

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.BaseWebSchemaHandler;

/**
 * Created by The eXo Platform SAS
 * @author : Hoa.Pham
 *          hoa.pham@exoplatform.com
 * May 28, 2008  
 */
public class WebContentSchemaHandler extends BaseWebSchemaHandler {

  protected String getHandlerNodeType() { return "exo:webContent"; }  
  protected String getParentNodeType() { return "nt:unstructured"; }
  
  @SuppressWarnings("unused")
  public void onCreateNode(final Node webContent, SessionProvider sessionProvider) throws Exception {
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
    return webContent.getNode("medias/images");
  }
  
  public Node getIllustrationImage(final Node webContent) throws Exception {
    return webContent.getNode("medias/images/illustration");
  }
  
  public Node getVideoFolder(final Node webContent) throws Exception {
    return webContent.getNode("medias/videos");
  }
  
  public Node getDocumentFolder (final Node webContent) throws Exception {
    return webContent.getNode("documents");
  }
  
  public void createDefaultSchema(Node webContent) throws Exception{
    addMixin(webContent,"exo:owneable");
    createSchema(webContent);
    //create empty css file:
    Node defaultCSS = addNodeAsNTFile(webContent.getNode("css"), "default.css", "text/css", "");
    addMixin(defaultCSS, "exo:cssFile");
    addMixin(defaultCSS,"exo:owneable");
    
    Node defaultJS = addNodeAsNTFile(webContent.getNode("js"), "default.js", "application/x-javascript", "");
    addMixin(defaultJS, "exo:jsFile");
    addMixin(defaultJS,"exo:owneable");
    
    Node defaultHTML = addNodeAsNTFile(webContent, "default.html", "text/html", "");
    addMixin(defaultHTML, "exo:htmlFile");
    addMixin(defaultHTML,"exo:owneable");
    
    Node illustration = addNodeAsNTFile(webContent.getNode("medias/images"), "illustration", "", "");
    addMixin(illustration, "exo:owneable");
  }     
  protected void createSchema(final Node webContent) throws Exception {    
    if (!webContent.hasNode("js")) {
      Node js = webContent.addNode("js","exo:jsFolder"); 
      addMixin(js,"exo:owneable");
    }   
    if (!webContent.hasNode("css")) {
      Node css = webContent.addNode("css","exo:cssFolder");
      addMixin(css,"exo:owneable");
    }       
    if (!webContent.hasNode("medias")) {
      Node multimedia = webContent.addNode("medias","exo:multimediaFolder");      
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
    //because exo:webcontent is exo:rss-enable so need set exo:title of the webcontent
    //by default, value of exo:title is webcontent name
    webContent.setProperty("exo:title", webContent.getName());
  }
  
  private Node addNodeAsNTFile(Node home, String fileName,String mimeType,String data) throws Exception{
    Node file = home.addNode(fileName,"nt:file");
    Node jcrContent = file.addNode("jcr:content","nt:resource");    
    jcrContent.addMixin("dc:elementSet");
    jcrContent.setProperty("jcr:encoding", "UTF-8");
    jcrContent.setProperty("jcr:lastModified", Calendar.getInstance());
    jcrContent.setProperty("jcr:mimeType", mimeType);
    jcrContent.setProperty("jcr:data", data);    
    return file;
  }
}
