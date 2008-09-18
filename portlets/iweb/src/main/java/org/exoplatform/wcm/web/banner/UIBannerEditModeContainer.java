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
package org.exoplatform.wcm.web.banner;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.core.WebSchemaConfigService;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.services.wcm.portal.PortalFolderSchemaHandler;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Phan Le Thanh Chuong
 *          chuong.phan@exoplatform.com, phan.le.thanh.chuong@gmail.com
 * Sep 16, 2008  
 */

@ComponentConfig (
    lifecycle = Lifecycle.class,
    template = "app:/groovy/banner/webui/UIBannerEditModeContainer.gtmpl",
    events = {
      @EventConfig(listeners = UIBannerEditModeContainer.ViewWebContentSelectorActionListener.class)
    }
)
  
public class UIBannerEditModeContainer extends UIContainer {
  
  public UIBannerEditModeContainer() throws Exception {
    UIBannerEditContentForm bannerEditContentForm = addChild(UIBannerEditContentForm.class, null, null);
    bannerEditContentForm.init();
  }
  
  public boolean useSharedPortal() throws Exception {
    PortletRequestContext portletRequestContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();
    String repositoryName = portletPreferences.getValue("repository", null);
    String workspaceName = portletPreferences.getValue("workspace", null);
    String nodeUUID = portletPreferences.getValue("nodeUUID", null);
    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository repository = repositoryService.getRepository(repositoryName);
    SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
    Node bannerWebContent = sessionProvider.getSession(workspaceName, repository).getNodeByUUID(nodeUUID);
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    Node sharedPortalFolder = livePortalManagerService.getLiveSharedPortal(sessionProvider);
    
    WebSchemaConfigService webSchemaConfigService = getApplicationComponent(WebSchemaConfigService.class);
    PortalFolderSchemaHandler portalFolderSchemaHandler = webSchemaConfigService.getWebSchemaHandlerByType(PortalFolderSchemaHandler.class);
    // TODO: Need to get banner node path, not banner folder node path, because user can add another banner within banner folder,
    // choose it from selector, and this method will return true
    Node bannerFolder = portalFolderSchemaHandler.getBannerThemes(sharedPortalFolder);
    if (bannerWebContent.getPath().indexOf(bannerFolder.getPath()) >= 0)
      return true;
    return false;
  }
  
  public static class ViewWebContentSelectorActionListener extends EventListener<UIBannerEditModeContainer> {
    public void execute(Event<UIBannerEditModeContainer> event) throws Exception {
      UIBannerEditModeContainer bannerEditModeContainer = event.getSource();
      UIBannerPortlet bannerPortlet = bannerEditModeContainer.getAncestorOfType(UIBannerPortlet.class);
      bannerPortlet.removeChildById(bannerEditModeContainer.getId());
      bannerPortlet.addChild(UIWebContentSelectorForm.class, null, null);
    }
  }
}