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
        listHyperlink.add(iterLink.next().replaceAll("SingleQuote", "'").replaceAll("SingleSharp", "#"));
      for (Iterator<String> iterImage = linkUtil.getImageLink(htmlRootNode).iterator(); iterImage.hasNext();)
        listHyperlink.add(iterImage.next().replaceAll("SingleQuote", "'").replaceAll("SingleSharp", "#"));
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
    List<String> listOld = new ArrayList<String>();
    if (webContent.hasProperty("exo:links")) {
      Property property = webContent.getProperty("exo:links");
      for (Value value : property.getValues())
        listOld.add(value.getString());
    }
    
    // get new link from web content form
    List<String> listNew = extractLink(webContent); 
    
    // compare, remove old link, add new link, create new List
    List<String> listResult = new ArrayList<String>();
    
    for (int intOld = 0; intOld < listOld.size(); intOld++)
      for (int intNew = 0; intNew < listNew.size(); intNew++)
        if (listOld.get(intOld).split(LinkBean.SEPARATOR)[1].replaceAll(LinkBean.URL, "").equals(listNew.get(intNew)))
          listResult.add(listOld.get(intOld));

    List<String> listNew_ = new ArrayList<String>();
    listNew_.addAll(listNew);
    for (int intNew = 0; intNew < listNew.size(); intNew++)
      for (int intOld = 0; intOld < listOld.size(); intOld++)
        if (listNew.get(intNew).equals(listOld.get(intOld).split(LinkBean.SEPARATOR)[1].replaceAll(LinkBean.URL, "")))
          listNew_.set(intNew, "");

    for (String strNew_ : listNew_)
      if (!strNew_.equals(""))
        listResult.add(strNew_);

    // Create an array of value to add to exo:links property
    Value[] values = new Value[listResult.size()];
    int intValue = 0;
    for(String strUrl: listResult) {
      if (strUrl.indexOf(LinkBean.STATUS) < 0)
        values[intValue] = valueFactory.createValue(LinkBean.STATUS + LinkBean.STATUS_UNCHECKED + LinkBean.SEPARATOR + LinkBean.URL + strUrl);
      else 
        values[intValue] = valueFactory.createValue(strUrl);
      intValue++;
    }
    
    webContent.setProperty("exo:links", values);
    
    webContent.getSession().save();   
  }
  
}
