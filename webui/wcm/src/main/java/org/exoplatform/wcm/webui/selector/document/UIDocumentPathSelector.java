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
package org.exoplatform.wcm.webui.selector.document;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.core.ManageableRepository;
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
 * Created by The eXo Platform SAS
 * Author : DANG TAN DUNG
 * dzungdev@gmail.com
 * Sep 3, 2008
 */
@ComponentConfigs({
  @ComponentConfig(
      lifecycle = Lifecycle.class,
      template = "classpath:groovy/wcm/webui/UIDocumentPathSelector.gtmpl",
      events = {
        @EventConfig(listeners = UIDocumentPathSelector.SelectPathActionListener.class)
      }
  ),
  @ComponentConfig(
      type = UIBreadcumbs.class,
      id = "UIBreadcrumbDocumentPathSelector",
      template = "system:/groovy/webui/core/UIBreadcumbs.gtmpl",
      events = @EventConfig(listeners = UIBreadcumbs.SelectPathActionListener.class)
  ),
  @ComponentConfig(
      type = UISelectPathPanel.class,      
      id = "UIDocumentSelectPathPanel",
      template = "classpath:groovy/wcm/webui/UIWCMSelectPathPanel.gtmpl",
      events = @EventConfig(listeners = UISelectPathPanel.SelectActionListener.class)
  )
}
)
public class UIDocumentPathSelector extends UIBaseNodeTreeSelector implements UIPopupComponent{

  /**
   * Instantiates a new UI document path selector.
   * 
   * @throws Exception the exception
   */

  private Node currentPortal;
  private Node sharedPortal;
  private Node currentNode;

  public UIDocumentPathSelector() throws Exception {
    addChild(UIBreadcumbs.class, "UIBreadcrumbDocumentPathSelector", "UIBreadcrumbDocumentPathSelector");
    addChild(UIDocumentTreeBuilder.class, null, UIDocumentTreeBuilder.class.getSimpleName() + hashCode());
    addChild(UISelectPathPanel.class, "UIDocumentSelectPathPanel", "UIDocumentSelectPathPanel");
  }

  /**
   * Inits the.
   * 
   * @throws Exception the exception
   */
  public void init() throws Exception {
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    String currentPortalName = Util.getUIPortal().getName();
    SessionProvider sessionProvider = Utils.getSessionProvider(this);
    currentPortal = livePortalManagerService.getLivePortal(currentPortalName, sessionProvider);
    sharedPortal = livePortalManagerService.getLiveSharedPortal(sessionProvider);
    String repositoryName = ((ManageableRepository)(currentPortal.getSession().getRepository())).getConfiguration().getName();
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    List<String> acceptedNodeTypes = templateService.getDocumentTemplates(repositoryName);
    UISelectPathPanel uiSelectPathPanel = getChild(UISelectPathPanel.class);
    String [] arrAcceptedNodeTypes = new String[acceptedNodeTypes.size()];
    acceptedNodeTypes.toArray(arrAcceptedNodeTypes);
    uiSelectPathPanel.setAcceptedNodeTypes(arrAcceptedNodeTypes);
    UIDocumentTreeBuilder treeBuilder = getChild(UIDocumentTreeBuilder.class);
    treeBuilder.setCurrentPortal(currentPortal);
    treeBuilder.setSharedPortal(sharedPortal);
    treeBuilder.setRootTreeNode(currentPortal.getParent());
  }

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector#onChange(javax.jcr.Node, java.lang.Object)
   */
  @Override
  public void onChange(Node node, Object context) throws Exception {
    UISelectPathPanel uiSelectPathPanel = getChild(UISelectPathPanel.class);
    changeFolder(node);
    setCurrentNode(node);
    uiSelectPathPanel.setParentNode(node);
    uiSelectPathPanel.updateGrid();
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

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIPopupComponent#activate()
   */
  public void activate() throws Exception {    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.webui.core.UIPopupComponent#deActivate()
   */
  public void deActivate() throws Exception {    
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

  /**
   * @return the currentNode
   */
  public Node getCurrentNode() {
    return currentNode;
  }

  /**
   * @param currentNode the currentNode to set
   */
  public void setCurrentNode(Node currentNode) {
    this.currentNode = currentNode;
  }

  public static class SelectPathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs uiBreadcumbs = event.getSource();
      String selectedNodePath = event.getRequestContext().getRequestParameter(OBJECTID);
      UIDocumentPathSelector uiDocumentPathSelector = uiBreadcumbs.getAncestorOfType(UIDocumentPathSelector.class);
      Node currentPortal = uiDocumentPathSelector.getCurrentPortal();
      Node sharedPortal = uiDocumentPathSelector.getSharedPortal();
      if(selectedNodePath.equals(currentPortal.getPath()) || selectedNodePath.equals(sharedPortal.getPath())) {
        selectedNodePath = currentPortal.getParent().getPath();
      }
      UIDocumentTreeBuilder uiDocumentTreeBuilder = uiDocumentPathSelector.getChild(UIDocumentTreeBuilder.class);
      uiDocumentTreeBuilder.changeNode(selectedNodePath, event.getRequestContext());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiDocumentPathSelector);
    }
  }
}
