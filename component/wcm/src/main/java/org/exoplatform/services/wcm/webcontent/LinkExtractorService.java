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
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.services.html.HTMLNode;
import org.exoplatform.services.html.parser.HTMLParser;
import org.exoplatform.services.wcm.link.HyperLinkUtilExtended;
import org.exoplatform.services.wcm.link.LinkBean;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong_phan@exoplatform.com
 * Jul 29, 2008  
 */
public class LinkExtractorService {
  
  
  public LinkExtractorService() {
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
      HyperLinkUtilExtended linkUtil = new HyperLinkUtilExtended();
      for (Iterator<String> iterLink = linkUtil.getSiteLink(htmlRootNode).iterator(); iterLink.hasNext();)
        listHyperlink.add(iterLink.next());
      for (Iterator<String> iterImage = linkUtil.getImageLink(htmlRootNode).iterator(); iterImage.hasNext();)
        listHyperlink.add(iterImage.next());
    }
    return listHyperlink;
  }

  /**
   * Add exo:links (multi value) property of exo:linkable node type to web content node, with pattern
   * 
   * @param webContent the current web content node
   *  
   * @throws Exception the exception
   */
  public void createLinkNode(Node webContent) throws Exception {
    ValueFactory valueFactory = webContent.getSession().getValueFactory(); 
    
    if (webContent.canAddMixin("exo:linkable"))
      webContent.addMixin("exo:linkable");
    
    // get old link from jcr
    List<String> listExtractedLink = new ArrayList<String>();
    if (webContent.hasProperty("exo:links")) {
      Property property = webContent.getProperty("exo:links");
      for (Value value : property.getValues())
        listExtractedLink.add(value.getString());
    }
    
    // get new url from web content form
    List<String> listNewUrl = extractLink(webContent); 
    
    // compare, remove old link, add new link, create new List
    List<String> listResult = new ArrayList<String>();
    
    for (String extractedLink : listExtractedLink) {
      for (String newUrl : listNewUrl) {
        if (new LinkBean(extractedLink).getLinkUrl().equals(newUrl)) {
          listResult.add(extractedLink);
        }
      }
    }
    
    List<String> listTemp = new ArrayList<String>();
    listTemp.addAll(listNewUrl);
    
    for (String newUrl : listNewUrl) {
      int i = 0;
      for (String extractedLink : listExtractedLink) {
        LinkBean linkBean = new LinkBean(extractedLink);
        if (newUrl.equals(linkBean.getLinkUrl())) 
          listTemp.set(i, "");
        i++;
      }
    }
    
    for (String strTemp : listTemp)
      if (!strTemp.equals(""))
        listResult.add(strTemp);

    // Create an array of value to add to exo:links property
    Value[] values = new Value[listResult.size()];
    int i = 0;
    for(String url: listResult) {
      if (url.indexOf(LinkBean.STATUS) < 0) {
        LinkBean linkBean = new LinkBean(url, LinkBean.STATUS_UNCHECKED);
        values[i] = valueFactory.createValue(linkBean.toString());
      } else {  
        values[i] = valueFactory.createValue(url);
      }
      i++;
    }
    
    webContent.setProperty("exo:links", values);
    
    webContent.getSession().save();   
  }
  
}
