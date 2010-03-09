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
package org.exoplatform.services.wcm.publication.lifecycle.stageversion.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.webui.navigation.PageNavigationUtils;
import org.exoplatform.services.wcm.utils.WCMCoreUtils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UITree;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 * chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Sep 23, 2008
 */

@ComponentConfig(
    template = "system:/groovy/webui/core/UITree.gtmpl"
)
public class UIPublicationTree extends UITree {

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UITree#event(java.lang.String, java.lang.String)
   */
  public String event(String name, String beanId) throws Exception {
    UIComponent component = getParent();
    return component.event(name, beanId);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UITree#getActionLink()
   */
  public String getActionLink() throws Exception {
    if(getSelected() == null) return "javascript:void(0)";
    if(getParentSelected() == null) return "javascript:void(0)";
    return event("ChangeNode", (String)getId(getParentSelected()));
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UITree#isSelected(java.lang.Object)
   */
  public boolean isSelected(Object obj) throws Exception{
    TreeNode selected = getSelected();
    if(selected == null) return false;
    TreeNode compared = TreeNode.class.cast(obj);
    return compared.getUri().equals(selected.getUri());
  }

  /**
   * The Class TreeNode.
   */
  public static class TreeNode {

    /** The portal name. */
    private String portalName;
    
    /** The is page node. */
    private boolean isPageNode;    
    
    /** The page node. */
    private PageNode pageNode;
    
    /** The navigation. */
    private PageNavigation navigation;    
    
    /** The children. */
    private List<TreeNode> children;
    
    /** The resource bundle. */
    private ResourceBundle resourceBundle;

    /**
     * Instantiates a new tree node.
     * 
     * @param portalName the portal name
     * @param navigation the navigation
     * @param res the res
     * @param isPageNode the is page node
     */
    public TreeNode(String portalName, final PageNavigation navigation, final ResourceBundle res, boolean isPageNode) {
      this.portalName = portalName;
      this.navigation = navigation;
      this.resourceBundle = res;
      this.isPageNode = isPageNode;
    }
    
    /**
     * Gets the uri.
     * 
     * @return the uri
     */
    public String getUri() {
      if(isPageNode) {        
        return "/" + portalName + "/" +pageNode.getUri() ;
      }
      return "/" +portalName;
    }

    /**
     * Gets the page node uri.
     * 
     * @return the page node uri
     */
    public String getPageNodeUri() {
      if(isPageNode) return pageNode.getUri();
      return null;
    }        

    /**
     * Gets the icon.
     * 
     * @return the icon
     */
    public String getIcon() {
      if(!isPageNode) return "";
      return pageNode.getIcon();
    }
    
    /**
     * Gets the tree node children.
     * 
     * @return the tree node children
     */
    public List<TreeNode> getTreeNodeChildren() { return children; }    
    
    /**
     * Sets the tree node children.
     * 
     * @param list the new tree node children
     */
    public void setTreeNodeChildren(List<TreeNode> list) { this.children = list; }

    /**
     * Sets the page node.
     * 
     * @param pageNode the new page node
     */
    public void setPageNode(PageNode pageNode) {
      this.pageNode = pageNode;
      this.pageNode.setResolvedLabel(resourceBundle);
      if(pageNode.getChildren() == null) {
        children = null;
      }      
    }

    /**
     * Checks if is page node.
     * 
     * @return true, if is page node
     */
    public boolean isPageNode() {return isPageNode;}
    
    /**
     * Sets the checks if is page node.
     * 
     * @param isPageNode the new checks if is page node
     */
    public void setIsPageNode(boolean isPageNode) {this.isPageNode = isPageNode;}
    
    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
      if(isPageNode) return pageNode.getName();
      return portalName;
    }    
    
    /**
     * Gets the resolved label.
     * 
     * @return the resolved label
     */
    public String getResolvedLabel() {
      if(isPageNode) return pageNode.getResolvedLabel();
      return portalName;
    }
    
    /**
     * Sets the portal name.
     * 
     * @param s the new portal name
     */
    public void setPortalName(String s) { this.portalName = s; }
    
    /**
     * Gets the portal name.
     * 
     * @return the portal name
     */
    public String getPortalName() {return this.portalName; }

    /**
     * Sets the children by page nodes.
     * 
     * @param pagesNodes the new children by page nodes
     * 
     * @throws Exception the exception
     */
    public void setChildrenByPageNodes(List<PageNode> pagesNodes) throws Exception {
      if(pagesNodes == null) return;
      List<TreeNode> list = new ArrayList<TreeNode>();
      UserPortalConfigService userPortalConfigService = WCMCoreUtils.getService(UserPortalConfigService.class);
      for(PageNode pNode: pagesNodes) {
      	if (pNode.getPageReference() == null) continue;
        Page page = userPortalConfigService.getPage(pNode.getPageReference(), org.exoplatform.portal.webui.util.Util.getPortalRequestContext().getRemoteUser());
        if (page == null) continue;
        if(!pNode.isDisplay()) continue;                
        TreeNode treeNode = new TreeNode(portalName,navigation,resourceBundle,true);
        treeNode.setPageNode(pNode);
        treeNode.setChildrenByPageNodes(pNode.getChildren());
        list.add(treeNode);
      }
      setTreeNodeChildren(list);
    }

    /**
     * Search tree node by uri.
     * 
     * @param uri the uri
     * 
     * @return the tree node
     * 
     * @throws Exception the exception
     */
    public TreeNode searchTreeNodeByURI(String uri) throws Exception {
      if(uri.equals("/"+portalName)) {
        TreeNode treeNode = new TreeNode(portalName, navigation,resourceBundle, false);
        treeNode.setChildrenByPageNodes(navigation.getNodes());
        return treeNode;
      }
      String pageNodeURI = StringUtils.substringAfter(uri, "/" + portalName + "/");
      PageNode other = PageNavigationUtils.searchPageNodeByUri(this.navigation, pageNodeURI);
      if(other == null) return null;
      TreeNode treeNode = new TreeNode(portalName,navigation,resourceBundle, true);
      treeNode.setPageNode(other);
      treeNode.setChildrenByPageNodes(other.getChildren());
      return treeNode; 
    }
    
    /**
     * Gets the children.
     * 
     * @return the children
     */
    public List<TreeNode> getChildren() {
      return children;
    }
    
    /**
     * Gets the navigation.
     * 
     * @return the navigation
     */
    public PageNavigation getNavigation() {
      return navigation;
    }
    
    /**
     * Gets the page node.
     * 
     * @return the page node
     */
    public PageNode getPageNode() {
      return pageNode;
    }
  }
}