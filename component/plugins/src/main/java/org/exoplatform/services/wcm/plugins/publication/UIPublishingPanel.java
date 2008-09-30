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
package org.exoplatform.services.wcm.plugins.publication;

import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.wcm.plugins.publication.UIPublicationTree.TreeNode;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Sep 9, 2008  
 */
@ComponentConfig (
    lifecycle = UIApplicationLifecycle.class,
    template = "classpath:groovy/wcm/webui/publication/UIPublishingPanel.gtmpl"
)
public class UIPublishingPanel extends UIForm {

  private Node currentNode;
  
  public Node getNode() {return this.currentNode;}
  public void setNode(Node node) {this.currentNode = node; }  
  
  public UIPublishingPanel() throws Exception {
    addChild(UIPortalNavigationExplorer.class,null,"UIPortalNavigationExplorer");
    //addChild(UIPublishedPages.class,null,"UIPublishedPages");
  }

  public void initPanel(Node node,String portalName,List<String> runningPortals) throws Exception {
    this.currentNode = node;
    UIPortalNavigationExplorer poExplorer = getChild(UIPortalNavigationExplorer.class);
    poExplorer.init(portalName,runningPortals);
  }
  
  public String getCurrentPortal() {
    UIPortalNavigationExplorer portalNavigationExplorer = getChild(UIPortalNavigationExplorer.class);
    TreeNode selectedNode = portalNavigationExplorer.getSelectedNode();
    if (selectedNode != null) return selectedNode.getPortalName();
    return null;
  }
  
  public String getCurrentTreeNode() {
    UIPortalNavigationExplorer portalNavigationExplorer = getChild(UIPortalNavigationExplorer.class);
    TreeNode selectedNode = portalNavigationExplorer.getSelectedNode();
    if (selectedNode != null) return selectedNode.getName();
    return null;
  }
  
}
