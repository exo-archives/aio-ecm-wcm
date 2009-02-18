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

import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.ecm.webui.tree.selectmany.UICategoriesSelectPanel;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
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
public class UICorrectContentSelectorForm extends UIBaseNodeTreeSelector implements
    UIPopupComponent {

  private List<String> existedCategoryList = new ArrayList<String>();

  public UICorrectContentSelectorForm() throws Exception {
    addChild(UIContentsSelectionTreeBuilder.class, null, null);
    addChild(UIMultiSelectionPanel.class, null, null);
    addChild(UISelectedContentGrid.class, null, null).setRendered(false);
  }

  public void init() throws Exception {
    UIContentsSelectionTreeBuilder treeBuilder = getChild(UIContentsSelectionTreeBuilder.class);
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    String currentPortalName = Util.getUIPortal().getName();
    SessionProvider provider = SessionProviderFactory.createSessionProvider();
    Node currentPortal = livePortalManagerService.getLivePortal(currentPortalName, provider);
    Node sharedPortal = livePortalManagerService.getLiveSharedPortal(provider);
    treeBuilder.setCurrentPortal(currentPortal);
    treeBuilder.setSharedPortal(sharedPortal);
    treeBuilder.setRootTreeNode(currentPortal.getParent());
    UIMultiSelectionPanel uiMultiSelectionPanel = getChild(UIMultiSelectionPanel.class);
    uiMultiSelectionPanel.updateGrid();
    UISelectedContentGrid contentsGrid = getChild(UISelectedContentGrid.class);
    contentsGrid.setSelectedCategories(existedCategoryList);
    if (existedCategoryList.size() > 0) {
      contentsGrid.setRendered(true);
    }
    contentsGrid.updateGrid();
    provider.close();
  }

  @Override
  public void onChange(Node currentNode, Object context) throws Exception {
    UICategoriesSelectPanel uiCategoriesSelectPanel = getChild(UICategoriesSelectPanel.class);
    uiCategoriesSelectPanel.setParentNode(currentNode);
    uiCategoriesSelectPanel.updateGrid();
  }

  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub

  }

  public static class CloseActionListener extends EventListener<UICorrectContentSelectorForm> {
    public void execute(Event<UICorrectContentSelectorForm> arg0) throws Exception {

    }
  }

}
