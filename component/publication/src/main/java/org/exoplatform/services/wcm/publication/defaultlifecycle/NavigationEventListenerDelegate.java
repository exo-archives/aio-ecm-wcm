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
import javax.jcr.Value;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham	
 *          hoa.pham@exoplatform.com
 * Oct 6, 2008  
 */
public class NavigationEventListenerDelegate {

  private String lifecycleName;
//  private ExoContainer container;

  public NavigationEventListenerDelegate(String lifecycleName, ExoContainer container) {
    this.lifecycleName = lifecycleName;
//    this.container = container;
  }

  public void updateLifecyleOnCreateNavigation(PageNavigation navigation) throws Exception { }
  
  public void updateLifecycleOnChangeNavigation(PageNavigation pageNavigation) throws Exception {
    updateAddedPageNode(pageNavigation);
    updateRemovedPageNode(pageNavigation);
  }
  
  public void updateLifecyleOnRemoveNavigation(PageNavigation navigation) throws Exception { }
  
  private void updateAddedPageNode(PageNavigation pageNavigation) throws Exception {
    UserPortalConfigService userPortalConfigService = Util.getServices(UserPortalConfigService.class);
    for (PageNode pageNode : pageNavigation.getNodes()) {
      Page page = userPortalConfigService.getPage(pageNode.getPageReference(), org.exoplatform.portal.webui.util.Util.getPortalRequestContext().getRemoteUser());
      if (page != null) {
        for (String applicationId : Util.getListApplicationIdByPage(page)) {
          Node content = Util.getNodeByApplicationId(applicationId);
          if (content != null) {
            if (content.hasProperty("publication:applicationIDs")) {
              for (Value value : content.getProperty("publication:applicationIDs").getValues()) {
                if (!value.getString().equals(applicationId)) {
                  Util.saveAddedItem(page, applicationId, content, lifecycleName);
                }
              }
            } else {
              Util.saveAddedItem(page, applicationId, content, lifecycleName);
            }
          }
        }
      }
    }
  }
  
  private void updateRemovedPageNode(PageNavigation pageNavigation) throws Exception {
    List<PageNode> listPortalPageNode = pageNavigation.getNodes();
    // PageNavigation -> got PageNodes -> + PortalName -> NavigationNodeUris
    // Query -> got all Node published -> got node's NavigationNodeUris
    // Compare -> If == -> remove node's properties
  }
}
