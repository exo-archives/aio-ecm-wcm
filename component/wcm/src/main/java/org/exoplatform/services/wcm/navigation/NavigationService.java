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
package org.exoplatform.services.wcm.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Nov 21, 2008  
 */
public class NavigationService {
  private UserPortalConfigService portalConfigService;

  public NavigationService(UserPortalConfigService userPortalConfigService) {
    this.portalConfigService = userPortalConfigService;
  }

  //TODO this method need be removed when use portal 2.5.1
  public String getNavigationsAsJSON(List<PageNavigation> navigations,String userId) throws Exception {
    List<PageNavigation>  copyList = new CopyOnWriteArrayList<PageNavigation>();
    for(PageNavigation nav: navigations) {
      copyList.add(filter(nav,userId));
    }    
    PortalNavigation portalNavigation = new PortalNavigation(copyList);
    JsonValue jsonValue = new JsonGeneratorImpl().createJsonObject(portalNavigation);
    String JSONnavigation = jsonValue.toString();
    JSONnavigation = JSONnavigation.substring(1, JSONnavigation.length() - 1);
    JSONnavigation = JSONnavigation.replaceFirst("\"navigations\":", "");
    return JSONnavigation;    
  }

  //TODO this method need be removed when use portal 2.5.1
  private PageNavigation filter(PageNavigation nav, String userName) throws Exception {
    PageNavigation filter = nav.clone();
    filter.setNodes(new ArrayList<PageNode>());
    for(PageNode node: nav.getNodes()){
      PageNode newNode = filter(node, userName);
      if(newNode != null ) filter.addNode(newNode);
    }
    return filter;
  }

  //TODO this method need be removed when use portal 2.5.1
  private PageNode filter(PageNode node, String userName) throws Exception {
    if(!node.isDisplay())
      return null;
    if(node.getPageReference() == null)
      return null;
    if(portalConfigService.getPage(node.getPageReference(),userName) == null)
      return null;       
    PageNode copyNode = node.clone();
    copyNode.setChildren(new ArrayList<PageNode>());
    List<PageNode> children = node.getChildren();
    if(children == null || children.size() == 0) return copyNode;
    for(PageNode child: children){
      PageNode newNode = filter(child, userName);
      if(newNode != null ) copyNode.getChildren().add(newNode);
    }
    if((copyNode.getChildren().size() == 0) && (copyNode.getPageReference() == null)) return null;
    return copyNode;
  }

  public String getNavigationsAsJSON(List<PageNavigation> navigations) throws Exception {
    PortalNavigation portalNavigation = new PortalNavigation(navigations);
    JsonValue jsonValue = new JsonGeneratorImpl().createJsonObject(portalNavigation);
    String JSONnavigation = jsonValue.toString();
    JSONnavigation = JSONnavigation.substring(1, JSONnavigation.length() - 1);
    JSONnavigation = JSONnavigation.replaceFirst("\"navigations\":", "");
    return JSONnavigation;    
  }

  /**
   * The Class PortalNavigation.
   */
  public static class PortalNavigation {

    private List<PageNavigation> navigations = new ArrayList<PageNavigation>();

    /**
     * Instantiates a new portal navigation.
     * 
     * @param list the list
     */
    public PortalNavigation(List<PageNavigation> list) {
      this.navigations = list;
    }    

    /**
     * Gets the navigations.
     * 
     * @return the navigations
     */
    public List<PageNavigation> getNavigations() { return this.navigations; }

    /**
     * Sets the navigations.
     * 
     * @param list the new navigations
     */
    public void setNavigations(List<PageNavigation> list) { this.navigations = list; }
  }      
}
