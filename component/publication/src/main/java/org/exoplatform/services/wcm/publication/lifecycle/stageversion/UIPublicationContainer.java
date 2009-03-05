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
package org.exoplatform.services.wcm.publication.lifecycle.stageversion;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.jcr.Node;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.publication.defaultlifecycle.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          chuong_phan@exoplatform.com
 * Mar 4, 2009  
 */

@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "system:/groovy/webui/core/UITabPane.gtmpl"              
)
               
public class UIPublicationContainer extends UIForm implements UIPopupComponent {  
  private static String selectedTabId = "";
  
  public UIPublicationContainer() { }  
  public void initContainer(Node node) throws Exception {
    UIPublicationPanel publicationPanel = addChild(UIPublicationPanel.class, null, null);
    publicationPanel.init(node);
    UIPublicationPages publicationPages = addChild(UIPublicationPages.class, null, null);
    List<String> runningPortals = getRunningPortals(node.getSession().getUserID());
    String portalName = getPortalForContent(node);
    publicationPages.init(node, portalName, runningPortals);
    publicationPages.setRendered(false);
    UIPublicationHistory publicationHistory = addChild(UIPublicationHistory.class, null, null);
    publicationHistory.init(node);
    publicationHistory.setRendered(false);
    setSelectedTab(1);
  }
  
  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }
  
  public String getSelectedTabId() { return selectedTabId; }
  public void setSelectedTab(String renderTabId) { selectedTabId = renderTabId; }
  public void setSelectedTab(int index) { selectedTabId = ((UIComponent)getChild(index-1)).getId();}
  
  private String getPortalForContent(Node contentNode) throws Exception {
    LivePortalManagerService livePortalManagerService = org.exoplatform.services.wcm.publication.defaultlifecycle.Util.getServices(LivePortalManagerService.class);
    for(String portalPath:livePortalManagerService.getLivePortalsPath()) {
      if(contentNode.getPath().startsWith(portalPath)) {
        return livePortalManagerService.getPortalNameByPath(portalPath);
      }
    }

    return null;
  }
  
  private List<String> getRunningPortals(String userId) throws Exception {
    List<String> listPortalName = new ArrayList<String>();
    DataStorage service = Util.getServices(DataStorage.class);
    Query<PortalConfig> query = new Query<PortalConfig>(null, null, null, null, PortalConfig.class) ;
    PageList pageList = service.find(query) ;
    UserACL userACL = Util.getServices(UserACL.class);
    for(Object object:pageList.getAll()) {
      PortalConfig portalConfig = (PortalConfig)object;
      if(userACL.hasPermission(portalConfig, userId)) {
        listPortalName.add(portalConfig.getName());
      }
    }
    return listPortalName;
  }
}
