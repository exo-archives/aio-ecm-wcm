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
package org.exoplatform.wcm.webui.category;

import javax.jcr.Node;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;

import org.exoplatform.services.cms.link.NodeFinder;
import org.exoplatform.wcm.webui.category.config.UICategoryNavigationConfig;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Jun 19, 2009  
 */
@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class
)
public class UICategoryNavigationPortlet extends UIPortletApplication {
  
  private static final String PREFERENCE_REPOSITORY         = "repository";
  
  private static final String PREFERENCE_WORKSPACE          = "workspace";
  
  private static final String PREFERENCE_TREE_PATH          = "treePath";
  
  private PortletMode mode = PortletMode.VIEW;
  
  public UICategoryNavigationPortlet() throws Exception {
    activateMode(mode);
  }
  
  public void activateMode(PortletMode mode) throws Exception {
    getChildren().clear();
    if (PortletMode.VIEW.equals(mode)) {
      PortletRequestContext portletRequestContext = WebuiRequestContext.getCurrentInstance();
      PortletRequest request = portletRequestContext.getRequest();
      PortletPreferences portletPreferences = request.getPreferences();
      String preferenceRepository = portletPreferences.getValue(PREFERENCE_REPOSITORY, "");
      String preferenceWorkspace = portletPreferences.getValue(PREFERENCE_WORKSPACE, "");
      String preferenceTreePath = portletPreferences.getValue(PREFERENCE_TREE_PATH, "");
      NodeFinder nodeFinder = getApplicationComponent(NodeFinder.class);
      Node rootTreeNode = (Node)nodeFinder.getItem(preferenceRepository, preferenceWorkspace, preferenceTreePath);
      UICategoryNavigationTree categoryNavigationTree = createUIComponent(UICategoryNavigationTree.class, null, null);
      categoryNavigationTree.setRootTreeNode(rootTreeNode);
      addChild(categoryNavigationTree);
    } else if (PortletMode.EDIT.equals(mode)) {
      addChild(UICategoryNavigationConfig.class, null, null);
    }
  }
  
  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext pContext = (PortletRequestContext) context;
    PortletMode newMode = pContext.getApplicationMode();
    if (!mode.equals(newMode)) {
      activateMode(newMode);
      mode = newMode;
    }
    super.processRender(app, context);
  }
}
