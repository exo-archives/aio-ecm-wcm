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
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.resources.LocaleConfig;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.wcm.core.WCMService;
import org.exoplatform.services.wcm.publication.lifecycle.stageversion.ui.UIPublicationTree.TreeNode;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham hoa.pham@exoplatform.com
 * Sep 9, 2008
 */

@ComponentConfig(lifecycle = Lifecycle.class, template = "classpath:groovy/wcm/webui/publication/lifecycle/stageversion/ui/UIPortalNavigationExplorer.gtmpl", events = { @EventConfig(listeners = UIPortalNavigationExplorer.ChangeNodeActionListener.class) })
public class UIPortalNavigationExplorer extends UIContainer {

  /** The portal name. */
  private String       portalName;

  /** The running portals. */
  private List<String> runningPortals = new ArrayList<String>();

  /** The current node. */
  private TreeNode     currentNode    = null;

  /**
   * Instantiates a new uI portal navigation explorer.
   * 
   * @throws Exception the exception
   */
  public UIPortalNavigationExplorer() throws Exception {
  }

  /**
   * Inits the.
   * 
   * @param portalName the portal name
   * @param runningPortals the running portals
   * @throws Exception the exception
   */
  public void init(String portalName, List<String> runningPortals) throws Exception {    
    this.portalName = portalName;
    this.runningPortals = runningPortals;
    List<TreeNode> list = new ArrayList<TreeNode>();
    UIPortalApplication portalApplication = Util.getUIPortalApplication();
    LocaleConfig localeConfig = getApplicationComponent(LocaleConfigService.class).getLocaleConfig(portalApplication.getLocale().getLanguage());
    WCMService wcmService = getApplicationComponent(WCMService.class);
    ThreadLocalSessionProviderService threadLocalSessionProviderService = getApplicationComponent(ThreadLocalSessionProviderService.class);
    SessionProvider sessionProvider = threadLocalSessionProviderService.getSessionProvider(null);
    if(wcmService.isSharedPortal(portalName, sessionProvider)) {
      UIPublicationTree tree = addChild(UIPublicationTree.class, null, "UIPortalTree");      
      for(String portal : this.runningPortals) {
        PageNavigation pageNavigation = getPortalNavigation(portal);
        ResourceBundle res = localeConfig.getNavigationResourceBundle(pageNavigation.getOwnerType(), pageNavigation.getOwnerId()) ;
        TreeNode treeNode = new TreeNode(portal,pageNavigation, res, false);
        if(pageNavigation.getNodes()!= null) 
          treeNode.setChildrenByPageNodes(pageNavigation.getNodes());
        list.add(treeNode);
      }
      tree.setSibbling(list);
      tree.setBeanIdField("uri");
      tree.setBeanLabelField("resolvedLabel");
      tree.setIcon("DefaultPageIcon");    
      tree.setSelectedIcon("DefaultPageIcon");
    } else {
      UIPublicationTree tree = addChild(UIPublicationTree.class, null, "UIPageNodeTree");
      PageNavigation navigation = getPortalNavigation(portalName);
      ResourceBundle res = localeConfig.getNavigationResourceBundle(navigation.getOwnerType(), navigation.getOwnerId()) ;
      TreeNode treeNode = new TreeNode(portalName, navigation, res, true);
      if(navigation.getNodes()!= null)
        treeNode.setChildrenByPageNodes(navigation.getNodes());
      tree.setSibbling(treeNode.getTreeNodeChildren());
      tree.setBeanIdField("uri");
      tree.setBeanLabelField("resolvedLabel");
      tree.setIcon("DefaultPageIcon");    
      tree.setSelectedIcon("DefaultPageIcon");
    }
  }

  /**
   * Gets the portal navigation.
   * 
   * @param portalName the portal name
   * @return the portal navigation
   * @throws Exception the exception
   */
  private PageNavigation getPortalNavigation(String portalName) throws Exception {
    DataStorage dataStorage = getApplicationComponent(DataStorage.class);
    Query<PageNavigation> query = new Query<PageNavigation>(PortalConfig.PORTAL_TYPE,
                                                            portalName,
                                                            PageNavigation.class);
    PageList list = dataStorage.find(query);
    for (Object object : list.getAll()) {
      return PageNavigation.class.cast(object);
    }
    return null;
  }

