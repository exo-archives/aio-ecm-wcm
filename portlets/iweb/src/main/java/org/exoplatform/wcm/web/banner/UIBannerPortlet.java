/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

import javax.portlet.PortletMode;

import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Author : Do Ngoc Anh *      
 * Email: anh.do@exoplatform.com *
 * May 9, 2008  
 */

@ComponentConfig(lifecycle = UIApplicationLifecycle.class)
public class UIBannerPortlet extends UIPortletApplication {

  private PortletMode currentMode = PortletMode.VIEW;

  public UIBannerPortlet() throws Exception {
    activeMode(currentMode);    
  }

  public void activeMode(PortletMode mode) throws Exception {
    getChildren().clear();
    if (PortletMode.VIEW.equals(mode)) {
      addChild(UIBannerViewModeContainer.class, null, UIPortletApplication.VIEW_MODE);
    } else if (PortletMode.EDIT.equals(mode)) {
      addChild(UIBannerEditModeContainer.class, null, UIPortletApplication.EDIT_MODE);
    }
  }
  
  public void processRender(WebuiApplication webuiApplication, WebuiRequestContext webuiRequestContext) throws Exception {
    PortletRequestContext portletRequestContext = (PortletRequestContext) webuiRequestContext;
    PortletMode newMode = portletRequestContext.getApplicationMode();
    if (!currentMode.equals(newMode)) {
      activeMode(newMode);
      currentMode = newMode;
    }
    super.processRender(webuiApplication, webuiRequestContext);
  }
}
