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

import javax.portlet.PortletMode;

import org.exoplatform.portal.webui.container.UIContainer;
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
    UIAdvancedPresentationPortlet uiportlet = getAncestorOfType(UIAdvancedPresentationPortlet.class);
    return uiportlet.canEditPortlet();
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
