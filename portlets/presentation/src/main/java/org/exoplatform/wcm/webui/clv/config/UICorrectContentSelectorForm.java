/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
import javax.portlet.PortletPreferences;

import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.ecm.webui.tree.selectmany.UICategoriesSelectPanel;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.wcm.webui.Utils;
import org.exoplatform.wcm.webui.clv.UIContentListViewerPortlet;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS Author : anh.do anh.do@exoplatform.com,
 * anhdn86@gmail.com Feb 13, 2009
 */
@ComponentConfig(
  lifecycle = Lifecycle.class, 
  events = @EventConfig(listeners = UIFolderPathSelectorForm.CloseActionListener.class), 
  template = "app:/groovy/ContentListViewer/config/UIMultiContentSlection.gtmpl"
)
public class UICorrectContentSelectorForm extends UIBaseNodeTreeSelector {

  private List<String> existedCategoryList = new ArrayList<String>();

  public UICorrectContentSelectorForm() throws Exception {
    addChild(UIContentsSelectionTreeBuilder.class, null, null);
    addChild(UIMultiSelectionPanel.class, null, null);
    addChild(UISelectedContentGrid.class, null, null).setRendered(false);        
  }

  public void init(PortletRequestContext context) throws Exception {
    UIContentsSelectionTreeBuilder treeBuilder = getChild(UIContentsSelectionTreeBuilder.class);
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    String currentPortalName = Util.getUIPortal().getName();
    SessionProvider provider = Utils.getSessionProvider(this);
    Node currentPortal = livePortalManagerService.getLivePortal(currentPortalName, provider);
    Node sharedPortal = livePortalManagerService.getLiveSharedPortal(provider);
    treeBuilder.setCurrentPortal(currentPortal);
    treeBuilder.setSharedPortal(sharedPortal);
    treeBuilder.setRootTreeNode(currentPortal.getParent());
    UIMultiSelectionPanel uiMultiSelectionPanel = getChild(UIMultiSelectionPanel.class);
    uiMultiSelectionPanel.updateGrid();
    UISelectedContentGrid contentsGrid = getChild(UISelectedContentGrid.class);
    PortletPreferences preferences = context.getRequest().getPreferences();
    String [] contents = preferences.getValues(UIContentListViewerPortlet.CONTENT_LIST, null);
    if (contents != null && contents.length > 0) {
      for (int i = 0; i < contents.length; i++) {
        if (contents[i] != null) existedCategoryList.add(contents[i]);
      }
    }
    contentsGrid.setSelectedCategories(existedCategoryList);    
    if (existedCategoryList.size() > 0) {
      contentsGrid.setRendered(true);
    }
    contentsGrid.updateGrid(contentsGrid.getUIPageIterator().getCurrentPage());
  }

  public void onChange(Node currentNode, Object context) throws Exception {
    UICategoriesSelectPanel uiCategoriesSelectPanel = getChild(UICategoriesSelectPanel.class);
    uiCategoriesSelectPanel.setParentNode(currentNode);
    uiCategoriesSelectPanel.updateGrid();
  }

  public static class CloseActionListener extends EventListener<UICorrectContentSelectorForm> {
    public void execute(Event<UICorrectContentSelectorForm> arg0) throws Exception {

    }
  }

}