  /**
   * Select tree node by uri.
   * 
   * @param uri the uri
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public void selectTreeNodeByUri(String uri) throws Exception {
    UIPublicationTree tree = getChild(UIPublicationTree.class);
    if (currentNode == null) {
      List<TreeNode> sibbling = (List<TreeNode>) tree.getSibbling();
      for (TreeNode childNode : sibbling) {
        if (childNode.getUri().equals(uri)) {
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
    if (tree.getId().equals("UIPortalTree")) {
      if (!uri.startsWith(currentNode.getUri())) {
        List<TreeNode> sibbling = (List<TreeNode>) tree.getSibbling();
        for (TreeNode childNode : sibbling) {
          if (childNode.getUri().equals(uri)) {
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
    if (selected == null)
      return;
    String parentURI = StringUtils.substringBeforeLast(uri, "/");
    TreeNode parent = currentNode.searchTreeNodeByURI(parentURI);
    if (!selected.isPageNode()) {
      if (!tree.getId().equals("UIPortalTree"))
        return;
      List<TreeNode> list = new ArrayList<TreeNode>();
      for (String portal : this.runningPortals) {
        PageNavigation pageNavigation = getPortalNavigation(portal);
        UIPortalApplication portalApplication = Util.getUIPortalApplication();
        LocaleConfig localeConfig = getApplicationComponent(LocaleConfigService.class).getLocaleConfig(portalApplication.getLocale()
                                                                                                                        .getLanguage());
        ResourceBundle res = localeConfig.getNavigationResourceBundle(pageNavigation.getOwnerType(),
                                                                      pageNavigation.getOwnerId());
        TreeNode treeNode = new TreeNode(portal, pageNavigation, res, false);
        if (pageNavigation.getNodes() != null)
          treeNode.setChildrenByPageNodes(pageNavigation.getNodes());
        list.add(treeNode);
      }
      tree.setSibbling(list);
      tree.setSelected(selected);
      List<TreeNode> listChildNode = selected.getTreeNodeChildren();
      if (listChildNode == null)
        tree.setChildren(null);
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
      if (sibling != null)
        tree.setSibbling(sibling);
      currentNode = selected;
    } else if (!parent.isPageNode()) {
      tree.setSelected(selected);
      tree.setChildren(selected.getTreeNodeChildren());
      currentNode = selected;
      tree.setParentSelected(parent);
      List<TreeNode> sibbling = parent.getTreeNodeChildren();
      if (sibbling != null)
        tree.setSibbling(sibbling);
    }
  }

  /**
   * The listener interface for receiving changeNodeAction events. The class
   * that is interested in processing a changeNodeAction event implements this
   * interface, and the object created with that class is registered with a
   * component using the component's
   * <code>addChangeNodeActionListener<code> method. When
   * the changeNodeAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see ChangeNodeActionEvent
   */
  public static class ChangeNodeActionListener extends EventListener<UIPortalNavigationExplorer> {

    /*
     * (non-Javadoc)
     * @see
     * org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui
     * .event.Event)
     */
    public void execute(Event<UIPortalNavigationExplorer> event) throws Exception {
      String uri = event.getRequestContext().getRequestParameter(OBJECTID);
      UIPortalNavigationExplorer portalNavigationExplorer = event.getSource();
      portalNavigationExplorer.selectTreeNodeByUri(uri);

      UIPublicationContainer publicationContainer = portalNavigationExplorer.getAncestorOfType(UIPublicationContainer.class);
      UIPublicationPagesContainer publicationPagesContainer = portalNavigationExplorer.getAncestorOfType(UIPublicationPagesContainer.class);
      publicationContainer.setActiveTab(publicationPagesContainer, event.getRequestContext());
    }
  }

  /**
   * Gets the selected node.
   * 
   * @return the selected node
   */
  public TreeNode getSelectedNode() {
    return currentNode;
  }

  /**
   * Sets the selected node.
   * 
   * @param selectedNode the new selected node
   */
  public void setSelectedNode(TreeNode selectedNode) {
    this.currentNode = selectedNode;
  }

}
