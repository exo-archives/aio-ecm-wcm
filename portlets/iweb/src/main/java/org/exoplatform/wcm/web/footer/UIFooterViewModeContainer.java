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

import javax.portlet.PortletMode;

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.wcm.contribution.WebContributionService;
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
 * May 13, 2008  
 */

@ComponentConfig(
    lifecycle=Lifecycle.class,
    template="app:/groovy/footer/webui/UIViewModeContainer.gtmpl",
    events={
      @EventConfig(listeners=UIFooterViewModeContainer.QuickEditActionListener.class)
    }
)

public class UIFooterViewModeContainer extends UIContainer {
  
  public UIFooterViewModeContainer() throws Exception {
    addChild(UIFooterViewMode.class,null,null) ;    
  }
  
  public boolean isQuickEditable() throws Exception {
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    String quickEdit = pContext.getRequest().getPreferences().getValue("quickEdit", "");
    boolean isQuickEdit  = Boolean.parseBoolean(quickEdit); 
    WebContributionService contributionService = getApplicationComponent(WebContributionService.class) ;
    String userId = Util.getPortalRequestContext().getRemoteUser();
    boolean displayQuickEdit = contributionService.hasContributionPermission(userId);    
    return (isQuickEdit & displayQuickEdit);    
  }

  public static class QuickEditActionListener extends EventListener<UIFooterViewModeContainer> {
    public void execute(Event<UIFooterViewModeContainer> event) throws Exception {
      PortletRequestContext context = (PortletRequestContext) event.getRequestContext();
      context.setApplicationMode(PortletMode.EDIT);      
    }
  }
  
}
