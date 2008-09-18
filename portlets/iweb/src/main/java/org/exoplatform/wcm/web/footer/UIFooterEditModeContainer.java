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
package org.exoplatform.wcm.web.footer;

import javax.jcr.Node;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.wcm.portal.LivePortalManagerService;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
    template = "app:/groovy/footer/webui/UIFooterEditModeContainer.gtmpl",
    events = {
      @EventConfig(listeners = UIFooterEditModeContainer.ViewWebContentSelectorActionListener.class)
    }
)
public class UIFooterEditModeContainer extends UIContainer {
  public UIFooterEditModeContainer() throws Exception {
    UIFooterEditContentForm footerEditContentForm = addChild(UIFooterEditContentForm.class, null, null);
    footerEditContentForm.init();
  }
  
  public boolean useSharedPortal() throws Exception {
    PortletRequestContext portletRequestContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences portletPreferences = portletRequestContext.getRequest().getPreferences();
    String repositoryName = portletPreferences.getValue("repository", "");
    String workspaceName = portletPreferences.getValue("workspace", "");
    String nodeUUID = portletPreferences.getValue("nodeUUID", "");

    RepositoryService repositoryService = getApplicationComponent(RepositoryService.class);
    ManageableRepository repository = repositoryService.getRepository(repositoryName);
    SessionProvider sessionProvider = SessionProviderFactory.createSystemProvider();
    Node bannerWebContent = sessionProvider.getSession(workspaceName, repository).getNodeByUUID(nodeUUID);
    
    LivePortalManagerService livePortalManagerService = getApplicationComponent(LivePortalManagerService.class);
    String sharedPortalName = livePortalManagerService.getLiveSharedPortal(sessionProvider).getName();
    
    if (bannerWebContent.getPath().indexOf(sharedPortalName) >= 0)
      return true;
    return false;
  }
  
  public static class ViewWebContentSelectorActionListener extends EventListener<UIFooterEditModeContainer> {
    public void execute(Event<UIFooterEditModeContainer> event) throws Exception {
      UIFooterEditModeContainer footerEditModeContainer = event.getSource();
      UIFooterPortlet footerPortlet = footerEditModeContainer.getAncestorOfType(UIFooterPortlet.class);
      footerPortlet.removeChildById(footerEditModeContainer.getId());
      footerPortlet.addChild(UIWebContentSelectorForm.class, null, null);
    }
  }
}