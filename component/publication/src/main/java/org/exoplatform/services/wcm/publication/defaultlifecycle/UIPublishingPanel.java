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

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.wcm.core.NodeLocation;
import org.exoplatform.services.wcm.publication.defaultlifecycle.UIPublicationTree.TreeNode;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoa.pham@exoplatform.com
 * Sep 9, 2008
 */
@ComponentConfig (
    lifecycle = UIApplicationLifecycle.class,
    template = "classpath:groovy/wcm/webui/publication/UIPublishingPanel.gtmpl"
)
public class UIPublishingPanel extends UIForm {

  /** The current node. */
  private NodeLocation currentNodeLocation;

  /**
   * Gets the node.
   * 
   * @return the node
   */
  public Node getNode() {
	return NodeLocation.getNodeByLocation(currentNodeLocation);
  }
  
  /**
   * Sets the node.
   * 
   * @param node the new node
   */
  public void setNode(Node node) {
	currentNodeLocation = NodeLocation.make(node);
  }  

  /**
   * Instantiates a new uI publishing panel.
   * 
   * @throws Exception the exception
   */
  public UIPublishingPanel() throws Exception {
    addChild(UIPortalNavigationExplorer.class,null,"UIPortalNavigationExplorer");
    addChild(UIPublicationAction.class,null,"UIPublicationAction");
    addChild(UIPublishedPages.class,null,"UIPublishedPages");
    addChild(UIPublicationComponentStatus.class, null, "UIPublicationComponentStatus");
  }

  /**
   * Inits the panel.
   * 
   * @param node the node
   * @param portalName the portal name
   * @param runningPortals the running portals
   * 
   * @throws Exception the exception
   */
  public void initPanel(Node node,String portalName,List<String> runningPortals) throws Exception {
	currentNodeLocation = NodeLocation.make(node);    
    UIPortalNavigationExplorer poExplorer = getChild(UIPortalNavigationExplorer.class);
    poExplorer.init(portalName,runningPortals);
    UIPublishedPages publishedPages = getChild(UIPublishedPages.class);
    publishedPages.init();
    UIPublicationComponentStatus publicationComponentStatus = getChild(UIPublicationComponentStatus.class);
    publicationComponentStatus.setNode(node);
  }

  /**
   * Gets the current portal.
   * 
   * @return the current portal
   */
  public String getCurrentPortal() {
    UIPortalNavigationExplorer portalNavigationExplorer = getChild(UIPortalNavigationExplorer.class);
    TreeNode selectedNode = portalNavigationExplorer.getSelectedNode();
    if (selectedNode != null) return selectedNode.getPortalName();
    return null;
  }

  /**
   * Gets the current tree node.
   * 
   * @return the current tree node
   */
  public String getCurrentTreeNode() {
    UIPortalNavigationExplorer portalNavigationExplorer = getChild(UIPortalNavigationExplorer.class);
    TreeNode selectedNode = portalNavigationExplorer.getSelectedNode();
    if (selectedNode != null) return selectedNode.getName();
    return null;
  }

}