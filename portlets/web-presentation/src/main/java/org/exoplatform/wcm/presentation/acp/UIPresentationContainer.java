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
package org.exoplatform.wcm.presentation.acp;

import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Author : Do Ngoc Anh *      
 * Email: anh.do@exoplatform.com *
 * May 14, 2008  
 */

@ComponentConfig(
    lifecycle=Lifecycle.class,
    template="app:/groovy/advancedPresentation/UIPresentationContainer.gtmpl",
    events = {
      @EventConfig(listeners=UIPresentationContainer.QuickEditActionListener.class)
    }
)


public class UIPresentationContainer extends UIContainer{
  public UIPresentationContainer() throws Exception{
    addChild(UIPresentation.class, null, null);
  }

  public boolean isQuickEditable() throws Exception {
    PortletRequestContext context = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences prefs = context.getRequest().getPreferences();
    String repositoryName = prefs.getValue(UIAdvancedPresentationPortlet.REPOSITORY, null);
    if(repositoryName == null) return true;
    String portalName = Util.getUIPortal().getName();
    String userId = context.getRemoteUser();
    String quickEdit = prefs.getValue("ShowQuickEdit", null);
    boolean isQuickEdit = Boolean.parseBoolean(quickEdit);
    DataStorage dataStorage = getApplicationComponent(DataStorage.class);
    PortalConfig portalConfig = dataStorage.getPortalConfig(portalName);
    UserACL userACL = getApplicationComponent(UserACL.class);
    boolean displayQuickEdit = userACL.hasEditPermission(portalConfig, userId);
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider();
    Node portalNode = livePortalManagerService.getLivePortal(portalName, sessionProvider);
    String UUID = prefs.getValue(UIAdvancedPresentationPortlet.UUID, null);
    try{
      ExtendedNode exWebContentNode = (ExtendedNode) portalNode.getSession().getNodeByUUID(UUID);
      AccessControlList acl = exWebContentNode.getACL();
      List<String> permList = acl.getPermissions(userId);
      if(permList.contains(PermissionType.ADD_NODE) && permList.contains(PermissionType.REMOVE) 
          && permList.contains(PermissionType.SET_PROPERTY)) {
        return (isQuickEdit && displayQuickEdit);
      }
    }catch(ItemNotFoundException e) {
      return true;
    }
    return false;
  }

  public String getPortletId() {
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    return pContext.getWindowId();
  }

  public static class QuickEditActionListener extends EventListener<UIPresentation>{   
    public void execute(Event<UIPresentation> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.setApplicationMode(PortletMode.EDIT);
    }
  }

}
