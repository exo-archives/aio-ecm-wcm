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
import javax.jcr.Property;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.exoplatform.services.html.HTMLDocument;
import org.exoplatform.services.html.HTMLNode;
import org.exoplatform.services.wcm.link.HyperLinkUtilExtended;
import org.exoplatform.services.wcm.link.LinkBean;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong_phan@exoplatform.com
 * Jul 29, 2008  
 */
public class LinkExtractor {


  public LinkExtractor() {
  }

  /**
   * Extract all link (<code>a</code>, <code>iframe</code>, <code>frame</code>, <code>href</code>) 
   * and all image (<code>img</code>) from html document
   * 
   * @param htmlDocument the html document
   * 
   * @return the list of link's URL
   * 
   * @throws Exception the exception
   */
  public List<String> extractLink(HTMLDocument htmlDocument) throws Exception {
    List<String> listHyperlink = new ArrayList<String>();        
    HTMLNode htmlRootNode = htmlDocument.getRoot();
    HyperLinkUtilExtended linkUtil = new HyperLinkUtilExtended();
    for (Iterator<String> iterLink = linkUtil.getSiteLink(htmlRootNode).iterator(); iterLink.hasNext();) {
      String link = iterLink.next();
      if (!listHyperlink.contains(link))
        listHyperlink.add(link);
    }
    for (Iterator<String> iterImage = linkUtil.getImageLink(htmlRootNode).iterator(); iterImage.hasNext();){
      String image = iterImage.next();
      if (!listHyperlink.contains(image))
        listHyperlink.add(image);
    }   
    return listHyperlink;
  }

  /**
   * Add exo:links (multi value) property of exo:linkable node type to web content node, with pattern
   * 
   * @param webContent the current web content node
   * @param newLinks the list of new links will be updated
   *  
   * @throws Exception the exception
   */
  public void updateLinks(Node webContent, List<String> newLinks) throws Exception {   
    ValueFactory valueFactory = webContent.getSession().getValueFactory();    
    if (webContent.canAddMixin("exo:linkable")) {
      webContent.addMixin("exo:linkable");
    }
    // get old link from exo:links property
    List<String> listExtractedLink = new ArrayList<String>();
    if (webContent.hasProperty("exo:links")) {
      Property property = webContent.getProperty("exo:links");
      for (Value value : property.getValues()) {
        listExtractedLink.add(value.getString());
      }
    }
    // compare, remove old link, add new link, create new List
    List<String> listResult = new ArrayList<String>();

    for (String extractedLink : listExtractedLink) {
      for (String newUrl : newLinks) {
        if (LinkBean.parse(extractedLink).getUrl().equals(newUrl)) {
          listResult.add(extractedLink);
        }
      }
    }
    List<String> listTemp = new ArrayList<String>();
    listTemp.addAll(newLinks);

    for (String newUrl : newLinks) {
      for (String extractedLink : listExtractedLink) {
        if (newUrl.equals(LinkBean.parse(extractedLink).getUrl())) {
          listTemp.set(newLinks.indexOf(newUrl), "");
        }
      }
    }
    
    for (String strTemp : listTemp) {
      if (!strTemp.equals("")) {
        listResult.add(strTemp);
      }
    }

    // Create an array of value to add to exo:links property
    Value[] values = new Value[listResult.size()];
    for(String url: listResult) {
      if (url.indexOf(LinkBean.STATUS) < 0) {
        LinkBean linkBean = new LinkBean(url, LinkBean.STATUS_UNCHECKED);
        values[listResult.indexOf(url)] = valueFactory.createValue(linkBean.toString());
      } else {  
        values[listResult.indexOf(url)] = valueFactory.createValue(url);
      }
    }
    webContent.setProperty("exo:links", values);    
  }

}
