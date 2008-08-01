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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.services.cms.actions.ActionServiceContainer;
import org.exoplatform.services.html.HTMLNode;
import org.exoplatform.services.html.parser.HTMLParser;
import org.exoplatform.services.html.util.HyperLinkUtil;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong_phan@exoplatform.com
 * Jul 29, 2008  
 */
public class LinkExtractorService {
  
  private WebSchemaConfigService webSchemaConfigService_;
  
  public LinkExtractorService(WebSchemaConfigService webSchemaConfigService, ActionServiceContainer actionServiceContainer) {
    this.webSchemaConfigService_ = webSchemaConfigService;
  }
  
  /**
   * Extract all link (<code>a</code>, <code>iframe</code>, <code>frame</code>, <code>href</code>) 
   * and all image (<code>img</code>) from all HTML file from current web content node 
   * 
   * @param webContent the current web content node
   * 
   * @return the list of link's URL
   * 
   * @throws Exception the exception
   */
  public List<String> extractLink(Node webContent) throws Exception {
    List<String> listHyperlink = new ArrayList<String>();
    Session session = webContent.getSession();
    QueryManager queryManager = session.getWorkspace().getQueryManager() ;
    Query query = queryManager.createQuery("select * from exo:htmlFile where jcr:path like '" + webContent.getPath() + "/%'", Query.SQL) ;
    NodeIterator nodeIterator = query.execute().getNodes() ;
    for (;nodeIterator.hasNext();) {
      Node htmlFile = nodeIterator.nextNode();
      Node jcrContent = htmlFile.getNode("jcr:content");
      Property jcrData = jcrContent.getProperty("jcr:data");
      String sContent = jcrData.getString();
      HTMLNode htmlRootNode = HTMLParser.createDocument(sContent).getRoot();
      HyperLinkUtil linkUtil = new HyperLinkUtil();
      for (Iterator<String> iterLink = linkUtil.getSiteLink(htmlRootNode).iterator(); iterLink.hasNext();) {
        listHyperlink.add(iterLink.next());
      }
      for (Iterator<String> iterImage = linkUtil.getImageLink(htmlRootNode).iterator(); iterImage.hasNext();) {
        listHyperlink.add(iterImage.next());
      }
    }
    return listHyperlink;
  }

  /**
   * Creates the link nodes and insert its to links folder in current web content node.
   * 
   * @param webContent the current web content node
   *  
   * @throws Exception the exception
   */
  public void createLinkNode(Node webContent) throws Exception {
    WebContentSchemaHandler webContentSchemaHandler = webSchemaConfigService_.getWebSchemaHandlerByType(WebContentSchemaHandler.class);   
    Node linkFolderNode = webContentSchemaHandler.getLinkFolder(webContent);
    
    // get old link from jcr
    List<String> listOld = new ArrayList<String>();
    for (NodeIterator iteratorNode = linkFolderNode.getNodes();iteratorNode.hasNext();) {
      listOld.add(iteratorNode.nextNode().getProperty("exo:linkURL").getString());
    }
    
    // get new link from web content form
    List<String> listNew = extractLink(webContent); 
    
    // create temp list to save all link from jcr
    List<String> listTmp = new ArrayList<String>();
    listTmp.addAll(listOld);
    
    // remove link from old list which is not avalable in new list
    listOld.removeAll(listNew);
    for(String linkUrl: listOld) {
      // remove ':' and '/' charater because it's conflict in jcr
      String linkName = linkUrl.replaceAll("://", "@").replaceAll("/", "|");
      Node linkNode = linkFolderNode.getNode(linkName);
      linkNode.remove();
    }

    // add link from new list which is not avalable in old list
    listNew.removeAll(listTmp);
    for(String linkUrl: listNew) {
      // remove ':' and '/' charater because it's conflict in jcr
      String linkName = linkUrl.replaceAll("://", "@").replaceAll("/", "|");
      Node linkNode = linkFolderNode.addNode(linkName, "exo:link");
      linkNode.setProperty("exo:linkURL", linkUrl);
    }
    
    webContent.getSession().save();   
  }
  
}
