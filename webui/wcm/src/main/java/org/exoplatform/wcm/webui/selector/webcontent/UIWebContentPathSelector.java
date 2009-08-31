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
package org.exoplatform.wcm.webui.selector.webcontent;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.selector.UISelectPathPanel;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIBreadcumbs.LocalPath;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author : Hoa.Pham hoa.pham@exoplatform.com Jun 23, 2008
 */
@ComponentConfigs({
  @ComponentConfig(
      lifecycle = Lifecycle.class,
      template = "classpath:groovy/wcm/webui/UIWebContentPathSelector.gtmpl",
      events = {
        @EventConfig(listeners = UIWebContentPathSelector.SelectPathActionListener.class)
      }
  ),
  @ComponentConfig(
      type = UIBreadcumbs.class,
      id = "UIBreadcrumbWebContentPathSelector",
      template = "system:/groovy/webui/core/UIBreadcumbs.gtmpl",
      events = @EventConfig(listeners = UIBreadcumbs.SelectPathActionListener.class)
  ),
  @ComponentConfig(
      type = UISelectPathPanel.class,      
      id = "UIWCMSelectPathPanel",
      template = "classpath:groovy/wcm/webui/UIWCMSelectPathPanel.gtmpl",
      events = @EventConfig(listeners = UISelectPathPanel.SelectActionListener.class)
  )
}
)
public class UIWebContentPathSelector extends UIBaseNodeTreeSelector implements UIPopupComponent{
  /**
   * Instantiates a new uI web content path selector.
   * 
   * @throws Exception the exception
   */

  private Node currentPortal;
  private Node sharedPortal;

  public UIWebContentPathSelector() throws Exception {
    addChild(UIBreadcumbs.class, "UIBreadcrumbWebContentPathSelector", "UIBreadcrumbWebContentPathSelector");
    addChild(UIWebContentTreeBuilder.class,null, UIWebContentTreeBuilder.class.getName()+hashCode());
    addChild(UISelectPathPanel.class, "UIWCMSelectPathPanel", "UIWCMSelectPathPanel");
  }

  /**
   * Inits the.
   * 
   * @throws Exception the exception
   */
  public void init() throws Exception {
    String[] acceptedNodeTypes = {"exo:webContent"};
    UISelectPathPanel selectPathPanel = getChild(UISelectPathPanel.class);
    selectPathPanel.setAcceptedNodeTypes(acceptedNodeTypes);       
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    String currentPortalName = Util.getUIPortal().getName();
    SessionProvider provider = Utils.getSessionProvider(this);
    currentPortal = livePortalManagerService.getLivePortal(currentPortalName,provider);
    sharedPortal = livePortalManagerService.getLiveSharedPortal(provider);
    UIWebContentTreeBuilder builder = getChild(UIWebContentTreeBuilder.class);    
    builder.setCurrentPortal(currentPortal);
    builder.setSharedPortal(sharedPortal);
    builder.setRootTreeNode(currentPortal.getParent());
    provider.close();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector#onChange(javax.jcr.Node, org.exoplatform.webui.application.WebuiRequestContext)
   */
  @Override
  public void onChange(Node node, Object context) throws Exception {
    UISelectPathPanel selectPathPanel = getChild(UISelectPathPanel.class);
    changeFolder(node);
    selectPathPanel.setParentNode(node);
    selectPathPanel.updateGrid();
  }

  private void changeFolder(Node selectedNode) throws Exception {
    UIBreadcumbs uiBreadcrumb = getChild(UIBreadcumbs.class);
    uiBreadcrumb.setPath(getPath(null, selectedNode));
  }

  private List<LocalPath> getPath(List<LocalPath> list, Node selectedNode) throws Exception {
    if(list == null) list = new ArrayList<LocalPath>(5);
    if(selectedNode == null || selectedNode.getPath().equalsIgnoreCase(currentPortal.getParent().getPath()) 
        || selectedNode.getPath().equals("/")) return list;
    list.add(0, new LocalPath(selectedNode.getPath(), selectedNode.getName()));    
    getPath(list, selectedNode.getParent());
    return list;
  }

  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub

  }

  /**
   * @return the currentPortal
   */
  public Node getCurrentPortal() {
    return currentPortal;
  }

  /**
   * @param currentPortal the currentPortal to set
   */
  public void setCurrentPortal(Node currentPortal) {
    this.currentPortal = currentPortal;
  }

  /**
   * @return the sharedPortal
   */
  public Node getSharedPortal() {
    return sharedPortal;
  }

  /**
   * @param sharedPortal the sharedPortal to set
   */
  public void setSharedPortal(Node sharedPortal) {
    this.sharedPortal = sharedPortal;
  }

  public static class SelectPathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs uiBreadcumbs = event.getSource();
      String selectedNodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIWebContentPathSelector uiWCPathSelector = uiBreadcumbs.getAncestorOfType(UIWebContentPathSelector.class);
      Node currentPortal = uiWCPathSelector.getCurrentPortal();
      Node sharedPortal = uiWCPathSelector.getSharedPortal();
      if(selectedNodePath.equals(currentPortal.getPath()) || selectedNodePath.equals(sharedPortal.getPath())) {
        selectedNodePath = currentPortal.getParent().getPath();
      }
      UIWebContentTreeBuilder uiWCTreeBuilder = uiWCPathSelector.getChild(UIWebContentTreeBuilder.class);
      uiWCTreeBuilder.changeNode(selectedNodePath, event.getRequestContext());
    }
  }
}
