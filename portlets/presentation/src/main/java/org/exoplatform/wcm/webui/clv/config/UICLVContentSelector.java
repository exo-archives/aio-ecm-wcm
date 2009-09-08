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
import org.exoplatform.wcm.webui.clv.UICLVPortlet;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

// TODO: Auto-generated Javadoc
/**
 * Created by The eXo Platform SAS Author : anh.do anh.do@exoplatform.com,
 * anhdn86@gmail.com Feb 13, 2009
 */
@ComponentConfig(
  lifecycle = Lifecycle.class, 
  events = @EventConfig(listeners = UICLVFolderSelector.CloseActionListener.class), 
  template = "app:/groovy/ContentListViewer/config/UICLVContentSelector.gtmpl"
)
public class UICLVContentSelector extends UIBaseNodeTreeSelector {

  /** The existed category list. */
  private List<String> existedCategoryList = new ArrayList<String>();

  /**
   * Instantiates a new uICLV content selector.
   * 
   * @throws Exception the exception
   */
  public UICLVContentSelector() throws Exception {
    addChild(UICLVContentTree.class, null, null);
    addChild(UICLVContentSelectionPanel.class, null, null);
    addChild(UICLVContentSelectedGrid.class, null, null).setRendered(false);        
  }

  /**
   * Inits the.
   * 
   * @param context the context
   * 
   * @throws Exception the exception
   */
  public void init(PortletRequestContext context) throws Exception {
    UICLVContentTree treeBuilder = getChild(UICLVContentTree.class);
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    String currentPortalName = Util.getUIPortal().getName();
    SessionProvider provider = Utils.getSessionProvider(this);
    Node currentPortal = livePortalManagerService.getLivePortal(currentPortalName, provider);
    Node sharedPortal = livePortalManagerService.getLiveSharedPortal(provider);
    treeBuilder.setCurrentPortal(currentPortal);
    treeBuilder.setSharedPortal(sharedPortal);
    treeBuilder.setRootTreeNode(currentPortal.getParent());
    UICLVContentSelectionPanel uiMultiSelectionPanel = getChild(UICLVContentSelectionPanel.class);
    uiMultiSelectionPanel.updateGrid();
    UICLVContentSelectedGrid contentsGrid = getChild(UICLVContentSelectedGrid.class);
    PortletPreferences preferences = context.getRequest().getPreferences();
    String [] contents = preferences.getValues(UICLVPortlet.CONTENT_LIST, null);
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

  /* (non-Javadoc)
   * @see org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector#onChange(javax.jcr.Node, java.lang.Object)
   */
  public void onChange(Node currentNode, Object context) throws Exception {
    UICategoriesSelectPanel uiCategoriesSelectPanel = getChild(UICategoriesSelectPanel.class);
    uiCategoriesSelectPanel.setParentNode(currentNode);
    uiCategoriesSelectPanel.updateGrid();
  }

  /**
   * The listener interface for receiving closeAction events.
   * The class that is interested in processing a closeAction
   * event implements this interface, and the object created
   * with that class is registered with a component using the
   * component's <code>addCloseActionListener<code> method. When
   * the closeAction event occurs, that object's appropriate
   * method is invoked.
   * 
   * @see CloseActionEvent
   */
  public static class CloseActionListener extends EventListener<UICLVContentSelector> {
    
    /* (non-Javadoc)
     * @see org.exoplatform.webui.event.EventListener#execute(org.exoplatform.webui.event.Event)
     */
    public void execute(Event<UICLVContentSelector> arg0) throws Exception {
    }
  }
}
