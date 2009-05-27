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
package org.exoplatform.wcm.webui.clv.config;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.ecm.webui.tree.selectone.UIOneNodePathSelector;
import org.exoplatform.ecm.webui.tree.selectone.UISelectPathPanel;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.services.cms.templates.TemplateService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

// TODO: Auto-generated Javadoc
/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 17, 2008
 */

/**
 * The Class UISelectFolderPathPanel.
 */
@ComponentConfig(
   template = "app:/groovy/ContentListViewer/config/UISelectFolderPathPanel.gtmpl", 
   events = { 
     @EventConfig(listeners = UIFolderPathSelectionPanel.SelectActionListener.class) 
   }
)
public class UIFolderPathSelectionPanel extends UISelectPathPanel {

  /**
   * Instantiates a new uI select folder path panel.
   * 
   * @throws Exception the exception
   */
  public UIFolderPathSelectionPanel() throws Exception {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.ecm.webui.tree.selectone.UISelectPathPanel#getSelectableNodes()
   */
  public List<Node> getSelectableNodes() throws Exception {
    List<Node> list = new ArrayList<Node>();
    if (parentNode == null)
      return list;
    UIComponent parent = getParent();
    UIContentsSelectionTreeBuilder uiFolderPathTreeBuilder = null;
    uiFolderPathTreeBuilder = ((UIFolderPathSelectorForm) parent).getChild(UIContentsSelectionTreeBuilder.class);
    Node root = uiFolderPathTreeBuilder.getRootTreeNode();
    Node currentPortal = uiFolderPathTreeBuilder.getCurrentPortal();
    Node sharedPortal = uiFolderPathTreeBuilder.getSharedPortal();
    Node webContentsFolder = null;
    Node documentsFolder = null;
    String parentNodePath = parentNode.getPath();
    if (parentNodePath.equals(root.getPath())) {
      list.clear();
    } else if (parentNodePath.equals(currentPortal.getPath())) {
      webContentsFolder = uiFolderPathTreeBuilder.getWebContentsFolder(currentPortal);
      documentsFolder = uiFolderPathTreeBuilder.getDocumentsFolder(currentPortal);
      list.add(webContentsFolder);
      list.add(documentsFolder);
    } else if (parentNodePath.equals(sharedPortal.getPath())) {
      webContentsFolder = uiFolderPathTreeBuilder.getWebContentsFolder(sharedPortal);
      documentsFolder = uiFolderPathTreeBuilder.getDocumentsFolder(sharedPortal);
      list.add(webContentsFolder);
      list.add(documentsFolder);
    } else {
      for (NodeIterator iterator = parentNode.getNodes(); iterator.hasNext();) {
        Node child = iterator.nextNode();
        if (child.isNodeType("exo:hiddenable"))
          continue;
        if (matchMimeType(child) && matchNodeType(child) && !isDocType(child)) {
          list.add(child);
        }
      }
    }
    return list;
  }

  public boolean isDocType(Node node) throws Exception {
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    String repository = repositoryService.getCurrentRepository().getConfiguration().getName();
    TemplateService templateService = getApplicationComponent(TemplateService.class);
    List<String> listDocumentTypes = templateService.getDocumentTemplates(repository);
    if (listDocumentTypes.contains(node.getPrimaryNodeType().getName()))
      return true;
    return false;
  }
  
  public static class SelectActionListener extends EventListener<UIFolderPathSelectionPanel> {
    public void execute(Event<UIFolderPathSelectionPanel> event) throws Exception {
      UIFolderPathSelectionPanel folderPathSelectionPanel = event.getSource() ;
      String value = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIContainer uiTreeSelector = folderPathSelectionPanel.getParent();
      if(uiTreeSelector instanceof UIOneNodePathSelector) {
        if(!((UIOneNodePathSelector)uiTreeSelector).isDisable()) {
          value = ((UIOneNodePathSelector)uiTreeSelector).getWorkspaceName() + ":" + value ;
        }
      } 
      String returnField = ((UIBaseNodeTreeSelector)uiTreeSelector).getReturnFieldName();
      ((UISelectable)((UIBaseNodeTreeSelector)uiTreeSelector).getSourceComponent()).doSelect(returnField, value) ;
      
      UIComponent uiOneNodePathSelector = folderPathSelectionPanel.getParent();
      if (uiOneNodePathSelector instanceof UIOneNodePathSelector) {
        UIComponent uiComponent = uiOneNodePathSelector.getParent();
        if (uiComponent instanceof UIPopupWindow) {
          ((UIPopupWindow)uiComponent).setShow(false);
          ((UIPopupWindow)uiComponent).setRendered(false);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiComponent);
        }
        UIComponent component = ((UIOneNodePathSelector)uiOneNodePathSelector).getSourceComponent().getParent();
        if (component != null) {
          event.getRequestContext().addUIComponentToUpdateByAjax(component);
        }
      }
    }
  }
}
