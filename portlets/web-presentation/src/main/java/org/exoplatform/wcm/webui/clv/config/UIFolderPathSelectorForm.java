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

import javax.jcr.Node;

import org.exoplatform.ecm.webui.tree.UIBaseNodeTreeSelector;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/*
 * Created by The eXo Platform SAS Author : Anh Do Ngoc anh.do@exoplatform.com
 * Oct 15, 2008
 */

@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIFolderPathSelectorForm extends UIBaseNodeTreeSelector {
  public UIFolderPathSelectorForm() throws Exception {
    addChild(UIFolderPathTreeBuilder.class, null, UIFolderPathTreeBuilder.class.getSimpleName()
        + hashCode());
    addChild(UISelectFolderPathPanel.class, null, UISelectFolderPathPanel.class.getSimpleName() + hashCode());
  }

  public void init() throws Exception {
    UIFolderPathTreeBuilder treeBuilder = getChild(UIFolderPathTreeBuilder.class);
    UISelectFolderPathPanel pathPanel = getChild(UISelectFolderPathPanel.class);
    String[] acceptNodeTypes = new String[] { "nt:folder",  "nt:unstructured"};
    pathPanel.setAcceptedNodeTypes(acceptNodeTypes);
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    String currentPortalName = Util.getUIPortal().getName();
    SessionProvider provider = SessionProviderFactory.createSessionProvider();
    Node currentPortal = livePortalManagerService.getLivePortal(currentPortalName, provider);
    Node sharedPortal = livePortalManagerService.getLiveSharedPortal(provider);
    treeBuilder.setCurrentPortal(currentPortal);
    treeBuilder.setSharedPortal(sharedPortal);
    treeBuilder.setRootTreeNode(currentPortal.getParent());
    provider.close();
  }

  public void onChange(Node node, Object val) throws Exception {
    UISelectFolderPathPanel selectPathPanel = getChild(UISelectFolderPathPanel.class);
    selectPathPanel.setParentNode(node);
  }

}
