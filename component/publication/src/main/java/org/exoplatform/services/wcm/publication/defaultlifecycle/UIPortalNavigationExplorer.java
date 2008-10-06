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

import javax.jcr.Node;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.publication.defaultlifecycle.UIPublicationTree.TreeNode;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Sep 9, 2008  
 */

@ComponentConfig(
    lifecycle = Lifecycle.class,
    template = "classpath:groovy/wcm/webui/publication/UIPortalNavigationExplorer.gtmpl",
    events = {
      @EventConfig(listeners = UIPortalNavigationExplorer.ChangeNodeActionListener.class)
    }
)

public class UIPortalNavigationExplorer extends UIContainer{

  private String portalName;
  private List<String> runningPortals = new ArrayList<String>();
  private TreeNode currentNode = null;

  public UIPortalNavigationExplorer() throws Exception {
  }

  public void init(String portalName, List<String> runningPortals) throws Exception {    
    this.portalName = portalName;
    this.runningPortals = runningPortals;
    List<TreeNode> list = new ArrayList<TreeNode>();
    if(isSharedPortalContent()) {
      UIPublicationTree tree = addChild(UIPublicationTree.class, null, "UIPortalTree");      
      for(String portal : this.runningPortals) {
        PageNavigation pageNavigation = getPortalNavigation(portal);
        TreeNode treeNode = new TreeNode(portal,pageNavigation,false);
        if(pageNavigation.getNodes()!= null) 
          treeNode.setChildrenByPageNodes(pageNavigation.getNodes());
        list.add(treeNode);
      }
      tree.setSibbling(list);
      tree.setBeanIdField("uri");
      tree.setBeanLabelField("name");
      tree.setIcon("DefaultPageIcon");    
      tree.setSelectedIcon("DefaultPageIcon");
    } else {
      UIPublicationTree tree = addChild(UIPublicationTree.class, null, "UIPageNodeTree");
      PageNavigation navigation = getPortalNavigation(portalName);
      TreeNode treeNode = new TreeNode(portalName,navigation,true);
      if(navigation.getNodes()!= null)
        treeNode.setChildrenByPageNodes(navigation.getNodes());
      tree.setSibbling(treeNode.getTreeNodeChildren());
      tree.setBeanIdField("uri");
      tree.setBeanLabelField("name");
      tree.setIcon("DefaultPageIcon");    
      tree.setSelectedIcon("DefaultPageIcon");
    }
  }

  private PageNavigation getPortalNavigation(String portalName) throws Exception{
    DataStorage dataStorage = getApplicationComponent(DataStorage.class);
    Query<PageNavigation> query = new Query<PageNavigation>(PortalConfig.PORTAL_TYPE,portalName,PageNavigation.class);
    PageList list = dataStorage.find(query);
    for(Object object: list.getAll()) {
      return PageNavigation.class.cast(object);
    }
    return null;
  }

  private boolean isSharedPortalContent() throws Exception{
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    Node sharedPortal = livePortalManagerService.getLiveSharedPortal(SessionProviderFactory.createSessionProvider());
    return sharedPortal.getName().equals(portalName);    
  }

  public void selectTreeNodeByUri(String uri) throws Exception {
    UIPublicationTree tree = getChild(UIPublicationTree.class);
    if(currentNode == null) {
      List<TreeNode> sibbling = (List<TreeNode>) tree.getSibbling();
      for (TreeNode childNode: sibbling) {
        if(childNode.getUri().equals(uri)) {
          currentNode = childNode;
          break;
        }
      }
      tree.setSelected(currentNode);
      List<TreeNode> listChildNode = currentNode.getTreeNodeChildren(); 
      tree.setChildren(listChildNode);
      tree.setParentSelected(null);
      tree.setSibbling(sibbling);
      return;
    }    
    if(tree.getId().equals("UIPortalTree")) {
      if(!uri.startsWith(currentNode.getUri())) {
        List<TreeNode> sibbling = (List<TreeNode>) tree.getSibbling();
        for (TreeNode childNode: sibbling) {
          if(childNode.getUri().equals(uri)) {
            currentNode = childNode;
            break;
          }
        }
        tree.setSelected(currentNode);
        List<TreeNode> listChildNode = currentNode.getTreeNodeChildren(); 
        tree.setChildren(listChildNode);
        tree.setParentSelected(null);
        tree.setSibbling(sibbling);
      }
    }
    TreeNode selected = currentNode.searchTreeNodeByURI(uri);
    if(selected == null) return;
    String parentURI = StringUtils.substringBeforeLast(uri, "/");
    TreeNode parent = currentNode.searchTreeNodeByURI(parentURI);
    if(!selected.isPageNode()) {
      if(!tree.getId().equals("UIPortalTree")) return ;
      List<TreeNode> list = new ArrayList<TreeNode>();
      for(String portal : this.runningPortals) {
        PageNavigation pageNavigation = getPortalNavigation(portal);
        TreeNode treeNode = new TreeNode(portal,pageNavigation,false);
        if(pageNavigation.getNodes() != null)
          treeNode.setChildrenByPageNodes(pageNavigation.getNodes());
        list.add(treeNode);
      }
      tree.setSibbling(list);
      tree.setSelected(selected);
      List<TreeNode> listChildNode = selected.getTreeNodeChildren();
      if(listChildNode == null) tree.setChildren(null);
      tree.setChildren(listChildNode);
      tree.setParentSelected(null);
      currentNode = selected;
      return;
    }
    if (parent.isPageNode()) {
      tree.setSelected(selected);
      tree.setChildren(selected.getTreeNodeChildren());
      tree.setParentSelected(parent);
      List<TreeNode> sibling = parent.getTreeNodeChildren();
      if(sibling != null) tree.setSibbling(sibling);
      currentNode = selected;
    } else if (!parent.isPageNode()) {      
      tree.setSelected(selected);
      tree.setChildren(selected.getTreeNodeChildren());
      currentNode = selected;      
      tree.setParentSelected(parent);
      List<TreeNode> sibbling = parent.getTreeNodeChildren();
      if(sibbling != null) 
        tree.setSibbling(sibbling);
    }
  }


  public static class ChangeNodeActionListener extends EventListener<UIPortalNavigationExplorer> {
    public void execute(Event<UIPortalNavigationExplorer> event) throws Exception {
      String uri = event.getRequestContext().getRequestParameter(OBJECTID);
      UIPortalNavigationExplorer portalNavigationExplorer = event.getSource();
      portalNavigationExplorer.selectTreeNodeByUri(uri);
    }
  }

  public TreeNode getSelectedNode() {
    return currentNode;
  }

  public void setSelectedNode(TreeNode selectedNode) {
    this.currentNode = selectedNode;
  }

}
