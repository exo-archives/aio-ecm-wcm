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
package org.exoplatform.services.wcm.publication.defaultlifecycle;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.webui.navigation.PageNavigationUtils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UITree;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Sep 23, 2008  
 */

@SuppressWarnings("unused")
@ComponentConfig(
    template = "system:/groovy/webui/core/UITree.gtmpl"
)
public class UIPublicationTree extends UITree {

  public String event(String name, String beanId) throws Exception {
    UIComponent component = getParent();
    return component.event(name, beanId);
  }

  public String getActionLink() throws Exception {
    if(getSelected() == null) return "javascript:void(0)";
    if(getParentSelected() == null) return "javascript:void(0)";
    return event("ChangeNode", (String)getId(getParentSelected()));
  }

  public boolean isSelected(Object obj) throws Exception{
    TreeNode selected = getSelected();
    if(selected == null) return false;
    TreeNode compared = TreeNode.class.cast(obj);
    return compared.getUri().equals(selected.getUri());
  }

  public static class TreeNode {

    private String portalName;
    private boolean isPageNode;    
    private PageNode pageNode;
    private PageNavigation navigation;    
    private List<TreeNode> children;

    public TreeNode(String portalName, final PageNavigation navigation, boolean isPageNode) {
      this.portalName = portalName;
      this.navigation = navigation;
      this.isPageNode = isPageNode;
    }
    public String getUri() {
      if(isPageNode) {        
        return "/" + portalName + "/" +pageNode.getUri() ;
      }
      return "/" +portalName;
    }

    public String getPageNodeUri() {
      if(isPageNode) return pageNode.getUri();
      return null;
    }        

    public String getIcon() {
      if(!isPageNode) return "";
      return pageNode.getIcon();
    }
    public List<TreeNode> getTreeNodeChildren() { return children; }    
    public void setTreeNodeChildren(List<TreeNode> list) { this.children = list; }

    public void setPageNode(PageNode pageNode) {
      this.pageNode = pageNode;
      if(pageNode.getChildren() == null) {
        children = null;
      }      
    }

    public boolean isPageNode() {return isPageNode;}
    public void setIsPageNode(boolean isPageNode) {this.isPageNode = isPageNode;}
    public String getName() {
      if(isPageNode) return pageNode.getName();
      return portalName;
    }    
    public void setPortalName(String s) { this.portalName = s; }
    public String getPortalName() {return this.portalName; }

    public void setChildrenByPageNodes(List<PageNode> pagesNodes) {
      if(pagesNodes == null) return;
      List<TreeNode> list = new ArrayList<TreeNode>();
      for(PageNode pageNode: pagesNodes) {
        if(!pageNode.isDisplay()) continue;                
        TreeNode treeNode = new TreeNode(portalName,navigation, true);
        treeNode.setPageNode(pageNode);
        treeNode.setChildrenByPageNodes(pageNode.getChildren());
        list.add(treeNode);
      }
      setTreeNodeChildren(list);
    }

    public TreeNode searchTreeNodeByURI(String uri) {
      if(uri.equals("/"+portalName)) {
        TreeNode treeNode = new TreeNode(portalName, navigation, false);
        treeNode.setChildrenByPageNodes(navigation.getNodes());
        return treeNode;
      }
      String pageNodeURI = StringUtils.substringAfter(uri, "/" + portalName + "/");
      PageNode other = PageNavigationUtils.searchPageNodeByUri(this.navigation, pageNodeURI);
      if(other == null) return null;
      TreeNode treeNode = new TreeNode(portalName,navigation,true);
      treeNode.setPageNode(other);
      treeNode.setChildrenByPageNodes(other.getChildren());
      return treeNode; 
    }
    public List<TreeNode> getChildren() {
      return children;
    }
    public PageNavigation getNavigation() {
      return navigation;
    }
    public PageNode getPageNode() {
      return pageNode;
    }
  }
}