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

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.html.HTMLDocument;
import org.exoplatform.services.html.parser.HTMLParser;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.BaseWebSchemaHandler;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.webcontent.TOCGeneratorService.Heading;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author : Hoa.Pham
 * hoa.pham@exoplatform.com
 * Jun 23, 2008
 */

public class HTMLFileSchemaHandler extends BaseWebSchemaHandler {

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.core.BaseWebSchemaHandler#getHandlerNodeType()
   */
  protected String getHandlerNodeType() {   return "nt:file"; }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.core.BaseWebSchemaHandler#getParentNodeType()
   */
  protected String getParentNodeType() { return "exo:webFolder"; }  
  
  /** The link extractor. */
  private LinkExtractor linkExtractor = new LinkExtractor();  

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.core.BaseWebSchemaHandler#matchHandler(javax.jcr.Node, org.exoplatform.services.jcr.ext.common.SessionProvider)
   */
  @SuppressWarnings("unused")
  public boolean matchHandler(Node node, SessionProvider sessionProvider) throws Exception {
    if(!matchNodeType(node))
      return false;
    if(!matchMimeType(node))
      return false;    
    if(!matchParentNodeType(node)) {
      if(!isInWebContent(node))
        return false;
    }        
    return true;
  }

  /**
   * Match node type.
   * 
   * @param node the node
   * 
   * @return true, if successful
   * 
   * @throws Exception the exception
   */
  private boolean matchNodeType(Node node) throws Exception{    
    return node.getPrimaryNodeType().getName().equals("nt:file");
  }

  /**
   * Match mime type.
   * 
   * @param node the node
   * 
   * @return true, if successful
   * 
   * @throws Exception the exception
   */
  private boolean matchMimeType(Node node) throws Exception {
    String mimeType = getFileMimeType(node);       
    if("text/html".equals(mimeType))
      return true;    
    if("text/plain".equals(mimeType))
      return true;
    return false;
  }

  /**
   * Checks if is in web content.
   * 
   * @param file the file
   * 
   * @return true, if is in web content
   * 
   * @throws Exception the exception
   */
  public boolean isInWebContent(Node file) throws Exception{
    if(file.getParent().isNodeType("exo:webContent")) {
      return file.isNodeType("exo:htmlFile");
    } 
    return false;
  }

  /**
   * Match parent node type.
   * 
   * @param file the file
   * 
   * @return true, if successful
   * 
   * @throws Exception the exception
   */
  private boolean matchParentNodeType(Node file) throws Exception{
    return file.getParent().isNodeType("exo:webFolder");
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.core.BaseWebSchemaHandler#onCreateNode(javax.jcr.Node, org.exoplatform.services.jcr.ext.common.SessionProvider)
   */
  @SuppressWarnings("unused")
  public void onCreateNode(final Node file, SessionProvider sessionProvider) throws Exception {
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
    WebSchemaConfigService schemaConfigService = getService(WebSchemaConfigService.class);
    WebContentSchemaHandler contentSchemaHandler = schemaConfigService.getWebSchemaHandlerByType(WebContentSchemaHandler.class);
    contentSchemaHandler.createSchema(webContent);
    session.save();
    //the htmlFile become default.html file for the web content
    String htmlFilePath = webContent.getPath() + "/default.html";    
    session.move(tempPath, htmlFilePath);
    tempFolder.remove();    
    session.save();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.wcm.core.BaseWebSchemaHandler#onModifyNode(javax.jcr.Node, org.exoplatform.services.jcr.ext.common.SessionProvider)
   */
  @SuppressWarnings("unused")
  public void onModifyNode(final Node node, final SessionProvider sessionProvider) throws Exception{   
    Node parent = node.getParent();
    if(!parent.isNodeType("exo:webContent"))
      return;    
    if (!parent.isCheckedOut() || parent.isLocked() || !node.isCheckedOut() || node.isLocked()) {
      return;
    }
    String htmlData = node.getNode("jcr:content").getProperty("jcr:data").getString();
    HTMLDocument document = HTMLParser.createDocument(htmlData);
    List<String> newLinks = linkExtractor.extractLink(document);
    linkExtractor.updateLinks(parent,newLinks);
    TOCGeneratorService tocGeneratorService = getService(TOCGeneratorService.class);
    List<Heading> headings = tocGeneratorService.extractHeadings(document);
    if(headings != null) {
      tocGeneratorService.updateTOC(node,headings);
    }    
  }

}
